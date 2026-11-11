package com.example.data.model

import java.util.UUID

enum class AuditAction(val label: String) {
    CREATE_PRODUCT("Criar Produto"),
    UPDATE_PRODUCT("Actualizar Produto"),
    DELETE_PRODUCT("Eliminar Produto"),
    STOCK_ADJUSTMENT("Ajuste de Stock"),
    PRICE_CHANGE("Alteração de Preço"),
    PURCHASE_STOCK("Entrada de Compra Fornecedor"),
    APPROVE_PAYMENT("Aprovação de Pagamento"),
    REJECT_PAYMENT("Rejeição de Pagamento"),
    CHANGE_PERMISSIONS("Alteração de Permissões"),
    EXPORT_BACKUP("Exportação de Cópia de Segurança"),
    RESTORE_BACKUP("Restauração de Dados"),
    SUSPEND_STORE("Suspensão de Empresa"),
    REACTIVATE_STORE("Reativação de Empresa"),
    DATA_ACCESS("Acesso a Dados Pessoais"),
    UPDATE_PRIVACY("Alteração de Preferências de Privacidade"),
    ERASE_ACCOUNT("Apagamento de Conta (Esquecimento)"),
    LOG_DATA_INCIDENT("Registo de Incidente de Dados"),
    CANCEL_SALE("Cancelamento de Venda")
}

data class AuditLog(
    val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val userId: String,
    val userName: String,
    val userRole: String,
    val action: AuditAction,
    val targetItem: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
