package com.example.ui.screens.privacy

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.legal.LegalDocuments
import com.example.data.repository.NexoraRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PrivacyAndDataScreen(
    onBack: () -> Unit,
    onNavigateToLegalViewer: (LegalTab) -> Unit
) {
    val currentUser by NexoraRepository.currentUser.collectAsState()
    val privacySettings by NexoraRepository.privacySettings.collectAsState()
    val userConsents by NexoraRepository.userConsents.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showEraseDialog by remember { mutableStateOf(false) }
    var exportedContent by remember { mutableStateOf("") }
    var erasePinInput by remember { mutableStateOf("") }
    var eraseErrorMessage by remember { mutableStateOf<String?>(null) }
    var eraseSuccessMessage by remember { mutableStateOf<String?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "MZ")) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D1527))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("privacy_back_button")
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
                        text = "Privacidade e Protecção de Dados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Moçambique · Constituição Art. 71 & Lei 3/2017",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Profile Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF0066FF), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${currentUser.phone} · ${currentUser.storeName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.testTag("edit_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Dados",
                            tint = Color(0xFF38BDF8)
                        )
                    }
                }
            }

            // Legal Documents Shortcut Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Documentos e Termos Legais",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { onNavigateToLegalViewer(LegalTab.PRIVACIDADE) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Política de Privacidade", fontSize = 11.sp, color = Color.White)
                        }
                        Button(
                            onClick = { onNavigateToLegalViewer(LegalTab.TERMOS) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Termos de Uso", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }

            // Privacy Preferences Toggles
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Preferências de Privacidade",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // AI Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Análise de Inteligência Artificial", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Permite ao Gemini analisar dados de stock e vendas da loja (sem PII de clientes).", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Switch(
                            checked = privacySettings.allowAiAnalysis,
                            onCheckedChange = { checked ->
                                NexoraRepository.updatePrivacySettings(
                                    allowAiAnalysis = checked,
                                    allowNotifications = privacySettings.allowNotifications
                                )
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF0066FF))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Notifications Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Alertas de Sistema & Alertas Legais", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Receber avisos de stock baixo, fiado e actualizações de segurança.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Switch(
                            checked = privacySettings.allowNotifications,
                            onCheckedChange = { checked ->
                                NexoraRepository.updatePrivacySettings(
                                    allowAiAnalysis = privacySettings.allowAiAnalysis,
                                    allowNotifications = checked
                                )
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF0066FF))
                        )
                    }
                }
            }

            // Data Portability & Rights Section
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Direitos do Titular dos Dados",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Direito de Acesso e Portabilidade (Art. 71 CRM): Podes descarregar uma cópia integral em formato legível por computador contendo o teu perfil, preferências, consentimentos, produtos e vendas.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            exportedContent = NexoraRepository.exportPersonalAndStoreDataCsvJson()
                            showExportDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_data_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exportar os Meus Dados (JSON / CSV)")
                    }
                }
            }

            // Consent History Log
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFFA855F7))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Histórico de Consentimentos",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (userConsents.isEmpty()) {
                        Text("Nenhum consentimento registado.", color = Color(0xFF64748B), fontSize = 12.sp)
                    } else {
                        userConsents.take(4).forEach { consent ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Consentimento v${consent.documentVersion}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Privacidade: ${consent.acceptedPrivacy} · IA: ${consent.acceptedAiAnalysis} · " + dateFormat.format(Date(consent.timestamp)), color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                                Text(
                                    text = if (consent.acceptedPrivacy) "Aceite" else "Revogado",
                                    color = if (consent.acceptedPrivacy) Color(0xFF10B981) else Color(0xFFEF4444),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Erasure & Right to be Forgotten (Account Deletion)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1215)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Direito ao Esquecimento (Eliminar Conta)",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFCA5A5),
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = LegalDocuments.LEGAL_NOTICE_DISCLAIMER,
                        color = Color(0xFFF87171),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showEraseDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("erase_account_button")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Solicitar Eliminação de Dados & Conta")
                    }
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var nameInput by remember { mutableStateOf(currentUser.name) }
        var phoneInput by remember { mutableStateOf(currentUser.phone) }
        var storeInput by remember { mutableStateOf(currentUser.storeName) }
        var locationInput by remember { mutableStateOf(currentUser.storeLocation) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Text("Rectificar Dados Pessoais", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Podes alterar as tuas informações pessoais a qualquer momento conforme a lei de protecção de dados.", color = Color(0xFF94A3B8), fontSize = 12.sp)

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nome Completo") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0066FF), unfocusedBorderColor = Color(0xFF475569), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth().testTag("edit_name_input")
                    )

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Número de Telefone") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0066FF), unfocusedBorderColor = Color(0xFF475569), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth().testTag("edit_phone_input")
                    )

                    OutlinedTextField(
                        value = storeInput,
                        onValueChange = { storeInput = it },
                        label = { Text("Nome do Negócio / Loja") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0066FF), unfocusedBorderColor = Color(0xFF475569), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth().testTag("edit_store_input")
                    )

                    OutlinedTextField(
                        value = locationInput,
                        onValueChange = { locationInput = it },
                        label = { Text("Localização (Cidade / Bairro)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0066FF), unfocusedBorderColor = Color(0xFF475569), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth().testTag("edit_location_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        NexoraRepository.updateUserPersonalDetails(nameInput, phoneInput, storeInput, locationInput)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF))
                ) {
                    Text("Guardar Alterações")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancelar", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Export Data Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Text("Cópia Completa dos Dados", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.height(300.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Estes são os teus dados exportados em formato estruturado (JSON / CSV):",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF090E1A), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = exportedContent,
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showExportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF))
                ) {
                    Text("Concluído")
                }
            }
        )
    }

    // Erase Account Dialog
    if (showEraseDialog) {
        AlertDialog(
            onDismissRequest = { showEraseDialog = false },
            containerColor = Color(0xFF2A1215),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirmar Eliminação com PIN", color = Color(0xFFFCA5A5), fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Atenção: Esta acção é irreversível. Os teus dados pessoais (nome, telefone, clientes, preferências) serão eliminados. Os registos de vendas fiscais serão retidos em formato anonimizado por 5 anos conforme a lei moçambicana.",
                        color = Color(0xFFFCA5A5),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = erasePinInput,
                        onValueChange = { erasePinInput = it },
                        label = { Text("Insere o teu PIN de Segurança") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFEF4444), unfocusedBorderColor = Color(0xFF7F1D1D), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth().testTag("erase_pin_input")
                    )

                    if (eraseErrorMessage != null) {
                        Text(eraseErrorMessage!!, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    if (eraseSuccessMessage != null) {
                        Text(eraseSuccessMessage!!, color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val res = NexoraRepository.eraseAccountAndUserData(erasePinInput)
                        res.onSuccess {
                            eraseSuccessMessage = "Conta eliminada com sucesso. Os dados pessoais foram removidos."
                            eraseErrorMessage = null
                        }.onFailure { err ->
                            eraseErrorMessage = err.message ?: "PIN incorrecto."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Eliminar Agora")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEraseDialog = false }) {
                    Text("Cancelar", color = Color(0xFFFCA5A5))
                }
            }
        )
    }
}
