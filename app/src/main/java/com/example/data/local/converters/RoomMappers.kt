package com.example.data.local.converters

import com.example.data.local.entities.*
import com.example.data.model.*
import org.json.JSONArray
import org.json.JSONObject

object RoomMappers {

    // --- Product Mapping ---
    fun ProductEntity.toDomain(): Product {
        return Product(
            id = id,
            name = name,
            category = category,
            description = description,
            unitType = try { UnitType.valueOf(unitType) } catch (e: Exception) { UnitType.UNIDADE },
            unitsPerPackage = unitsPerPackage,
            costPrice = costPrice,
            sellingPrice = sellingPrice,
            currentStock = currentStock,
            minStock = minStock,
            barcode = barcode,
            iconEmoji = iconEmoji,
            isActive = isActive,
            supplierName = supplierName,
            packageEnabled = packageEnabled,
            packagePrice = packagePrice,
            packageName = packageName
        )
    }

    fun Product.toEntity(businessId: String): ProductEntity {
        return ProductEntity(
            id = id,
            businessId = businessId,
            name = name,
            category = category,
            description = description,
            unitType = unitType.name,
            unitsPerPackage = unitsPerPackage,
            costPrice = costPrice,
            sellingPrice = sellingPrice,
            currentStock = currentStock,
            minStock = minStock,
            barcode = barcode,
            iconEmoji = iconEmoji,
            isActive = isActive,
            supplierName = supplierName,
            packageEnabled = packageEnabled,
            packagePrice = packagePrice,
            packageName = packageName,
            updatedAt = System.currentTimeMillis()
        )
    }

    // --- CartItems JSON serialization ---
    fun serializeCartItems(items: List<CartItem>): String {
        val array = JSONArray()
        items.forEach { item ->
            val obj = JSONObject()
            obj.put("productId", item.product.id)
            obj.put("productName", item.product.name)
            obj.put("productCategory", item.product.category)
            obj.put("sellingPrice", item.product.sellingPrice)
            obj.put("costPrice", item.product.costPrice)
            obj.put("unitType", item.product.unitType.name)
            obj.put("iconEmoji", item.product.iconEmoji)
            obj.put("quantity", item.quantity)
            if (item.customPrice != null) {
                obj.put("customPrice", item.customPrice)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeCartItems(json: String): List<CartItem> {
        val list = mutableListOf<CartItem>()
        if (json.isBlank()) return list
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val pId = obj.optString("productId", "prod-unknown")
                val pName = obj.optString("productName", "Produto")
                val pCat = obj.optString("productCategory", "Geral")
                val sPrice = obj.optDouble("sellingPrice", 0.0)
                val cPrice = obj.optDouble("costPrice", 0.0)
                val uTypeStr = obj.optString("unitType", "UNIDADE")
                val uType = try { UnitType.valueOf(uTypeStr) } catch (e: Exception) { UnitType.UNIDADE }
                val emoji = obj.optString("iconEmoji", "📦")
                val qty = obj.optInt("quantity", 1)
                val customPrice = if (obj.has("customPrice")) obj.getDouble("customPrice") else null

                val dummyProd = Product(
                    id = pId,
                    name = pName,
                    category = pCat,
                    sellingPrice = sPrice,
                    costPrice = cPrice,
                    unitType = uType,
                    iconEmoji = emoji,
                    currentStock = 0
                )
                list.add(CartItem(product = dummyProd, quantity = qty, customPrice = customPrice))
            }
        } catch (e: Exception) {
            // Safe fallback
        }
        return list
    }

    // --- Sale Mapping ---
    fun SaleEntity.toDomain(): Sale {
        return Sale(
            id = id,
            receiptNumber = receiptNumber,
            timestamp = timestamp,
            items = deserializeCartItems(itemsJson),
            subtotal = subtotal,
            discount = discount,
            total = total,
            paymentMethod = try { PaymentMethod.valueOf(paymentMethod) } catch (e: Exception) { PaymentMethod.DINHEIRO },
            amountPaid = amountPaid,
            change = change,
            customerId = customerId,
            customerName = customerName,
            customerPhone = customerPhone,
            operatorName = operatorName,
            paymentReference = paymentReference,
            dueDate = dueDate,
            notes = notes
        )
    }

    fun Sale.toEntity(businessId: String): SaleEntity {
        return SaleEntity(
            id = id,
            businessId = businessId,
            receiptNumber = receiptNumber,
            timestamp = timestamp,
            itemsJson = serializeCartItems(items),
            subtotal = subtotal,
            discount = discount,
            total = total,
            paymentMethod = paymentMethod.name,
            amountPaid = amountPaid,
            change = change,
            customerId = customerId,
            customerName = customerName,
            customerPhone = customerPhone,
            operatorName = operatorName,
            paymentReference = paymentReference,
            dueDate = dueDate,
            notes = notes,
            updatedAt = System.currentTimeMillis()
        )
    }

    // --- Client Mapping ---
    fun ClientEntity.toDomain(): Client {
        return Client(
            id = id,
            name = name,
            phone = phone,
            address = address,
            creditLimit = creditLimit,
            totalDebt = totalDebt,
            notes = notes,
            createdAt = createdAt
        )
    }

    fun Client.toEntity(businessId: String): ClientEntity {
        return ClientEntity(
            id = id,
            businessId = businessId,
            name = name,
            phone = phone,
            address = address,
            creditLimit = creditLimit,
            totalDebt = totalDebt,
            notes = notes,
            createdAt = createdAt,
            updatedAt = System.currentTimeMillis()
        )
    }

    // --- Debt Payments JSON serialization ---
    fun serializeDebtPayments(payments: List<DebtPayment>): String {
        val array = JSONArray()
        payments.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("date", p.date)
            obj.put("amount", p.amount)
            obj.put("paymentMethod", p.paymentMethod.name)
            obj.put("notes", p.notes)
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeDebtPayments(json: String): List<DebtPayment> {
        val list = mutableListOf<DebtPayment>()
        if (json.isBlank()) return list
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val pId = obj.optString("id", "")
                val pDate = obj.optLong("date", System.currentTimeMillis())
                val pAmount = obj.optDouble("amount", 0.0)
                val methodStr = obj.optString("paymentMethod", "DINHEIRO")
                val method = try { PaymentMethod.valueOf(methodStr) } catch (e: Exception) { PaymentMethod.DINHEIRO }
                val pNotes = obj.optString("notes", "")
                list.add(DebtPayment(id = pId, date = pDate, amount = pAmount, paymentMethod = method, notes = pNotes))
            }
        } catch (e: Exception) {
            // fallback
        }
        return list
    }

    // --- Debt Mapping ---
    fun DebtEntity.toDomain(): DebtRecord {
        return DebtRecord(
            id = id,
            saleId = saleId,
            receiptNumber = receiptNumber,
            clientId = clientId,
            clientName = clientName,
            clientPhone = clientPhone,
            originalAmount = originalAmount,
            remainingBalance = remainingBalance,
            createdAt = createdAt,
            dueDate = dueDate,
            payments = deserializeDebtPayments(paymentsJson)
        )
    }

    fun DebtRecord.toEntity(businessId: String): DebtEntity {
        return DebtEntity(
            id = id,
            businessId = businessId,
            saleId = saleId,
            receiptNumber = receiptNumber,
            clientId = clientId,
            clientName = clientName,
            clientPhone = clientPhone,
            originalAmount = originalAmount,
            remainingBalance = remainingBalance,
            createdAt = createdAt,
            dueDate = dueDate,
            paymentsJson = serializeDebtPayments(payments),
            updatedAt = System.currentTimeMillis()
        )
    }

    // --- Expense Mapping ---
    fun ExpenseEntity.toDomain(): Expense {
        return Expense(
            id = id,
            description = description,
            amount = amount,
            category = try { ExpenseCategory.valueOf(category) } catch (e: Exception) { ExpenseCategory.OUTRAS },
            paymentMethod = try { PaymentMethod.valueOf(paymentMethod) } catch (e: Exception) { PaymentMethod.DINHEIRO },
            date = date,
            notes = notes,
            recordedBy = recordedBy
        )
    }

    fun Expense.toEntity(businessId: String): ExpenseEntity {
        return ExpenseEntity(
            id = id,
            businessId = businessId,
            description = description,
            amount = amount,
            category = category.name,
            paymentMethod = paymentMethod.name,
            date = date,
            notes = notes,
            recordedBy = recordedBy,
            updatedAt = System.currentTimeMillis()
        )
    }

    // --- Supplier Mapping ---
    fun SupplierEntity.toDomain(): Supplier {
        return Supplier(
            id = id,
            name = name,
            phone = phone,
            address = address,
            suppliedCategories = suppliedCategories,
            totalDebt = totalDebt,
            notes = notes,
            createdAt = createdAt
        )
    }

    fun Supplier.toEntity(businessId: String): SupplierEntity {
        return SupplierEntity(
            id = id,
            businessId = businessId,
            name = name,
            phone = phone,
            address = address,
            suppliedCategories = suppliedCategories,
            totalDebt = totalDebt,
            notes = notes,
            createdAt = createdAt,
            updatedAt = System.currentTimeMillis()
        )
    }

    // --- Purchase Items Serialization ---
    fun serializePurchaseItems(items: List<PurchaseItem>): String {
        val array = JSONArray()
        items.forEach { item ->
            val obj = JSONObject()
            obj.put("productId", item.productId)
            obj.put("productName", item.productName)
            obj.put("quantity", item.quantity)
            obj.put("unitCost", item.unitCost)
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializePurchaseItems(json: String): List<PurchaseItem> {
        val list = mutableListOf<PurchaseItem>()
        if (json.isBlank()) return list
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    PurchaseItem(
                        productId = obj.optString("productId", ""),
                        productName = obj.optString("productName", ""),
                        quantity = obj.optInt("quantity", 1),
                        unitCost = obj.optDouble("unitCost", 0.0)
                    )
                )
            }
        } catch (e: Exception) {
            // fallback
        }
        return list
    }

    // --- SupplierPurchase Mapping ---
    fun SupplierPurchaseEntity.toDomain(): SupplierPurchase {
        return SupplierPurchase(
            id = id,
            supplierId = supplierId,
            supplierName = supplierName,
            date = date,
            items = deserializePurchaseItems(itemsJson),
            totalCost = totalCost,
            amountPaid = amountPaid,
            debtRemaining = debtRemaining,
            paymentMethod = try { PaymentMethod.valueOf(paymentMethod) } catch (e: Exception) { PaymentMethod.DINHEIRO },
            receiptOrInvoiceNumber = receiptOrInvoiceNumber,
            notes = notes
        )
    }

    fun SupplierPurchase.toEntity(businessId: String): SupplierPurchaseEntity {
        return SupplierPurchaseEntity(
            id = id,
            businessId = businessId,
            supplierId = supplierId,
            supplierName = supplierName,
            date = date,
            itemsJson = serializePurchaseItems(items),
            totalCost = totalCost,
            amountPaid = amountPaid,
            debtRemaining = debtRemaining,
            paymentMethod = paymentMethod.name,
            receiptOrInvoiceNumber = receiptOrInvoiceNumber,
            notes = notes,
            updatedAt = System.currentTimeMillis()
        )
    }

    // --- StockMovement Mapping ---
    fun StockMovementEntity.toDomain(): StockMovement {
        return StockMovement(
            id = id,
            productId = productId,
            productName = productName,
            type = try { MovementType.valueOf(type) } catch (e: Exception) { MovementType.ENTRADA },
            quantity = quantity,
            previousStock = previousStock,
            newStock = newStock,
            reason = reason,
            operatorName = operatorName,
            timestamp = timestamp
        )
    }

    fun StockMovement.toEntity(businessId: String): StockMovementEntity {
        return StockMovementEntity(
            id = id,
            businessId = businessId,
            productId = productId,
            productName = productName,
            type = type.name,
            quantity = quantity,
            previousStock = previousStock,
            newStock = newStock,
            reason = reason,
            operatorName = operatorName,
            timestamp = timestamp
        )
    }

    // --- AppNotification Mapping ---
    fun AppNotificationEntity.toDomain(): AppNotification {
        return AppNotification(
            id = id,
            title = title,
            message = message,
            type = try { NotificationType.valueOf(type) } catch (e: Exception) { NotificationType.SISTEMA },
            timestamp = timestamp,
            isRead = isRead
        )
    }

    fun AppNotification.toEntity(businessId: String): AppNotificationEntity {
        return AppNotificationEntity(
            id = id,
            businessId = businessId,
            title = title,
            message = message,
            type = type.name,
            timestamp = timestamp,
            isRead = isRead
        )
    }
}
