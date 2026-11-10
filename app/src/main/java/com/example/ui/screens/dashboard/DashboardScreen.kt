package com.example.ui.screens.dashboard

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.User
import com.example.ui.theme.HairlineDivider
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

    val topSellingItems = remember(sales) {
        val counts = mutableMapOf<String, Pair<Product, Int>>()
        sales.flatMap { it.items }.forEach { item ->
            val existing = counts[item.product.id]
            val qty = (existing?.second ?: 0) + item.quantity
            counts[item.product.id] = Pair(item.product, qty)
        }
        counts.values.sortedByDescending { it.second }.take(4)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // Executive Header with Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Painel Comercial",
                        color = TextWhite,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "Valores expressos em Meticais (MT)",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                // POS Quick Action Pill
                Button(
                    onClick = onNavigateToPos,
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("dashboard_quick_pos_btn")
                ) {
                    Icon(imageVector = Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Balcão POS", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        // Onboarding Card (if applicable)
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

        // Segmented Period Filter
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavySurface, RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TimePeriod.values().forEach { period ->
                    val isSelected = period == selectedPeriod
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) NexoraBluePrimary else Color.Transparent)
                            .clickable { selectedPeriod = period }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = period.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else TextMuted
                        )
                    }
                }
            }
        }

        // Low stock warning strip (compact and purposeful)
        if (lowStockProducts.isNotEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigateToStock() }
                        .testTag("dashboard_low_stock_banner"),
                    color = NexoraAmberWarning.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexoraAmberWarning.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(NexoraAmberWarning.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = NexoraAmberWarning,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${lowStockProducts.size} artigos com stock no limite",
                                    color = TextWhite,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Toque para ver a lista de reposição",
                                    color = NexoraAmberWarning,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Ver",
                            tint = NexoraAmberWarning,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Unified Executive Financial Cockpit
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stat_card_total_vendas"),
                shape = RoundedCornerShape(14.dp),
                color = NavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, HairlineDivider)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Primary Figure: Total Revenue
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "FATURAMENTO TOTAL",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = Formatters.formatMoney(totalSalesAmount),
                                color = TextWhite,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                        }

                        if (salesPercentageChange != null) {
                            val isPos = salesPercentageChange >= 0.0
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isPos) NexoraGreenSuccess.copy(alpha = 0.12f) else NexoraRedAlert.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isPos) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                        contentDescription = null,
                                        tint = if (isPos) NexoraGreenSuccess else NexoraRedAlert,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
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

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = HairlineDivider, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Secondary Metric Indicators Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricCell(
                            label = "Transacções",
                            value = "$totalSalesCount vendas",
                            highlight = false
                        )
                        MetricCell(
                            label = "Ticket Médio",
                            value = if (totalSalesCount > 0) Formatters.formatMoney(totalSalesAmount / totalSalesCount) else "0.00 MT",
                            highlight = false
                        )
                        if (currentUser.canViewProfits) {
                            MetricCell(
                                label = "Lucro Líquido",
                                value = Formatters.formatMoney(netProfit),
                                highlight = true,
                                highlightColor = NexoraGreenSuccess
                            )
                        } else {
                            MetricCell(
                                label = "Lucro",
                                value = "Confidencial",
                                highlight = false,
                                isProtected = true
                            )
                        }
                    }
                }
            }
        }

        // Sales Evolution Line Chart
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = NavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, HairlineDivider)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EVOLUÇÃO RECENTE DE VENDAS",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "7 Dias",
                            color = TextFaint,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (sales.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Registe as suas primeiras vendas para acompanhar o histórico gráfico.",
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
                                .height(120.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Hoje").forEach { day ->
                                Text(text = day, color = TextFaint, fontSize = 10.sp)
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
                    text = "Artigos Mais Vendidos",
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Catálogo →",
                    color = NexoraCyanAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToProducts() }
                )
            }
        }

        if (topSellingItems.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = NavySurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, HairlineDivider)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ainda sem vendas no período seleccionado.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(topSellingItems) { (product, quantitySold) ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = NavySurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, HairlineDivider)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(NavySurfaceVariant, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = product.iconEmoji, fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = product.name,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$quantitySold ${product.unitType.shortLabel} vendidas",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = Formatters.formatMoney(product.sellingPrice * quantitySold),
                                color = TextWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            if (currentUser.canViewProfits) {
                                Text(
                                    text = "Lucro: +${Formatters.formatMoney(product.profitPerUnit * quantitySold)}",
                                    color = NexoraGreenSuccess,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Fast Commercial Actions Grid
        item {
            Text(
                text = "Operações Rápidas",
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionStripButton(
                    title = "Cadastrar Produto",
                    subtitle = "Novo artigo no catálogo",
                    icon = Icons.Default.Add,
                    onClick = onNavigateToProducts,
                    modifier = Modifier.weight(1f)
                )
                QuickActionStripButton(
                    title = "Controle de Fiados",
                    subtitle = "Cobrança de clientes",
                    icon = Icons.Default.AttachMoney,
                    onClick = onNavigateToDebts,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun MetricCell(
    label: String,
    value: String,
    highlight: Boolean,
    highlightColor: Color = NexoraBlueLight,
    isProtected: Boolean = false
) {
    Column {
        Text(text = label, color = TextMuted, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isProtected) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = TextFaint,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = value,
                color = if (highlight) highlightColor else (if (isProtected) TextFaint else TextWhite),
                fontSize = 13.sp,
                fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SalesTrendChart(sales: List<Sale>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

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
            val ratio = (total / maxVal).toFloat().coerceIn(0.08f, 0.92f)
            val y = height - (ratio * (height - 20f)) - 10f
            Offset(x, y)
        }

        val path = Path()
        points.forEachIndexed { i, p ->
            if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
        }

        val fillPath = Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                listOf(NexoraBluePrimary.copy(alpha = 0.25f), Color.Transparent)
            )
        )

        drawPath(
            path = path,
            color = NexoraBlueLight,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Clean point indicator for the latest day
        points.lastOrNull()?.let { lastPoint ->
            drawCircle(color = NavySurface, radius = 5.dp.toPx(), center = lastPoint)
            drawCircle(color = NexoraBlueLight, radius = 3.dp.toPx(), center = lastPoint)
        }
    }
}

@Composable
private fun QuickActionStripButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("quick_action_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(12.dp),
        color = NavySurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, HairlineDivider)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(NexoraBluePrimary.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NexoraBlueLight,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = TextWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}
