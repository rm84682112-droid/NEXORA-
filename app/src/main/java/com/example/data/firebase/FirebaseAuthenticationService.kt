package com.example.data.firebase

import android.app.Activity
import android.util.Log
import com.example.util.SecurityUtils
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

sealed class PhoneAuthState {
    object Idle : PhoneAuthState()
    object SendingCode : PhoneAuthState()
    data class CodeSent(val verificationId: String, val token: PhoneAuthProvider.ForceResendingToken) : PhoneAuthState()
    data class VerificationCompleted(val user: FirebaseUser?) : PhoneAuthState()
    data class Error(val message: String) : PhoneAuthState()
}

class FirebaseAuthenticationService private constructor() {

    val auth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        null
    }

    // Configuração para ligar verificação por SMS no futuro sem custos imediatos
    var isSmsVerificationEnabled: Boolean = false

    private val _authState = MutableStateFlow<PhoneAuthState>(PhoneAuthState.Idle)
    val authState: StateFlow<PhoneAuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth?.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        auth?.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    val isUserLoggedIn: Boolean
        get() = auth?.currentUser != null

    val currentUid: String?
        get() = auth?.currentUser?.uid

    val currentPhoneNumber: String?
        get() = auth?.currentUser?.phoneNumber

    /**
     * Converte o número de telemóvel Moçambicano para um identificador de e-mail compatível com Firebase Auth.
     * Exemplo: +258 84 123 4567 -> 258841234567@nexora.app
     */
    fun phoneToEmailIdentifier(phoneNumber: String): String {
        val cleanDigits = phoneNumber.replace("[^0-9]".toRegex(), "")
        val nationalDigits = if (cleanDigits.startsWith("258")) cleanDigits else "258$cleanDigits"
        return "$nationalDigits@nexora.app"
    }

    /**
     * Cria conta REAL no Firebase Authentication via email derivado do telemóvel e palavra-passe.
     */
    suspend fun createAccountWithPhoneAndPassword(phoneNumber: String, password: String): Result<FirebaseUser> {
        return try {
            val fa = auth ?: throw Exception("Serviço Firebase Auth não inicializado.")
            val emailIdentifier = phoneToEmailIdentifier(phoneNumber)
            val authResult = fa.createUserWithEmailAndPassword(emailIdentifier, password).await()
            val user = authResult.user ?: throw Exception("Utilizador não retornado pelo Firebase Auth.")
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Erro ao criar conta no Firebase Auth: ${e.message}", e)
            val friendlyMsg = when {
                e.message?.contains("The email address is already in use", ignoreCase = true) == true ->
                    "Este número de telemóvel já se encontra registado na NEXORA."
                e.message?.contains("network error", ignoreCase = true) == true ->
                    "Sem conexão à Internet. Verifique a sua rede e tente novamente."
                e.message?.contains("Password should be at least", ignoreCase = true) == true ->
                    "A palavra-passe deve ter pelo menos 8 caracteres."
                else -> e.localizedMessage ?: "Erro ao criar conta no Firebase."
            }
            Result.failure(Exception(friendlyMsg, e))
        }
    }

    /**
     * Autentica utilizador REAL no Firebase Authentication via telemóvel e palavra-passe.
     */
    suspend fun signInWithPhoneAndPassword(phoneNumber: String, password: String): Result<FirebaseUser> {
        return try {
            val fa = auth ?: throw Exception("Serviço Firebase Auth não inicializado.")
            val emailIdentifier = phoneToEmailIdentifier(phoneNumber)
            val authResult = fa.signInWithEmailAndPassword(emailIdentifier, password).await()
            val user = authResult.user ?: throw Exception("Utilizador não retornado pelo Firebase Auth.")
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Erro de autenticação no Firebase Auth: ${e.message}", e)
            val friendlyMsg = when {
                e is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ||
                e is com.google.firebase.auth.FirebaseAuthInvalidUserException ||
                e.message?.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) == true ||
                e.message?.contains("There is no user record", ignoreCase = true) == true ||
                e.message?.contains("invalid-credential", ignoreCase = true) == true ||
                e.message?.contains("incorrect, malformed or has expired", ignoreCase = true) == true ->
                    "Número de telemóvel ou palavra-passe incorrectos. Verifique os dados introduzidos."
                e.message?.contains("network error", ignoreCase = true) == true ->
                    "Sem conexão à Internet. Verifique a sua ligação móvel ou Wi-Fi."
                e.message?.contains("too many requests", ignoreCase = true) == true ->
                    "Muitas tentativas falhadas. Aguarde alguns instantes antes de tentar novamente."
                else -> "Número de telemóvel ou palavra-passe incorretos."
            }
            Result.failure(Exception(friendlyMsg, e))
        }
    }

    /**
     * Envia o código SMS de verificação para o número de telemóvel fornecido (+258...).
     * Utiliza o PhoneAuthProvider do Firebase Auth com suporte a reenvio.
     */
    fun sendSmsVerificationCode(
        phoneNumber: String,
        activity: Activity,
        timeoutSeconds: Long = 60L,
        resendToken: PhoneAuthProvider.ForceResendingToken? = null
    ) {
        val formattedPhone = SecurityUtils.formatMozambiquePhone(phoneNumber)
        _authState.value = PhoneAuthState.SendingCode

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e("FirebaseAuthService", "Falha na verificação Phone Auth: ${e.message}", e)
                _authState.value = PhoneAuthState.Error(e.localizedMessage ?: "Falha ao enviar SMS de verificação.")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d("FirebaseAuthService", "Código SMS enviado com sucesso para $formattedPhone. VerificationID: $verificationId")
                _authState.value = PhoneAuthState.CodeSent(verificationId, token)
            }
        }

        val fa = auth
        if (fa == null) {
            _authState.value = PhoneAuthState.Error("Serviço de autenticação não inicializado.")
            return
        }

        val optionsBuilder = PhoneAuthOptions.newBuilder(fa)
            .setPhoneNumber(formattedPhone)
            .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        if (resendToken != null) {
            optionsBuilder.setForceResendingToken(resendToken)
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    suspend fun verifyOtpCode(verificationId: String, code: String): Result<FirebaseUser?> {
        return try {
            val fa = auth ?: throw Exception("Serviço Firebase Auth não inicializado.")
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val result = fa.signInWithCredential(credential).await()
            _authState.value = PhoneAuthState.VerificationCompleted(result.user)
            _currentUser.value = result.user
            Result.success(result.user)
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Erro na verificação de OTP: ${e.message}", e)
            _authState.value = PhoneAuthState.Error(e.localizedMessage ?: "Código OTP inválido.")
            Result.failure(e)
        }
    }

    fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        val fa = auth ?: return
        fa.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    _authState.value = PhoneAuthState.VerificationCompleted(user)
                    _currentUser.value = user
                } else {
                    val exception = task.exception
                    _authState.value = PhoneAuthState.Error(exception?.localizedMessage ?: "Erro na autenticação por telemóvel.")
                }
            }
    }

    fun resetState() {
        _authState.value = PhoneAuthState.Idle
    }

    fun signOut() {
        auth?.signOut()
        _currentUser.value = null
        _authState.value = PhoneAuthState.Idle
    }

    companion object {
        const val SMS_VERIFICATION_DEFAULT = false

        @Volatile
        private var INSTANCE: FirebaseAuthenticationService? = null

        fun getInstance(): FirebaseAuthenticationService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseAuthenticationService().also { INSTANCE = it }
            }
        }
    }
}
