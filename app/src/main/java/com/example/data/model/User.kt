package com.example.data.model

enum class UserRole(val label: String) {
    DONO("Proprietário (Dono)"),
    CAIXA("Operador de Caixa"),
    GERENTE("Gerente de Loja"),
    ADMIN("Super Administrador NEXORA")
}

data class User(
    val id: String,
    val name: String,
    val phone: String,
    val role: UserRole,
    val pinHash: String = "", // Securely hashed PIN (SHA-256)
    val passwordHash: String = "", // Optional hashed password
    val recoveryCodes: List<String> = emptyList(), // 5 single-use recovery codes
    val storeName: String,
    val storeLocation: String,
    val businessType: String = "Mercearia",
    val businessId: String = "store-matriz-1",
    val nuit: String = "",
    val currencySymbol: String = "MT",
    val failedAttempts: Int = 0,
    val isLockedOut: Boolean = false,
    val lockoutUntil: Long = 0L,
    val isRemembered: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val canViewProfits: Boolean
        get() = role == UserRole.DONO || role == UserRole.GERENTE || role == UserRole.ADMIN

    val canEditProducts: Boolean
        get() = role == UserRole.DONO || role == UserRole.GERENTE || role == UserRole.ADMIN

    val canGiveDiscounts: Boolean
        get() = role == UserRole.DONO || role == UserRole.GERENTE || role == UserRole.ADMIN

    val isSuperAdmin: Boolean
        get() = role == UserRole.ADMIN
}
