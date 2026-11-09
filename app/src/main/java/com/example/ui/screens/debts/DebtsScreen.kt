package com.example.ui.screens.debts

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DebtRecord
import com.example.data.model.DebtStatus
import com.example.data.model.PaymentMethod
import com.example.ui.theme.CashGreen
import com.example.ui.theme.EMolaOrange
import com.example.ui.theme.FiadoBlue
import com.example.ui.theme.MKeshYellow
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
import com.example.ui.theme.NexoraRedAlert
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.Formatters

@Composable
fun DebtsScreen(
    debts: List<DebtRecord>,
    onRecordPayment: (debtId: String, amount: Double, method: PaymentMethod, notes: String) -> Unit,
    onNavigateToPos: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("Abertas") }
    var debtToPay by remember { mutableStateOf<DebtRecord?>(null) }

    val openDebts = remember(debts) { debts.filter { it.status != DebtStatus.PAGA } }
    val overdueDebts = remember(debts) { debts.filter { it.status == DebtStatus.VENCIDA } }
    val totalPendingAmount = remember(openDebts) { openDebts.sumOf { it.remainingBalance } }

    val filteredDebts = remember(debts, selectedFilter) {
        when (selectedFilter) {
            "Abertas" -> openDebts
            "Vencidas" -> overdueDebts
            "Pagas" -> debts.filter { it.status == DebtStatus.PAGA }
            else -> debts
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                Column {
                    Text(
                        text = "Fiado e Dívidas de Clientes",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Controle de vendas a crédito e amortizações",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            // Overview Metric Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "TOTAL DE FIADO PENDENTE",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatMoney(totalPendingAmount),
                            color = if (totalPendingAmount > 0) NexoraAmberWarning else NexoraGreenSuccess,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DebtMetricItem(
                                label = "Créditos Ativos",
                                value = "${openDebts.size} registos",
                                color = NexoraCyanAccent
                            )
                            DebtMetricItem(
                                label = "Em Atraso",
                                value = "${overdueDebts.size} vencidas",
                                color = if (overdueDebts.isNotEmpty()) NexoraRedAlert else TextMuted
                            )
                            DebtMetricItem(
                                label = "Pagas",
                                value = "${debts.count { it.status == DebtStatus.PAGA }} quitadas",
                                color = NexoraGreenSuccess
                            )
                        }
                    }
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Abertas", "Vencidas", "Pagas", "Todas").forEach { filter ->
                        val isSelected = filter == selectedFilter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NexoraBluePrimary,
                                selectedLabelColor = Color.White,
                                containerColor = NavyCardElevated,
                                labelColor = TextMuted
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = NavyCardBorder,
                                selectedBorderColor = NexoraBlueLight
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Contextual Tooltip for Fiado Screen
            item {
                val dismissedTooltips by com.example.data.repository.NexoraRepository.dismissedTooltips.collectAsState()
                if (com.example.config.OnboardingConfig.ONBOARDING_TIPS && "fiado" !in dismissedTooltips) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = NexoraCyanAccent.copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NexoraCyanAccent.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "💡", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Dica de Fiado: Veja quem deve, consulte datas limite e lembre clientes de forma amigável pelo WhatsApp.",
                                    color = TextWhite,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = { com.example.data.repository.NexoraRepository.dismissTooltip("fiado") }
                            ) {
                                Text("Entendi", color = NexoraCyanAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Debts List
            if (filteredDebts.isEmpty()) {
                item {
                    com.example.ui.components.EducationalEmptyState(
                        iconEmoji = "📑",
                        title = "Ninguém lhe deve nada",
                        description = "Quando vender a crédito, os registos e botões de cobrança por WhatsApp aparecem aqui.",
                        actionLabel = "Registar Venda a Fiado",
                        onAction = onNavigateToPos
                    )
                }
            }

            items(filteredDebts, key = { it.id }) { debt ->
                DebtItemCard(
                    debt = debt,
                    onPay = { debtToPay = debt },
                    onSendWhatsAppReminder = {
                        sendDebtReminderViaWhatsApp(context, debt)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal: Record Payment
    debtToPay?.let { debt ->
        RecordPaymentDialog(
            debt = debt,
            onDismiss = { debtToPay = null },
            onConfirm = { amount, method, notes ->
                onRecordPayment(debt.id, amount, method, notes)
                debtToPay = null
            }
        )
    }
}

@Composable
private fun DebtMetricItem(label: String, value: String, color: Color) {
    Column {
        Text(text = label, color = TextMuted, fontSize = 10.sp)
        Text(text = value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DebtItemCard(
    debt: DebtRecord,
    onPay: () -> Unit,
    onSendWhatsAppReminder: () -> Unit
) {
    val (statusColor, statusIcon) = when (debt.status) {
        DebtStatus.PAGA -> Pair(NexoraGreenSuccess, Icons.Default.CheckCircle)
        DebtStatus.VENCIDA -> Pair(NexoraRedAlert, Icons.Default.Error)
        DebtStatus.PARCIAL -> Pair(NexoraCyanAccent, Icons.Default.AccessTime)
        DebtStatus.PENDENTE -> Pair(NexoraAmberWarning, Icons.Default.AccessTime)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("debt_card_${debt.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(statusColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = debt.clientName,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${debt.clientPhone} • ${debt.receiptNumber}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = debt.status.label,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = NavyCardBorder)
            Spacer(modifier = Modifier.height(10.dp))

            // Debt Values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Saldo a Pagar", color = TextMuted, fontSize = 11.sp)
                    Text(
                        text = Formatters.formatMoney(debt.remainingBalance),
                        color = if (debt.remainingBalance <= 0) NexoraGreenSuccess else NexoraAmberWarning,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Vencimento: ${Formatters.formatDate(debt.dueDate)}",
                        color = if (debt.isOverdue) NexoraRedAlert else TextLight,
                        fontSize = 11.sp,
                        fontWeight = if (debt.isOverdue) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = Formatters.formatRelativeDate(debt.dueDate),
                        color = if (debt.isOverdue) NexoraRedAlert else TextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            // Payment History if partial
            if (debt.payments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                debt.payments.forEach { p ->
                    Text(
                        text = "Amortizado: ${Formatters.formatMoney(p.amount)} via ${p.paymentMethod.displayName} (${Formatters.formatDate(p.date)})",
                        color = NexoraGreenSuccess,
                        fontSize = 10.sp
                    )
                }
            }

            // Actions: Amortize or Send WhatsApp reminder
            if (debt.status != DebtStatus.PAGA) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSendWhatsAppReminder,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NexoraGreenSuccess),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NexoraGreenSuccess.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = NexoraGreenSuccess, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Lembrar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onPay,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Amortizar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordPaymentDialog(
    debt: DebtRecord,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, method: PaymentMethod, notes: String) -> Unit
) {
    var amountStr by remember { mutableStateOf(debt.remainingBalance.toString()) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.DINHEIRO) }
    var notes by remember { mutableStateOf("Pagamento de fiado") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Registar Pagamento de Fiado",
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Cliente: ${debt.clientName}",
                    color = TextLight,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Saldo restante: ${Formatters.formatMoney(debt.remainingBalance)}",
                    color = NexoraAmberWarning,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Valor a Pagar (MT)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

                Spacer(modifier = Modifier.height(10.dp))
                Text("Método de Pagamento Recebido", color = TextMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(PaymentMethod.DINHEIRO, PaymentMethod.MPESA, PaymentMethod.EMOLA, PaymentMethod.MKESH).forEach { method ->
                        val isSelected = method == selectedMethod
                        val color = when (method) {
                            PaymentMethod.DINHEIRO -> CashGreen
                            PaymentMethod.MPESA -> MPesaRed
                            PaymentMethod.EMOLA -> EMolaOrange
                            PaymentMethod.MKESH -> MKeshYellow
                            else -> NexoraBluePrimary
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) color.copy(alpha = 0.25f) else NavyCardElevated,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, if (isSelected) color else NavyCardBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedMethod = method }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = method.displayName,
                                color = if (isSelected) Color.White else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()?.coerceAtLeast(1.0) ?: debt.remainingBalance
                    onConfirm(amount, selectedMethod, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NexoraGreenSuccess)
            ) {
                Text("Confirmar Pagamento", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextLight)
            }
        },
        containerColor = NavySurface,
        shape = RoundedCornerShape(20.dp)
    )
}

private fun sendDebtReminderViaWhatsApp(context: Context, debt: DebtRecord) {
    val message = """
        Olá, *${debt.clientName}*!
        
        Esperamos que esteja bem. Enviamos este lembrete cordial da *NEXORA Comércio Geral* referente à sua compra a crédito (#${debt.receiptNumber}).
        
        • *Saldo Pendente:* ${Formatters.formatMoney(debt.remainingBalance)}
        • *Data de Vencimento:* ${Formatters.formatDate(debt.dueDate)}
        
        Pode efetuar o pagamento presencialmente ou por *M-Pesa* / *e-Mola* para o nosso contacto comercial: +258 84 700 8899.
        
        Agradecemos a sua confiança e preferência!
    """.trimIndent()

    try {
        val phone = debt.clientPhone.replace("[^0-9+]".toRegex(), "")
        val cleanPhone = if (phone.startsWith("+")) phone.substring(1) else phone
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Lembrar Fiado no WhatsApp"))
    }
}
