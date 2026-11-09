package com.example.data.model

import java.util.UUID

data class UserSession(
    val id: String = UUID.randomUUID().toString(),
    val deviceModel: String,
    val platform: String = "Android",
    val ipAddressOrCity: String = "Maputo, Moçambique",
    val lastActive: Long = System.currentTimeMillis(),
    val isCurrent: Boolean = false
)

data class PhoneAuthCredentials(
    val phoneNumber: String,
    val isVerified: Boolean = false,
    val recoveryCodes: List<String> = listOf("NX-8392", "NX-1940", "NX-7731", "NX-4402", "NX-9915"),
    val biometricEnabled: Boolean = false
)

enum class AuthScreenMode {
    MAIN_APP,
    LANDING,
    LOGIN,
    REGISTER_WIZARD,
    ACCESS_RECOVERY
}

