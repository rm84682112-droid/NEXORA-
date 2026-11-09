package com.example.ui.screens.suppliers

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.PurchaseItem
import com.example.data.model.Supplier
import com.example.data.model.SupplierPurchase
import com.example.ui.theme.CashGreen
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
import com.example.ui.theme.NexoraRedAlert
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliersScreen(
    suppliers: List<Supplier>,
    products: List<Product>,
    onAddSupplier: (name: String, phone: String, address: String, categories: String, notes: String) -> Unit,
    onRegisterPurchase: (supplier: Supplier, items: List<PurchaseItem>, amountPaid: Double, method: PaymentMethod, invoice: String, notes: String) -> Unit,
    onPaySupplierDebt: (supplierId: String, amount: Double, method: PaymentMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddSupplierSheet by remember { mutableStateOf(false) }
    var showPurchaseSheet by remember { mutableStateOf(false) }
    var supplierToPayDebt by remember { mutableStateOf<Supplier?>(null) }

    val totalSupplierDebt = remember(suppliers) { suppliers.sumOf { it.totalDebt } }

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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Gestão de Fornecedores",
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Entradas de mercadoria e contas a pagar",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { showPurchaseSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NexoraGreenSuccess),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("new_purchase_button")
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Comprar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showAddSupplierSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_supplier_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("DÍVIDAS A FORNECEDORES", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Formatters.formatMoney(totalSupplierDebt),
                                color = if (totalSupplierDebt > 0) NexoraAmberWarning else NexoraGreenSuccess,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(NavyCardElevated, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${suppliers.size} fornecedores",
                                color = NexoraCyanAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (suppliers.isEmpty()) {
                item {
                    com.example.ui.components.EducationalEmptyState(
                        iconEmoji = "🚛",
                        title = "Ainda não tem fornecedores",
                        description = "Adicione fornecedores para registar compras de mercadorias, facturas e pagamentos.",
                        actionLabel = "Novo Fornecedor",
                        onAction = { showAddSupplierSheet = true }
                    )
                }
            } else {
                items(suppliers, key = { it.id }) { supplier ->
                    SupplierItemCard(
                        supplier = supplier,
                        onPayDebt = { supplierToPayDebt = supplier }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal: Add Supplier Sheet
    if (showAddSupplierSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAddSupplierSheet = false },
            sheetState = sheetState,
            containerColor = NavySurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            AddSupplierSheetContent(
                onDismiss = { showAddSupplierSheet = false },
                onSave = { name, phone, address, categories, notes ->
                    onAddSupplier(name, phone, address, categories, notes)
                    showAddSupplierSheet = false
                }
            )
        }
    }

    // Modal: Register Purchase Sheet (automatic stock increment)
    if (showPurchaseSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPurchaseSheet = false },
            sheetState = sheetState,
            containerColor = NavySurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            RegisterPurchaseSheetContent(
                suppliers = suppliers,
                products = products,
                onDismiss = { showPurchaseSheet = false },
                onSave = { supplier, items, paid, method, invoice, notes ->
                    onRegisterPurchase(supplier, items, paid, method, invoice, notes)
                    showPurchaseSheet = false
                }
            )
        }
    }

    // Modal: Pay Supplier Debt
    supplierToPayDebt?.let { supplier ->
        PaySupplierDebtDialog(
            supplier = supplier,
            onDismiss = { supplierToPayDebt = null },
            onConfirm = { amount, method ->
                onPaySupplierDebt(supplier.id, amount, method)
                supplierToPayDebt = null
            }
        )
    }
}

@Composable
private fun SupplierItemCard(
    supplier: Supplier,
    onPayDebt: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(NexoraBluePrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = NexoraCyanAccent, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(supplier.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("📞 ${supplier.phone}", color = TextMuted, fontSize = 11.sp)
                        Text("📍 ${supplier.address}", color = TextMuted, fontSize = 10.sp)
                    }
                }

                if (supplier.totalDebt > 0) {
                    Box(
                        modifier = Modifier
                            .background(NexoraRedAlert.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Devemos: ${Formatters.formatMoney(supplier.totalDebt)}",
                            color = NexoraRedAlert,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(NexoraGreenSuccess.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Sem dívida", color = NexoraGreenSuccess, fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
                Text(
                    text = "Fornece: ${supplier.suppliedCategories}",
                    color = TextLight,
                    fontSize = 11.sp
                )

                if (supplier.totalDebt > 0) {
                    Button(
                        onClick = onPayDebt,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Pagar Dívida", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddSupplierSheetContent(
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, address: String, categories: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+258 ") }
    var address by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Cadastrar Novo Fornecedor", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted) }
        }

        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome da Empresa / Fornecedor") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Telemóvel Comercial (+258)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Morada / Armazém (ex: Baixa de Maputo)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = categories, onValueChange = { categories = it }, label = { Text("Artigos que Fornece (ex: Arroz, Farinha, Bebidas)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Condições de Pagamento / Notas") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onSave(name.ifBlank { "Fornecedor" }, phone, address, categories, notes) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Guardar Fornecedor", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun RegisterPurchaseSheetContent(
    suppliers: List<Supplier>,
    products: List<Product>,
    onDismiss: () -> Unit,
    onSave: (supplier: Supplier, items: List<PurchaseItem>, paid: Double, method: PaymentMethod, invoice: String, notes: String) -> Unit
) {
    var selectedSupplier by remember { mutableStateOf(suppliers.firstOrNull()) }
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var quantityStr by remember { mutableStateOf("10") }
    var unitCostStr by remember { mutableStateOf(selectedProduct?.costPrice?.toString() ?: "100") }
    var amountPaidStr by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.DINHEIRO) }
    var invoiceNumber by remember { mutableStateOf("") }

    val qty = quantityStr.toIntOrNull() ?: 1
    val cost = unitCostStr.toDoubleOrNull() ?: 0.0
    val totalCost = qty * cost
    val paid = amountPaidStr.toDoubleOrNull() ?: totalCost
    val debt = (totalCost - paid).coerceAtLeast(0.0)

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Registar Compra de Mercadoria", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted) }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text("O stock do produto selecionado aumentará automaticamente!", color = NexoraGreenSuccess, fontSize = 11.sp, fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(12.dp))
        Text("Fornecedor", color = TextMuted, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(6.dp))
        suppliers.forEach { s ->
            val isSelected = s.id == selectedSupplier?.id
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { selectedSupplier = s },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) NexoraBluePrimary.copy(alpha = 0.2f) else NavyCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NexoraBlueLight else NavyCardBorder)
            ) {
                Text(s.name, color = if (isSelected) Color.White else TextMuted, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Produto a Repor", color = TextMuted, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(6.dp))
        products.take(6).forEach { p ->
            val isSelected = p.id == selectedProduct?.id
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable {
                    selectedProduct = p
                    unitCostStr = p.costPrice.toString()
                },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) NexoraGreenSuccess.copy(alpha = 0.2f) else NavyCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NexoraGreenSuccess else NavyCardBorder)
            ) {
                Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${p.iconEmoji} ${p.name}", color = if (isSelected) Color.White else TextLight, fontSize = 12.sp)
                    Text("Stock atual: ${p.currentStock}", color = TextMuted, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(value = quantityStr, onValueChange = { quantityStr = it }, label = { Text("Qtd Comprada") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
            OutlinedTextField(value = unitCostStr, onValueChange = { unitCostStr = it }, label = { Text("Custo Unit. (MT)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = NavyCardElevated)) {
            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Total da Fatura:", color = TextLight, fontSize = 12.sp)
                Text(Formatters.formatMoney(totalCost), color = NexoraCyanAccent, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text("Método de Pagamento", color = TextMuted, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(PaymentMethod.DINHEIRO, PaymentMethod.MPESA, PaymentMethod.EMOLA, PaymentMethod.MKESH).forEach { method ->
                val isSelected = method == paymentMethod
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
                        .background(if (isSelected) color.copy(alpha = 0.25f) else NavyCardElevated, RoundedCornerShape(8.dp))
                        .border(1.dp, if (isSelected) color else NavyCardBorder, RoundedCornerShape(8.dp))
                        .clickable { paymentMethod = method }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(method.displayName, color = if (isSelected) color else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = amountPaidStr, onValueChange = { amountPaidStr = it }, label = { Text("Valor Pago Imediato (Deixar vazio para total)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))

        if (debt > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Ficará uma dívida de ${Formatters.formatMoney(debt)} a pagar a este fornecedor.", color = NexoraAmberWarning, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (selectedSupplier != null && selectedProduct != null) {
                    val purchaseItems = listOf(PurchaseItem(selectedProduct!!.id, selectedProduct!!.name, qty, cost))
                    onSave(selectedSupplier!!, purchaseItems, paid, paymentMethod, invoiceNumber, "")
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NexoraGreenSuccess),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Confirmar Entrada no Stock", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun PaySupplierDebtDialog(
    supplier: Supplier,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, method: PaymentMethod) -> Unit
) {
    var amountStr by remember { mutableStateOf(supplier.totalDebt.toString()) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.MPESA) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Amortizar Dívida ao Fornecedor", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Fornecedor: ${supplier.name}", color = TextLight, fontSize = 13.sp)
                Text("Dívida total atual: ${Formatters.formatMoney(supplier.totalDebt)}", color = NexoraRedAlert, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("Valor a Transferir/Pagar (MT)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                Spacer(modifier = Modifier.height(10.dp))
                Text("Método de Pagamento Utilizado", color = TextMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
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
                                .background(if (isSelected) color.copy(alpha = 0.25f) else NavyCardElevated, RoundedCornerShape(8.dp))
                                .border(1.dp, if (isSelected) color else NavyCardBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedMethod = method }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(method.displayName, color = if (isSelected) color else TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amountStr.toDoubleOrNull() ?: supplier.totalDebt
                onConfirm(amt, selectedMethod)
            }, colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary)) {
                Text("Confirmar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = TextLight) } },
        containerColor = NavySurface,
        shape = RoundedCornerShape(20.dp)
    )
}
