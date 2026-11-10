package com.example.ui.screens.subscription

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessSubscription
import com.example.data.model.PaymentMethod
import com.example.data.model.PlanTier
import com.example.data.model.PlatformPaymentAccount
import com.example.data.model.SubscriptionStatus
import com.example.data.repository.NexoraRepository
import com.example.ui.theme.EMolaOrange
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
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.Formatters

@Composable
fun SubscriptionScreen(
    subscription: BusinessSubscription,
    onSubmitProof: (reference: String, method: PaymentMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by NexoraRepository.currentUser.collectAsState()
    val accounts by NexoraRepository.platformPaymentAccounts.collectAsState()
    val whatsappSupportNumber by NexoraRepository.whatsappSupportNumber.collectAsState()

    var showProofModal by remember { mutableStateOf(false) }

    val openWhatsAppSupport = {
        val cleanPhone = whatsappSupportNumber.replace("[^0-9]".toRegex(), "")
        val storeName = currentUser.storeName.ifBlank { "Minha Empresa" }
        val message = Uri.encode("Olá, preciso de ajuda com a Nexora na empresa $storeName.")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanPhone?text=$message"))
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Não foi possível abrir o WhatsApp: ${whatsappSupportNumber}", Toast.LENGTH_LONG).show()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(NavyBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Column {
                    Text("Planos & Subscrições", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Gestão de licenças e renovação comercial NEXORA", color = TextMuted, fontSize = 12.sp)
                }
            }

            // Current Plan Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NexoraCyanAccent)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("PLANO ATIVO", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(subscription.currentPlan.title, color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                            }

                            Box(
                                modifier = Modifier
                                    .background(NexoraGreenSuccess.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = subscription.status.label,
                                    color = if (subscription.status == SubscriptionStatus.PENDENTE_APROVACAO) NexoraAmberWarning else NexoraGreenSuccess,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
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
                                Text("Validade Restante", color = TextMuted, fontSize = 11.sp)
                                Text("${subscription.daysRemaining} dias", color = NexoraCyanAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Button(
                                onClick = { showProofModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("pay_renewal_button")
                            ) {
                                Text("Pagar Renovação", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (subscription.status == SubscriptionStatus.PENDENTE_APROVACAO) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NexoraAmberWarning.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NexoraAmberWarning)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.HourglassTop, contentDescription = null, tint = NexoraAmberWarning, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Comprovativo '${subscription.paymentProofReference}' em análise manual.",
                                            color = TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "A equipa NEXORA irá validar a transação SMS em breve. Se precisar de agilizar a aprovação, fale connosco pelo WhatsApp.",
                                        color = TextLight,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = openWhatsAppSupport,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(36.dp)
                                    ) {
                                        Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Falar com o suporte no WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // WHATSAPP SUPPORT BUTTON
            item {
                Button(
                    onClick = openWhatsAppSupport,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("whatsapp_support_subscription_button")
                ) {
                    Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Falar com o suporte no WhatsApp", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Text("Planos Comerciais Disponíveis", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Plan Cards
            PlanTier.values().forEach { tier ->
                item {
                    PlanComparisonCard(tier = tier, isCurrent = tier == subscription.currentPlan, onSelect = { showProofModal = true })
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal: Manual Payment Proof (Shown ONLY at the moment of payment)
    if (showProofModal) {
        ManualPaymentProofDialog(
            subscription = subscription,
            accounts = accounts,
            onDismiss = { showProofModal = false },
            onSubmit = { ref, method ->
                onSubmitProof(ref, method)
                showProofModal = false
            },
            onOpenWhatsApp = openWhatsAppSupport
        )
    }
}

@Composable
private fun PlanComparisonCard(
    tier: PlanTier,
    isCurrent: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = NavySurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            if (isCurrent) 1.5.dp else 1.dp,
            if (isCurrent) NexoraCyanAccent else NavyCardBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tier.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = NexoraCyanAccent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Atual",
                                color = NexoraCyanAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = if (tier.monthlyPriceMT == 0.0) "Grátis" else "${Formatters.formatMoney(tier.monthlyPriceMT)}/mês",
                    color = NexoraCyanAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            PlanFeatureLine(text = "Até ${tier.maxProducts} produtos cadastrados")
            PlanFeatureLine(text = "Até ${tier.maxUsers} operadores de caixa")
            PlanFeatureLine(text = if (tier.hasAdvancedReports) "Relatórios financeiros completos e DRE" else "Relatórios básicos")
            PlanFeatureLine(text = if (tier.hasWhatsAppAutomation) "Envio de recibos e lembretes por WhatsApp" else "Sem envio por WhatsApp")

            if (!isCurrent) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onSelect,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary)
                ) {
                    Text("Escolher este Plano", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PlanFeatureLine(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(Icons.Default.Check, contentDescription = null, tint = NexoraGreenSuccess, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = TextLight, fontSize = 12.sp)
    }
}

@Composable
private fun ManualPaymentProofDialog(
    subscription: BusinessSubscription,
    accounts: List<PlatformPaymentAccount>,
    onDismiss: () -> Unit,
    onSubmit: (reference: String, method: PaymentMethod) -> Unit,
    onOpenWhatsApp: () -> Unit
) {
    val context = LocalContext.current
    var reference by remember { mutableStateOf("") }
    
    // Unique payment reference for this request
    val uniqueRef = remember { "NX-${(1000..9999).random()}" }

    val activeAccounts = accounts.filter { it.method == PaymentMethod.MPESA || it.method == PaymentMethod.MKESH }
    var selectedMethod by remember { mutableStateOf(activeAccounts.firstOrNull()?.method ?: PaymentMethod.MPESA) }

    val currentSelectedAccount = activeAccounts.find { it.method == selectedMethod }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pagamento da Subscrição", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // a) Resumo: plano escolhido, valor em MT e duração (30 dias)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Plano Seleccionado:", color = TextMuted, fontSize = 12.sp)
                            Text(subscription.currentPlan.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Valor:", color = TextMuted, fontSize = 12.sp)
                            Text(Formatters.formatMoney(subscription.currentPlan.monthlyPriceMT), color = NexoraCyanAccent, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Duração:", color = TextMuted, fontSize = 12.sp)
                            Text("30 dias", color = TextLight, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = NavyCardBorder)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Referência Única:", color = TextMuted, fontSize = 12.sp)
                            Text(uniqueRef, color = NexoraGreenSuccess, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }
                    }
                }

                if (activeAccounts.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = NexoraAmberWarning.copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NexoraAmberWarning)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Pagamento indisponível de momento. Fale com o suporte.",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onOpenWhatsApp,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Falar com o suporte no WhatsApp", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Text("Escolha o método de pagamento:", color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    // b) Escolha do método de pagamento: M-Pesa ou mKesh
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        activeAccounts.forEach { acc ->
                            val isSelected = acc.method == selectedMethod
                            val color = when (acc.method) {
                                PaymentMethod.MPESA -> MPesaRed
                                PaymentMethod.MKESH -> MKeshYellow
                                else -> NexoraBluePrimary
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSelected) color.copy(alpha = 0.25f) else NavyCardElevated, RoundedCornerShape(10.dp))
                                    .border(1.dp, if (isSelected) color else NavyCardBorder, RoundedCornerShape(10.dp))
                                    .clickable { selectedMethod = acc.method }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(acc.method.displayName, color = if (isSelected) color else TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // c) Depois de escolher o método, mostrar APENAS o número e o titular desse método
                    if (currentSelectedAccount != null) {
                        val color = when (selectedMethod) {
                            PaymentMethod.MPESA -> MPesaRed
                            PaymentMethod.MKESH -> MKeshYellow
                            else -> NexoraBluePrimary
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Conta ${currentSelectedAccount.method.displayName}:", color = TextMuted, fontSize = 10.sp)
                                        Text(currentSelectedAccount.accountNumber, color = TextWhite, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                        Text("Titular: ${currentSelectedAccount.accountHolder}", color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Número ${currentSelectedAccount.method.displayName}", currentSelectedAccount.accountNumber)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Número ${currentSelectedAccount.accountNumber} copiado!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, color),
                                        modifier = Modifier.height(34.dp).testTag("copy_account_btn")
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copiar número", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        // Warning
                        Text(
                            text = "Confirme no seu telemóvel que o nome do titular coincide com o mostrado aqui antes de transferir.",
                            color = NexoraAmberWarning,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reference,
                        onValueChange = { reference = it },
                        label = { Text("Código de Transacção SMS") },
                        placeholder = { Text("ex: MP-994012 ou TXN-8821") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Escreva a referência [$uniqueRef] na descrição da transferência se possível.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = onOpenWhatsApp,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Falar com o suporte no WhatsApp", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            if (activeAccounts.isNotEmpty()) {
                Button(
                    onClick = {
                        if (reference.isNotBlank()) {
                            onSubmit("$uniqueRef - ${reference.trim()}", selectedMethod)
                        } else {
                            Toast.makeText(context, "Insira o código da transacção SMS", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraGreenSuccess),
                    modifier = Modifier.testTag("already_paid_button")
                ) {
                    Text("Já paguei", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = TextLight) } },
        containerColor = NavySurface,
        shape = RoundedCornerShape(20.dp)
    )
}
