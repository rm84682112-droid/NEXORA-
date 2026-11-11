package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.AppNotificationEntity
import com.example.data.local.entities.ClientEntity
import com.example.data.local.entities.DebtEntity
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.ProductEntity
import com.example.data.local.entities.SaleEntity
import com.example.data.local.entities.StockMovementEntity
import com.example.data.local.entities.SupplierEntity
import com.example.data.local.entities.SupplierPurchaseEntity
import com.example.data.local.entities.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE businessId = :businessId ORDER BY updatedAt DESC")
    fun getProductsFlow(businessId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE businessId = :businessId ORDER BY updatedAt DESC")
    suspend fun getProducts(businessId: String): List<ProductEntity>

    @Query("SELECT * FROM products WHERE businessId = :businessId AND id = :id LIMIT 1")
    suspend fun getProductById(businessId: String, id: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Query("DELETE FROM products WHERE businessId = :businessId AND id = :id")
    suspend fun deleteProduct(businessId: String, id: String)

    @Query("UPDATE products SET currentStock = :newStock, updatedAt = :updatedAt WHERE businessId = :businessId AND id = :id")
    suspend fun updateStock(businessId: String, id: String, newStock: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM products WHERE businessId = :businessId")
    suspend fun clearBusinessProducts(businessId: String)
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales WHERE businessId = :businessId ORDER BY timestamp DESC")
    fun getSalesFlow(businessId: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE businessId = :businessId ORDER BY timestamp DESC")
    suspend fun getSales(businessId: String): List<SaleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sales: List<SaleEntity>)

    @Query("DELETE FROM sales WHERE businessId = :businessId AND id = :id")
    suspend fun deleteSaleById(businessId: String, id: String)

    @Query("DELETE FROM sales WHERE businessId = :businessId")
    suspend fun clearBusinessSales(businessId: String)
}

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients WHERE businessId = :businessId ORDER BY name ASC")
    fun getClientsFlow(businessId: String): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE businessId = :businessId ORDER BY name ASC")
    suspend fun getClients(businessId: String): List<ClientEntity>

    @Query("SELECT * FROM clients WHERE businessId = :businessId AND id = :id LIMIT 1")
    suspend fun getClientById(businessId: String, id: String): ClientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(client: ClientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(clients: List<ClientEntity>)

    @Query("UPDATE clients SET totalDebt = :newDebt, updatedAt = :updatedAt WHERE businessId = :businessId AND id = :id")
    suspend fun updateDebt(businessId: String, id: String, newDebt: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM clients WHERE businessId = :businessId AND id = :id")
    suspend fun deleteClient(businessId: String, id: String)

    @Query("DELETE FROM clients WHERE businessId = :businessId")
    suspend fun clearBusinessClients(businessId: String)
}

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getDebtsFlow(businessId: String): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE businessId = :businessId ORDER BY createdAt DESC")
    suspend fun getDebts(businessId: String): List<DebtEntity>

    @Query("SELECT * FROM debts WHERE businessId = :businessId AND id = :id LIMIT 1")
    suspend fun getDebtById(businessId: String, id: String): DebtEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(debt: DebtEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(debts: List<DebtEntity>)

    @Query("UPDATE debts SET remainingBalance = :balance, paymentsJson = :paymentsJson, updatedAt = :updatedAt WHERE businessId = :businessId AND id = :id")
    suspend fun updateBalanceAndPayments(businessId: String, id: String, balance: Double, paymentsJson: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM debts WHERE businessId = :businessId AND id = :id")
    suspend fun deleteById(businessId: String, id: String)

    @Query("DELETE FROM debts WHERE businessId = :businessId")
    suspend fun clearBusinessDebts(businessId: String)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE businessId = :businessId ORDER BY date DESC")
    fun getExpensesFlow(businessId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE businessId = :businessId ORDER BY date DESC")
    suspend fun getExpenses(businessId: String): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<ExpenseEntity>)

    @Query("DELETE FROM expenses WHERE businessId = :businessId AND id = :id")
    suspend fun delete(businessId: String, id: String)

    @Query("DELETE FROM expenses WHERE businessId = :businessId")
    suspend fun clearBusinessExpenses(businessId: String)
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers WHERE businessId = :businessId ORDER BY name ASC")
    fun getSuppliersFlow(businessId: String): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE businessId = :businessId ORDER BY name ASC")
    suspend fun getSuppliers(businessId: String): List<SupplierEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(supplier: SupplierEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(suppliers: List<SupplierEntity>)

    @Query("SELECT * FROM supplier_purchases WHERE businessId = :businessId ORDER BY date DESC")
    fun getPurchasesFlow(businessId: String): Flow<List<SupplierPurchaseEntity>>

    @Query("SELECT * FROM supplier_purchases WHERE businessId = :businessId ORDER BY date DESC")
    suspend fun getPurchases(businessId: String): List<SupplierPurchaseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: SupplierPurchaseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPurchases(purchases: List<SupplierPurchaseEntity>)

    @Query("DELETE FROM suppliers WHERE businessId = :businessId")
    suspend fun clearBusinessSuppliers(businessId: String)
}

@Dao
interface StockMovementDao {
    @Query("SELECT * FROM stock_movements WHERE businessId = :businessId ORDER BY timestamp DESC")
    fun getMovementsFlow(businessId: String): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements WHERE businessId = :businessId ORDER BY timestamp DESC")
    suspend fun getMovements(businessId: String): List<StockMovementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movement: StockMovementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movements: List<StockMovementEntity>)

    @Query("DELETE FROM stock_movements WHERE businessId = :businessId")
    suspend fun clearBusinessMovements(businessId: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM app_notifications WHERE businessId = :businessId ORDER BY timestamp DESC")
    fun getNotificationsFlow(businessId: String): Flow<List<AppNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: AppNotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<AppNotificationEntity>)

    @Query("UPDATE app_notifications SET isRead = 1 WHERE businessId = :businessId AND id = :id")
    suspend fun markAsRead(businessId: String, id: String)

    @Query("DELETE FROM app_notifications WHERE businessId = :businessId")
    suspend fun clearBusinessNotifications(businessId: String)
}

@Dao
interface SyncQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: SyncQueueEntity): Long

    @Query("SELECT * FROM sync_queue WHERE businessId = :businessId AND status = 'PENDING' ORDER BY id ASC LIMIT :limit")
    suspend fun getPendingItems(businessId: String, limit: Int = 50): List<SyncQueueEntity>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE businessId = :businessId AND status = 'PENDING'")
    suspend fun getAllPendingCount(businessId: String): Int

    @Query("SELECT COUNT(*) FROM sync_queue WHERE businessId = :businessId AND status = 'PENDING'")
    fun getPendingCountFlow(businessId: String): Flow<Int>

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE sync_queue SET attempts = :attempts, status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, attempts: Int, status: String)

    @Query("DELETE FROM sync_queue WHERE businessId = :businessId")
    suspend fun clearBusinessQueue(businessId: String)
}
