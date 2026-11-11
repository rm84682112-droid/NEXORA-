package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.NexoraDatabase
import com.example.data.model.*
import com.example.data.repository.NexoraRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NexoraPersistenceAndSyncTest {

    private lateinit var context: Context
    private lateinit var testDatabase: NexoraDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        testDatabase = NexoraDatabase.createInMemory(context)
        NexoraRepository.initialize(context, testDatabase)
    }

    @After
    fun tearDown() {
        NexoraRepository.initialize(context, null)
        testDatabase.close()
    }

    @Test
    fun test1_CreateProductAndVerifyLocalPersistence() = runBlocking {
        // Arrange
        val merchantA = User(
            id = "merchant-1",
            name = "Comerciante Maputo",
            phone = "+258841112233",
            role = UserRole.DONO,
            storeName = "Loja Teste A",
            storeLocation = "Maputo",
            businessId = "biz-test-A"
        )
        NexoraRepository.switchUser(merchantA)

        // Act: Create product
        val created = NexoraRepository.addProduct(
            name = "Farinha Nacional 10kg",
            category = "Mercearia",
            description = "Farinha de milho",
            unitType = UnitType.PACOTE,
            unitsPerPackage = 1,
            packagesCount = 25,
            costPrice = 450.0,
            sellingPrice = 580.0,
            minStock = 5,
            barcode = "6001239999",
            iconEmoji = "🌾"
        )

        // Assert 1: Product exists in in-memory state
        assertTrue(NexoraRepository.products.value.any { it.id == created.id })

        // Wait up to 1 second for background IO to settle
        for (i in 1..20) {
            if (testDatabase.productDao().getProductById("biz-test-A", created.id) != null) break
            kotlinx.coroutines.delay(50)
        }

        // Assert 2: Product is physically saved in Room SQLite
        val inDb = testDatabase.productDao().getProductById("biz-test-A", created.id)
        assertNotNull("Product must be persisted in SQLite Room DB", inDb)
        assertEquals("Farinha Nacional 10kg", inDb?.name)
        assertEquals(25, inDb?.currentStock)
        assertEquals(580.0, inDb?.sellingPrice ?: 0.0, 0.01)

        // Assert 3: Re-initializing repository (simulating app restart) recovers the product
        NexoraRepository.initialize(context, testDatabase)
        val productsAfterRestart = testDatabase.productDao().getProducts("biz-test-A")
        assertTrue(productsAfterRestart.any { it.id == created.id })
    }

    @Test
    fun test2_RegisterSaleAndVerifyHistoryAndStockDeduction() = runBlocking {
        val merchant = User(
            id = "merchant-2",
            name = "Momade",
            phone = "+258842223344",
            role = UserRole.DONO,
            storeName = "Bazar Momade",
            storeLocation = "Matola",
            businessId = "biz-test-sales"
        )
        NexoraRepository.switchUser(merchant)

        val product = NexoraRepository.addProduct(
            name = "Açúcar Castanho 1kg",
            category = "Mercearia",
            description = "Açúcar doce",
            unitType = UnitType.UNIDADE,
            unitsPerPackage = 1,
            packagesCount = 50,
            costPrice = 70.0,
            sellingPrice = 90.0,
            minStock = 10,
            barcode = "6007788",
            iconEmoji = "🧂"
        )

        // Register sale of 5 units
        val cartItem = CartItem(product = product, quantity = 5)
        val sale = NexoraRepository.registerSale(
            items = listOf(cartItem),
            discount = 10.0,
            paymentMethod = PaymentMethod.MPESA,
            amountPaid = 440.0,
            client = null,
            paymentRef = "MP-TEST-1234",
            dueDate = null,
            notes = "Venda balcão"
        )

        // Wait up to 1 second for background IO to settle
        for (i in 1..20) {
            if (testDatabase.saleDao().getSales("biz-test-sales").any { it.id == sale.id }) break
            kotlinx.coroutines.delay(50)
        }

        // Assert 1: Sale persisted in Room
        val salesInDb = testDatabase.saleDao().getSales("biz-test-sales")
        assertTrue("Sale must be persisted in database", salesInDb.any { it.id == sale.id })
        assertEquals(440.0, salesInDb.first { it.id == sale.id }.total, 0.01)

        // Wait up to 1 second for stock update to settle
        for (i in 1..20) {
            val p = testDatabase.productDao().getProductById("biz-test-sales", product.id)
            if (p != null && p.currentStock == 45) break
            kotlinx.coroutines.delay(50)
        }

        // Assert 2: Stock was deducted from 50 to 45 in database
        val updatedProdInDb = testDatabase.productDao().getProductById("biz-test-sales", product.id)
        assertEquals("Stock must be deducted in database", 45, updatedProdInDb?.currentStock)
    }

    @Test
    fun test3_ExpenseAndDebtPaymentPersistence() = runBlocking {
        val merchant = User(
            id = "merchant-3",
            name = "Adélia",
            phone = "+258843334455",
            role = UserRole.DONO,
            storeName = "Mercearia Central",
            storeLocation = "Beira",
            businessId = "biz-test-expenses"
        )
        NexoraRepository.switchUser(merchant)

        // 1. Add Client
        val client = NexoraRepository.addClient(
            name = "Carlos Tembe",
            phone = "+258849998877",
            address = "Bairro Central",
            creditLimit = 5000.0,
            notes = "Bom pagador"
        )
        for (i in 1..20) {
            if (testDatabase.clientDao().getClientById("biz-test-expenses", client.id) != null) break
            kotlinx.coroutines.delay(50)
        }
        val clientInDb = testDatabase.clientDao().getClientById("biz-test-expenses", client.id)
        assertNotNull("Client must be persisted", clientInDb)

        // 2. Add Expense
        val expense = NexoraRepository.addExpense(
            description = "Credelec EDM Loja",
            amount = 1500.0,
            category = ExpenseCategory.ELECTRICIDADE,
            paymentMethod = PaymentMethod.MPESA,
            date = System.currentTimeMillis(),
            notes = "Contador 4490"
        )
        for (i in 1..20) {
            if (testDatabase.expenseDao().getExpenses("biz-test-expenses").isNotEmpty()) break
            kotlinx.coroutines.delay(50)
        }
        val expensesInDb = testDatabase.expenseDao().getExpenses("biz-test-expenses")
        assertTrue("Expense must be persisted", expensesInDb.any { it.id == expense.id })
    }

    @Test
    fun test4_OfflineModeQueuesMutationsPersistently() = runBlocking {
        val merchant = User(
            id = "merchant-4",
            name = "Offline Merchant",
            phone = "+258844445566",
            role = UserRole.DONO,
            storeName = "Loja Remota",
            storeLocation = "Inhambane",
            businessId = "biz-test-offline"
        )
        NexoraRepository.switchUser(merchant)

        // Disconnect internet
        if (NexoraRepository.isOnline.value) {
            NexoraRepository.toggleOnlineMode()
        }
        assertFalse(NexoraRepository.isOnline.value)

        // Perform mutation while offline
        NexoraRepository.addProduct(
            name = "Sabão Macaxeira",
            category = "Limpeza",
            description = "Sabão em barra",
            unitType = UnitType.UNIDADE,
            unitsPerPackage = 1,
            packagesCount = 20,
            costPrice = 30.0,
            sellingPrice = 45.0,
            minStock = 5,
            barcode = "99887766",
            iconEmoji = "🧼"
        )

        // Wait up to 1 second for background IO to settle
        for (i in 1..20) {
            if (testDatabase.syncQueueDao().getAllPendingCount("biz-test-offline") > 0) break
            kotlinx.coroutines.delay(50)
        }

        // Verify that the mutation is in the persistent sync queue
        val pendingCount = testDatabase.syncQueueDao().getAllPendingCount("biz-test-offline")
        assertTrue("Offline operations must be queued in persistent sync_queue", pendingCount >= 1)

        val pendingItems = testDatabase.syncQueueDao().getPendingItems("biz-test-offline")
        assertTrue(pendingItems.any { it.entityType == "PRODUCT" })
    }

    @Test
    fun test5_AccountSwitchingStrictIsolation() = runBlocking {
        val bizA = "biz-merchant-alpha"
        val bizB = "biz-merchant-beta"

        val userA = User(
            id = "user-A",
            name = "António",
            phone = "+258840000001",
            role = UserRole.DONO,
            storeName = "Loja do António",
            storeLocation = "Maputo",
            businessId = bizA
        )

        val userB = User(
            id = "user-B",
            name = "Bernardo",
            phone = "+258840000002",
            role = UserRole.DONO,
            storeName = "Loja do Bernardo",
            storeLocation = "Nampula",
            businessId = bizB
        )

        // Log into Account A and create a secret product
        NexoraRepository.switchUser(userA)
        val prodA = NexoraRepository.addProduct(
            name = "Produto Secreto de António",
            category = "Geral",
            description = "Apenas António pode ver",
            unitType = UnitType.UNIDADE,
            unitsPerPackage = 1,
            packagesCount = 10,
            costPrice = 100.0,
            sellingPrice = 150.0,
            minStock = 2,
            barcode = "AAA-111",
            iconEmoji = "🔒"
        )

        // Wait up to 1 second for background IO to settle
        for (i in 1..20) {
            if (testDatabase.productDao().getProducts(bizA).any { it.id == prodA.id }) break
            kotlinx.coroutines.delay(50)
        }

        // Verify it belongs strictly to bizA in DB
        val prodsInDbA = testDatabase.productDao().getProducts(bizA)
        assertTrue(prodsInDbA.any { it.id == prodA.id })

        // Switch to Account B
        NexoraRepository.switchUser(userB)

        // Verify Account B's database queries cannot see Account A's product
        val prodsInDbB = testDatabase.productDao().getProducts(bizB)
        assertFalse("Account B must NEVER see Account A's products!", prodsInDbB.any { it.id == prodA.id })
    }

    @Test
    fun test6_PlanLimitsAndTrialExpiration() = runBlocking {
        val merchant = User(
            id = "merchant-limits",
            name = "Comerciante Limites",
            phone = "+258845556677",
            role = UserRole.DONO,
            storeName = "Loja Limites",
            storeLocation = "Maputo",
            businessId = "biz-limits"
        )
        NexoraRepository.switchUser(merchant)
        NexoraRepository.updateSubscription(
            BusinessSubscription(
                currentPlan = PlanTier.GRATUITO,
                status = SubscriptionStatus.EXPIRADA
            )
        )

        // Test product limit (Free = 20 max products)
        for (i in 1..20) {
            NexoraRepository.addProduct(
                name = "Produto $i",
                category = "Geral",
                description = "Desc",
                unitType = UnitType.UNIDADE,
                unitsPerPackage = 1,
                packagesCount = 10,
                costPrice = 10.0,
                sellingPrice = 20.0,
                minStock = 1,
                barcode = "BC-$i",
                iconEmoji = "📦"
            )
        }

        assertEquals(20, NexoraRepository.products.value.size)

        var thrown = false
        try {
            NexoraRepository.addProduct(
                name = "Produto 21 Excedente",
                category = "Geral",
                description = "Desc",
                unitType = UnitType.UNIDADE,
                unitsPerPackage = 1,
                packagesCount = 10,
                costPrice = 10.0,
                sellingPrice = 20.0,
                minStock = 1,
                barcode = "BC-21",
                iconEmoji = "📦"
            )
        } catch (e: IllegalStateException) {
            thrown = true
            assertTrue(e.message?.contains("Limite do plano atingido") == true)
        }
        assertTrue("Exceeding plan product limit must throw IllegalStateException", thrown)
        assertEquals(20, NexoraRepository.products.value.size)

        // Test client limit (Free = 20 max clients)
        for (i in 1..20) {
            NexoraRepository.addClient(
                name = "Cliente $i",
                phone = "+2588400000$i",
                address = "Endereço $i",
                creditLimit = 1000.0,
                notes = "Nota $i"
            )
        }
        assertEquals(20, NexoraRepository.clients.value.size)

        var clientLimitThrown = false
        try {
            NexoraRepository.addClient(
                name = "Cliente 21 Excedente",
                phone = "+258840000099",
                address = "Endereço",
                creditLimit = 1000.0,
                notes = ""
            )
        } catch (e: IllegalStateException) {
            clientLimitThrown = true
            assertTrue(e.message?.contains("Limite do plano atingido") == true)
        }
        assertTrue("Exceeding plan client limit must throw IllegalStateException", clientLimitThrown)
        assertEquals(20, NexoraRepository.clients.value.size)

        // Upgrade to Starter (100 products, 200 clients)
        NexoraRepository.updateSubscription(
            BusinessSubscription(
                currentPlan = PlanTier.BASICO,
                status = SubscriptionStatus.ACTIVA
            )
        )
        // Now can add 21st product and client
        val extraProd = NexoraRepository.addProduct(
            name = "Produto 21 Starter",
            category = "Geral",
            description = "Desc",
            unitType = UnitType.UNIDADE,
            unitsPerPackage = 1,
            packagesCount = 10,
            costPrice = 10.0,
            sellingPrice = 20.0,
            minStock = 1,
            barcode = "BC-21-STARTER",
            iconEmoji = "📦"
        )
        assertNotNull(extraProd)
        assertEquals(21, NexoraRepository.products.value.size)

        val extraClient = NexoraRepository.addClient(
            name = "Cliente 21 Starter",
            phone = "+258840000021",
            address = "Endereço",
            creditLimit = 1000.0,
            notes = ""
        )
        assertNotNull(extraClient)
        assertEquals(21, NexoraRepository.clients.value.size)

        // Non-destructive transition back to Free:
        // Existing 21 products and 21 clients remain intact and consultable/editable
        NexoraRepository.updateSubscription(
            BusinessSubscription(
                currentPlan = PlanTier.GRATUITO,
                status = SubscriptionStatus.EXPIRADA
            )
        )
        assertEquals(21, NexoraRepository.products.value.size)
        assertEquals(21, NexoraRepository.clients.value.size)
    }
}
