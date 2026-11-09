package com.example.ui.screens.stock

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SwapVert
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MovementType
import com.example.data.model.Product
import com.example.data.model.StockMovement
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
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.Formatters

enum class StockTab(val title: String) {
    INVENTARIO("Inventário"),
    SUGESTOES("Reposição"),
    HISTORICO("Movimentações")
}

@Composable
fun StockScreen(
    currentUser: User,
    products: List<Product>,
    movements: List<StockMovement>,
    onAdjustStock: (productId: String, quantity: Int, type: MovementType, reason: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(StockTab.INVENTARIO) }
    var selectedStockFilter by remember { mutableStateOf("Todos") }
    var productToAdjust by remember { mutableStateOf<Product?>(null) }

    val lowStockItems = remember(products) { products.filter { it.isLowStock } }
    val outOfStockItems = remember(products) { products.filter { it.isOutOfStock } }
    val totalInventoryCost = remember(products) { products.sumOf { it.costPrice * it.currentStock } }
    val totalInventoryValue = remember(products) { products.sumOf { it.sellingPrice * it.currentStock } }

    val suggestions = remember(products) {
        products.filter { it.currentStock <= it.minStock * 2 }
            .sortedBy { it.currentStock }
    }

    val filteredProducts = remember(products, selectedStockFilter) {
        when (selectedStockFilter) {
            "Baixo Stock" -> lowStockItems
            "Esgotados" -> outOfStockItems
            "Em Stock" -> products.filter { !it.isLowStock && !it.isOutOfStock }
            else -> products
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

                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Gestão de Stock",
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Controle de inventário, alertas e histórico",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Inventory Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StockMetricCard(
                        title = "Itens em Alerta",
                        value = "${lowStockItems.size + outOfStockItems.size}",
                        subtitle = "${outOfStockItems.size} esgotados",
                        color = if (outOfStockItems.isNotEmpty()) NexoraRedAlert else NexoraAmberWarning,
                        modifier = Modifier.weight(1f)
                    )

                    StockMetricCard(
                        title = "Valor de Venda",
                        value = Formatters.formatCompactMoney(totalInventoryValue),
                        subtitle = "${products.sumOf { it.currentStock }} unidades totais",
                        color = NexoraCyanAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = NavySurfaceVariant,
                    contentColor = NexoraCyanAccent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = NexoraCyanAccent
                        )
                    },
                    modifier = Modifier.background(NavySurfaceVariant, RoundedCornerShape(14.dp))
                ) {
                    StockTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == tab) TextWhite else TextMuted
                                )
                            }
                        )
                    }
                }
            }

            // Content per tab
            when (selectedTab) {
                StockTab.INVENTARIO -> {
                    // Filter Chips
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Todos", "Baixo Stock", "Esgotados", "Em Stock").forEach { filter ->
                                val isSelected = filter == selectedStockFilter
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedStockFilter = filter },
                                    label = { Text(filter, fontSize = 11.sp) },
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
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }

                    // Contextual Tooltip for Stock Screen
                    item {
                        val dismissedTooltips by com.example.data.repository.NexoraRepository.dismissedTooltips.collectAsState()
                        if (com.example.config.OnboardingConfig.ONBOARDING_TIPS && "stock" !in dismissedTooltips) {
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
                                            text = "Dica de Stock: A Nexora gera alertas automáticos de reposição quando um produto atinge a quantidade mínima.",
                                            color = TextWhite,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextButton(
                                        onClick = { com.example.data.repository.NexoraRepository.dismissTooltip("stock") }
                                    ) {
                                        Text("Entendi", color = NexoraCyanAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (products.isEmpty()) {
                        item {
                            com.example.ui.components.EducationalEmptyState(
                                iconEmoji = "🏬",
                                title = "O seu stock está em dia",
                                description = "Adicione produtos no catálogo para gerir quantidades e receber alertas de stock baixo."
                            )
                        }
                    } else {
                        items(filteredProducts, key = { it.id }) { product ->
                            StockProductRow(
                                product = product,
                                onAdjust = { productToAdjust = product }
                            )
                        }
                    }
                }

                StockTab.SUGESTOES -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = NexoraBluePrimary.copy(alpha = 0.15f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NexoraBlueLight.copy(alpha = 0.4f))
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = NexoraCyanAccent)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Sugestões de reposição geradas com base na velocidade de vendas e stock de segurança.",
                                    color = TextWhite,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    if (suggestions.isEmpty()) {
                        item {
                            Text(
                                text = "Nenhuma reposição urgente necessária no momento. Stock saudável!",
                                color = NexoraGreenSuccess,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                        }
                    }

                    items(suggestions) { prod ->
                        val suggestedOrder = (prod.minStock * 3) - prod.currentStock
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
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Text(prod.iconEmoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(prod.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Atual: ${prod.currentStock} un • Mínimo: ${prod.minStock} un", color = TextMuted, fontSize = 11.sp)
                                    }
                                }

                                Button(
                                    onClick = { productToAdjust = prod },
                                    colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Repor +$suggestedOrder", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                StockTab.HISTORICO -> {
                    items(movements, key = { it.id }) { mov ->
                        StockMovementCard(movement = mov)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal: Stock Adjustment (Entrada, Saída, Quebra, etc.)
    productToAdjust?.let { product ->
        StockAdjustmentDialog(
            product = product,
            onDismiss = { productToAdjust = null },
            onConfirm = { qty, type, reason ->
                onAdjustStock(product.id, qty, type, reason)
                productToAdjust = null
            }
        )
    }
}

@Composable
private fun StockMetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun StockProductRow(
    product: Product,
    onAdjust: () -> Unit
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
                        .size(38.dp)
                        .background(NavyCardElevated, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = product.iconEmoji, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = product.name,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Mínimo definido: ${product.minStock} un",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val (color, text) = when {
                    product.isOutOfStock -> Pair(NexoraRedAlert, "0 un")
                    product.isLowStock -> Pair(NexoraAmberWarning, "${product.currentStock} un")
                    else -> Pair(NexoraGreenSuccess, "${product.currentStock} un")
                }

                Box(
                    modifier = Modifier
                        .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = text, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onAdjust,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Ajustar",
                        tint = NexoraCyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StockMovementCard(movement: StockMovement) {
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
                        .size(36.dp)
                        .background(
                            if (movement.type.isAddition) NexoraGreenSuccess.copy(alpha = 0.2f) else NexoraRedAlert.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (movement.type.isAddition) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (movement.type.isAddition) NexoraGreenSuccess else NexoraRedAlert,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = movement.productName,
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${movement.type.label} • ${movement.reason}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${Formatters.formatDateTime(movement.timestamp)} • Operador: ${movement.operatorName}",
                        color = TextMuted.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (movement.type.isAddition) "+" else "-"}${movement.quantity}",
                    color = if (movement.type.isAddition) NexoraGreenSuccess else NexoraRedAlert,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Saldo: ${movement.newStock}",
                    color = TextLight,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun StockAdjustmentDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int, type: MovementType, reason: String) -> Unit
) {
    var quantityStr by remember { mutableStateOf("10") }
    var selectedType by remember { mutableStateOf(MovementType.ENTRADA) }
    var reason by remember { mutableStateOf("Entrada regular de mercadoria") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ajustar Stock: ${product.name}",
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Stock atual no sistema: ${product.currentStock} unidades.",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Movement Type Selector
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(MovementType.ENTRADA, MovementType.SAIDA, MovementType.AJUSTE).forEach { type ->
                        val isSelected = type == selectedType
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) NexoraBluePrimary else NavyCardElevated,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedType = type
                                    reason = when (type) {
                                        MovementType.ENTRADA -> "Entrada regular de mercadoria"
                                        MovementType.SAIDA -> "Saída manual do balcão"
                                        MovementType.AJUSTE -> "Avaria / Quebra de stock"
                                        else -> ""
                                    }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.label.split(" ").first(),
                                color = if (isSelected) Color.White else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text("Quantidade a movimentar") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo / Observação") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityStr.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    onConfirm(qty, selectedType, reason)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary)
            ) {
                Text("Confirmar Ajuste", fontWeight = FontWeight.Bold)
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
