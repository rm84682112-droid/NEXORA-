package com.example.data.model

import java.util.UUID

data class UserConsentRecord(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val userPhone: String,
    val documentVersion: String = "1.0",
    val acceptedTerms: Boolean = true,
    val acceptedPrivacy: Boolean = true,
    val acceptedAiAnalysis: Boolean = true,
    val acceptedNotifications: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

data class PrivacySettings(
    val allowAiAnalysis: Boolean = true,
    val allowNotifications: Boolean = true,
    val acceptedConsentVersion: String = "1.0",
    val aiDisclaimerAccepted: Boolean = false
)

enum class IncidentSeverity(val label: String) {
    BAIXO("Baixo Impacto"),
    MEDIO("Médio Impacto"),
    ALTO("Alto Impacto"),
    CRITICO("Crítico / Fuga Grave")
}

enum class IncidentStatus(val label: String) {
    INVESTIGANDO("Em Investigação"),
    MITIGADO("Mitigado / Resolvido"),
    NOTIFICADO("Notificado aos Utilizadores")
}

data class DataIncident(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val severity: IncidentSeverity = IncidentSeverity.MEDIO,
    val status: IncidentStatus = IncidentStatus.INVESTIGANDO,
    val affectedCount: Int = 0,
    val loggedBy: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isNotifiedToUsers: Boolean = false
)
