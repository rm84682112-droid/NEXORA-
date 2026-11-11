package com.example.data.sync

import android.content.Context
import android.util.Log
import com.example.data.local.NexoraDatabase
import com.example.data.local.converters.RoomMappers.toDomain
import com.example.data.local.converters.RoomMappers.toEntity
import com.example.data.local.entities.SyncQueueEntity
import com.example.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

class SyncManager(
    private val db: NexoraDatabase,
    private val firestore: FirebaseFirestore? = null,
    private val context: Context? = null
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(0L)
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val activeListeners = mutableListOf<ListenerRegistration>()

    private fun getFirestoreInstance(): FirebaseFirestore? {
        if (firestore != null) return firestore
        if (context != null) {
            return try {
                val dbId = context.getString(com.example.R.string.firestore_database_id)
                FirebaseFirestore.getInstance(dbId)
            } catch (e: Exception) {
                Log.w("SyncManager", "FirebaseFirestore with dbId unavailable: ${e.message}")
                null
            }
        }
        return try {
            val dbId = "ai-studio-android-nexora-fbbcaacc-ed44-4093-bc01-cd89427354fe"
            FirebaseFirestore.getInstance(dbId)
        } catch (e: Exception) {
            Log.w("SyncManager", "FirebaseFirestore unavailable: ${e.message}")
            null
        }
    }

    fun stopCloudListeners() {
        activeListeners.forEach { it.remove() }
        activeListeners.clear()
    }

    /**
     * Drains pending mutations in sync_queue to Firestore
     */
    suspend fun drainQueue(businessId: String) = withContext(Dispatchers.IO) {
        if (businessId.isBlank()) return@withContext
        val fs = getFirestoreInstance() ?: return@withContext
        val pending = db.syncQueueDao().getPendingItems(businessId, limit = 100)
        if (pending.isEmpty()) return@withContext

        _isSyncing.value = true
        val bDoc = fs.collection("businesses").document(businessId)

        for (item in pending) {
            try {
                when (item.entityType) {
                    "PRODUCT" -> {
                        if (item.operation == "DELETE") {
                            bDoc.collection("products").document(item.entityId).delete().await()
                        } else {
                            val prodEntity = db.productDao().getProductById(businessId, item.entityId)
                            if (prodEntity != null) {
                                val map = productToMap(prodEntity.toDomain())
                                bDoc.collection("products").document(item.entityId).set(map, SetOptions.merge()).await()
                            }
                        }
                    }
                    "SALE" -> {
                        val saleEntity = db.saleDao().getSales(businessId).find { it.id == item.entityId }
                        if (saleEntity != null) {
                            val map = saleToMap(saleEntity.toDomain())
                            bDoc.collection("sales").document(item.entityId).set(map, SetOptions.merge()).await()
                        }
                    }
                    "CLIENT" -> {
                        if (item.operation == "DELETE") {
                            bDoc.collection("clients").document(item.entityId).delete().await()
                        } else {
                            val clientEntity = db.clientDao().getClientById(businessId, item.entityId)
                            if (clientEntity != null) {
                                val map = clientToMap(clientEntity.toDomain())
                                bDoc.collection("clients").document(item.entityId).set(map, SetOptions.merge()).await()
                            }
                        }
                    }
                    "DEBT" -> {
                        val debtEntity = db.debtDao().getDebtById(businessId, item.entityId)
                        if (debtEntity != null) {
                            val map = debtToMap(debtEntity.toDomain())
                            bDoc.collection("debts").document(item.entityId).set(map, SetOptions.merge()).await()
                        }
                    }
                    "EXPENSE" -> {
                        if (item.operation == "DELETE") {
                            bDoc.collection("expenses").document(item.entityId).delete().await()
                        } else {
                            val expenseEntity = db.expenseDao().getExpenses(businessId).find { it.id == item.entityId }
                            if (expenseEntity != null) {
                                val map = expenseToMap(expenseEntity.toDomain())
                                bDoc.collection("expenses").document(item.entityId).set(map, SetOptions.merge()).await()
                            }
                        }
                    }
                    "SUPPLIER" -> {
                        val supEntity = db.supplierDao().getSuppliers(businessId).find { it.id == item.entityId }
                        if (supEntity != null) {
                            val map = supplierToMap(supEntity.toDomain())
                            bDoc.collection("suppliers").document(item.entityId).set(map, SetOptions.merge()).await()
                        }
                    }
                    "PURCHASE" -> {
                        val purEntity = db.supplierDao().getPurchases(businessId).find { it.id == item.entityId }
                        if (purEntity != null) {
                            val map = purchaseToMap(purEntity.toDomain())
                            bDoc.collection("purchases").document(item.entityId).set(map, SetOptions.merge()).await()
                        }
                    }
                    "STOCK_MOVEMENT" -> {
                        val movEntity = db.stockMovementDao().getMovements(businessId).find { it.id == item.entityId }
                        if (movEntity != null) {
                            val map = movementToMap(movEntity.toDomain())
                            bDoc.collection("movements").document(item.entityId).set(map, SetOptions.merge()).await()
                        }
                    }
                }
                // Successfully synced
                db.syncQueueDao().delete(item.id)
            } catch (e: Exception) {
                Log.w("SyncManager", "Failed to sync item ${item.entityId}: ${e.message}")
                db.syncQueueDao().updateStatus(item.id, item.attempts + 1, "FAILED")
            }
        }
        _lastSyncTimestamp.value = System.currentTimeMillis()
        _isSyncing.value = false
    }

    /**
     * Pulls full cloud state from Firestore into Room for initial setup, account switch, or recovery
     */
    suspend fun syncFromCloud(businessId: String) = withContext(Dispatchers.IO) {
        if (businessId.isBlank()) return@withContext
        val fs = getFirestoreInstance() ?: return@withContext
        _isSyncing.value = true

        try {
            val bDoc = fs.collection("businesses").document(businessId)

            // 1. Products
            val prodSnaps = bDoc.collection("products").get().await()
            val prodEntities = prodSnaps.documents.mapNotNull { doc ->
                try {
                    val p = mapToProduct(doc.id, doc.data ?: emptyMap())
                    p.toEntity(businessId)
                } catch (e: Exception) { null }
            }
            if (prodEntities.isNotEmpty()) {
                db.productDao().insertAll(prodEntities)
            }

            // 2. Sales
            val saleSnaps = bDoc.collection("sales").get().await()
            val saleEntities = saleSnaps.documents.mapNotNull { doc ->
                try {
                    val s = mapToSale(doc.id, doc.data ?: emptyMap())
                    s.toEntity(businessId)
                } catch (e: Exception) { null }
            }
            if (saleEntities.isNotEmpty()) {
                db.saleDao().insertAll(saleEntities)
            }

            // 3. Clients
            val clientSnaps = bDoc.collection("clients").get().await()
            val clientEntities = clientSnaps.documents.mapNotNull { doc ->
                try {
                    val c = mapToClient(doc.id, doc.data ?: emptyMap())
                    c.toEntity(businessId)
                } catch (e: Exception) { null }
            }
            if (clientEntities.isNotEmpty()) {
                db.clientDao().insertAll(clientEntities)
            }

            // 4. Debts
            val debtSnaps = bDoc.collection("debts").get().await()
            val debtEntities = debtSnaps.documents.mapNotNull { doc ->
                try {
                    val d = mapToDebt(doc.id, doc.data ?: emptyMap())
                    d.toEntity(businessId)
                } catch (e: Exception) { null }
            }
            if (debtEntities.isNotEmpty()) {
                db.debtDao().insertAll(debtEntities)
            }

            // 5. Expenses
            val expSnaps = bDoc.collection("expenses").get().await()
            val expEntities = expSnaps.documents.mapNotNull { doc ->
                try {
                    val exp = mapToExpense(doc.id, doc.data ?: emptyMap())
                    exp.toEntity(businessId)
                } catch (e: Exception) { null }
            }
            if (expEntities.isNotEmpty()) {
                db.expenseDao().insertAll(expEntities)
            }

            // 6. Suppliers
            val supSnaps = bDoc.collection("suppliers").get().await()
            val supEntities = supSnaps.documents.mapNotNull { doc ->
                try {
                    val sup = mapToSupplier(doc.id, doc.data ?: emptyMap())
                    sup.toEntity(businessId)
                } catch (e: Exception) { null }
            }
            if (supEntities.isNotEmpty()) {
                db.supplierDao().insertAll(supEntities)
            }

            // 7. Supplier Purchases
            val purSnaps = bDoc.collection("purchases").get().await()
            val purEntities = purSnaps.documents.mapNotNull { doc ->
                try {
                    val pur = mapToPurchase(doc.id, doc.data ?: emptyMap())
                    pur.toEntity(businessId)
                } catch (e: Exception) { null }
            }
            if (purEntities.isNotEmpty()) {
                db.supplierDao().insertAllPurchases(purEntities)
            }

            // 8. Stock Movements
            val movSnaps = bDoc.collection("movements").get().await()
            val movEntities = movSnaps.documents.mapNotNull { doc ->
                try {
                    val m = mapToMovement(doc.id, doc.data ?: emptyMap())
                    m.toEntity(businessId)
                } catch (e: Exception) { null }
            }
            if (movEntities.isNotEmpty()) {
                db.stockMovementDao().insertAll(movEntities)
            }

            _lastSyncTimestamp.value = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.w("SyncManager", "Cloud sync pull note: ${e.message}")
        } finally {
            _isSyncing.value = false
        }
    }

    /**
     * Start live real-time listeners for multi-device sync
     */
    fun startRealtimeListeners(businessId: String) {
        if (businessId.isBlank()) return
        val fs = getFirestoreInstance() ?: return
        stopCloudListeners()

        val bDoc = fs.collection("businesses").document(businessId)

        // Products listener
        val prodReg = bDoc.collection("products").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            scope.launch {
                val entities = snapshot.documents.mapNotNull { doc ->
                    try {
                        mapToProduct(doc.id, doc.data ?: emptyMap()).toEntity(businessId)
                    } catch (e: Exception) { null }
                }
                if (entities.isNotEmpty()) {
                    db.productDao().insertAll(entities)
                }
            }
        }
        activeListeners.add(prodReg)

        // Sales listener
        val saleReg = bDoc.collection("sales").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            scope.launch {
                val entities = snapshot.documents.mapNotNull { doc ->
                    try {
                        mapToSale(doc.id, doc.data ?: emptyMap()).toEntity(businessId)
                    } catch (e: Exception) { null }
                }
                if (entities.isNotEmpty()) {
                    db.saleDao().insertAll(entities)
                }
            }
        }
        activeListeners.add(saleReg)
    }

    // --- Helpers to serialize/deserialize Firestore documents ---

    private fun productToMap(p: Product): Map<String, Any?> = mapOf(
        "id" to p.id,
        "name" to p.name,
        "category" to p.category,
        "description" to p.description,
        "unitType" to p.unitType.name,
        "unitsPerPackage" to p.unitsPerPackage,
        "costPrice" to p.costPrice,
        "sellingPrice" to p.sellingPrice,
        "currentStock" to p.currentStock,
        "minStock" to p.minStock,
        "barcode" to p.barcode,
        "iconEmoji" to p.iconEmoji,
        "isActive" to p.isActive,
        "supplierName" to p.supplierName,
        "packageEnabled" to p.packageEnabled,
        "packagePrice" to p.packagePrice,
        "packageName" to p.packageName,
        "updatedAt" to System.currentTimeMillis()
    )

    private fun mapToProduct(id: String, map: Map<String, Any>): Product {
        return Product(
            id = id,
            name = (map["name"] as? String) ?: "Produto",
            category = (map["category"] as? String) ?: "Geral",
            description = (map["description"] as? String) ?: "",
            unitType = try { UnitType.valueOf((map["unitType"] as? String) ?: "UNIDADE") } catch (e: Exception) { UnitType.UNIDADE },
            unitsPerPackage = ((map["unitsPerPackage"] as? Number)?.toInt()) ?: 1,
            costPrice = ((map["costPrice"] as? Number)?.toDouble()) ?: 0.0,
            sellingPrice = ((map["sellingPrice"] as? Number)?.toDouble()) ?: 0.0,
            currentStock = ((map["currentStock"] as? Number)?.toInt()) ?: 0,
            minStock = ((map["minStock"] as? Number)?.toInt()) ?: 5,
            barcode = (map["barcode"] as? String) ?: "",
            iconEmoji = (map["iconEmoji"] as? String) ?: "📦",
            isActive = (map["isActive"] as? Boolean) ?: true,
            supplierName = (map["supplierName"] as? String) ?: "",
            packageEnabled = (map["packageEnabled"] as? Boolean) ?: false,
            packagePrice = ((map["packagePrice"] as? Number)?.toDouble()) ?: 0.0,
            packageName = (map["packageName"] as? String) ?: "Caixa"
        )
    }

    private fun saleToMap(s: Sale): Map<String, Any?> = mapOf(
        "id" to s.id,
        "receiptNumber" to s.receiptNumber,
        "timestamp" to s.timestamp,
        "subtotal" to s.subtotal,
        "discount" to s.discount,
        "total" to s.total,
        "paymentMethod" to s.paymentMethod.name,
        "amountPaid" to s.amountPaid,
        "change" to s.change,
        "customerId" to s.customerId,
        "customerName" to s.customerName,
        "customerPhone" to s.customerPhone,
        "operatorName" to s.operatorName,
        "paymentReference" to s.paymentReference,
        "dueDate" to s.dueDate,
        "notes" to s.notes,
        "items" to s.items.map { item ->
            mapOf(
                "productId" to item.product.id,
                "productName" to item.product.name,
                "sellingPrice" to item.product.sellingPrice,
                "costPrice" to item.product.costPrice,
                "unitType" to item.product.unitType.name,
                "quantity" to item.quantity,
                "customPrice" to item.customPrice
            )
        }
    )

    private fun mapToSale(id: String, map: Map<String, Any>): Sale {
        val rawItems = (map["items"] as? List<Map<String, Any>>) ?: emptyList()
        val cartItems = rawItems.map { iMap ->
            val p = Product(
                id = (iMap["productId"] as? String) ?: "prod-unknown",
                name = (iMap["productName"] as? String) ?: "Produto",
                category = "Geral",
                sellingPrice = ((iMap["sellingPrice"] as? Number)?.toDouble()) ?: 0.0,
                costPrice = ((iMap["costPrice"] as? Number)?.toDouble()) ?: 0.0,
                unitType = try { UnitType.valueOf((iMap["unitType"] as? String) ?: "UNIDADE") } catch (e: Exception) { UnitType.UNIDADE },
                currentStock = 0
            )
            CartItem(
                product = p,
                quantity = ((iMap["quantity"] as? Number)?.toInt()) ?: 1,
                customPrice = (iMap["customPrice"] as? Number)?.toDouble()
            )
        }
        return Sale(
            id = id,
            receiptNumber = (map["receiptNumber"] as? String) ?: "REC-000",
            timestamp = ((map["timestamp"] as? Number)?.toLong()) ?: System.currentTimeMillis(),
            items = cartItems,
            subtotal = ((map["subtotal"] as? Number)?.toDouble()) ?: 0.0,
            discount = ((map["discount"] as? Number)?.toDouble()) ?: 0.0,
            total = ((map["total"] as? Number)?.toDouble()) ?: 0.0,
            paymentMethod = try { PaymentMethod.valueOf((map["paymentMethod"] as? String) ?: "DINHEIRO") } catch (e: Exception) { PaymentMethod.DINHEIRO },
            amountPaid = ((map["amountPaid"] as? Number)?.toDouble()) ?: 0.0,
            change = ((map["change"] as? Number)?.toDouble()) ?: 0.0,
            customerId = map["customerId"] as? String,
            customerName = map["customerName"] as? String,
            customerPhone = map["customerPhone"] as? String,
            operatorName = (map["operatorName"] as? String) ?: "Operador",
            paymentReference = map["paymentReference"] as? String,
            dueDate = (map["dueDate"] as? Number)?.toLong(),
            notes = (map["notes"] as? String) ?: ""
        )
    }

    private fun clientToMap(c: Client): Map<String, Any?> = mapOf(
        "id" to c.id,
        "name" to c.name,
        "phone" to c.phone,
        "address" to c.address,
        "creditLimit" to c.creditLimit,
        "totalDebt" to c.totalDebt,
        "notes" to c.notes,
        "createdAt" to c.createdAt
    )

    private fun mapToClient(id: String, map: Map<String, Any>): Client = Client(
        id = id,
        name = (map["name"] as? String) ?: "Cliente",
        phone = (map["phone"] as? String) ?: "",
        address = (map["address"] as? String) ?: "",
        creditLimit = ((map["creditLimit"] as? Number)?.toDouble()) ?: 5000.0,
        totalDebt = ((map["totalDebt"] as? Number)?.toDouble()) ?: 0.0,
        notes = (map["notes"] as? String) ?: "",
        createdAt = ((map["createdAt"] as? Number)?.toLong()) ?: System.currentTimeMillis()
    )

    private fun debtToMap(d: DebtRecord): Map<String, Any?> = mapOf(
        "id" to d.id,
        "saleId" to d.saleId,
        "receiptNumber" to d.receiptNumber,
        "clientId" to d.clientId,
        "clientName" to d.clientName,
        "clientPhone" to d.clientPhone,
        "originalAmount" to d.originalAmount,
        "remainingBalance" to d.remainingBalance,
        "createdAt" to d.createdAt,
        "dueDate" to d.dueDate,
        "payments" to d.payments.map { p ->
            mapOf(
                "id" to p.id,
                "date" to p.date,
                "amount" to p.amount,
                "paymentMethod" to p.paymentMethod.name,
                "notes" to p.notes
            )
        }
    )

    private fun mapToDebt(id: String, map: Map<String, Any>): DebtRecord {
        val rawPayments = (map["payments"] as? List<Map<String, Any>>) ?: emptyList()
        val payments = rawPayments.map { pMap ->
            DebtPayment(
                id = (pMap["id"] as? String) ?: "",
                date = ((pMap["date"] as? Number)?.toLong()) ?: System.currentTimeMillis(),
                amount = ((pMap["amount"] as? Number)?.toDouble()) ?: 0.0,
                paymentMethod = try { PaymentMethod.valueOf((pMap["paymentMethod"] as? String) ?: "DINHEIRO") } catch (e: Exception) { PaymentMethod.DINHEIRO },
                notes = (pMap["notes"] as? String) ?: ""
            )
        }
        return DebtRecord(
            id = id,
            saleId = (map["saleId"] as? String) ?: "",
            receiptNumber = (map["receiptNumber"] as? String) ?: "",
            clientId = (map["clientId"] as? String) ?: "",
            clientName = (map["clientName"] as? String) ?: "",
            clientPhone = (map["clientPhone"] as? String) ?: "",
            originalAmount = ((map["originalAmount"] as? Number)?.toDouble()) ?: 0.0,
            remainingBalance = ((map["remainingBalance"] as? Number)?.toDouble()) ?: 0.0,
            createdAt = ((map["createdAt"] as? Number)?.toLong()) ?: System.currentTimeMillis(),
            dueDate = ((map["dueDate"] as? Number)?.toLong()) ?: System.currentTimeMillis(),
            payments = payments
        )
    }

    private fun expenseToMap(e: Expense): Map<String, Any?> = mapOf(
        "id" to e.id,
        "description" to e.description,
        "amount" to e.amount,
        "category" to e.category.name,
        "paymentMethod" to e.paymentMethod.name,
        "date" to e.date,
        "notes" to e.notes,
        "recordedBy" to e.recordedBy
    )

    private fun mapToExpense(id: String, map: Map<String, Any>): Expense = Expense(
        id = id,
        description = (map["description"] as? String) ?: "Despesa",
        amount = ((map["amount"] as? Number)?.toDouble()) ?: 0.0,
        category = try { ExpenseCategory.valueOf((map["category"] as? String) ?: "OUTRAS") } catch (e: Exception) { ExpenseCategory.OUTRAS },
        paymentMethod = try { PaymentMethod.valueOf((map["paymentMethod"] as? String) ?: "DINHEIRO") } catch (e: Exception) { PaymentMethod.DINHEIRO },
        date = ((map["date"] as? Number)?.toLong()) ?: System.currentTimeMillis(),
        notes = (map["notes"] as? String) ?: "",
        recordedBy = (map["recordedBy"] as? String) ?: ""
    )

    private fun supplierToMap(s: Supplier): Map<String, Any?> = mapOf(
        "id" to s.id,
        "name" to s.name,
        "phone" to s.phone,
        "address" to s.address,
        "suppliedCategories" to s.suppliedCategories,
        "totalDebt" to s.totalDebt,
        "notes" to s.notes,
        "createdAt" to s.createdAt
    )

    private fun mapToSupplier(id: String, map: Map<String, Any>): Supplier = Supplier(
        id = id,
        name = (map["name"] as? String) ?: "Fornecedor",
        phone = (map["phone"] as? String) ?: "",
        address = (map["address"] as? String) ?: "",
        suppliedCategories = (map["suppliedCategories"] as? String) ?: "Mercearia Geral",
        totalDebt = ((map["totalDebt"] as? Number)?.toDouble()) ?: 0.0,
        notes = (map["notes"] as? String) ?: "",
        createdAt = ((map["createdAt"] as? Number)?.toLong()) ?: System.currentTimeMillis()
    )

    private fun purchaseToMap(p: SupplierPurchase): Map<String, Any?> = mapOf(
        "id" to p.id,
        "supplierId" to p.supplierId,
        "supplierName" to p.supplierName,
        "date" to p.date,
        "totalCost" to p.totalCost,
        "amountPaid" to p.amountPaid,
        "debtRemaining" to p.debtRemaining,
        "paymentMethod" to p.paymentMethod.name,
        "receiptOrInvoiceNumber" to p.receiptOrInvoiceNumber,
        "notes" to p.notes,
        "items" to p.items.map { mapOf("productId" to it.productId, "productName" to it.productName, "quantity" to it.quantity, "unitCost" to it.unitCost) }
    )

    private fun mapToPurchase(id: String, map: Map<String, Any>): SupplierPurchase {
        val rawItems = (map["items"] as? List<Map<String, Any>>) ?: emptyList()
        val items = rawItems.map { iMap ->
            PurchaseItem(
                productId = (iMap["productId"] as? String) ?: "",
                productName = (iMap["productName"] as? String) ?: "",
                quantity = ((iMap["quantity"] as? Number)?.toInt()) ?: 1,
                unitCost = ((iMap["unitCost"] as? Number)?.toDouble()) ?: 0.0
            )
        }
        return SupplierPurchase(
            id = id,
            supplierId = (map["supplierId"] as? String) ?: "",
            supplierName = (map["supplierName"] as? String) ?: "",
            date = ((map["date"] as? Number)?.toLong()) ?: System.currentTimeMillis(),
            totalCost = ((map["totalCost"] as? Number)?.toDouble()) ?: 0.0,
            amountPaid = ((map["amountPaid"] as? Number)?.toDouble()) ?: 0.0,
            debtRemaining = ((map["debtRemaining"] as? Number)?.toDouble()) ?: 0.0,
            paymentMethod = try { PaymentMethod.valueOf((map["paymentMethod"] as? String) ?: "DINHEIRO") } catch (e: Exception) { PaymentMethod.DINHEIRO },
            receiptOrInvoiceNumber = (map["receiptOrInvoiceNumber"] as? String) ?: "",
            notes = (map["notes"] as? String) ?: "",
            items = items
        )
    }

    private fun movementToMap(m: StockMovement): Map<String, Any?> = mapOf(
        "id" to m.id,
        "productId" to m.productId,
        "productName" to m.productName,
        "type" to m.type.name,
        "quantity" to m.quantity,
        "previousStock" to m.previousStock,
        "newStock" to m.newStock,
        "reason" to m.reason,
        "operatorName" to m.operatorName,
        "timestamp" to m.timestamp
    )

    private fun mapToMovement(id: String, map: Map<String, Any>): StockMovement = StockMovement(
        id = id,
        productId = (map["productId"] as? String) ?: "",
        productName = (map["productName"] as? String) ?: "",
        type = try { MovementType.valueOf((map["type"] as? String) ?: "ENTRADA") } catch (e: Exception) { MovementType.ENTRADA },
        quantity = ((map["quantity"] as? Number)?.toInt()) ?: 0,
        previousStock = ((map["previousStock"] as? Number)?.toInt()) ?: 0,
        newStock = ((map["newStock"] as? Number)?.toInt()) ?: 0,
        reason = (map["reason"] as? String) ?: "",
        operatorName = (map["operatorName"] as? String) ?: "",
        timestamp = ((map["timestamp"] as? Number)?.toLong()) ?: System.currentTimeMillis()
    )
}
