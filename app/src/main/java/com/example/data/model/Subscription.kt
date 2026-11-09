package com.example.data.model

enum class PlanTier(
    val title: String,
    val monthlyPriceMT: Double,
    val maxProducts: Int,
    val maxUsers: Int,
    val aiAnalysesLimit: Int,
    val hasAdvancedReports: Boolean,
    val hasWhatsAppAutomation: Boolean
) {
    GRATUITO("Plano Gratuito", 0.0, 50, 1, 5, false, false),
    BASICO("Plano Básico", 1500.0, 300, 3, 25, true, true),
    PROFISSIONAL("Plano Profissional", 3500.0, 5000, 15, 100, true, true)
}

enum class SubscriptionStatus(val label: String) {
    EXPERIMENTAL("Período Experimental"),
    ACTIVA("Subscrição Ativa"),
    PENDENTE_APROVACAO("Comprovativo em Análise"),
    EXPIRADA("Expirada (No Gratuito)"),
    SUSPENSA("Suspensa pela Administração")
}

data class BusinessSubscription(
    val currentPlan: PlanTier = PlanTier.PROFISSIONAL,
    val status: SubscriptionStatus = SubscriptionStatus.EXPERIMENTAL,
    val startDate: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (14L * 86400_000L), // 14 days trial
    val paymentProofReference: String? = null,
    val paymentProofMethod: PaymentMethod? = null,
    val proofSubmittedAt: Long? = null,
    val rejectionReason: String? = null
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() > expiresAt && status != SubscriptionStatus.PENDENTE_APROVACAO

    val daysRemaining: Int
        get() = (((expiresAt - System.currentTimeMillis()) / (86400_000L)).toInt()).coerceAtLeast(0)
}
