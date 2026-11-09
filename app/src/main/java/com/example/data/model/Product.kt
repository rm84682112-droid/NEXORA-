package com.example.data.model

enum class UnitType(val label: String, val shortLabel: String) {
    UNIDADE("Unidade", "un"),
    FARDO("Fardo", "fd"),
    CAIXA("Caixa", "cx"),
    PACOTE("Pacote", "pct"),
    KG("Quilograma", "kg"),
    LITRO("Litro", "L"),
    OUTRA("Outra", "outra")
}

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val description: String = "",
    val unitType: UnitType = UnitType.UNIDADE,
    val unitsPerPackage: Int = 1,
    val costPrice: Double = 0.0,
    val sellingPrice: Double,
    val currentStock: Int,
    val minStock: Int = 5,
    val barcode: String = "",
    val iconEmoji: String = "📦",
    val isActive: Boolean = true,
    val supplierName: String = "",
    val packageEnabled: Boolean = false,
    val packagePrice: Double = 0.0,
    val packageName: String = "Caixa"
) {
    val profitPerUnit: Double
        get() = sellingPrice - costPrice

    val profitMarginPercent: Double
        get() = if (sellingPrice > 0.0) ((sellingPrice - costPrice) / sellingPrice) * 100.0 else 0.0

    val isLowStock: Boolean
        get() = currentStock in 1..minStock

    val isOutOfStock: Boolean
        get() = currentStock <= 0
}
