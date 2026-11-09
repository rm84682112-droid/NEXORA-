package com.example.data.model

data class Client(
    val id: String,
    val name: String,
    val phone: String,
    val address: String,
    val creditLimit: Double = 5000.0,
    val totalDebt: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val hasDebt: Boolean
        get() = totalDebt > 0.0

    val availableCredit: Double
        get() = (creditLimit - totalDebt).coerceAtLeast(0.0)
}
