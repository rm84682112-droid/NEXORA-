package com.example.data.model

enum class PaymentMethod(val displayName: String, val code: String) {
    DINHEIRO("Dinheiro", "DINHEIRO"),
    MPESA("M-Pesa", "MPESA"),
    EMOLA("e-Mola", "EMOLA"),
    MKESH("mKesh", "MKESH"),
    FIADO("Fiado", "FIADO")
}

data class CartItem(
    val product: Product,
    val quantity: Int,
    val customPrice: Double? = null
) {
    val unitPrice: Double
        get() = customPrice ?: product.sellingPrice

    val subtotal: Double
        get() = unitPrice * quantity

    val costSubtotal: Double
        get() = product.costPrice * quantity
}

data class Sale(
    val id: String,
    val receiptNumber: String,
    val timestamp: Long,
    val items: List<CartItem>,
    val subtotal: Double,
    val discount: Double,
    val total: Double,
    val paymentMethod: PaymentMethod,
    val amountPaid: Double,
    val change: Double,
    val customerId: String? = null,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val operatorName: String,
    val paymentReference: String? = null,
    val dueDate: Long? = null,
    val notes: String = ""
) {
    val totalCost: Double
        get() = items.sumOf { it.costSubtotal }

    val profit: Double
        get() = total - totalCost
}
