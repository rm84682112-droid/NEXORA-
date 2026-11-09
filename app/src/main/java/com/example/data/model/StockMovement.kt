package com.example.data.model

enum class MovementType(val label: String, val isAddition: Boolean) {
    ENTRADA("Entrada de Mercadoria", true),
    SAIDA("Saída Manual", false),
    VENDA("Venda no Balcão", false),
    AJUSTE("Ajuste / Quebra", false),
    DEVOLUCAO("Devolução ao Fornecedor", false)
}

data class StockMovement(
    val id: String,
    val productId: String,
    val productName: String,
    val type: MovementType,
    val quantity: Int,
    val previousStock: Int,
    val newStock: Int,
    val reason: String,
    val operatorName: String,
    val timestamp: Long
)
