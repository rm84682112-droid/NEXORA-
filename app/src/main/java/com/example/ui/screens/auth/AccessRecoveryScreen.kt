package com.example.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

enum class RecoveryTab(val label: String) {
    RECOVERY_CODE("Código de Recuperação"),
    FORGOT_PIN("Esqueci o PIN"),
    CHANGE_PHONE("Mudar de Número")
}

@Composable
fun AccessRecoveryScreen(
    onBack: () -> Unit,
    onRecoverySuccess: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(RecoveryTab.RECOVERY_CODE) }

    var phoneNumber by remember { mutableStateOf("+258 ") }
    var recoveryCodeInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var confirmNewPinInput by remember { mutableStateOf("") }
    var newPhoneInput by remember { mutableStateOf("+258 ") }
    var smsVerificationInput by remember { mutableStateOf("") }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D1527))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("recovery_back_button")) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Recuperação de Acesso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Códigos de Emergência & Redefinição de PIN", style = MaterialTheme.typography.bodySmall, color = Color(0xFF38BDF8))
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
                // Tab Selection Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RecoveryTab.values().forEach { tab ->
                        FilterChip(
                            selected = (selectedTab == tab),
                            onClick = { selectedTab = tab },
                            label = { Text(tab.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0066FF),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                }

                when (selectedTab) {
                    RecoveryTab.RECOVERY_CODE -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Entrar com Código Único (NX-XXXX)", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Usa um dos 5 códigos gerados durante o cadastro para recuperar o acesso se perdeste o cartão SIM ou telemóvel.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Número de Telemóvel Registado") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF10B981)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0066FF), unfocusedBorderColor = Color(0xFF475569), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth().testTag("recovery_phone_input")
                        )

                        OutlinedTextField(
                            value = recoveryCodeInput,
                            onValueChange = { recoveryCodeInput = it },
                            label = { Text("Código de Recuperação (Ex: NX-8392)") },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFFF59E0B)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0066FF), unfocusedBorderColor = Color(0xFF475569), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth().testTag("recovery_code_input")
                        )
                    }

                    RecoveryTab.FORGOT_PIN -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Redefinir PIN por SMS", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Receberás um SMS de validação e poderás criar um novo PIN de 4 dígitos.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                        }

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Número de Telemóvel (+258)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF10B981)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0066FF), unfocusedBorderColor = Color(0xFF475569), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = smsVerificationInput,
                            onValueChange = { smsVerificationInput = it },
                            label = { Text("Código SMS (Firebase Auth: 884920)") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0066FF), unfocusedBorderColor = Color(0xFF475569), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { if (it.length <= 4) newPinInput = it },
                            label = { Text("Novo PIN de 4 dígitos") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0066FF), unfocusedBorderColor = Color(0xFF475569), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = confirmNewPinInput,
                            onValueChange = { if (it.length <= 4) confirmNewPinInput = it },
                            label = { Text("Confirmar Novo PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0066FF), unfocusedBorderColor = Color(0xFF475569), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    RecoveryTab.CHANGE_PHONE -> {
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Número de Telemóvel Actual") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0066FF), unfocusedBorderColor = Color(0xFF475569), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newPhoneInput,
                            onValueChange = { newPhoneInput = it },
                            label = { Text("Novo Número de Telemóvel (+258)") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0066FF), unfocusedBorderColor = Color(0xFF475569), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = smsVerificationInput,
                            onValueChange = { smsVerificationInput = it },
                            label = { Text("SMS no Novo Número (884920)") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0066FF), unfocusedBorderColor = Color(0xFF475569), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (statusMessage != null) {
                    Text(
                        text = statusMessage!!,
                        color = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = {
                    when (selectedTab) {
                        RecoveryTab.RECOVERY_CODE -> {
                            val res = NexoraRepository.loginWithRecoveryCode(phoneNumber, recoveryCodeInput)
                            res.onSuccess {
                                isSuccess = true
                                statusMessage = "Acesso recuperado com sucesso via código NX."
                                onRecoverySuccess()
                            }.onFailure { err ->
                                isSuccess = false
                                statusMessage = err.message ?: "Código de recuperação inválido."
                            }
                        }

                        RecoveryTab.FORGOT_PIN -> {
                            if (newPinInput.length != 4 || newPinInput != confirmNewPinInput) {
                                isSuccess = false
                                statusMessage = "Os PINs devem ter 4 dígitos e ser idênticos."
                            } else {
                                val res = NexoraRepository.resetPinWithSms(phoneNumber, smsVerificationInput, newPinInput)
                                res.onSuccess {
                                    isSuccess = true
                                    statusMessage = "PIN alterado com sucesso."
                                    onRecoverySuccess()
                                }.onFailure { err ->
                                    isSuccess = false
                                    statusMessage = err.message ?: "Falha ao alterar o PIN."
                                }
                            }
                        }

                        RecoveryTab.CHANGE_PHONE -> {
                            val res = NexoraRepository.changePhoneNumber(phoneNumber, newPhoneInput, smsVerificationInput)
                            res.onSuccess {
                                isSuccess = true
                                statusMessage = "Número alterado com sucesso."
                                onRecoverySuccess()
                            }.onFailure { err ->
                                isSuccess = false
                                statusMessage = err.message ?: "Falha ao alterar número."
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_recovery_button")
            ) {
                Text("Confirmar e Restaurar Acesso", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
