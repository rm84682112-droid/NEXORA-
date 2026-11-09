package com.example.data.model

import java.util.UUID

enum class TenantStatus(val label: String) {
    ATIVA("Ativa"),
    SUSPENSA("Suspensa"),
    BLOQUEADA("Bloqueada")
}

data class BusinessTenant(
    val id: String,
    val name: String,
    val ownerName: String,
    val ownerPhone: String,
    val plan: PlanTier,
    val status: TenantStatus,
    val createdAt: Long,
    val totalProducts: Int = 0,
    val totalSalesCount: Int = 0,
    val totalRevenueMT: Double = 0.0,
    val firstProductAt: Long? = null,
    val firstSaleAt: Long? = null,
    val lastActiveAt: Long = System.currentTimeMillis()
)

enum class PaymentProofStatus(val label: String) {
    PENDENTE("Pendente de Análise"),
    APROVADO("Aprovado"),
    REJEITADO("Rejeitado")
}

data class SubscriptionPaymentProof(
    val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val businessName: String,
    val requestedPlan: PlanTier,
    val amountMT: Double,
    val paymentMethod: PaymentMethod,
    val transactionReference: String,
    val destinationAccount: String,
    val status: PaymentProofStatus = PaymentProofStatus.PENDENTE,
    val submittedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,
    val rejectionReason: String? = null
)

enum class TicketStatus(val label: String) {
    ABERTO("Aberto"),
    EM_ANALISE("Em Análise"),
    RESOLVIDO("Resolvido")
}

data class SupportTicket(
    val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val businessName: String,
    val contactPhone: String,
    val subject: String,
    val message: String,
    val status: TicketStatus = TicketStatus.ABERTO,
    val createdAt: Long = System.currentTimeMillis(),
    val replyText: String? = null
)

data class PlanPricingConfig(
    val plan: PlanTier,
    val priceMT: Double,
    val maxProducts: Int,
    val maxUsers: Int,
    val aiAnalysesPerMonth: Int,
    val isActive: Boolean = true
)

data class PlatformPaymentAccount(
    val id: String = UUID.randomUUID().toString(),
    val method: PaymentMethod,
    val accountNumber: String,
    val accountHolder: String,
    val isPrimary: Boolean = true
)
