package com.example.ui.screens.dashboard

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.User
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
import com.example.ui.theme.TextFaint
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.Formatters

enum class TimePeriod(val label: String) {
    HOJE("Hoje"),
    SEMANA("Esta Semana"),
    MES("Este Mês")
}

@Composable
fun DashboardScreen(
    currentUser: User,
    sales: List<Sale>,
    products: List<Product>,
    expenses: List<com.example.data.model.Expense> = emptyList(),
    onNavigateToPos: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToStock: () -> Unit,
    onNavigateToDebts: () -> Unit,
    onNavigateToReports: () -> Unit = {},
    onNavigateToAi: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf(TimePeriod.HOJE) }
    val latestAiSummary by com.example.data.repository.NexoraRepository.latestAiSummary.collectAsState()
    val hasViewedProfit by com.example.data.repository.NexoraRepository.hasViewedProfit.collectAsState()

    // Period calculations
    val now = System.currentTimeMillis()
    val periodCutoff = when (selectedPeriod) {
        TimePeriod.HOJE -> now - 86400_000L
        TimePeriod.SEMANA -> now - (86400_000L * 7)
        TimePeriod.MES -> now - (86400_000L * 30)
    }
    val periodLength = when (selectedPeriod) {
        TimePeriod.HOJE -> 86400_000L
        TimePeriod.SEMANA -> 86400_000L * 7
        TimePeriod.MES -> 86400_000L * 30
    }
    val previousCutoff = periodCutoff - periodLength

    val periodFilteredSales = remember(sales, selectedPeriod) {
        sales.filter { it.timestamp >= periodCutoff }
    }

    val previousFilteredSales = remember(sales, selectedPeriod) {
        sales.filter { it.timestamp in previousCutoff until periodCutoff }
    }

    val periodFilteredExpenses = remember(expenses, selectedPeriod) {
        expenses.filter { it.date >= periodCutoff }
    }

    val totalSalesAmount = periodFilteredSales.sumOf { it.total }
    val previousSalesAmount = previousFilteredSales.sumOf { it.total }
    val totalSalesCount = periodFilteredSales.size
    val totalCostAmount = periodFilteredSales.sumOf { it.totalCost }
    val totalProfitAmount = (totalSalesAmount - totalCostAmount).coerceAtLeast(0.0)
    val estimatedExpenses = periodFilteredExpenses.sumOf { it.amount }
    val netProfit = (totalProfitAmount - estimatedExpenses).coerceAtLeast(0.0)

    val salesPercentageChange = remember(totalSalesAmount, previousSalesAmount) {
        if (previousSalesAmount > 0.0) {
            ((totalSalesAmount - previousSalesAmount) / previousSalesAmount) * 100.0
        } else if (totalSalesAmount > 0.0) {
            100.0
        } else {
            null
        }
    }

    val lowStockProducts = remember(products) {
        products.filter { it.isLowStock || it.isOutOfStock }
    }

    // Top selling products calculated from sales
    val topSellingItems = remember(sales) {
        val counts = mutableMapOf<String, Pair<Product, Int>>()
        sales.flatMap { it.items }.forEach { item ->
            val existing = counts[item.product.id]
            val qty = (existing?.second ?: 0) + item.quantity
            counts[item.product.id] = Pair(item.product, qty)
        }
        counts.values.sortedByDescending { it.second }.take(5)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // Greeting & Period Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Resumo Comercial",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mercado Nacional • Moeda: Meticais (MT)",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                // Quick POS Floating pill
                Button(
                    onClick = onNavigateToPos,
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("dashboard_quick_pos_btn")
                ) {
                    Icon(imageVector = Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Vender", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Onboarding "Primeiros Passos" Card for Owner
        if (com.example.config.OnboardingConfig.ONBOARDING_CHECKLIST && currentUser.role == com.example.data.model.UserRole.DONO) {
            item {
                com.example.ui.components.FirstStepsCard(
                    hasProduct = products.isNotEmpty(),
                    hasSale = sales.isNotEmpty(),
                    hasViewedProfit = hasViewedProfit,
                    onAddProductClick = onNavigateToProducts,
                    onMakeSaleClick = onNavigateToPos,
                    onViewProfitClick = {
                        com.example.data.repository.NexoraRepository.markProfitViewed()
                        onNavigateToReports()
                    }
                )
            }
        }

        // Period filter chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimePeriod.values().forEach { period ->
                    val isSelected = period == selectedPeriod
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPeriod = period },
                        label = { Text(period.label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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

        // Smart AI Business Summary Card (Module 11)
        val aiSummary = latestAiSummary
        if (currentUser.canViewProfits) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAi() }
                        .testTag("dashboard_smart_summary_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.NexoraPurple.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✨", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Resumo Inteligente Nexora",
                                    color = com.example.ui.theme.NexoraCyanAccent,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(com.example.ui.theme.NexoraPurple.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("Gemini AI", color = com.example.ui.theme.NexoraPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (aiSummary != null) aiSummary.summaryText else "Ainda não há dados suficientes. Registe as suas vendas e produtos e a IA começa a analisar o seu negócio.",
                            color = if (aiSummary != null) TextLight else TextMuted,
                            fontSize = 13.sp,
                            maxLines = 3,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )

                        if (aiSummary?.restockRecommendations?.isNotEmpty() == true) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📦 ", fontSize = 12.sp)
                                Text(
                                    text = aiSummary.restockRecommendations.first(),
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Abrir Assistente Completo →",
                                color = com.example.ui.theme.NexoraCyanAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Low stock warning banner if any
        if (lowStockProducts.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToStock() }
                        .testTag("dashboard_low_stock_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NexoraAmberWarning.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexoraAmberWarning.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(NexoraAmberWarning.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Alerta",
                                    tint = NexoraAmberWarning,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${lowStockProducts.size} Artigos com Stock Crítico!",
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Toque para ver e repor mercadoria",
                                    color = NexoraAmberWarning,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Ver",
                            tint = NexoraAmberWarning,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Main Stat Highlight: Total Vendas
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stat_card_total_vendas"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "TOTAL DE VENDAS",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Formatters.formatMoney(totalSalesAmount),
                                color = TextWhite,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        if (salesPercentageChange != null) {
                            val isPos = salesPercentageChange >= 0.0
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isPos) NexoraGreenSuccess.copy(alpha = 0.15f) else NexoraRedAlert.copy(alpha = 0.15f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isPos) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                        contentDescription = null,
                                        tint = if (isPos) NexoraGreenSuccess else NexoraRedAlert,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${if (isPos) "+" else ""}${String.format(java.util.Locale.US, "%.1f", salesPercentageChange)}%",
                                        color = if (isPos) NexoraGreenSuccess else NexoraRedAlert,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatSubItem(
                            label = "Transacções",
                            value = "$totalSalesCount vendas",
                            color = NexoraCyanAccent
                        )
                        StatSubItem(
                            label = "Ticket Médio",
                            value = if (totalSalesCount > 0) Formatters.formatMoney(totalSalesAmount / totalSalesCount) else "0.00 MT",
                            color = TextLight
                        )
                        StatSubItem(
                            label = "Despesas Est.",
                            value = Formatters.formatMoney(estimatedExpenses),
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // Lucro Bruto e Líquido (Gated by User Role: Only Dono can see!)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stat_card_lucro"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(NexoraCyanAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachMoney,
                                    contentDescription = null,
                                    tint = NexoraCyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "LUCRO ESTIMADO",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }

                        if (!currentUser.canViewProfits) {
                            Box(
                                modifier = Modifier
                                    .background(NavyCardElevated, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Apenas Dono", color = TextMuted, fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (currentUser.canViewProfits) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = Formatters.formatMoney(netProfit),
                                    color = NexoraGreenSuccess,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Lucro Líquido pós-despesas estimadas",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = Formatters.formatMoney(totalProfitAmount),
                                    color = TextLight,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Margem Bruta",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    } else {
                        // Caixa view: Confidential metrics hidden
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NavyBackground, RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Métricas de lucro e custos são confidenciais do proprietário.",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Sales Trend Canvas Chart
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
                        text = "EVOLUÇÃO DAS VENDAS (MT)",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (sales.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ainda sem vendas. Faça a primeira venda para ver o gráfico.",
                                color = TextMuted,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        SalesTrendChart(
                            sales = sales,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Hoje").forEach { day ->
                                Text(text = day, color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // Top Selling Products Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mais Vendidos",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ver Todos",
                    color = NexoraCyanAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToProducts() }
                )
            }
        }

        if (topSellingItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCardElevated)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Ainda sem vendas registadas", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Registe a sua primeira venda no POS para acompanhar os produtos com maior saída.", color = TextMuted, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        } else {
            items(topSellingItems) { (product, quantitySold) ->
                ProductSalesRankCard(
                    product = product,
                    quantitySold = quantitySold,
                    canViewProfits = currentUser.canViewProfits
                )
            }
        }

        // Quick Action Grid at the bottom
        item {
            Text(
                text = "Operações Rápidas",
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = "Novo Artigo",
                    subtitle = "Cadastrar produto",
                    icon = Icons.Default.Add,
                    color = NexoraBlueLight,
                    onClick = onNavigateToProducts,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "Fiado & Cobrança",
                    subtitle = "Gerir dívidas",
                    icon = Icons.Default.AttachMoney,
                    color = NexoraCyanAccent,
                    onClick = onNavigateToDebts,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatSubItem(label: String, value: String, color: Color) {
    Column {
        Text(text = label, color = TextMuted, fontSize = 10.sp)
        Text(text = value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SalesTrendChart(sales: List<Sale>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Group sales by day (last 7 days)
        val now = System.currentTimeMillis()
        val oneDayMillis = 86400_000L
        val dailyTotals = mutableListOf<Double>()
        
        for (i in 6 downTo 0) {
            val dayStart = now - (i * oneDayMillis)
            val dayEnd = dayStart + oneDayMillis
            val daySum = sales.filter { it.timestamp in dayStart until dayEnd }.sumOf { it.total }
            dailyTotals.add(daySum)
        }

        val maxVal = dailyTotals.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
        val stepX = if (dailyTotals.size > 1) width / (dailyTotals.size - 1) else width

        val points = dailyTotals.mapIndexed { index, total ->
            val x = index * stepX
            val ratio = (total / maxVal).toFloat().coerceIn(0.05f, 0.95f)
            val y = height - (ratio * (height - 16f)) - 8f
            Offset(x, y)
        }

        val path = Path()
        points.forEachIndexed { i, p ->
            if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
        }

        // Fill area under path
        val fillPath = Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                listOf(NexoraBluePrimary.copy(alpha = 0.35f), Color.Transparent)
            )
        )

        // Draw line
        drawPath(
            path = path,
            color = NexoraBlueLight,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw circles on points
        points.forEach { p ->
            drawCircle(color = NavyBackground, radius = 5.dp.toPx(), center = p)
            drawCircle(color = NexoraCyanAccent, radius = 3.5.dp.toPx(), center = p)
        }
    }
}

@Composable
private fun ProductSalesRankCard(
    product: Product,
    quantitySold: Int,
    canViewProfits: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(NavyCardElevated, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = product.iconEmoji, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = product.name,
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                    Text(
                        text = "${Formatters.formatMoney(product.sellingPrice)} un • $quantitySold un vendidas",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Formatters.formatMoney(product.sellingPrice * quantitySold),
                    color = NexoraBlueLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                if (canViewProfits) {
                    Text(
                        text = "Lucro: +${Formatters.formatMoney(product.profitPerUnit * quantitySold)}",
                        color = NexoraGreenSuccess,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("quick_action_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = subtitle, color = TextMuted, fontSize = 11.sp)
        }
    }
}
