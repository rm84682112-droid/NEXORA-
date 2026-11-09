package com.example.data.model

enum class ExpenseCategory(val label: String, val icon: String) {
    RENDA("Renda do Imóvel", "🏢"),
    TRANSPORTE("Transporte e Logística", "🚚"),
    SALARIOS("Salários e Ajudas de Custo", "👥"),
    ELECTRICIDADE("Electricidade (EDM)", "⚡"),
    AGUA("Água (FIPAG)", "💧"),
    ALVARA_TAXAS("Taxas Municipais e Alvará", "🏛️"),
    MANUTENCAO("Manutenção e Reparos", "🔧"),
    OUTRAS("Outras Despesas", "📋")
}

data class Expense(
    val id: String,
    val description: String,
    val amount: Double,
    val category: ExpenseCategory,
    val paymentMethod: PaymentMethod,
    val date: Long,
    val notes: String = "",
    val recordedBy: String = ""
)
