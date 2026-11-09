package com.example.data.model

data class Supplier(
    val id: String,
    val name: String,
    val phone: String,
    val address: String,
    val suppliedCategories: String = "Mercearia Geral",
    val totalDebt: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class PurchaseItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitCost: Double
) {
    val subtotal: Double get() = quantity * unitCost
}

data class SupplierPurchase(
    val id: String,
    val supplierId: String,
    val supplierName: String,
    val date: Long,
    val items: List<PurchaseItem>,
    val totalCost: Double,
    val amountPaid: Double,
    val debtRemaining: Double,
    val paymentMethod: PaymentMethod,
    val receiptOrInvoiceNumber: String = "",
    val notes: String = ""
)
