package com.example.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.NexoraRepository
import com.example.util.SecurityUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class LoginMethod(val label: String) {
    PASSWORD("Palavra-passe"),
    PIN("PIN de Balcão"),
    BIOMETRIC("Biometria")
}

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNavigateToRecovery: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedMethod by remember { mutableStateOf(LoginMethod.PASSWORD) }
    var phoneNumber by remember { mutableStateOf("+258 ") }
    var passwordInput by remember { mutableStateOf("") }
    var pinInput by remember { mutableStateOf("") }
    var rememberDevice by remember { mutableStateOf(true) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var failedAttempts by remember { mutableIntStateOf(0) }
    var lockoutSeconds by remember { mutableIntStateOf(0) }

    // Lockout timer effect
    LaunchedEffect(lockoutSeconds) {
        if (lockoutSeconds > 0) {
            delay(1000L)
            lockoutSeconds -= 1
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D1527))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("login_back_button")) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Entrar na NEXORA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Acesso por Telemóvel · Moçambique", style = MaterialTheme.typography.bodySmall, color = Color(0xFF38BDF8))
                }
            }
        },
        containerColor = Color(0xFF090E1A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Method selection chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LoginMethod.values().forEach { method ->
                        FilterChip(
                            selected = (selectedMethod == method),
                            onClick = { selectedMethod = method },
                            label = { Text(method.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0066FF),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                }

                // Lockout Banner
                if (lockoutSeconds > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1215)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Conta Bloqueada Temporariamente", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Excedeste o limite de 5 tentativas erradas. Por razões de segurança, aguarda ${lockoutSeconds}s para tentar novamente ou clica em 'Recuperar Acesso'.",
                                color = Color(0xFFFCA5A5),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Phone Input
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Número de Telemóvel (+258)") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF10B981)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    enabled = lockoutSeconds == 0 && !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0066FF),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_phone_input")
                )

                when (selectedMethod) {
                    LoginMethod.PASSWORD -> {
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Palavra-passe (mínimo 8 caracteres)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF38BDF8)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            enabled = lockoutSeconds == 0 && !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0066FF),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_pin_password_input")
                        )
                    }

                    LoginMethod.PIN -> {
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { if (it.length <= 4) pinInput = it },
                            label = { Text("PIN de Balcão (4 dígitos)") },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFFF59E0B)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            enabled = lockoutSeconds == 0 && !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0066FF),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_pin_input")
                        )
                    }

                    LoginMethod.BIOMETRIC -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Autenticação Biométrica", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Acede rapidamente usando a impressão digital ou reconhecimento facial configurado no teu dispositivo.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Remember Device Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = rememberDevice,
                        onCheckedChange = { rememberDevice = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0066FF))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lembrar este dispositivo (manter sessão ativa)", color = Color.White, fontSize = 13.sp)
                }
            }

            // Bottom Buttons & Recovery
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (lockoutSeconds > 0 || isLoading) return@Button

                        if (!SecurityUtils.isValidMozambiquePhone(phoneNumber)) {
                            errorMessage = "Número inválido. Usa um número de Moçambique (+258 82 a 87)."
                            return@Button
                        }

                        val secret = when (selectedMethod) {
                            LoginMethod.PASSWORD -> passwordInput
                            LoginMethod.PIN -> pinInput
                            LoginMethod.BIOMETRIC -> "biometric"
                        }

                        if (selectedMethod == LoginMethod.PASSWORD && secret.length < 8) {
                            errorMessage = "A palavra-passe deve ter pelo menos 8 caracteres."
                            return@Button
                        }

                        if (selectedMethod == LoginMethod.PIN && secret.length != 4) {
                            errorMessage = "O PIN deve ter 4 dígitos numéricos."
                            return@Button
                        }

                        isLoading = true
                        errorMessage = null

                        coroutineScope.launch {
                            val res = NexoraRepository.loginWithFirebase(
                                phone = phoneNumber,
                                secret = secret,
                                isPin = (selectedMethod == LoginMethod.PIN)
                            )

                            isLoading = false
                            res.onSuccess {
                                failedAttempts = 0
                                errorMessage = null
                                onLoginSuccess()
                            }.onFailure { err ->
                                failedAttempts += 1
                                if (failedAttempts >= 5) {
                                    lockoutSeconds = 900 // 15 minutes lockout
                                    errorMessage = "5 tentativas erradas. Conta bloqueada por 15 minutos."
                                } else {
                                    errorMessage = err.message ?: "Credenciais incorretas (${5 - failedAttempts} tentativas restantes)."
                                }
                            }
                        }
                    },
                    enabled = lockoutSeconds == 0 && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0066FF),
                        disabledContainerColor = Color(0xFF334155)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_login_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Entrar na Conta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onNavigateToRecovery) {
                        Text("Esqueci a Palavra-passe / Recuperar", color = Color(0xFF38BDF8), fontSize = 12.sp)
                    }

                    TextButton(onClick = onNavigateToRegister) {
                        Text("Criar Conta", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
