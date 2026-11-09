package com.example.ui.screens.more

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.SwapHoriz
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.util.SecurityUtils
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
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun MoreScreen(
    currentUser: User,
    allUsers: List<User>,
    onSwitchUser: (User) -> Unit,
    onNavigateToClients: () -> Unit,
    onNavigateToDebts: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToSuppliers: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onNavigateToAi: () -> Unit = {},
    onNavigateToSuperAdmin: () -> Unit = {},
    onNavigateToSecuritySync: () -> Unit = {},
    onNavigateToPaymentAccounts: () -> Unit = {},
    onNavigateToTeam: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToLegalViewer: (com.example.ui.screens.privacy.LegalTab) -> Unit = {},
    activeDebtsCount: Int,
    clientsCount: Int,
    unreadNotificationsCount: Int,
    onReopenTour: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showUserSwitchDialog by remember { mutableStateOf(false) }
    var showPinModalForUser by remember { mutableStateOf<User?>(null) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                Column {
                    Text(
                        text = "Mais Opções & Gestão",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Contabilidade, compras, clientes e configurações",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            // Current Active Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexoraBlueLight.copy(alpha = 0.5f))
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
                                        .size(48.dp)
                                        .background(
                                            if (currentUser.role == UserRole.DONO) NexoraBluePrimary else NavyCardElevated,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (currentUser.role == UserRole.DONO) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = currentUser.name,
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = currentUser.role.label,
                                        color = NexoraCyanAccent,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = currentUser.phone,
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = NavyCardBorder)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Estabelecimento Registado", color = TextMuted, fontSize = 10.sp)
                                Text(currentUser.storeName, color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            Box(
                                modifier = Modifier
                                    .background(NexoraBluePrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Moeda: ${currentUser.currencySymbol}", color = NexoraCyanAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Section: Finanças & Gestão (Módulos 8, 9, 10)
            item {
                Text("Finanças e Relatórios", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            item {
                MoreMenuActionItem(
                    title = "Relatórios Financeiros",
                    subtitle = if (currentUser.canViewProfits) "DRE, CMV, margens e vendas" else "Acesso exclusivo ao proprietário",
                    icon = Icons.Default.AccountBalance,
                    iconColor = NexoraCyanAccent,
                    badge = if (currentUser.canViewProfits) null else "Restrito",
                    onClick = onNavigateToReports
                )
            }

            item {
                MoreMenuActionItem(
                    title = "Gestão de Despesas",
                    subtitle = if (currentUser.canViewProfits) "Renda, EDM, salários e transporte" else "Acesso exclusivo ao proprietário",
                    icon = Icons.Default.MoneyOff,
                    iconColor = NexoraRedAlert,
                    badge = null,
                    onClick = onNavigateToExpenses
                )
            }

            item {
                MoreMenuActionItem(
                    title = "Gestão de Fornecedores",
                    subtitle = "Compras, entradas no stock e dívidas",
                    icon = Icons.Default.LocalShipping,
                    iconColor = NexoraGreenSuccess,
                    badge = null,
                    onClick = onNavigateToSuppliers
                )
            }

            // Section: Clientes & Dívidas (Módulos 6 e 7)
            item {
                Text("Clientes e Fiado", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            item {
                MoreMenuActionItem(
                    title = "Gestão de Clientes",
                    subtitle = "$clientsCount clientes cadastrados",
                    icon = Icons.Default.People,
                    iconColor = NexoraBlueLight,
                    badge = null,
                    onClick = onNavigateToClients
                )
            }

            item {
                MoreMenuActionItem(
                    title = "Fiado e Dívidas",
                    subtitle = "Créditos concedidos e amortizações",
                    icon = Icons.Default.CreditCard,
                    iconColor = NexoraAmberWarning,
                    badge = if (activeDebtsCount > 0) "$activeDebtsCount ativas" else null,
                    onClick = onNavigateToDebts
                )
            }

            // Section: Sistema & Subscrição (Módulos 12 e 14)
            item {
                Text("Sistema e Subscrição", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            item {
                MoreMenuActionItem(
                    title = "Centro de Notificações",
                    subtitle = "Alertas de stock e cobranças",
                    icon = Icons.Default.Notifications,
                    iconColor = NexoraAmberWarning,
                    badge = if (unreadNotificationsCount > 0) "$unreadNotificationsCount não lidas" else null,
                    onClick = onNavigateToNotifications
                )
            }

            item {
                MoreMenuActionItem(
                    title = "Minha Subscrição & Planos",
                    subtitle = "Plano ativo e pagamento M-Pesa / mKesh / e-Mola",
                    icon = Icons.Default.CardMembership,
                    iconColor = NexoraCyanAccent,
                    badge = "14 dias teste",
                    onClick = onNavigateToSubscription
                )
            }

            // Section: Ajuda e Suporte
            item {
                Text("Ajuda e Suporte", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            if (com.example.config.OnboardingConfig.ONBOARDING_TOUR && currentUser.role == UserRole.DONO) {
                item {
                    MoreMenuActionItem(
                        title = "Ver o tour outra vez",
                        subtitle = "Aprenda os 4 passos principais para gerir o seu negócio",
                        icon = Icons.Default.Info,
                        iconColor = NexoraCyanAccent,
                        badge = "Tour",
                        onClick = onReopenTour
                    )
                }
            }

            item {
                MoreMenuActionItem(
                    title = "Falar com o suporte no WhatsApp",
                    subtitle = "+258 83 568 1467 · Atendimento oficial NEXORA",
                    icon = Icons.Default.SupportAgent,
                    iconColor = NexoraGreenSuccess,
                    badge = "WhatsApp",
                    onClick = {
                        val cleanPhone = "258835681467"
                        val storeName = currentUser.storeName.ifBlank { "Minha Empresa" }
                        val message = Uri.encode("Olá, preciso de ajuda com a Nexora na empresa $storeName.")
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanPhone?text=$message"))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Não foi possível abrir o WhatsApp: +258 83 568 1467", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }

            // Section: Inteligência Artificial (Módulo 11)
            item {
                Text("Inteligência Artificial Comercial", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            item {
                MoreMenuActionItem(
                    title = "Assistente Nexora (Gemini AI)",
                    subtitle = if (currentUser.canViewProfits) "Diagnóstico, tendências e perguntas" else "Acesso exclusivo ao proprietário",
                    icon = Icons.Default.AutoAwesome,
                    iconColor = NexoraPurple,
                    badge = "Gemini 3.8",
                    onClick = onNavigateToAi
                )
            }

            // Section: Segurança, Sessões & Administração (Módulos 13, 15, 16)
            item {
                Text("Segurança, Sistema e Contas", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            item {
                MoreMenuActionItem(
                    title = "Trocar utilizador",
                    subtitle = "Sessão ativa: ${currentUser.name} (${currentUser.role.label}) · Alternar operador com PIN",
                    icon = Icons.Default.SwapHoriz,
                    iconColor = NexoraCyanAccent,
                    badge = if (currentUser.role == UserRole.DONO) "Dono" else "Caixa",
                    onClick = {
                        showUserSwitchDialog = true
                    }
                )
            }

            item {
                MoreMenuActionItem(
                    title = "Segurança, Sessões & Backup",
                    subtitle = "Dispositivos conectados, cópia JSON e biometria",
                    icon = Icons.Default.Security,
                    iconColor = NexoraCyanAccent,
                    badge = null,
                    onClick = onNavigateToSecuritySync
                )
            }

            item {
                MoreMenuActionItem(
                    title = "Privacidade e Dados",
                    subtitle = "Direitos do utilizador, exportação e eliminação",
                    icon = Icons.Default.Shield,
                    iconColor = NexoraGreenSuccess,
                    badge = "Lei 3/2017",
                    onClick = onNavigateToPrivacy
                )
            }

            item {
                MoreMenuActionItem(
                    title = "Gestão de Equipa & Caixas",
                    subtitle = if (currentUser.canViewProfits) "Adicionar e gerir operadores" else "Acesso exclusivo ao proprietário",
                    icon = Icons.Default.People,
                    iconColor = NexoraBlueLight,
                    badge = null,
                    onClick = onNavigateToTeam
                )
            }

            if (currentUser.isSuperAdmin) {
                item {
                    MoreMenuActionItem(
                        title = "Painel Super-Administrador",
                        subtitle = "Aprovação de comprovativos e empresas",
                        icon = Icons.Default.AdminPanelSettings,
                        iconColor = NexoraPurple,
                        badge = "Admin",
                        onClick = onNavigateToSuperAdmin
                    )
                }
            }

            // Section: Operadores & Turnos (Módulo 13)
            item {
                Text("Operadores de Caixa e Turnos", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            items(allUsers) { user ->
                val isCurrent = user.id == currentUser.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!isCurrent) {
                                showPinModalForUser = user
                                enteredPin = ""
                                pinError = false
                            }
                        }
                        .testTag("operator_item_${user.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) NexoraBluePrimary.copy(alpha = 0.15f) else NavySurfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isCurrent) NexoraBlueLight else NavyCardBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (user.role == UserRole.DONO) NexoraBluePrimary else NavyCardElevated,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (user.role == UserRole.DONO) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(user.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (isCurrent) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(NexoraGreenSuccess.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Sessão Atual", color = NexoraGreenSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text(user.role.label, color = NexoraCyanAccent, fontSize = 11.sp)
                            }
                        }

                        if (!isCurrent) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Alternar", tint = NexoraCyanAccent)
                        }
                    }
                }
            }

            // About NEXORA Section
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            com.example.data.repository.NexoraRepository.logout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NexoraRedAlert.copy(alpha = 0.2f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NexoraRedAlert),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("more_screen_logout_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = NexoraRedAlert, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Terminar Sessão", color = NexoraRedAlert, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
        }
    }

    // Modal: PIN Verification for switching operator
    showPinModalForUser?.let { targetUser ->
        AlertDialog(
            onDismissRequest = { showPinModalForUser = null },
            title = {
                Text(
                    text = "Mudar de Turno / Operador",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "A alternar para: ${targetUser.name} (${targetUser.role.label})",
                        color = TextLight,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Introduz o PIN de 4 dígitos do operador",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            enteredPin = it
                            pinError = false
                        },
                        label = { Text("PIN de Acesso") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = pinError,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NavySurfaceVariant,
                            unfocusedContainerColor = NavySurfaceVariant,
                            focusedBorderColor = NexoraBlueLight,
                            unfocusedBorderColor = NavyCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextLight
                        )
                    )

                    if (pinError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("PIN incorreto. Tenta novamente.", color = NexoraRedAlert, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (SecurityUtils.verifyPin(enteredPin, targetUser.pinHash)) {
                            onSwitchUser(targetUser)
                            showPinModalForUser = null
                        } else {
                            pinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary)
                ) {
                    Text("Aceder", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinModalForUser = null }) {
                    Text("Cancelar", color = TextLight)
                }
            },
            containerColor = NavySurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Modal: Select Operator to Switch To
    if (showUserSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showUserSwitchDialog = false },
            title = {
                Text(
                    text = "Trocar Utilizador",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Sessão ativa: ${currentUser.name} (${currentUser.role.label})",
                        color = NexoraCyanAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Seleccione o operador para alternar de turno. Será solicitado o PIN de 4 dígitos:",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    allUsers.forEach { user ->
                        val isCurrent = user.id == currentUser.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!isCurrent) {
                                        showUserSwitchDialog = false
                                        showPinModalForUser = user
                                        enteredPin = ""
                                        pinError = false
                                    }
                                }
                                .testTag("modal_operator_item_${user.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) NexoraBluePrimary.copy(alpha = 0.2f) else NavyCardElevated
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCurrent) NexoraBlueLight else NavyCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(
                                                if (user.role == UserRole.DONO) NexoraBluePrimary else NavySurface,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (user.role == UserRole.DONO) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(user.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(user.role.label, color = NexoraCyanAccent, fontSize = 11.sp)
                                    }
                                }

                                if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .background(NexoraGreenSuccess.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Ativo", color = NexoraGreenSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                } else {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = NexoraCyanAccent, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUserSwitchDialog = false }) {
                    Text("Cancelar", color = TextLight)
                }
            },
            containerColor = NavySurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun MoreMenuActionItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    badge: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("more_action_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(subtitle, color = TextMuted, fontSize = 11.sp)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .background(NexoraAmberWarning.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(badge, color = NexoraAmberWarning, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
            }
        }
    }
}
