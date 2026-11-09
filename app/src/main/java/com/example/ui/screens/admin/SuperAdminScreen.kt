package com.example.ui.screens.admin

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditLog
import com.example.data.model.BusinessTenant
import com.example.data.model.PaymentProofStatus
import com.example.data.model.PlanPricingConfig
import com.example.data.model.PlanTier
import com.example.data.model.SubscriptionPaymentProof
import com.example.data.model.SupportTicket
import com.example.data.model.TenantStatus
import com.example.data.model.TicketStatus
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.repository.NexoraRepository
import com.example.ui.theme.EMolaOrange
import com.example.ui.theme.MPesaRed
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
fun SuperAdminScreen(
    currentUser: User,
    onBack: () -> Unit
) {
    val allTenants by NexoraRepository.allTenants.collectAsState()
    val paymentProofs by NexoraRepository.allPaymentProofs.collectAsState()
    val supportTickets by NexoraRepository.supportTickets.collectAsState()
    val pricingConfigs by NexoraRepository.pricingConfigs.collectAsState()
    val auditLogs by NexoraRepository.auditLogs.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var proofToReject by remember { mutableStateOf<SubscriptionPaymentProof?>(null) }
    var rejectionReasonText by remember { mutableStateOf("") }
    var ticketToReply by remember { mutableStateOf<SupportTicket?>(null) }
    var ticketReplyText by remember { mutableStateOf("") }
    var editingPlanConfig by remember { mutableStateOf<PlanPricingConfig?>(null) }
    var editedPriceMT by remember { mutableStateOf("") }

    if (currentUser.role != UserRole.ADMIN) {
        // Gate screen for non-admin
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(NexoraPurple.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = NexoraPurple,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Acesso Restrito: Super-Administrador",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "O Painel de Super-Administrador é exclusivo para a gestão da infra-estrutura NEXORA, aprovação de pagamentos de clientes e monitoria de auditoria.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavySurfaceVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Acesso protegido: Requer claim de Administrador (admin: true) atribuída no Firebase Auth pelo backend.",
                            color = NexoraCyanAccent,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onBack,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Voltar", color = TextMuted)
                    }
                }
            }
        }
        return
    }

    val dataIncidents by NexoraRepository.dataIncidents.collectAsState()

    val tabs = listOf(
        "Visão Global",
        "Pagamentos (${paymentProofs.count { it.status == PaymentProofStatus.PENDENTE }})",
        "Empresas (${allTenants.size})",
        "Planos & Preços",
        "Suporte (${supportTickets.count { it.status == TicketStatus.ABERTO }})",
        "Incidentes (${dataIncidents.size})",
        "Auditoria"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(NexoraPurple.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = NexoraPurple,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Painel Super-Admin",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Plataforma NEXORA Moçambique",
                        color = NexoraCyanAccent,
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .background(NexoraGreenSuccess.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Backend Conectado", color = NexoraGreenSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Scrollable Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = NavySurface,
            contentColor = NexoraCyanAccent,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = NexoraPurple
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) NexoraCyanAccent else TextMuted,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> GlobalOverviewTab(allTenants, paymentProofs)
                1 -> PaymentProofsTab(
                    paymentProofs = paymentProofs,
                    onApprove = { NexoraRepository.approvePaymentProof(it) },
                    onRejectPrompt = { proofToReject = it }
                )
                2 -> TenantsTab(
                    tenants = allTenants,
                    onToggleStatus = { NexoraRepository.toggleTenantStatus(it) }
                )
                3 -> PricingPlansTab(
                    pricingConfigs = pricingConfigs,
                    onEditPlan = {
                        editingPlanConfig = it
                        editedPriceMT = it.priceMT.toString()
                    }
                )
                4 -> SupportTicketsTab(
                    tickets = supportTickets,
                    onReplyPrompt = { ticketToReply = it }
                )
                5 -> DataIncidentsTab(dataIncidents)
                6 -> AuditLogsTab(auditLogs)
            }
        }
    }

    // Reject Payment Dialog
    proofToReject?.let { proof ->
        AlertDialog(
            onDismissRequest = { proofToReject = null },
            title = { Text("Rejeitar Comprovativo de Pagamento", color = TextWhite, fontSize = 16.sp) },
            text = {
                Column {
                    Text(
                        text = "Indica o motivo da rejeição para ${proof.businessName} (Ref: ${proof.transactionReference}). O comerciante receberá este aviso no centro de notificações.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rejectionReasonText,
                        onValueChange = { rejectionReasonText = it },
                        placeholder = { Text("Ex: Referência não encontrada no extracto M-Pesa", color = TextFaint) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = NexoraRedAlert
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = if (rejectionReasonText.isNotBlank()) rejectionReasonText else "Referência de transacção não confirmada."
                        NexoraRepository.rejectPaymentProof(proof.id, reason)
                        proofToReject = null
                        rejectionReasonText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraRedAlert)
                ) {
                    Text("Confirmar Rejeição")
                }
            },
            dismissButton = {
                TextButton(onClick = { proofToReject = null }) {
                    Text("Cancelar", color = TextMuted)
                }
            },
            containerColor = NavyCardElevated
        )
    }

    // Support Reply Dialog
    ticketToReply?.let { ticket ->
        AlertDialog(
            onDismissRequest = { ticketToReply = null },
            title = { Text("Responder a Ticket de Suporte", color = TextWhite, fontSize = 16.sp) },
            text = {
                Column {
                    Text(text = "Empresa: ${ticket.businessName}", color = NexoraCyanAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Assunto: ${ticket.subject}", color = TextWhite, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "\"${ticket.message}\"", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = ticketReplyText,
                        onValueChange = { ticketReplyText = it },
                        placeholder = { Text("Escreve a resposta oficial da NEXORA...", color = TextFaint) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = NexoraPurple
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (ticketReplyText.isNotBlank()) {
                            NexoraRepository.replySupportTicket(ticket.id, ticketReplyText)
                            ticketToReply = null
                            ticketReplyText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraPurple)
                ) {
                    Text("Enviar Resposta")
                }
            },
            dismissButton = {
                TextButton(onClick = { ticketToReply = null }) {
                    Text("Cancelar", color = TextMuted)
                }
            },
            containerColor = NavyCardElevated
        )
    }

    // Edit Pricing Dialog
    editingPlanConfig?.let { config ->
        AlertDialog(
            onDismissRequest = { editingPlanConfig = null },
            title = { Text("Actualizar Preço: ${config.plan.title}", color = TextWhite, fontSize = 16.sp) },
            text = {
                Column {
                    Text("Define o novo valor mensal em Meticais (MT):", color = TextMuted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editedPriceMT,
                        onValueChange = { editedPriceMT = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = NexoraCyanAccent
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newPrice = editedPriceMT.toDoubleOrNull() ?: config.priceMT
                        NexoraRepository.updatePlanPrice(config.plan, newPrice, config.maxProducts, config.maxUsers, config.aiAnalysesPerMonth)
                        editingPlanConfig = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary)
                ) {
                    Text("Guardar Alterações")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPlanConfig = null }) {
                    Text("Cancelar", color = TextMuted)
                }
            },
            containerColor = NavyCardElevated
        )
    }
}

@Composable
private fun GlobalOverviewTab(
    tenants: List<BusinessTenant>,
    proofs: List<SubscriptionPaymentProof>
) {
    val totalRevenueMT = proofs.filter { it.status == PaymentProofStatus.APROVADO }.sumOf { it.amountMT }
    val totalSalesVolume = tenants.sumOf { it.totalRevenueMT }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Métricas da Plataforma NEXORA", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Empresas Registadas",
                    value = "${tenants.size}",
                    subtitle = "${tenants.count { it.status == TenantStatus.ATIVA }} activas",
                    color = NexoraBlueLight,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Receita de Subscrições",
                    value = Formatters.formatMoney(totalRevenueMT),
                    subtitle = "Validadas via M-Pesa",
                    color = NexoraGreenSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Volume Transaccionado",
                    value = Formatters.formatMoney(totalSalesVolume),
                    subtitle = "Em vendas comerciais",
                    color = NexoraCyanAccent,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Pagamentos Pendentes",
                    value = "${proofs.count { it.status == PaymentProofStatus.PENDENTE }}",
                    subtitle = "A aguardar análise",
                    color = NexoraAmberWarning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, NexoraCyanAccent.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🚀", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Métricas de Aprendizagem & Onboarding",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    val totalCreated = tenants.size
                    val withFirstSale = tenants.count { it.firstSaleAt != null || it.totalSalesCount > 0 }
                    val saleConversion = if (totalCreated > 0) ((withFirstSale.toDouble() / totalCreated) * 100).toInt() else 0
                    val activeNextDay = tenants.count { (it.lastActiveAt - it.createdAt) >= 86400_000L }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Contas Criadas", color = TextMuted, fontSize = 11.sp)
                            Text("$totalCreated", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Fizeram 1ª Venda", color = TextMuted, fontSize = 11.sp)
                            Text("$withFirstSale ($saleConversion%)", color = NexoraGreenSuccess, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Voltaram 2º Dia (Aprox)", color = TextMuted, fontSize = 11.sp)
                            Text("$activeNextDay", color = NexoraCyanAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Configuração de Segurança dos Dados",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "As regras de segurança do Firestore (Security Rules) aplicam isolamento absoluto por empresa (tenant-isolation). Nenhuma empresa tem permissão de leitura nos documentos de outra. As finanças e relatórios estão protegidos contra operadores de caixa.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = TextMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = TextFaint, fontSize = 10.sp)
        }
    }
}

@Composable
private fun PaymentProofsTab(
    paymentProofs: List<SubscriptionPaymentProof>,
    onApprove: (String) -> Unit,
    onRejectPrompt: (SubscriptionPaymentProof) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(
                text = "Validação de Comprovativos M-Pesa & e-Mola",
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Confirma a recepção do valor na conta bancária/carteira e aprova para activar o plano do cliente imediatamente.",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        items(paymentProofs) { proof ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = proof.businessName,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    when (proof.status) {
                                        PaymentProofStatus.APROVADO -> NexoraGreenSuccess.copy(alpha = 0.2f)
                                        PaymentProofStatus.PENDENTE -> NexoraAmberWarning.copy(alpha = 0.2f)
                                        PaymentProofStatus.REJEITADO -> NexoraRedAlert.copy(alpha = 0.2f)
                                    },
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = proof.status.label,
                                color = when (proof.status) {
                                    PaymentProofStatus.APROVADO -> NexoraGreenSuccess
                                    PaymentProofStatus.PENDENTE -> NexoraAmberWarning
                                    PaymentProofStatus.REJEITADO -> NexoraRedAlert
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Plano Solicitado: ${proof.requestedPlan.title} (${Formatters.formatMoney(proof.amountMT)})",
                        color = NexoraCyanAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Método: ${proof.paymentMethod.displayName} • Conta Destino: ${proof.destinationAccount}",
                        color = TextLight,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Referência de Transacção: ${proof.transactionReference}",
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Submetido em: ${Formatters.formatDateTime(proof.submittedAt)}",
                        color = TextFaint,
                        fontSize = 11.sp
                    )

                    if (proof.rejectionReason != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Motivo da rejeição: ${proof.rejectionReason}",
                            color = NexoraRedAlert,
                            fontSize = 12.sp
                        )
                    }

                    if (proof.status == PaymentProofStatus.PENDENTE) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onApprove(proof.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = NexoraGreenSuccess),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Aprovar Plano")
                            }

                            Button(
                                onClick = { onRejectPrompt(proof) },
                                colors = ButtonDefaults.buttonColors(containerColor = NexoraRedAlert),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Rejeitar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TenantsTab(
    tenants: List<BusinessTenant>,
    onToggleStatus: (String) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Gestão de Empresas & Clientes NEXORA", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        items(tenants) { tenant ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(tenant.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Dono: ${tenant.ownerName} (${tenant.ownerPhone})", color = TextMuted, fontSize = 12.sp)
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    if (tenant.status == TenantStatus.ATIVA) NexoraGreenSuccess.copy(alpha = 0.2f) else NexoraRedAlert.copy(alpha = 0.2f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tenant.status.label,
                                color = if (tenant.status == TenantStatus.ATIVA) NexoraGreenSuccess else NexoraRedAlert,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Plano: ${tenant.plan.title} • Artigos: ${tenant.totalProducts} • Vendas: ${tenant.totalSalesCount}",
                        color = NexoraCyanAccent,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onToggleStatus(tenant.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (tenant.status == TenantStatus.ATIVA) NexoraRedAlert else NexoraGreenSuccess
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (tenant.status == TenantStatus.ATIVA) "Suspender Empresa" else "Reactivar Empresa")
                    }
                }
            }
        }
    }
}

@Composable
private fun PricingPlansTab(
    pricingConfigs: List<PlanPricingConfig>,
    onEditPlan: (PlanPricingConfig) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Gestão de Planos & Preços (Moçambique)", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Os valores em Meticais (MT) reflectem imediatamente na página de subscrições dos clientes.", color = TextMuted, fontSize = 12.sp)
        }

        items(pricingConfigs) { config ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(config.plan.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (config.priceMT == 0.0) "Gratuito" else "${Formatters.formatMoney(config.priceMT)} / mês",
                            color = NexoraCyanAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Até ${config.maxProducts} produtos • ${config.maxUsers} operadores • ${config.aiAnalysesPerMonth} análises IA",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        onClick = { onEditPlan(config) },
                        modifier = Modifier.background(NavySurfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = NexoraCyanAccent)
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportTicketsTab(
    tickets: List<SupportTicket>,
    onReplyPrompt: (SupportTicket) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Pedidos de Apoio & Suporte Técnico", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        items(tickets) { ticket ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(ticket.businessName, color = NexoraCyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(ticket.status.label, color = if (ticket.status == TicketStatus.ABERTO) NexoraAmberWarning else NexoraGreenSuccess, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(ticket.subject, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(ticket.message, color = TextLight, fontSize = 13.sp)

                    if (ticket.replyText != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NavySurface, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text("Resposta enviada: ${ticket.replyText}", color = NexoraGreenSuccess, fontSize = 12.sp)
                        }
                    }

                    if (ticket.status != TicketStatus.RESOLVIDO) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onReplyPrompt(ticket) },
                            colors = ButtonDefaults.buttonColors(containerColor = NexoraPurple),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Responder ao Comerciante")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditLogsTab(auditLogs: List<AuditLog>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Registo Oficial de Auditoria (Audit Logs)", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Rastreamento de todas as alterações críticas de dados e permissões no sistema.", color = TextMuted, fontSize = 12.sp)
        }

        items(auditLogs) { log ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(NexoraBluePrimary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = NexoraCyanAccent, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(log.action.label, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(Formatters.formatDateTime(log.timestamp), color = TextFaint, fontSize = 10.sp)
                        }
                        Text("Por: ${log.userName} (${log.userRole}) • Alvo: ${log.targetItem}", color = NexoraCyanAccent, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DataIncidentsTab(incidents: List<com.example.data.model.DataIncident>) {
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Plano de Gestão de Incidentes de Dados", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Registo oficial de fugas ou inconformidades de segurança (Lei n.º 3/2017)", color = TextMuted, fontSize = 12.sp)
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Registar Incidente", fontSize = 12.sp)
                }
            }
        }

        items(incidents) { inc ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(inc.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Box(
                            modifier = Modifier
                                .background(
                                    when (inc.severity) {
                                        com.example.data.model.IncidentSeverity.BAIXO -> NexoraGreenSuccess.copy(alpha = 0.2f)
                                        com.example.data.model.IncidentSeverity.MEDIO -> NexoraAmberWarning.copy(alpha = 0.2f)
                                        com.example.data.model.IncidentSeverity.ALTO, com.example.data.model.IncidentSeverity.CRITICO -> NexoraRedAlert.copy(alpha = 0.2f)
                                    },
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = inc.severity.label,
                                color = when (inc.severity) {
                                    com.example.data.model.IncidentSeverity.BAIXO -> NexoraGreenSuccess
                                    com.example.data.model.IncidentSeverity.MEDIO -> NexoraAmberWarning
                                    com.example.data.model.IncidentSeverity.ALTO, com.example.data.model.IncidentSeverity.CRITICO -> NexoraRedAlert
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Estado: ${inc.status.label} · Utilizadores afectados: ${inc.affectedCount}", color = NexoraCyanAccent, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(inc.description, color = TextLight, fontSize = 13.sp)

                    if (inc.status != com.example.data.model.IncidentStatus.MITIGADO) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { NexoraRepository.resolveDataIncident(inc.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = NexoraGreenSuccess),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Marcar como Mitigado")
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var titleInput by remember { mutableStateOf("") }
        var descInput by remember { mutableStateOf("") }
        var countInput by remember { mutableStateOf("0") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Registar Incidente de Segurança", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Indica os detalhes do incidente de dados de acordo com a legislação de Moçambique:", color = TextMuted, fontSize = 12.sp)

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Título do Incidente") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    OutlinedTextField(
                        value = countInput,
                        onValueChange = { countInput = it },
                        label = { Text("Número Estimado de Afectados") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("Descrição dos Fatos e Mitigação") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titleInput.isNotBlank()) {
                            val count = countInput.toIntOrNull() ?: 0
                            NexoraRepository.logDataIncident(
                                title = titleInput,
                                severity = com.example.data.model.IncidentSeverity.MEDIO,
                                affectedCount = count,
                                description = descInput
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraPurple)
                ) {
                    Text("Registar & Notificar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            },
            containerColor = NavyCardElevated
        )
    }
}

