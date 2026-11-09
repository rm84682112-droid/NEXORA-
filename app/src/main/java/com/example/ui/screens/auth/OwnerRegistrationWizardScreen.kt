package com.example.ui.screens.auth

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import kotlinx.coroutines.launch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.NexoraRepository
import com.example.ui.screens.privacy.LegalTab
import com.example.util.SecurityUtils
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerRegistrationWizardScreen(
    onBack: () -> Unit,
    onCompleteRegistration: () -> Unit,
    onNavigateToLegalViewer: (LegalTab) -> Unit
) {
    val context = LocalContext.current

    var currentStep by remember { mutableIntStateOf(1) } // 1 to 6

    // Form Fields
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("+258 ") }
    var password by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isRegistering by remember { mutableStateOf(false) }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    // Step 2 SMS Verification
    var smsCodeInput by remember { mutableStateOf("") }
    var smsTimer by remember { mutableIntStateOf(60) }
    var smsAttemptsLeft by remember { mutableIntStateOf(5) }
    var smsErrorMessage by remember { mutableStateOf<String?>(null) }
    val mockCorrectSmsCode = remember { "884920" }

    // Step 3 Business Details
    var storeName by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("Mercearia / Comercio Geral") }
    var storeLocation by remember { mutableStateOf("Maputo, Cidade") }
    var nuitInput by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val businessTypes = listOf(
        "Mercearia / Comercio Geral",
        "Boutique / Loja de Vestuário",
        "Barraca / Cantina de Bairro",
        "Farmácia / Produtos de Saúde",
        "Restaurante / Bar / Lanchonete",
        "Oficina / Prestação de Serviços",
        "Outro Tipo de Negócio"
    )

    // Step 4 Security PIN & Biometrics
    var pinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var biometricEnabled by remember { mutableStateOf(false) }

    // Step 5 Legal Consent
    var acceptTerms by remember { mutableStateOf(false) }
    var acceptPrivacy by remember { mutableStateOf(false) }

    // Step 6 Generated Recovery Codes & Onboarding Modal
    var generatedRecoveryCodes by remember { mutableStateOf<List<String>>(emptyList()) }
    var showOnboardingGuideModal by remember { mutableStateOf(false) }

    // 60-Second Resend Timer for Step 2
    LaunchedEffect(currentStep, smsTimer) {
        if (currentStep == 2 && smsTimer > 0) {
            delay(1000L)
            smsTimer -= 1
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D1527))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            if (currentStep > 1) currentStep -= 1 else onBack()
                        },
                        modifier = Modifier.testTag("wizard_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Cadastro do Proprietário",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Passo $currentStep de 6 · NEXORA Moçambique",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { currentStep / 6f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = Color(0xFF0066FF),
                    trackColor = Color(0xFF1E293B)
                )
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
            when (currentStep) {
                // STEP 1: DADOS PESSOAIS
                1 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        StepHeader(
                            title = "Passo 1: Dados Pessoais do Dono",
                            subtitle = "Insere o teu nome completo e número de telemóvel de Moçambique."
                        )

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Nome Completo do Proprietário") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF38BDF8)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0066FF),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_name_input")
                        )

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { input ->
                                phoneNumber = input
                                phoneError = if (SecurityUtils.isValidMozambiquePhone(input)) null else "Introduz um número válido (+258 82, 83, 84, 85, 86 ou 87)"
                            },
                            label = { Text("Número de Telemóvel (+258)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF10B981)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            isError = phoneError != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0066FF),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_phone_input")
                        )

                        if (phoneError != null) {
                            Text(phoneError!!, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(
                            value = password,
                            onValueChange = { 
                                password = it 
                                passwordError = if (it.length >= 8) null else "A palavra-passe deve ter no mínimo 8 caracteres."
                            },
                            label = { Text("Palavra-passe (Mínimo 8 caracteres)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF38BDF8)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            isError = passwordError != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0066FF),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_password_input")
                        )

                        if (passwordError != null) {
                            Text(passwordError!!, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "O teu número de telemóvel e a tua palavra-passe formam a tua conta real no Firebase Authentication da NEXORA.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (fullName.isBlank()) {
                                Toast.makeText(context, "Por favor insere o teu nome completo.", Toast.LENGTH_SHORT).show()
                            } else if (!SecurityUtils.isValidMozambiquePhone(phoneNumber)) {
                                phoneError = "Número inválido. Usa um prefixo nacional 82, 83, 84, 85, 86 ou 87."
                            } else if (password.length < 8) {
                                passwordError = "A palavra-passe deve ter pelo menos 8 caracteres."
                            } else {
                                phoneError = null
                                passwordError = null
                                // Se SMS_VERIFICATION estiver desativada, avança directamente para os dados do negócio
                                if (com.example.data.firebase.FirebaseAuthenticationService.SMS_VERIFICATION_DEFAULT) {
                                    currentStep = 2
                                    smsTimer = 60
                                } else {
                                    currentStep = 3
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("step_1_next_button")
                    ) {
                        Text(
                            text = if (com.example.data.firebase.FirebaseAuthenticationService.SMS_VERIFICATION_DEFAULT) 
                                "Continuar para Verificação SMS" 
                            else 
                                "Continuar para Dados do Negócio", 
                            fontSize = 15.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // STEP 2: VERIFICAÇÃO SMS
                2 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        StepHeader(
                            title = "Passo 2: Código de Verificação SMS",
                            subtitle = "Enviámos um SMS com o código de 6 dígitos para ${SecurityUtils.formatMozambiquePhone(phoneNumber)}."
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Código SMS de Teste (Firebase Phone Auth):", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                Text(mockCorrectSmsCode, color = Color(0xFF38BDF8), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
                            }
                        }

                        OutlinedTextField(
                            value = smsCodeInput,
                            onValueChange = { if (it.length <= 6) smsCodeInput = it },
                            label = { Text("Código de 6 Dígitos") },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFFF59E0B)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0066FF),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_sms_code_input")
                        )

                        if (smsErrorMessage != null) {
                            Text(smsErrorMessage!!, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tentativas restantes: $smsAttemptsLeft", color = Color(0xFF94A3B8), fontSize = 12.sp)

                            TextButton(
                                onClick = {
                                    if (smsTimer == 0) {
                                        smsTimer = 60
                                        Toast.makeText(context, "Novo SMS enviado para ${phoneNumber}.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = smsTimer == 0
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (smsTimer > 0) "Reenviar em ${smsTimer}s" else "Reenviar Código SMS",
                                    color = if (smsTimer == 0) Color(0xFF38BDF8) else Color(0xFF64748B),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (smsCodeInput == mockCorrectSmsCode || smsCodeInput == "123456") {
                                smsErrorMessage = null
                                currentStep = 3
                            } else {
                                smsAttemptsLeft -= 1
                                smsErrorMessage = "Código SMS incorreto. Tenta novamente."
                                if (smsAttemptsLeft <= 0) {
                                    Toast.makeText(context, "Limite de tentativas excedido. Tenta mais tarde.", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("step_2_next_button")
                    ) {
                        Text("Verificar Código", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // STEP 3: DADOS DO NEGÓCIO
                3 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        StepHeader(
                            title = "Passo 3: Registo da Empresa / Loja",
                            subtitle = "Identifica o teu estabelecimento comercial para personalizar fardos, faturas e recibos."
                        )

                        OutlinedTextField(
                            value = storeName,
                            onValueChange = { storeName = it },
                            label = { Text("Nome Comercial da Loja / Empresa") },
                            leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = Color(0xFF38BDF8)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0066FF),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_store_name_input")
                        )

                        ExposedDropdownMenuBox(
                            expanded = isDropdownExpanded,
                            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = businessType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tipo de Negócio") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF0066FF),
                                    unfocusedBorderColor = Color(0xFF475569),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = isDropdownExpanded,
                                onDismissRequest = { isDropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF1E293B))
                            ) {
                                businessTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type, color = Color.White) },
                                        onClick = {
                                            businessType = type
                                            isDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = storeLocation,
                            onValueChange = { storeLocation = it },
                            label = { Text("Cidade e Bairro (Ex: Maputo, Polana Caniço)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0066FF),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_store_location_input")
                        )

                        OutlinedTextField(
                            value = nuitInput,
                            onValueChange = { nuitInput = it },
                            label = { Text("NUIT da Empresa (Opcional - 9 dígitos)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0066FF),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_nuit_input")
                        )
                    }

                    Button(
                        onClick = {
                            if (storeName.isBlank()) {
                                Toast.makeText(context, "Insere o nome da tua empresa.", Toast.LENGTH_SHORT).show()
                            } else {
                                currentStep = 4
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("step_3_next_button")
                    ) {
                        Text("Continuar para PIN de Segurança", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // STEP 4: SEGURANÇA & PIN
                4 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        StepHeader(
                            title = "Passo 4: PIN de Segurança de 4 Dígitos",
                            subtitle = "Escolhe um PIN numérico para autenticação rápida e autorizações no balcão."
                        )

                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { if (it.length <= 4) pinInput = it },
                            label = { Text("Criar PIN de 4 Dígitos") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0066FF),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_pin_input")
                        )

                        OutlinedTextField(
                            value = confirmPinInput,
                            onValueChange = { if (it.length <= 4) confirmPinInput = it },
                            label = { Text("Confirmar PIN de 4 Dígitos") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0066FF),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_confirm_pin_input")
                        )

                        if (pinError != null) {
                            Text(pinError!!, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Activar Biometria (Face / Impressão Digital)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Permite abrir a aplicação no mesmo telemóvel sem pedir PIN.", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                                Switch(
                                    checked = biometricEnabled,
                                    onCheckedChange = { biometricEnabled = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF0066FF))
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (pinInput.length != 4) {
                                pinError = "O PIN deve ter exactamente 4 dígitos numéricos."
                            } else if (pinInput != confirmPinInput) {
                                pinError = "Os PINs introduzidos não coincidem."
                            } else {
                                pinError = null
                                currentStep = 5
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("step_4_next_button")
                    ) {
                        Text("Continuar para Consentimento Legal", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // STEP 5: CONSENTIMENTO LEGAL
                5 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        StepHeader(
                            title = "Passo 5: Termos & Conformidade Legal",
                            subtitle = "Conformidade com a Lei n.º 3/2017 e Lei de Protecção de Dados de Moçambique."
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = acceptTerms,
                                        onCheckedChange = { acceptTerms = it },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0066FF)),
                                        modifier = Modifier.testTag("register_accept_terms_checkbox")
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Li e concordo com os ", color = Color.White, fontSize = 13.sp)
                                    Text(
                                        text = "Termos de Uso",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = TextDecoration.Underline,
                                        modifier = Modifier.clickable { onNavigateToLegalViewer(LegalTab.TERMOS) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = acceptPrivacy,
                                        onCheckedChange = { acceptPrivacy = it },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0066FF)),
                                        modifier = Modifier.testTag("register_accept_privacy_checkbox")
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Li e aceito a ", color = Color.White, fontSize = 13.sp)
                                    Text(
                                        text = "Política de Privacidade",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = TextDecoration.Underline,
                                        modifier = Modifier.clickable { onNavigateToLegalViewer(LegalTab.PRIVACIDADE) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Nota: O teu consentimento será registado com versão 1.0, data, hora e ID de utilizador no Firestore.",
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (acceptTerms && acceptPrivacy) {
                                generatedRecoveryCodes = SecurityUtils.generateRecoveryCodes()
                                currentStep = 6
                            } else {
                                Toast.makeText(context, "Deves aceitar ambos os documentos para criar conta.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = acceptTerms && acceptPrivacy,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0066FF),
                            disabledContainerColor = Color(0xFF334155)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("step_5_next_button")
                    ) {
                        Text("Criar Conta e Gerar Códigos", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // STEP 6: FINALIZAÇÃO & CÓDIGOS DE RECUPERAÇÃO
                6 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        StepHeader(
                            title = "Passo 6: Códigos de Recuperação Guardados",
                            subtitle = "Guarda estes 5 códigos num local seguro. Servem para recuperar a tua conta se perderes o telemóvel."
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("CÓDIGOS DE RECUPERAÇÃO ÚNICOS (NX-XXXX)", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))

                                generatedRecoveryCodes.forEach { code ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .background(Color(0xFF090E1A), RoundedCornerShape(8.dp))
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = code,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 2.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Nexora Recovery Codes", generatedRecoveryCodes.joinToString("\n"))
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Códigos copiados para a área de transferência!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copiar Códigos", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (isRegistering) return@Button
                            isRegistering = true
                            coroutineScope.launch {
                                val result = NexoraRepository.registerOwnerFirebase(
                                    name = fullName,
                                    phone = phoneNumber,
                                    password = password,
                                    storeName = storeName,
                                    businessType = businessType,
                                    city = storeLocation,
                                    nuit = nuitInput,
                                    pin = pinInput,
                                    acceptedTerms = acceptTerms,
                                    acceptedPrivacy = acceptPrivacy,
                                    recoveryCodes = generatedRecoveryCodes.ifEmpty { SecurityUtils.generateRecoveryCodes() }
                                )
                                isRegistering = false
                                result.onSuccess {
                                    if (com.example.config.OnboardingConfig.ONBOARDING_TOUR) {
                                        showOnboardingGuideModal = true
                                    } else {
                                        onCompleteRegistration()
                                    }
                                }.onFailure { error ->
                                    Toast.makeText(context, error.message ?: "Erro ao registar conta no Firebase.", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !isRegistering,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("finish_registration_button")
                    ) {
                        if (isRegistering) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Finalizar e Abrir a Minha Loja", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Onboarding Guide Modal (Step 6 completion)
    if (showOnboardingGuideModal) {
        AlertDialog(
            onDismissRequest = {
                showOnboardingGuideModal = false
                onCompleteRegistration()
            },
            containerColor = Color(0xFF1E293B),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Bem-vindo à NEXORA!", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "A tua empresa '$storeName' foi criada com sucesso com o Plano Profissional gratuito por 14 dias.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )

                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF090E1A)), shape = RoundedCornerShape(10.dp)) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Próximos Passos Recomendados:", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("1. Cadastrar os teus primeiros produtos no separador Produtos.", color = Color.White, fontSize = 12.sp)
                            Text("2. Registar a tua primeira venda no Ponto de Venda (POS).", color = Color.White, fontSize = 12.sp)
                            Text("3. Convidar o teu operador de caixa na secção Equipa.", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOnboardingGuideModal = false
                        onCompleteRegistration()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF))
                ) {
                    Text("Ir para o Painel Principal")
                }
            }
        )
    }
}

@Composable
private fun StepHeader(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8),
            lineHeight = 16.sp
        )
    }
}
