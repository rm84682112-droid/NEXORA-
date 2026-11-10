package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "id"], unique = true),
        Index(value = ["businessId", "barcode"])
    ]
)
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val businessId: String,
    val name: String,
    val category: String,
    val description: String = "",
    val unitType: String = "UNIDADE",
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
    val packageName: String = "Caixa",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sales",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "id"], unique = true),
        Index(value = ["businessId", "timestamp"])
    ]
)
data class SaleEntity(
    @PrimaryKey
    val id: String,
    val businessId: String,
    val receiptNumber: String,
    val timestamp: Long,
    val itemsJson: String, // serialized CartItem list
    val subtotal: Double,
    val discount: Double,
    val total: Double,
    val paymentMethod: String,
    val amountPaid: Double,
    val change: Double,
    val customerId: String? = null,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val operatorName: String,
    val paymentReference: String? = null,
    val dueDate: Long? = null,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "clients",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "id"], unique = true),
        Index(value = ["businessId", "phone"])
    ]
)
data class ClientEntity(
    @PrimaryKey
    val id: String,
    val businessId: String,
    val name: String,
    val phone: String,
    val address: String,
    val creditLimit: Double = 5000.0,
    val totalDebt: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "debts",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "id"], unique = true),
        Index(value = ["businessId", "clientId"])
    ]
)
data class DebtEntity(
    @PrimaryKey
    val id: String,
    val businessId: String,
    val saleId: String,
    val receiptNumber: String,
    val clientId: String,
    val clientName: String,
    val clientPhone: String,
    val originalAmount: Double,
    val remainingBalance: Double,
    val createdAt: Long,
    val dueDate: Long,
    val paymentsJson: String = "[]", // serialized DebtPayment list
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "id"], unique = true),
        Index(value = ["businessId", "date"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey
    val id: String,
    val businessId: String,
    val description: String,
    val amount: Double,
    val category: String,
    val paymentMethod: String,
    val date: Long,
    val notes: String = "",
    val recordedBy: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "suppliers",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "id"], unique = true)
    ]
)
data class SupplierEntity(
    @PrimaryKey
    val id: String,
    val businessId: String,
    val name: String,
    val phone: String,
    val address: String,
    val suppliedCategories: String = "Mercearia Geral",
    val totalDebt: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "supplier_purchases",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "id"], unique = true)
    ]
)
data class SupplierPurchaseEntity(
    @PrimaryKey
    val id: String,
    val businessId: String,
    val supplierId: String,
    val supplierName: String,
    val date: Long,
    val itemsJson: String,
    val totalCost: Double,
    val amountPaid: Double,
    val debtRemaining: Double,
    val paymentMethod: String,
    val receiptOrInvoiceNumber: String = "",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "stock_movements",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "id"], unique = true),
        Index(value = ["businessId", "productId"])
    ]
)
data class StockMovementEntity(
    @PrimaryKey
    val id: String,
    val businessId: String,
    val productId: String,
    val productName: String,
    val type: String,
    val quantity: Int,
    val previousStock: Int,
    val newStock: Int,
    val reason: String,
    val operatorName: String,
    val timestamp: Long
)

@Entity(
    tableName = "app_notifications",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "id"], unique = true)
    ]
)
data class AppNotificationEntity(
    @PrimaryKey
    val id: String,
    val businessId: String,
    val title: String,
    val message: String,
    val type: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

@Entity(
    tableName = "sync_queue",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["status"])
    ]
)
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: String,
    val entityType: String, // PRODUCT, SALE, CLIENT, DEBT, EXPENSE, SUPPLIER, PURCHASE, STOCK_MOVEMENT
    val entityId: String,
    val operation: String, // UPSERT, DELETE
    val payloadJson: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attempts: Int = 0,
    val status: String = "PENDING" // PENDING, FAILED
)
