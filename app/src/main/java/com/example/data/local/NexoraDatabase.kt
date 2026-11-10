package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.*
import com.example.data.local.entities.*

@Database(
    entities = [
        ProductEntity::class,
        SaleEntity::class,
        ClientEntity::class,
        DebtEntity::class,
        ExpenseEntity::class,
        SupplierEntity::class,
        SupplierPurchaseEntity::class,
        StockMovementEntity::class,
        AppNotificationEntity::class,
        SyncQueueEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NexoraDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun clientDao(): ClientDao
    abstract fun debtDao(): DebtDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun supplierDao(): SupplierDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun notificationDao(): NotificationDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        @Volatile
        private var INSTANCE: NexoraDatabase? = null

        fun getInstance(context: Context): NexoraDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NexoraDatabase::class.java,
                    "nexora_local.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
            }
        }

        // For in-memory testing in Robolectric tests
        fun createInMemory(context: Context): NexoraDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                NexoraDatabase::class.java
            )
            .allowMainThreadQueries()
            .build()
        }

        fun setInstanceForTesting(db: NexoraDatabase?) {
            INSTANCE = db
        }
    }
}
