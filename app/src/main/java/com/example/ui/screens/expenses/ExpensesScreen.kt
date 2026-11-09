package com.example.ui.screens.expenses

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.example.data.model.Expense
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMethod
import com.example.data.model.User
import com.example.ui.theme.CashGreen
import com.example.ui.theme.EMolaOrange
import com.example.ui.theme.MKeshYellow
import com.example.ui.theme.MPesaRed
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.NexoraBlueLight
import com.example.ui.theme.NexoraBluePrimary
import com.example.ui.theme.NexoraCyanAccent
import com.example.ui.theme.NexoraRedAlert
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    currentUser: User,
    expenses: List<Expense>,
    onAddExpense: (description: String, amount: Double, category: ExpenseCategory, method: PaymentMethod, date: Long, notes: String) -> Unit,
    onDeleteExpense: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<ExpenseCategory?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }

    // If Caixa tries to view expenses, display confidentiality protection
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
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(NexoraRedAlert.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = NexoraRedAlert, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Acesso Restrito ao Dono", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "A gestão e histórico de despesas são dados confidenciais do proprietário da loja.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        return
    }

    val filteredExpenses = remember(expenses, searchQuery, selectedCategoryFilter) {
        expenses.filter { exp ->
            val matchCat = selectedCategoryFilter == null || exp.category == selectedCategoryFilter
            val matchSearch = searchQuery.isBlank() ||
                    exp.description.contains(searchQuery, ignoreCase = true) ||
                    exp.notes.contains(searchQuery, ignoreCase = true)
            matchCat && matchSearch
        }
    }

    val totalExpenses = remember(expenses) { expenses.sumOf { it.amount } }

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
                            text = "Gestão de Despesas",
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Controle de saídas operacionais e custos fixos",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { showAddSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NexoraRedAlert),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("add_expense_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Registar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text("TOTAL DE DESPESAS", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = Formatters.formatMoney(totalExpenses),
                                    color = NexoraRedAlert,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(NexoraRedAlert.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.TrendingDown, contentDescription = null, tint = NexoraRedAlert, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Operacional", color = NexoraRedAlert, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Estas despesas abatem automaticamente no cálculo do Lucro Líquido do Dashboard.",
                            color = TextLight,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Pesquisar despesa por descrição...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NexoraCyanAccent) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = TextMuted)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
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

            // Category Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("Todas") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NexoraBluePrimary,
                                selectedLabelColor = Color.White,
                                containerColor = NavyCardElevated,
                                labelColor = TextMuted
                            )
                        )
                    }
                    items(ExpenseCategory.values()) { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryFilter = if (isSelected) null else cat },
                            label = { Text("${cat.icon} ${cat.label.split(" ").first()}") },
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

            // Expenses items
            if (filteredExpenses.isEmpty()) {
                item {
                    com.example.ui.components.EducationalEmptyState(
                        iconEmoji = "💸",
                        title = "Nenhuma despesa registada",
                        description = "Adicione os custos do negócio como renda, luz, salários ou transporte para calcular o seu lucro real.",
                        actionLabel = "Registar Despesa",
                        onAction = { showAddSheet = true }
                    )
                }
            }

            items(filteredExpenses, key = { it.id }) { exp ->
                ExpenseItemCard(expense = exp, onDelete = { onDeleteExpense(exp.id) })
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal: Add Expense Sheet
    if (showAddSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            containerColor = NavySurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            AddExpenseForm(
                onDismiss = { showAddSheet = false },
                onSave = { desc, amount, cat, method, date, notes ->
                    onAddExpense(desc, amount, cat, method, date, notes)
                    showAddSheet = false
                }
            )
        }
    }
}

@Composable
private fun ExpenseItemCard(
    expense: Expense,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(NavyCardElevated, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(expense.category.icon, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = expense.description,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${expense.category.label} • ${Formatters.formatDate(expense.date)}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Pago via ${expense.paymentMethod.displayName}",
                        color = NexoraCyanAccent,
                        fontSize = 10.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "- ${Formatters.formatMoney(expense.amount)}",
                    color = NexoraRedAlert,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AddExpenseForm(
    onDismiss: () -> Unit,
    onSave: (desc: String, amount: Double, cat: ExpenseCategory, method: PaymentMethod, date: Long, notes: String) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExpenseCategory.OUTRAS) }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.DINHEIRO) }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Registar Nova Despesa", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descrição (ex: Recarga Credelec EDM)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = amountStr,
            onValueChange = { amountStr = it },
            label = { Text("Valor da Despesa (MT)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))
        Text("Categoria da Despesa", color = TextMuted, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ExpenseCategory.values()) { cat ->
                val isSelected = cat == category
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) NexoraBluePrimary else NavyCardElevated,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { category = cat }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${cat.icon} ${cat.label}",
                        color = if (isSelected) Color.White else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text("Método de Pagamento", color = TextMuted, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        .background(if (isSelected) color.copy(alpha = 0.25f) else NavyCardElevated, RoundedCornerShape(10.dp))
                        .clickable { paymentMethod = method }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = method.displayName,
                        color = if (isSelected) Color.White else TextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Observações (Opcional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val amt = amountStr.toDoubleOrNull() ?: 0.0
                if (amt > 0) {
                    onSave(description.ifBlank { "Despesa" }, amt, category, paymentMethod, System.currentTimeMillis(), notes)
                }
            },
            enabled = (amountStr.toDoubleOrNull() ?: 0.0) > 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NexoraRedAlert),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Registar Despesa", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
