package com.example.ui.screens.pos

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.CartItem
import com.example.data.model.Client
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.User
import com.example.ui.components.ReceiptDialog
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    currentUser: User,
    products: List<Product>,
    clients: List<Client>,
    onCompleteSale: (
        items: List<CartItem>,
        discount: Double,
        paymentMethod: PaymentMethod,
        amountPaid: Double,
        client: Client?,
        paymentRef: String?,
        dueDate: Long?,
        notes: String
    ) -> Sale,
    modifier: Modifier = Modifier
) {
    // POS Cart state
    val cartItems = remember { mutableStateListOf<CartItem>() }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todas") }

    // Checkout sheet & receipt state
    var showCheckoutSheet by remember { mutableStateOf(false) }
    var completedSaleReceipt by remember { mutableStateOf<Sale?>(null) }

    val categories = remember(products) {
        listOf("Todas") + products.map { it.category }.distinct()
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { p ->
            val matchesCategory = selectedCategory == "Todas" || p.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.barcode.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val cartTotal = cartItems.sumOf { it.subtotal }
    val cartCount = cartItems.sumOf { it.quantity }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // POS Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ponto de Venda (PDV)",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Registo rápido de vendas e emissão de recibo",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                // Cart pill toggle
                if (cartCount > 0) {
                    Button(
                        onClick = { showCheckoutSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NexoraGreenSuccess),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("pos_open_cart_button")
                    ) {
                        Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("$cartCount • ${Formatters.formatCompactMoney(cartTotal)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Cashier Tip Banner
            if (com.example.config.OnboardingConfig.ONBOARDING_TIPS && currentUser.role == com.example.data.model.UserRole.CAIXA) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NexoraCyanAccent.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexoraCyanAccent.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "💡", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Dica de Balcão: Selecione os produtos, escolha o método de pagamento (Dinheiro, M-Pesa, e-Mola ou mKesh) e toque em Confirmar Venda.",
                            color = TextWhite,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Pesquisar produto ou código de barras...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Pesquisar", tint = NexoraCyanAccent)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpar", tint = TextMuted)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pos_search_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = NavySurfaceVariant,
                    unfocusedContainerColor = NavySurfaceVariant,
                    focusedBorderColor = NexoraBlueLight,
                    unfocusedBorderColor = NavyCardBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextLight
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(text = "📦", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Ainda não tem produtos cadastrados",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Adicione o primeiro produto na aba Artigos para começar a vender.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                // Products Grid for Quick Touch Selection
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        val inCart = cartItems.find { it.product.id == product.id }
                        val currentInCartQty = inCart?.quantity ?: 0
                        val isAvailable = product.currentStock > currentInCartQty

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isAvailable) {
                                    if (inCart != null) {
                                        val index = cartItems.indexOf(inCart)
                                        cartItems[index] = inCart.copy(quantity = inCart.quantity + 1)
                                    } else {
                                        cartItems.add(CartItem(product, 1))
                                    }
                                }
                                .testTag("pos_product_${product.id}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (currentInCartQty > 0) NavyCardElevated else NavySurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                if (currentInCartQty > 0) 1.5.dp else 1.dp,
                                if (currentInCartQty > 0) NexoraBlueLight else NavyCardBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(NavySurface, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = product.iconEmoji, fontSize = 20.sp)
                                    }

                                    if (currentInCartQty > 0) {
                                        Surface(
                                            color = NexoraBluePrimary,
                                            shape = CircleShape
                                        ) {
                                            Text(
                                                text = "${currentInCartQty}x",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = product.name,
                                    color = TextWhite,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    maxLines = 2,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = Formatters.formatMoney(product.sellingPrice),
                                    color = NexoraCyanAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                val remaining = product.currentStock - currentInCartQty
                                val stockColor = if (remaining <= 0) NexoraRedAlert else if (remaining <= product.minStock) NexoraAmberWarning else TextMuted
                                Text(
                                    text = if (remaining <= 0) "Esgotado" else "$remaining un disp.",
                                    color = stockColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Sticky Checkout Bar
            if (cartCount > 0) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = NavySurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexoraBlueLight.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "$cartCount artigos",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = Formatters.formatMoney(cartTotal),
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Button(
                            onClick = { showCheckoutSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("pos_checkout_button")
                        ) {
                            Text("Cobrar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Cart & Payment Checkout
    if (showCheckoutSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { showCheckoutSheet = false },
            sheetState = sheetState,
            containerColor = NavySurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.testTag("checkout_bottom_sheet")
        ) {
            CheckoutSheetContent(
                cartItems = cartItems,
                clients = clients,
                currentUser = currentUser,
                onDismiss = { showCheckoutSheet = false },
                onCompleteSale = { items, disc, method, amountPaid, client, ref, due, notes ->
                    val sale = onCompleteSale(items, disc, method, amountPaid, client, ref, due, notes)
                    cartItems.clear()
                    showCheckoutSheet = false
                    completedSaleReceipt = sale
                }
            )
        }
    }

    // Receipt Dialog on sale completion
    completedSaleReceipt?.let { sale ->
        ReceiptDialog(
            sale = sale,
            onDismiss = { completedSaleReceipt = null }
        )
    }
}

@Composable
private fun CheckoutSheetContent(
    cartItems: MutableList<CartItem>,
    clients: List<Client>,
    currentUser: User,
    onDismiss: () -> Unit,
    onCompleteSale: (
        items: List<CartItem>,
        discount: Double,
        paymentMethod: PaymentMethod,
        amountPaid: Double,
        client: Client?,
        paymentRef: String?,
        dueDate: Long?,
        notes: String
    ) -> Unit
) {
    var discountStr by remember { mutableStateOf("0") }
    // User requested ONLY 4 payment methods: Dinheiro, M-Pesa, e-Mola e Fiado
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.DINHEIRO) }
    var selectedClient by remember { mutableStateOf<Client?>(null) }
    var clientDropdownOpen by remember { mutableStateOf(false) }

    // Specific payment fields
    var cashPaidStr by remember { mutableStateOf("") }
    var mobileMoneyPhone by remember { mutableStateOf("") }
    var paymentReference by remember { mutableStateOf("") }
    var dueDaysStr by remember { mutableStateOf("7") } // Default 7 days for Fiado
    var notes by remember { mutableStateOf("") }

    val subtotal = cartItems.sumOf { it.subtotal }
    val discount = discountStr.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val totalToPay = (subtotal - discount).coerceAtLeast(0.0)

    val cashPaid = cashPaidStr.toDoubleOrNull() ?: totalToPay
    val change = if (selectedPaymentMethod == PaymentMethod.DINHEIRO) (cashPaid - totalToPay).coerceAtLeast(0.0) else 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Carrinho e Pagamento",
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Cart items list with quantity adjustments
        Text("Artigos Selecionados", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        cartItems.forEachIndexed { index, item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.product.name, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(Formatters.formatMoney(item.unitPrice), color = NexoraCyanAccent, fontSize = 11.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (item.quantity > 1) {
                                    cartItems[index] = item.copy(quantity = item.quantity - 1)
                                } else {
                                    cartItems.removeAt(index)
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (item.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                                contentDescription = "Diminuir",
                                tint = if (item.quantity == 1) NexoraRedAlert else TextLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = "${item.quantity}",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        IconButton(
                            onClick = {
                                if (item.quantity < item.product.currentStock) {
                                    cartItems[index] = item.copy(quantity = item.quantity + 1)
                                }
                            },
                            enabled = item.quantity < item.product.currentStock,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Aumentar",
                                tint = if (item.quantity < item.product.currentStock) NexoraBlueLight else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Client Selection
        Text("Associar Cliente (Obrigatório para Fiado)", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))

        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { clientDropdownOpen = true },
                shape = RoundedCornerShape(12.dp),
                color = NavySurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = NexoraCyanAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedClient?.let { "${it.name} (${it.phone})" } ?: "Cliente Não Identificado (Balcão)",
                            color = if (selectedClient != null) TextWhite else TextMuted,
                            fontSize = 13.sp
                        )
                    }
                    if (selectedClient != null) {
                        IconButton(
                            onClick = { selectedClient = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Limpar", tint = TextMuted, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            DropdownMenu(
                expanded = clientDropdownOpen,
                onDismissRequest = { clientDropdownOpen = false },
                modifier = Modifier.background(NavySurface)
            ) {
                clients.forEach { c ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(c.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${c.phone} • Dívida Atual: ${Formatters.formatMoney(c.totalDebt)}", color = TextMuted, fontSize = 11.sp)
                            }
                        },
                        onClick = {
                            selectedClient = c
                            clientDropdownOpen = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Desconto (Permissão do Dono/Gerente)
        if (currentUser.canGiveDiscounts) {
            OutlinedTextField(
                value = discountStr,
                onValueChange = { discountStr = it },
                label = { Text("Desconto em MT (Opcional)") },
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
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Totals Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal:", color = TextMuted, fontSize = 12.sp)
                    Text(Formatters.formatMoney(subtotal), color = TextLight, fontSize = 13.sp)
                }
                if (discount > 0.0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Desconto:", color = NexoraGreenSuccess, fontSize = 12.sp)
                        Text("- ${Formatters.formatMoney(discount)}", color = NexoraGreenSuccess, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = NavyCardBorder)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TOTAL A PAGAR:", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(Formatters.formatMoney(totalToPay), color = NexoraCyanAccent, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PAYMENT METHODS SELECTION (Dinheiro, M-Pesa, e-Mola, Fiado)
        Text(
            text = "Método de Pagamento Moçambique",
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val paymentMethods = listOf(
                Pair(PaymentMethod.DINHEIRO, CashGreen),
                Pair(PaymentMethod.MPESA, MPesaRed),
                Pair(PaymentMethod.EMOLA, EMolaOrange),
                Pair(PaymentMethod.MKESH, MKeshYellow),
                Pair(PaymentMethod.FIADO, FiadoBlue)
            )

            paymentMethods.forEach { (method, brandColor) ->
                val isSelected = method == selectedPaymentMethod
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedPaymentMethod = method }
                        .testTag("pay_method_${method.code.lowercase()}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) brandColor.copy(alpha = 0.2f) else NavySurfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isSelected) brandColor else NavyCardBorder
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(brandColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = method.displayName,
                            color = if (isSelected) Color.White else TextMuted,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Contextual payment fields based on method
        when (selectedPaymentMethod) {
            PaymentMethod.DINHEIRO -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = NavySurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Cálculo de Troco em Dinheiro", color = CashGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick cash shortcut pills
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                Surface(
                                    modifier = Modifier.clickable { cashPaidStr = totalToPay.toString() },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (cashPaid == totalToPay) CashGreen.copy(alpha = 0.2f) else NavySurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (cashPaid == totalToPay) CashGreen else NavyCardBorder)
                                ) {
                                    Text(
                                        text = "Exato",
                                        color = if (cashPaid == totalToPay) CashGreen else TextLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            val bills = listOf(100.0, 200.0, 500.0, 1000.0, 2000.0)
                            items(bills) { bill ->
                                if (bill >= totalToPay) {
                                    Surface(
                                        modifier = Modifier.clickable { cashPaidStr = bill.toInt().toString() },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (cashPaid == bill) CashGreen.copy(alpha = 0.2f) else NavySurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (cashPaid == bill) CashGreen else NavyCardBorder)
                                    ) {
                                        Text(
                                            text = "${bill.toInt()} MT",
                                            color = if (cashPaid == bill) CashGreen else TextLight,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = cashPaidStr,
                            onValueChange = { cashPaidStr = it },
                            label = { Text("Valor Entregue pelo Cliente (MT)") },
                            placeholder = { Text(Formatters.formatMoney(totalToPay)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = NavyBackground,
                                unfocusedContainerColor = NavyBackground,
                                focusedBorderColor = CashGreen,
                                unfocusedBorderColor = NavyCardBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextLight
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Troco a devolver:", color = TextMuted, fontSize = 12.sp)
                            Text(
                                text = Formatters.formatMoney(change),
                                color = CashGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            PaymentMethod.MPESA -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MPesaRed.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Pagamento Vodacom M-Pesa", color = MPesaRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = paymentReference,
                            onValueChange = { paymentReference = it },
                            label = { Text("ID da Transacção / Ref. SMS (ex: MP-109283)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = NavyBackground,
                                unfocusedContainerColor = NavyBackground,
                                focusedBorderColor = MPesaRed,
                                unfocusedBorderColor = NavyCardBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextLight
                            )
                        )
                    }
                }
            }

            PaymentMethod.EMOLA -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EMolaOrange.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Pagamento Movitel e-Mola", color = EMolaOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = paymentReference,
                            onValueChange = { paymentReference = it },
                            label = { Text("ID da Transacção / Ref. e-Mola (ex: EM-449102)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = NavyBackground,
                                unfocusedContainerColor = NavyBackground,
                                focusedBorderColor = EMolaOrange,
                                unfocusedBorderColor = NavyCardBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextLight
                            )
                        )
                    }
                }
            }

            PaymentMethod.MKESH -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MKeshYellow.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Pagamento mKesh (Tmcel)", color = MKeshYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = paymentReference,
                            onValueChange = { paymentReference = it },
                            label = { Text("ID da Transacção / Ref. mKesh (ex: MK-88201)") },
                            modifier = Modifier.fillMaxWidth().testTag("mkesh_ref_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = NavyBackground,
                                unfocusedContainerColor = NavyBackground,
                                focusedBorderColor = MKeshYellow,
                                unfocusedBorderColor = NavyCardBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextLight
                            )
                        )
                    }
                }
            }

            PaymentMethod.FIADO -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FiadoBlue.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Venda a Crédito (Fiado)", color = FiadoBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (selectedClient == null) {
                            Text(
                                text = "⚠️ Atenção: Para vender fiado deves seleccionar um cliente cadastrado acima!",
                                color = NexoraAmberWarning,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        OutlinedTextField(
                            value = dueDaysStr,
                            onValueChange = { dueDaysStr = it },
                            label = { Text("Prazo de Pagamento (dias)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = NavyBackground,
                                unfocusedContainerColor = NavyBackground,
                                focusedBorderColor = FiadoBlue,
                                unfocusedBorderColor = NavyCardBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextLight
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Confirm Sale Button
        val isFiadoValid = selectedPaymentMethod != PaymentMethod.FIADO || selectedClient != null

        Button(
            onClick = {
                val days = dueDaysStr.toIntOrNull() ?: 7
                val dueDate = System.currentTimeMillis() + (days * 86400_000L)
                val effectivePaid = if (selectedPaymentMethod == PaymentMethod.FIADO) 0.0 else (cashPaidStr.toDoubleOrNull() ?: totalToPay)

                onCompleteSale(
                    cartItems.toList(),
                    discount,
                    selectedPaymentMethod,
                    effectivePaid,
                    selectedClient,
                    paymentReference.ifBlank { null },
                    if (selectedPaymentMethod == PaymentMethod.FIADO) dueDate else null,
                    notes
                )
            },
            enabled = cartItems.isNotEmpty() && isFiadoValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("confirm_sale_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(imageVector = Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Confirmar Venda • ${Formatters.formatMoney(totalToPay)}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
