package com.example.ui.screens.products

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.example.data.model.Product
import com.example.data.model.Supplier
import com.example.data.model.UnitType
import com.example.data.model.User
import com.example.data.model.UserRole
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
fun ProductsScreen(
    currentUser: User,
    products: List<Product>,
    suppliers: List<Supplier>,
    onAddProduct: (
        name: String,
        category: String,
        description: String,
        unitType: UnitType,
        unitsPerPackage: Int,
        packagesCount: Int,
        costPrice: Double,
        sellingPrice: Double,
        minStock: Int,
        barcode: String,
        iconEmoji: String,
        isActive: Boolean,
        supplierName: String,
        packageEnabled: Boolean,
        packagePrice: Double,
        packageName: String
    ) -> Unit,
    onUpdateProduct: (Product) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onNavigateToPos: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todas") }
    var showAddScreen by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val context = LocalContext.current
    val canManageProducts = currentUser.role == UserRole.DONO

    // Categories extraction
    val categories = remember(products) {
        listOf("Todas") + products.map { it.category }.distinct()
    }

    // Filtered products list
    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { p ->
            val matchesCategory = selectedCategory == "Todas" || p.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.barcode.contains(searchQuery, ignoreCase = true) ||
                    p.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                // Title and Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Catálogo de Produtos",
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${products.size} produtos cadastrados no sistema",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            if (!canManageProducts) {
                                Toast.makeText(context, "Acesso restrito: Apenas o Dono pode adicionar produtos.", Toast.LENGTH_SHORT).show()
                            } else {
                                showAddScreen = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("add_product_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Novo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Pesquisar por nome, categoria ou código...", color = TextMuted, fontSize = 13.sp) },
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
                        .testTag("product_search_input"),
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
            }

            // Category Filter Chips
            if (categories.size > 2) {
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NexoraBluePrimary,
                                    selectedLabelColor = TextWhite,
                                    containerColor = NavySurfaceVariant,
                                    labelColor = TextMuted
                                )
                            )
                        }
                    }
                }
            }

            if (filteredProducts.isEmpty()) {
                item {
                    com.example.ui.components.EducationalEmptyState(
                        iconEmoji = "📦",
                        title = "Ainda não tem produtos no catálogo",
                        description = "Adicione o seu primeiro produto para começar a registar vendas no balcão (POS).",
                        actionLabel = "Novo Produto",
                        onAction = {
                            if (!canManageProducts) {
                                Toast.makeText(context, "Acesso restrito ao Dono.", Toast.LENGTH_SHORT).show()
                            } else {
                                showAddScreen = true
                            }
                        }
                    )
                }
            } else {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        currentUser = currentUser,
                        onClick = {
                            if (!canManageProducts) {
                                Toast.makeText(context, "Caixa: Consulta de produto (edição restrita ao Dono)", Toast.LENGTH_SHORT).show()
                            } else {
                                productToEdit = product
                            }
                        },
                        onEdit = {
                            if (!canManageProducts) {
                                Toast.makeText(context, "Acesso restrito: Apenas o Dono pode editar produtos.", Toast.LENGTH_SHORT).show()
                            } else {
                                productToEdit = product
                            }
                        },
                        onDelete = {
                            if (!canManageProducts) {
                                Toast.makeText(context, "Acesso restrito: Apenas o Dono pode apagar produtos.", Toast.LENGTH_SHORT).show()
                            } else {
                                productToDelete = product
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Full Screen Add or Edit Product Form
    if (showAddScreen || productToEdit != null) {
        ProductFormScreen(
            currentUser = currentUser,
            existingProduct = productToEdit,
            categories = categories,
            suppliers = suppliers,
            existingProducts = products,
            onDismiss = {
                showAddScreen = false
                productToEdit = null
            },
            onSave = { name, cat, desc, unitType, perPkg, pkgCount, cost, sell, min, barcode, emoji, isActive, supplierName, pkgEnabled, pkgPrice, pkgName, saveAndAnother ->
                if (productToEdit != null) {
                    onUpdateProduct(
                        productToEdit!!.copy(
                            name = name,
                            category = cat,
                            description = desc,
                            unitType = unitType,
                            unitsPerPackage = perPkg,
                            currentStock = if (pkgEnabled && perPkg > 1) pkgCount * perPkg else pkgCount,
                            costPrice = cost,
                            sellingPrice = sell,
                            minStock = min,
                            barcode = barcode,
                            iconEmoji = emoji,
                            isActive = isActive,
                            supplierName = supplierName,
                            packageEnabled = pkgEnabled,
                            packagePrice = pkgPrice,
                            packageName = pkgName
                        )
                    )
                    Toast.makeText(context, "Produto atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    onAddProduct(
                        name, cat, desc, unitType, perPkg, pkgCount, cost, sell, min, barcode, emoji,
                        isActive, supplierName, pkgEnabled, pkgPrice, pkgName
                    )
                    Toast.makeText(context, "Produto cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                }

                if (saveAndAnother) {
                    showAddScreen = true
                    productToEdit = null
                } else {
                    showAddScreen = false
                    productToEdit = null
                }
            }
        )
    }

    // Delete Confirmation Dialog
    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Eliminar Produto", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza que deseja eliminar '${product.name}' do catálogo? Esta ação não pode ser desfeita.", color = TextLight) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProduct(product.id)
                        Toast.makeText(context, "Produto eliminado.", Toast.LENGTH_SHORT).show()
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraRedAlert)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancelar", color = TextMuted)
                }
            },
            containerColor = NavySurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun ProductCard(
    product: Product,
    currentUser: User,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val canViewProfits = currentUser.role == UserRole.DONO

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("product_card_${product.id}"),
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(NexoraBluePrimary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = product.iconEmoji, fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = product.name,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = product.category,
                                color = NexoraCyanAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (product.barcode.isNotBlank()) {
                                Text(
                                    text = " • Cód: ${product.barcode}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            if (!product.isActive) {
                                Text(
                                    text = " • [Inactivo]",
                                    color = NexoraRedAlert,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Row {
                    if (canViewProfits) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = NexoraCyanAccent, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = NexoraRedAlert, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = NavyCardBorder)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (stockText, stockColor) = when {
                    product.isOutOfStock -> Pair("Esgotado (0 un)", NexoraRedAlert)
                    product.isLowStock -> Pair("Baixo (${product.currentStock} un)", NexoraAmberWarning)
                    else -> Pair("${product.currentStock} un em stock", NexoraGreenSuccess)
                }

                Box(
                    modifier = Modifier
                        .background(stockColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stockText,
                        color = stockColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Formatters.formatMoney(product.sellingPrice),
                        color = TextWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    if (canViewProfits) {
                        Text(
                            text = "Lucro: +${Formatters.formatMoney(product.profitPerUnit)} (${product.profitMarginPercent.toInt()}%)",
                            color = NexoraGreenSuccess,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "Preço Balcão",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    currentUser: User,
    existingProduct: Product?,
    categories: List<String>,
    suppliers: List<Supplier>,
    existingProducts: List<Product>,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        category: String,
        description: String,
        unitType: UnitType,
        unitsPerPackage: Int,
        packagesCount: Int,
        costPrice: Double,
        sellingPrice: Double,
        minStock: Int,
        barcode: String,
        iconEmoji: String,
        isActive: Boolean,
        supplierName: String,
        packageEnabled: Boolean,
        packagePrice: Double,
        packageName: String,
        saveAndAnother: Boolean
    ) -> Unit
) {
    val canViewCosts = currentUser.role == UserRole.DONO

    var name by remember { mutableStateOf(existingProduct?.name ?: "") }
    var description by remember { mutableStateOf(existingProduct?.description ?: "") }
    var categoryInput by remember { mutableStateOf(existingProduct?.category ?: "Mercearia") }
    var barcode by remember { mutableStateOf(existingProduct?.barcode ?: "") }
    
    var costPriceStr by remember { mutableStateOf(existingProduct?.costPrice?.toString() ?: "") }
    var sellingPriceStr by remember { mutableStateOf(existingProduct?.sellingPrice?.toString() ?: "") }
    
    var currentStockStr by remember { mutableStateOf(existingProduct?.currentStock?.toString() ?: "") }
    var unitType by remember { mutableStateOf(existingProduct?.unitType ?: UnitType.UNIDADE) }
    var minStockStr by remember { mutableStateOf(existingProduct?.minStock?.toString() ?: "5") }
    
    var packageEnabled by remember { mutableStateOf(existingProduct?.packageEnabled ?: false) }
    var packageName by remember { mutableStateOf(existingProduct?.packageName ?: "Caixa") }
    var unitsPerPackageStr by remember { mutableStateOf(existingProduct?.unitsPerPackage?.toString() ?: "12") }
    var packagePriceStr by remember { mutableStateOf(existingProduct?.packagePrice?.toString() ?: "") }
    
    var supplierName by remember { mutableStateOf(existingProduct?.supplierName ?: "") }
    var isActive by remember { mutableStateOf(existingProduct?.isActive ?: true) }
    var iconEmoji by remember { mutableStateOf(existingProduct?.iconEmoji ?: "📦") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDuplicateWarning by remember { mutableStateOf(false) }
    var showCancelConfirmation by remember { mutableStateOf(false) }

    val cost = costPriceStr.toDoubleOrNull() ?: 0.0
    val sell = sellingPriceStr.toDoubleOrNull() ?: 0.0
    val profit = sell - cost
    val margin = if (sell > 0.0) ((profit / sell) * 100.0) else 0.0
    val isSellBelowCost = sell > 0.0 && cost > 0.0 && sell < cost

    val isDirty = name.isNotBlank() || sellingPriceStr.isNotBlank() || currentStockStr.isNotBlank()

    val pkgCount = currentStockStr.toIntOrNull() ?: 0
    val unitsPerPkg = unitsPerPackageStr.toIntOrNull() ?: 1
    val totalConvertedStock = if (packageEnabled && unitsPerPkg > 1) pkgCount * unitsPerPkg else pkgCount

    val context = LocalContext.current

    fun executeSave(saveAndAnother: Boolean) {
        val perPkg = unitsPerPkg.coerceAtLeast(1)
        val finalStock = pkgCount.coerceAtLeast(0)
        val minStk = minStockStr.toIntOrNull() ?: 5
        val pPrice = packagePriceStr.toDoubleOrNull() ?: 0.0

        onSave(
            name.trim(),
            categoryInput.ifBlank { "Geral" },
            description.trim(),
            unitType,
            perPkg,
            finalStock,
            cost,
            sell,
            minStk,
            barcode.trim(),
            iconEmoji,
            isActive,
            supplierName.trim(),
            packageEnabled,
            pPrice,
            packageName.trim(),
            saveAndAnother
        )
    }

    fun validateAndTriggerSave(saveAndAnother: Boolean) {
        if (name.isBlank()) {
            errorMessage = "O nome do produto é obrigatório (*)."
            return
        }
        val sellVal = sellingPriceStr.toDoubleOrNull()
        if (sellVal == null || sellVal <= 0.0) {
            errorMessage = "Indique um preço de venda válido (*)."
            return
        }
        val stockVal = currentStockStr.toIntOrNull()
        if (stockVal == null) {
            errorMessage = "Indique uma quantidade em stock válida (*)."
            return
        }

        errorMessage = null
        val duplicate = existingProducts.any { p ->
            p.id != existingProduct?.id && (
                p.name.equals(name.trim(), ignoreCase = true) ||
                (barcode.isNotBlank() && p.barcode == barcode.trim())
            )
        }

        if (duplicate) {
            showDuplicateWarning = true
        } else {
            executeSave(saveAndAnother)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingProduct == null) "Registar Novo Produto" else "Editar Produto",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isDirty) showCancelConfirmation = true else onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavySurface)
            )
        },
        bottomBar = {
            Surface(
                color = NavySurface,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (isDirty) showCancelConfirmation = true else onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextLight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.Bold)
                    }

                    if (existingProduct == null) {
                        Button(
                            onClick = { validateAndTriggerSave(true) },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyCardElevated),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Guardar + Outro", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = { validateAndTriggerSave(false) },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(52.dp)
                            .testTag("save_product_final_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        },
        containerColor = NavyBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            errorMessage?.let { err ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = NexoraRedAlert.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexoraRedAlert)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = NexoraRedAlert)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = err, color = NexoraRedAlert, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // SECÇÃO 1: Essencial para Cálculo do Lucro Unitário
            FormSectionContainer(title = "1. Dados Essenciais e Preços (Cálculo de Lucro)") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Produto *") },
                    placeholder = { Text("ex: Arroz Touro 25kg, Coca-Cola Lata") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = formTextFieldColors(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Interruptor de Embalagem simples para custo fracionado
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavyCardElevated, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Compra por Embalagem / Fardo / Caixa?", color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            text = if (packageEnabled) "O custo será dividido pelas unidades" else "Compra e venda direta por unidade individual",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    androidx.compose.material3.Switch(
                        checked = packageEnabled,
                        onCheckedChange = { packageEnabled = it }
                    )
                }

                if (packageEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = packageName,
                            onValueChange = { packageName = it },
                            label = { Text("Tipo Embalagem") },
                            placeholder = { Text("Caixa/Fardo") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = formTextFieldColors(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = unitsPerPackageStr,
                            onValueChange = { unitsPerPackageStr = it },
                            label = { Text("Qtd de Unidades *") },
                            placeholder = { Text("ex: 24") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = formTextFieldColors(),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Campos de Preços
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (canViewCosts) {
                        OutlinedTextField(
                            value = costPriceStr,
                            onValueChange = { costPriceStr = it },
                            label = { Text(if (packageEnabled) "Preço Compra ($packageName)" else "Preço Compra Unitário") },
                            placeholder = { Text("0.00 MT") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_cost_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = formTextFieldColors(),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = sellingPriceStr,
                        onValueChange = { sellingPriceStr = it },
                        label = { Text("Preço Venda Unitário *") },
                        placeholder = { Text("0.00 MT") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_price_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = formTextFieldColors(),
                        singleLine = true
                    )
                }

                val effectiveUnitCost = if (packageEnabled && unitsPerPkg > 0) (cost / unitsPerPkg) else cost
                val unitProfit = sell - effectiveUnitCost
                val unitMargin = if (sell > 0.0) ((unitProfit / sell) * 100.0) else 0.0
                val unitLoss = sell > 0.0 && effectiveUnitCost > 0.0 && sell < effectiveUnitCost

                // Cartão Destacado: Resultado do Cálculo de Lucro Unitário
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (unitLoss) NexoraRedAlert.copy(alpha = 0.15f) else NexoraGreenSuccess.copy(alpha = 0.12f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (unitLoss) NexoraRedAlert.copy(alpha = 0.6f) else NexoraGreenSuccess.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (packageEnabled) "Custo Unitário Calculado" else "Custo Base",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = Formatters.formatMoney(effectiveUnitCost),
                                    color = TextLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (packageEnabled && unitsPerPkg > 0 && cost > 0.0) {
                                    Text(
                                        text = "(${Formatters.formatMoney(cost)} ÷ $unitsPerPkg un)",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Lucro por Unidade",
                                    color = if (unitLoss) NexoraRedAlert else NexoraGreenSuccess,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${if (unitProfit >= 0) "+" else ""}${Formatters.formatMoney(unitProfit)}",
                                    color = if (unitLoss) NexoraRedAlert else NexoraGreenSuccess,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Margem: ${unitMargin.toInt()}%",
                                    color = if (unitLoss) NexoraRedAlert else NexoraCyanAccent,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (unitLoss) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "⚠️ Atenção: Preço de venda inferior ao custo. Terá prejuízo por cada unidade vendida!",
                                color = NexoraRedAlert,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stock Inicial
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = currentStockStr,
                        onValueChange = { currentStockStr = it },
                        label = { Text(if (packageEnabled) "Qtd de ${packageName}s em Stock *" else "Stock Inicial (Unidades) *") },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_stock_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = formTextFieldColors(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = minStockStr,
                        onValueChange = { minStockStr = it },
                        label = { Text("Alerta Stock Baixo") },
                        placeholder = { Text("5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = formTextFieldColors(),
                        singleLine = true
                    )
                }

                if (packageEnabled && pkgCount > 0 && unitsPerPkg > 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "📦 Stock Total: $pkgCount ${packageName}(s) = $totalConvertedStock unidades para venda no balcão.",
                        color = NexoraCyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // SECÇÃO 2: Outros Detalhes Opcionais (Categoria, Código, Fornecedor)
            var showExtraDetails by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showExtraDetails = !showExtraDetails },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⚙️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Detalhes Opcionais (Categoria, Código, Fornecedor)",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = if (showExtraDetails) "▲ Fechar" else "▼ Abrir",
                        color = NexoraCyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (showExtraDetails) {
                FormSectionContainer(title = "Outros Detalhes (Opcional)") {
                    Text("Ícone / Fotografia", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    val emojis = listOf("📦", "🍚", "🌻", "🌽", "🧂", "🥛", "🥤", "🍺", "💧", "🧼", "🪥", "🍞", "🥩", "🍎", "🍗", "☕")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(emojis) { em ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (iconEmoji == em) NexoraBluePrimary.copy(alpha = 0.35f) else NavySurface,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { iconEmoji = em },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = em, fontSize = 20.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = categoryInput,
                            onValueChange = { categoryInput = it },
                            label = { Text("Categoria") },
                            placeholder = { Text("Mercearia") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = formTextFieldColors(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("Código de Barras") },
                            placeholder = { Text("opcional") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = formTextFieldColors(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = supplierName,
                        onValueChange = { supplierName = it },
                        label = { Text("Fornecedor Habitual") },
                        placeholder = { Text("ex: Grossista Maputo") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = formTextFieldColors(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição adicional") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = formTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Produto Activo no Balcão", color = TextWhite, fontSize = 13.sp)
                        androidx.compose.material3.Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Duplicate Warning Dialog
    if (showDuplicateWarning) {
        AlertDialog(
            onDismissRequest = { showDuplicateWarning = false },
            title = { Text("Produto Semelhante", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = { Text("Já existe um produto com o mesmo nome ou código de barras. Deseja guardar mesmo assim?", color = TextLight) },
            confirmButton = {
                Button(
                    onClick = {
                        showDuplicateWarning = false
                        executeSave(false)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary)
                ) {
                    Text("Sim, Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDuplicateWarning = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            },
            containerColor = NavySurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Cancel Unsaved Changes Dialog
    if (showCancelConfirmation) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmation = false },
            title = { Text("Descartar Alterações?", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = { Text("Tem alterações não guardadas que serão perdidas se sair.", color = TextLight) },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelConfirmation = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraRedAlert)
                ) {
                    Text("Descartar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmation = false }) {
                    Text("Continuar a Editar", color = NexoraCyanAccent)
                }
            },
            containerColor = NavySurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun FormSectionContainer(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                color = NexoraCyanAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun formTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = NavySurfaceVariant,
    unfocusedContainerColor = NavySurfaceVariant,
    focusedBorderColor = NexoraBlueLight,
    unfocusedBorderColor = NavyCardBorder,
    focusedTextColor = TextWhite,
    unfocusedTextColor = TextLight,
    focusedLabelColor = NexoraCyanAccent,
    unfocusedLabelColor = TextMuted
)
