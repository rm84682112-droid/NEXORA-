package com.example.data.model

enum class DebtStatus(val label: String) {
    PENDENTE("Pendente"),
    PARCIAL("Parcialmente Paga"),
    PAGA("Liquidada"),
    VENCIDA("Em Atraso / Vencida")
}

data class DebtPayment(
    val id: String,
    val date: Long,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val notes: String = ""
)

data class DebtRecord(
    val id: String,
    val saleId: String,
    val receiptNumber: String,
    val clientId: String,
    val clientName: String,
    val clientPhone: String,
    val originalAmount: Double,
    val remainingBalance: Double,
    val createdAt: Long,
    val dueDate: Long,
    val payments: List<DebtPayment> = emptyList()
) {
    val status: DebtStatus
        get() = when {
            remainingBalance <= 0.05 -> DebtStatus.PAGA
            System.currentTimeMillis() > dueDate -> DebtStatus.VENCIDA
            payments.isNotEmpty() -> DebtStatus.PARCIAL
            else -> DebtStatus.PENDENTE
        }

    val isOverdue: Boolean
        get() = status == DebtStatus.VENCIDA
}
