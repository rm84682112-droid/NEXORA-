package com.example.ui.screens.security

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.UserSession
import com.example.data.repository.NexoraRepository
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.NexoraAmberWarning
import com.example.ui.theme.NexoraBlueLight
import com.example.ui.theme.NexoraBluePrimary
import com.example.ui.theme.NexoraCyanAccent
import com.example.ui.theme.NexoraGreenSuccess
import com.example.ui.theme.NexoraPurple
import com.example.ui.theme.NexoraRedAlert
import com.example.ui.theme.TextFaint
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.Formatters

@Composable
fun SecurityAndSyncScreen(
    currentUser: User,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val isOnline by NexoraRepository.isOnline.collectAsState()
    val pendingSync by NexoraRepository.pendingSyncOperations.collectAsState()
    val phoneCredentials by NexoraRepository.phoneCredentials.collectAsState()
    val activeSessions by NexoraRepository.activeSessions.collectAsState()

    var showBackupModal by remember { mutableStateOf(false) }
    var generatedBackupJson by remember { mutableStateOf("") }
    var showRestoreModal by remember { mutableStateOf(false) }
    var restoreInputJson by remember { mutableStateOf("") }
    var restoreMessage by remember { mutableStateOf<String?>(null) }
    var showRecoveryCodesModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(NexoraBluePrimary.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = NexoraCyanAccent)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Segurança, Sessões & Backup",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Gestão de dispositivos, cópias de segurança e regras",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Section: Sync & Offline Status
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        if (isOnline) NexoraGreenSuccess else NexoraAmberWarning,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isOnline) "Estado: Online (Sincronizado)" else "Estado: Offline · $pendingSync pendente(s)",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Switch(
                            checked = isOnline,
                            onCheckedChange = { NexoraRepository.toggleOnlineMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextWhite,
                                checkedTrackColor = NexoraGreenSuccess,
                                uncheckedThumbColor = TextWhite,
                                uncheckedTrackColor = NexoraAmberWarning
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Resolução Automática de Conflitos: Quando a ligação cai, as vendas e o stock são guardados na memória local protegida do dispositivo. Ao restabelecer a ligação, o Firestore processa transacções atómicas no servidor para prevenir conflitos de stock caso outro terminal também tenha vendido o mesmo produto.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Section: Phone Auth & Recovery
        item {
            Text(
                text = "Autenticação por Número de Telefone (+258)",
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Número Verificado", color = TextMuted, fontSize = 12.sp)
                            Text(
                                text = phoneCredentials.phoneNumber,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(NexoraGreenSuccess.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("SMS Ativo", color = NexoraGreenSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showRecoveryCodesModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NavySurfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = NexoraCyanAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Códigos de Recuperação", color = TextWhite, fontSize = 11.sp)
                        }

                        // Biometrics Switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = NexoraPurple, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Biometria", color = TextWhite, fontSize = 12.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(
                                checked = phoneCredentials.biometricEnabled,
                                onCheckedChange = { NexoraRepository.toggleBiometric(it) },
                                colors = SwitchDefaults.colors(checkedTrackColor = NexoraPurple)
                            )
                        }
                    }
                }
            }
        }

        // Section: Connected Devices & Sessions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Dispositivos & Sessões Activas (${activeSessions.size})",
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { NexoraRepository.terminateAllOtherSessions() }) {
                    Text("Terminar Outras", color = NexoraRedAlert, fontSize = 12.sp)
                }
            }
        }

        items(activeSessions) { session ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (session.isCurrent) NexoraGreenSuccess.copy(alpha = 0.2f) else NavySurfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = null,
                            tint = if (session.isCurrent) NexoraGreenSuccess else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(session.deviceModel, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (session.isCurrent) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(NexoraGreenSuccess.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Actual", color = NexoraGreenSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text("${session.platform} • ${session.ipAddressOrCity}", color = TextMuted, fontSize = 11.sp)
                        Text("Activo: ${Formatters.formatDateTime(session.lastActive)}", color = TextFaint, fontSize = 10.sp)
                    }

                    if (!session.isCurrent) {
                        IconButton(
                            onClick = { NexoraRepository.terminateSession(session.id) }
                        ) {
                            Text("Sair", color = NexoraRedAlert, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Backup & Export / Restore
        item {
            Text(
                text = "Cópia de Segurança & Exportação de Dados",
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Garante que nunca perdes as informações da tua empresa mesmo em caso de perda ou troca de telemóvel.",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val json = NexoraRepository.exportAllBusinessDataJson()
                        generatedBackupJson = json
                        showBackupModal = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("export_backup_button")
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exportar Cópia", fontSize = 12.sp)
                }

                Button(
                    onClick = { showRestoreModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NavySurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("restore_backup_button")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = NexoraCyanAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Restaurar Dados", color = TextWhite, fontSize = 12.sp)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal: Backup Exported
    if (showBackupModal) {
        AlertDialog(
            onDismissRequest = { showBackupModal = false },
            title = { Text("Cópia de Segurança Gerada", color = TextWhite, fontSize = 16.sp) },
            text = {
                Column {
                    Text(
                        text = "Os dados da tua empresa foram exportados com sucesso em formato JSON estruturado.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(NavySurface, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = generatedBackupJson.take(300) + "...",
                            color = TextLight,
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboard.setText(AnnotatedString(generatedBackupJson))
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, generatedBackupJson)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Guardar Cópia de Segurança NEXORA"))
                        showBackupModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraGreenSuccess)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Partilhar / Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupModal = false }) {
                    Text("Fechar", color = TextMuted)
                }
            },
            containerColor = NavyCardElevated
        )
    }

    // Modal: Restore Data
    if (showRestoreModal) {
        AlertDialog(
            onDismissRequest = { showRestoreModal = false },
            title = { Text("Restaurar Dados da Empresa", color = TextWhite, fontSize = 16.sp) },
            text = {
                Column {
                    Text(
                        text = "Cola aqui o texto JSON da tua cópia de segurança para restaurar o catálogo de produtos e configurações:",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = restoreInputJson,
                        onValueChange = { restoreInputJson = it },
                        placeholder = { Text("Cola o JSON aqui...", color = TextFaint) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = NexoraCyanAccent
                        )
                    )
                    if (restoreMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(restoreMessage ?: "", color = NexoraGreenSuccess, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val res = NexoraRepository.restoreBusinessDataFromJson(restoreInputJson)
                        res.onSuccess { count ->
                            restoreMessage = "Sucesso! Foram restaurados $count artigos."
                            Toast.makeText(context, "Restaurados $count artigos!", Toast.LENGTH_LONG).show()
                            showRestoreModal = false
                        }.onFailure {
                            restoreMessage = "Erro no ficheiro JSON: ${it.localizedMessage}"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary)
                ) {
                    Text("Confirmar Restauração")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreModal = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            },
            containerColor = NavyCardElevated
        )
    }

    // Modal: Recovery Codes
    if (showRecoveryCodesModal) {
        AlertDialog(
            onDismissRequest = { showRecoveryCodesModal = false },
            title = { Text("Códigos de Recuperação de Acesso", color = TextWhite, fontSize = 16.sp) },
            text = {
                Column {
                    Text(
                        text = "Guarda estes códigos num lugar seguro. Se perderes acesso ao teu número de telefone (+258), podes usar qualquer um destes códigos para aceder à tua conta:",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    phoneCredentials.recoveryCodes.forEach { code ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .background(NavySurface, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = code,
                                color = NexoraCyanAccent,
                                fontWeight = FontWeight.Bold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboard.setText(AnnotatedString(phoneCredentials.recoveryCodes.joinToString("\n")))
                        Toast.makeText(context, "Códigos copiados para a área de transferência!", Toast.LENGTH_SHORT).show()
                        showRecoveryCodesModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraPurple)
                ) {
                    Text("Copiar Todos")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecoveryCodesModal = false }) {
                    Text("Fechar", color = TextMuted)
                }
            },
            containerColor = NavyCardElevated
        )
    }
}
