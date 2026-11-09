package com.example.data.model

enum class NotificationType(val label: String, val iconEmoji: String) {
    STOCK_BAIXO("Stock Baixo", "⚠️"),
    ESGOTADO("Artigo Esgotado", "🚨"),
    FIADO_VENCIDO("Fiado Vencido", "⏰"),
    FORNECEDOR_PENDENTE("Dívida a Fornecedor", "📦"),
    PAGAMENTO_RECEBIDO("Pagamento Registado", "💰"),
    IA_ALERTA("Alerta Inteligente IA", "✨"),
    SISTEMA("Aviso Nexora", "🔔")
}

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long,
    val isRead: Boolean = false,
    val actionDeepLink: String? = null
)
