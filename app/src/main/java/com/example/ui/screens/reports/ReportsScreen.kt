package com.example.ui.screens.reports

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Client
import com.example.data.model.Expense
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.Supplier
import com.example.data.model.User
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

enum class ReportPeriod(val label: String) {
    HOJE("Hoje"),
    SEMANAL("Esta Semana"),
    MENSAL("Este Mês")
}

@Composable
fun ReportsScreen(
    currentUser: User,
    sales: List<Sale>,
    expenses: List<Expense>,
    clients: List<Client>,
    suppliers: List<Supplier>,
    products: List<Product>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.MENSAL) }

    // Owner confidentiality protection
    if (!currentUser.canViewProfits) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(NavyBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = NexoraRedAlert, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Relatórios Restritos ao Dono", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Apenas o proprietário tem acesso aos balanços e relatórios financeiros da empresa.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        return
    }

    val now = System.currentTimeMillis()
    val cutoff = remember(selectedPeriod) {
        when (selectedPeriod) {
            ReportPeriod.HOJE -> now - 86400_000L
            ReportPeriod.SEMANAL -> now - (86400_000L * 7)
            ReportPeriod.MENSAL -> now - (86400_000L * 30)
        }
    }

    val filteredSales = remember(sales, cutoff) { sales.filter { it.timestamp >= cutoff } }
    val filteredExpenses = remember(expenses, cutoff) { expenses.filter { it.date >= cutoff } }

    val totalSales = filteredSales.sumOf { it.total }
    val totalCost = filteredSales.sumOf { it.totalCost }
    val grossProfit = (totalSales - totalCost).coerceAtLeast(0.0)
    val totalExpenses = filteredExpenses.sumOf { it.amount }
    val netResult = grossProfit - totalExpenses

    val totalClientFiado = remember(clients) { clients.sumOf { it.totalDebt } }
    val totalSupplierDebt = remember(suppliers) { suppliers.sumOf { it.totalDebt } }

    val salesByMethod = remember(filteredSales) {
        PaymentMethod.values().associateWith { method ->
            filteredSales.filter { it.paymentMethod == method }.sumOf { it.total }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(NavyBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Relatórios Financeiros", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Demonstração de resultados e balanço", color = TextMuted, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            shareFinancialReportWhatsApp(
                                context = context,
                                storeName = currentUser.storeName,
                                period = selectedPeriod.label,
                                sales = totalSales,
                                cost = totalCost,
                                gross = grossProfit,
                                expenses = totalExpenses,
                                net = netResult,
                                fiado = totalClientFiado,
                                supplierDebt = totalSupplierDebt
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NexoraGreenSuccess),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Partilhar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Period Selector
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportPeriod.values().forEach { period ->
                        val isSelected = period == selectedPeriod
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPeriod = period },
                            label = { Text(period.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NexoraBluePrimary,
                                selectedLabelColor = Color.White,
                                containerColor = NavyCardElevated,
                                labelColor = TextMuted
                            )
                        )
                    }
                }
            }

            // Net Result Hero Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = NavySurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "RESULTADO LÍQUIDO DO PERÍODO",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = Formatters.formatMoney(netResult),
                            color = if (netResult >= 0) NexoraGreenSuccess else NexoraRedAlert,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Lucro comercial bruto deduzido das despesas operacionais.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // DRE Breakdown Table Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = NavySurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Demonstração de Resultados (DRE)", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = NavyCardBorder)
                        Spacer(modifier = Modifier.height(10.dp))

                        ReportLine(label = "(+) Faturação / Vendas Totais", value = Formatters.formatMoney(totalSales), color = TextWhite)
                        ReportLine(label = "(-) Custo das Mercadorias (CMV)", value = "- ${Formatters.formatMoney(totalCost)}", color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        ReportLine(label = "(=) Lucro Bruto das Vendas", value = Formatters.formatMoney(grossProfit), color = NexoraCyanAccent, isBold = true)

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = NavyCardBorder)
                        Spacer(modifier = Modifier.height(10.dp))

                        ReportLine(label = "(-) Despesas Operacionais", value = "- ${Formatters.formatMoney(totalExpenses)}", color = NexoraRedAlert)
                        Spacer(modifier = Modifier.height(4.dp))
                        ReportLine(label = "(=) Lucro Líquido Real", value = Formatters.formatMoney(netResult), color = if (netResult >= 0) NexoraGreenSuccess else NexoraRedAlert, isBold = true)
                    }
                }
            }

            // Receivables & Payables Balance
            item {
                Text("Balanço de Créditos e Dívidas", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("A Receber (Fiado)", color = TextMuted, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(Formatters.formatMoney(totalClientFiado), color = NexoraAmberWarning, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("A Pagar (Fornecedores)", color = TextMuted, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(Formatters.formatMoney(totalSupplierDebt), color = NexoraRedAlert, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            // Payment Methods Breakdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Vendas por Método de Pagamento", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        salesByMethod.forEach { (method, amount) ->
                            val color = when (method) {
                                PaymentMethod.DINHEIRO -> CashGreen
                                PaymentMethod.MPESA -> MPesaRed
                                PaymentMethod.EMOLA -> EMolaOrange
                                PaymentMethod.MKESH -> MKeshYellow
                                PaymentMethod.FIADO -> FiadoBlue
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(method.displayName, color = TextLight, fontSize = 12.sp)
                                }
                                Text(Formatters.formatMoney(amount), color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ReportLine(label: String, value: String, color: Color, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = if (isBold) TextWhite else TextMuted, fontSize = 12.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(text = value, color = color, fontSize = 13.sp, fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold)
    }
}

private fun shareFinancialReportWhatsApp(
    context: Context,
    storeName: String,
    period: String,
    sales: Double,
    cost: Double,
    gross: Double,
    expenses: Double,
    net: Double,
    fiado: Double,
    supplierDebt: Double
) {
    val message = """
        📊 *RELATÓRIO FINANCEIRO - $storeName*
        *Período:* $period
        ------------------------------------
        *Receita de Vendas:* ${Formatters.formatMoney(sales)}
        *Custo de Mercadorias (CMV):* -${Formatters.formatMoney(cost)}
        *Lucro Comercial Bruto:* ${Formatters.formatMoney(gross)}
        *Despesas Operacionais:* -${Formatters.formatMoney(expenses)}
        ------------------------------------
        *LUCRO LÍQUIDO REAL:* ${Formatters.formatMoney(net)}
        ------------------------------------
        *Balanço Pendente:*
        • Fiado a Receber de Clientes: ${Formatters.formatMoney(fiado)}
        • Contas a Pagar a Fornecedores: ${Formatters.formatMoney(supplierDebt)}
        ------------------------------------
        Gerado pela plataforma NEXORA Moçambique
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(intent, "Partilhar Relatório NEXORA"))
}
