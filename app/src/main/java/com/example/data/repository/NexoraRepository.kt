package com.example.data.repository

import com.example.data.firebase.GeminiBusinessService
import com.example.data.model.AiBusinessSummary
import com.example.data.model.AiChatMessage
import com.example.data.model.AiMonthlyUsage
import com.example.data.model.AiStatusState
import com.example.data.model.AppNotification
import com.example.data.model.AuditAction
import com.example.data.model.AuditLog
import com.example.data.model.BusinessSubscription
import com.example.data.model.BusinessTenant
import com.example.data.model.CartItem
import com.example.data.model.Client
import com.example.data.model.DebtPayment
import com.example.data.model.DebtRecord
import com.example.data.model.DebtStatus
import com.example.data.model.Expense
import com.example.data.model.ExpenseCategory
import com.example.data.model.MovementType
import com.example.data.model.NotificationType
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentProofStatus
import com.example.data.model.PlanPricingConfig
import com.example.data.model.PlanTier
import com.example.data.model.Product
import com.example.data.model.PurchaseItem
import com.example.data.model.Sale
import com.example.data.model.StockMovement
import com.example.data.model.SubscriptionPaymentProof
import com.example.data.model.SubscriptionStatus
import com.example.data.model.Supplier
import com.example.data.model.SupplierPurchase
import com.example.data.model.SupportTicket
import com.example.data.model.TenantStatus
import com.example.data.model.TicketStatus
import com.example.data.model.UnitType
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.model.UserSession
import com.example.data.model.PhoneAuthCredentials
import com.example.util.SecurityUtils
import com.example.data.model.UserConsentRecord
import com.example.data.model.PrivacySettings
import com.example.data.model.DataIncident
import com.example.data.model.IncidentSeverity
import com.example.data.model.IncidentStatus
import com.example.data.model.PlatformPaymentAccount
import android.content.Context
import com.example.data.local.NexoraDatabase
import com.example.data.local.converters.RoomMappers.toDomain
import com.example.data.local.converters.RoomMappers.toEntity
import com.example.data.local.entities.SyncQueueEntity
import com.example.data.sync.SyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object NexoraRepository {

    // Persistent Database & Cloud Sync Bridge
    private var database: NexoraDatabase? = null
    private var syncManager: SyncManager? = null
    private val repoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val activeBusinessJobs = mutableListOf<Job>()

    fun initialize(context: Context, customDb: NexoraDatabase? = null) {
        val db = customDb ?: NexoraDatabase.getInstance(context)
        database = db
        syncManager = SyncManager(db)
        val bizId = currentBusinessId()
        if (bizId.isNotBlank() && _isLoggedIn.value) {
            bindBusinessData(bizId)
        }
    }

    private fun currentBusinessId(): String {
        val id = _currentUser.value.businessId.ifBlank { _currentUser.value.id }
        return id.ifBlank { "store-matriz-1" }
    }

    fun bindBusinessData(businessId: String) {
        if (businessId.isBlank()) return
        val db = database ?: return

        activeBusinessJobs.forEach { it.cancel() }
        activeBusinessJobs.clear()

        // 1. Observe Products
        activeBusinessJobs.add(
            repoScope.launch {
                db.productDao().getProductsFlow(businessId).collect { entities ->
                    if (!_isDemoMode.value) {
                        _products.value = entities.map { it.toDomain() }
                    }
                }
            }
        )

        // 2. Observe Sales
        activeBusinessJobs.add(
            repoScope.launch {
                db.saleDao().getSalesFlow(businessId).collect { entities ->
                    if (!_isDemoMode.value) {
                        _sales.value = entities.map { it.toDomain() }
                    }
                }
            }
        )

        // 3. Observe Clients
        activeBusinessJobs.add(
            repoScope.launch {
                db.clientDao().getClientsFlow(businessId).collect { entities ->
                    if (!_isDemoMode.value) {
                        _clients.value = entities.map { it.toDomain() }
                    }
                }
            }
        )

        // 4. Observe Debts
        activeBusinessJobs.add(
            repoScope.launch {
                db.debtDao().getDebtsFlow(businessId).collect { entities ->
                    if (!_isDemoMode.value) {
                        _debts.value = entities.map { it.toDomain() }
                    }
                }
            }
        )

        // 5. Observe Expenses
        activeBusinessJobs.add(
            repoScope.launch {
                db.expenseDao().getExpensesFlow(businessId).collect { entities ->
                    if (!_isDemoMode.value) {
                        _expenses.value = entities.map { it.toDomain() }
                    }
                }
            }
        )

        // 6. Observe Suppliers
        activeBusinessJobs.add(
            repoScope.launch {
                db.supplierDao().getSuppliersFlow(businessId).collect { entities ->
                    if (!_isDemoMode.value) {
                        _suppliers.value = entities.map { it.toDomain() }
                    }
                }
            }
        )

        // 7. Observe Purchases
        activeBusinessJobs.add(
            repoScope.launch {
                db.supplierDao().getPurchasesFlow(businessId).collect { entities ->
                    if (!_isDemoMode.value) {
                        _supplierPurchases.value = entities.map { it.toDomain() }
                    }
                }
            }
        )

        // 8. Observe Movements
        activeBusinessJobs.add(
            repoScope.launch {
                db.stockMovementDao().getMovementsFlow(businessId).collect { entities ->
                    if (!_isDemoMode.value) {
                        _movements.value = entities.map { it.toDomain() }
                    }
                }
            }
        )

        // 9. Observe Notifications
        activeBusinessJobs.add(
            repoScope.launch {
                db.notificationDao().getNotificationsFlow(businessId).collect { entities ->
                    if (!_isDemoMode.value) {
                        _notifications.value = entities.map { it.toDomain() }
                    }
                }
            }
        )

        // 10. Observe Pending Sync Queue
        activeBusinessJobs.add(
            repoScope.launch {
                db.syncQueueDao().getPendingCountFlow(businessId).collect { count ->
                    _pendingSyncOperations.value = count
                }
            }
        )

        // 11. Initial Cloud Sync if online
        if (_isOnline.value) {
            repoScope.launch {
                syncManager?.syncFromCloud(businessId)
                syncManager?.drainQueue(businessId)
                syncManager?.startRealtimeListeners(businessId)
            }
        }
    }

    fun syncNow() {
        val bizId = currentBusinessId()
        if (_isOnline.value && bizId.isNotBlank()) {
            repoScope.launch {
                syncManager?.drainQueue(bizId)
                syncManager?.syncFromCloud(bizId)
            }
        }
    }

    // Demo Mode toggle
    private val _isDemoMode = MutableStateFlow(false)
    val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    fun carregarDadosDemonstracao() {
        if (!_isDemoMode.value) return
        _products.value = initialProducts
        _clients.value = initialClients
        _sales.value = initialSales
        _debts.value = initialDebts
        _movements.value = initialMovements
        _expenses.value = initialExpenses
        _suppliers.value = initialSuppliers
        _notifications.value = initialNotifications
    }

    fun toggleDemoMode(enabled: Boolean) {
        _isDemoMode.value = enabled
        if (enabled) {
            carregarDadosDemonstracao()
            addSystemNotification(
                title = "Modo Demo Ativo",
                message = "A operar com dados de demonstração isolados.",
                type = NotificationType.SISTEMA
            )
        } else {
            _products.value = emptyList()
            _sales.value = emptyList()
            _clients.value = emptyList()
            _debts.value = emptyList()
            _movements.value = emptyList()
            _expenses.value = emptyList()
            _suppliers.value = emptyList()
            _notifications.value = emptyList()
            addSystemNotification(
                title = "Modo de Produção",
                message = "A operar com dados reais do comerciante.",
                type = NotificationType.SISTEMA
            )
        }
    }

    val emptyUser = User(
        id = "",
        name = "Comerciante",
        phone = "",
        role = UserRole.DONO,
        pinHash = "",
        storeName = "NEXORA Loja",
        storeLocation = "Moçambique",
        currencySymbol = "MT"
    )

    private val authService = com.example.data.firebase.FirebaseAuthenticationService.getInstance()

    private val _currentUser = MutableStateFlow<User>(
        authService.auth?.currentUser?.let { fbUser ->
            val pNum = fbUser.email?.substringBefore("@") ?: ""
            User(
                id = fbUser.uid,
                name = fbUser.displayName?.ifBlank { "Comerciante" } ?: "Comerciante",
                phone = if (pNum.startsWith("258")) "+$pNum" else pNum,
                role = UserRole.DONO,
                pinHash = "",
                storeName = "NEXORA Loja",
                storeLocation = "Moçambique",
                businessId = fbUser.uid,
                currencySymbol = "MT"
            )
        } ?: emptyUser
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User>>(listOf(_currentUser.value))
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    // Onboarding & Telemetry State
    private val _hasCompletedTour = MutableStateFlow(false)
    val hasCompletedTour: StateFlow<Boolean> = _hasCompletedTour.asStateFlow()

    private val _dismissedTooltips = MutableStateFlow<Set<String>>(emptySet())
    val dismissedTooltips: StateFlow<Set<String>> = _dismissedTooltips.asStateFlow()

    private val _hasViewedProfit = MutableStateFlow(false)
    val hasViewedProfit: StateFlow<Boolean> = _hasViewedProfit.asStateFlow()

    private val _helpVideosConfig = MutableStateFlow<Map<String, String>>(emptyMap())
    val helpVideosConfig: StateFlow<Map<String, String>> = _helpVideosConfig.asStateFlow()

    fun completeTour() {
        _hasCompletedTour.value = true
    }

    fun resetTour() {
        _hasCompletedTour.value = false
    }

    fun dismissTooltip(key: String) {
        _dismissedTooltips.value = _dismissedTooltips.value + key
    }

    fun markProfitViewed() {
        _hasViewedProfit.value = true
    }

    fun inviteTeamMember(name: String, phone: String, role: UserRole, pin: String) {
        val bizId = _currentUser.value.businessId.ifBlank { _currentUser.value.id }
        val newUser = User(
            id = "usr-${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            phone = SecurityUtils.formatMozambiquePhone(phone),
            role = role,
            pinHash = SecurityUtils.hashPin(pin),
            storeName = _currentUser.value.storeName,
            storeLocation = _currentUser.value.storeLocation,
            businessId = bizId,
            currencySymbol = _currentUser.value.currencySymbol
        )
        _allUsers.value = _allUsers.value + newUser
        try {
            if (bizId.isNotBlank()) {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val memberMap = mapOf(
                    "uid" to newUser.id,
                    "name" to newUser.name,
                    "phone" to newUser.phone,
                    "role" to if (role == UserRole.DONO) "owner" else "cashier",
                    "pinHash" to newUser.pinHash,
                    "businessId" to bizId,
                    "createdAt" to System.currentTimeMillis()
                )
                db.collection("businesses").document(bizId).collection("members").document(newUser.id).set(memberMap)
            }
        } catch (e: Exception) {
            android.util.Log.w("NexoraRepository", "Invite member note: ${e.message}")
        }
        addAuditLog(
            action = AuditAction.CHANGE_PERMISSIONS,
            targetItem = "Membro de Equipa: $name",
            details = "Adicionado novo operador com o papel ${role.label}."
        )
        addSystemNotification(
            title = "Novo Membro Adicionado",
            message = "$name foi registado na equipa como ${role.label}.",
            type = NotificationType.SISTEMA
        )
    }

    fun removeTeamMember(userId: String) {
        val target = _allUsers.value.find { it.id == userId }
        if (target != null && target.role != UserRole.DONO) {
            _allUsers.value = _allUsers.value.filter { it.id != userId }
            addAuditLog(
                action = AuditAction.CHANGE_PERMISSIONS,
                targetItem = "Remoção de Membro: ${target.name}",
                details = "Operador removido da equipa comercial."
            )
        }
    }

    // Offline & Sync tracking
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _pendingSyncOperations = MutableStateFlow(0)
    val pendingSyncOperations: StateFlow<Int> = _pendingSyncOperations.asStateFlow()

    fun toggleOnlineMode() {
        _isOnline.value = !_isOnline.value
        val bizId = currentBusinessId()
        if (_isOnline.value && bizId.isNotBlank()) {
            repoScope.launch {
                syncManager?.drainQueue(bizId)
                syncManager?.syncFromCloud(bizId)
                syncManager?.startRealtimeListeners(bizId)
            }
            addSystemNotification(
                title = "Ligação Restabelecida",
                message = "A sincronizar dados pendentes com a nuvem...",
                type = NotificationType.SISTEMA
            )
        } else {
            syncManager?.stopCloudListeners()
        }
    }

    // Products
    private val initialProducts = listOf(
        Product(
            id = "prod-1",
            name = "Arroz Touro Branco 25kg",
            category = "Mercearia",
            description = "Arroz agulha de alta qualidade",
            unitType = UnitType.UNIDADE,
            unitsPerPackage = 1,
            costPrice = 1350.0,
            sellingPrice = 1650.0,
            currentStock = 18,
            minStock = 5,
            barcode = "6001234567890",
            iconEmoji = "🍚"
        ),
        Product(
            id = "prod-2",
            name = "Óleo Alimentar Sol 5 Litros",
            category = "Mercearia",
            description = "Óleo vegetal 100% puro",
            unitType = UnitType.UNIDADE,
            unitsPerPackage = 1,
            costPrice = 480.0,
            sellingPrice = 580.0,
            currentStock = 22,
            minStock = 6,
            barcode = "6002345678901",
            iconEmoji = "🌻"
        ),
        Product(
            id = "prod-3",
            name = "Farinha de Milho Nacional 10kg",
            category = "Mercearia",
            description = "Farinha de milho branca super fina",
            unitType = UnitType.UNIDADE,
            unitsPerPackage = 1,
            costPrice = 360.0,
            sellingPrice = 450.0,
            currentStock = 14,
            minStock = 5,
            barcode = "6003456789012",
            iconEmoji = "🌽"
        ),
        Product(
            id = "prod-4",
            name = "Açúcar Castelo Branco 1kg",
            category = "Mercearia",
            description = "Açúcar refinado nacional",
            unitType = UnitType.UNIDADE,
            unitsPerPackage = 1,
            costPrice = 65.0,
            sellingPrice = 85.0,
            currentStock = 45,
            minStock = 10,
            barcode = "6004567890123",
            iconEmoji = "🧂"
        ),
        Product(
            id = "prod-5",
            name = "Leite Condensado Moça 395g",
            category = "Mercearia",
            description = "Leite condensado açucarado",
            unitType = UnitType.UNIDADE,
            unitsPerPackage = 1,
            costPrice = 85.0,
            sellingPrice = 115.0,
            currentStock = 3,
            minStock = 8,
            barcode = "6005678901234",
            iconEmoji = "🥛"
        ),
        Product(
            id = "prod-6",
            name = "Coca-Cola 330ml Lata",
            category = "Bebidas",
            description = "Refrigerante gaseificado lata",
            unitType = UnitType.CAIXA,
            unitsPerPackage = 24,
            costPrice = 35.0,
            sellingPrice = 50.0,
            currentStock = 72,
            minStock = 24,
            barcode = "5449000000996",
            iconEmoji = "🥤"
        ),
        Product(
            id = "prod-7",
            name = "Cerveja 2M 330ml Garrafa",
            category = "Bebidas",
            description = "Cerveja nacional Laurentina / 2M",
            unitType = UnitType.CAIXA,
            unitsPerPackage = 24,
            costPrice = 45.0,
            sellingPrice = 65.0,
            currentStock = 96,
            minStock = 24,
            barcode = "6009876543210",
            iconEmoji = "🍺"
        ),
        Product(
            id = "prod-8",
            name = "Água Mineral Vumba 1.5L",
            category = "Bebidas",
            description = "Água mineral pura de nascente",
            unitType = UnitType.FARDO,
            unitsPerPackage = 6,
            costPrice = 30.0,
            sellingPrice = 45.0,
            currentStock = 36,
            minStock = 12,
            barcode = "6006789012345",
            iconEmoji = "💧"
        ),
        Product(
            id = "prod-9",
            name = "Suco Santal Laranja 1L",
            category = "Bebidas",
            description = "Néctar de fruta pura",
            unitType = UnitType.UNIDADE,
            unitsPerPackage = 1,
            costPrice = 75.0,
            sellingPrice = 100.0,
            currentStock = 2,
            minStock = 6,
            barcode = "6007890123456",
            iconEmoji = "🍊"
        ),
        Product(
            id = "prod-10",
            name = "Sabão em Pó Omo 1kg",
            category = "Higiene & Limpeza",
            description = "Detergente para lavagem de roupas",
            unitType = UnitType.UNIDADE,
            unitsPerPackage = 1,
            costPrice = 120.0,
            sellingPrice = 155.0,
            currentStock = 16,
            minStock = 6,
            barcode = "6008901234567",
            iconEmoji = "🧼"
        ),
        Product(
            id = "prod-11",
            name = "Pasta de Dente Colgate Tripla Ação 100ml",
            category = "Higiene & Limpeza",
            description = "Creme dental com flúor",
            unitType = UnitType.UNIDADE,
            unitsPerPackage = 1,
            costPrice = 50.0,
            sellingPrice = 75.0,
            currentStock = 0,
            minStock = 8,
            barcode = "6009012345678",
            iconEmoji = "🪥"
        ),
        Product(
            id = "prod-12",
            name = "Pão de Forma Especial",
            category = "Padaria",
            description = "Pão de forma fatiado diário",
            unitType = UnitType.UNIDADE,
            unitsPerPackage = 1,
            costPrice = 45.0,
            sellingPrice = 65.0,
            currentStock = 8,
            minStock = 6,
            barcode = "6000123456789",
            iconEmoji = "🍞"
        )
    )

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    // Clients
    private val initialClients = listOf(
        Client(
            id = "cli-1",
            name = "Dona Fátima Matusse",
            phone = "+258 84 321 4567",
            address = "Bairro 25 de Junho, Maputo",
            creditLimit = 4000.0,
            totalDebt = 1450.0,
            notes = "Cliente habitual, paga pontual no fim do mês"
        ),
        Client(
            id = "cli-2",
            name = "Sr. Carlos Tembe",
            phone = "+258 85 992 1100",
            address = "Bairro do Jardim, Casa 42",
            creditLimit = 5000.0,
            totalDebt = 3200.0,
            notes = "Oficina mecânica na esquina, acorda pagar quinzenalmente"
        ),
        Client(
            id = "cli-3",
            name = "Manuel Sitoe",
            phone = "+258 86 543 8976",
            address = "Matola Rio",
            creditLimit = 3000.0,
            totalDebt = 0.0,
            notes = "Compras à vista via M-Pesa"
        ),
        Client(
            id = "cli-4",
            name = "Marta Nhantumbo",
            phone = "+258 87 112 3344",
            address = "Chamanculo C, Rua 8",
            creditLimit = 2500.0,
            totalDebt = 850.0,
            notes = "Professora primária"
        )
    )

    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients: StateFlow<List<Client>> = _clients.asStateFlow()

    // Sales History
    private val now = System.currentTimeMillis()
    private val oneHour = 3600_000L
    private val oneDay = 86400_000L

    private val initialSales = listOf(
        Sale(
            id = "sale-1",
            receiptNumber = "REC-2026-0081",
            timestamp = now - (oneHour * 2),
            items = listOf(
                CartItem(initialProducts[0], 1),
                CartItem(initialProducts[1], 1),
                CartItem(initialProducts[3], 2)
            ),
            subtotal = 2400.0,
            discount = 50.0,
            total = 2350.0,
            paymentMethod = PaymentMethod.MPESA,
            amountPaid = 2350.0,
            change = 0.0,
            customerId = "cli-3",
            customerName = "Manuel Sitoe",
            customerPhone = "+258 86 543 8976",
            operatorName = "Sr. Momade Chale",
            paymentReference = "MP-8823901"
        ),
        Sale(
            id = "sale-2",
            receiptNumber = "REC-2026-0082",
            timestamp = now - (oneHour * 5),
            items = listOf(
                CartItem(initialProducts[5], 6),
                CartItem(initialProducts[7], 2)
            ),
            subtotal = 390.0,
            discount = 0.0,
            total = 390.0,
            paymentMethod = PaymentMethod.DINHEIRO,
            amountPaid = 500.0,
            change = 110.0,
            operatorName = "Adélia Cossa"
        ),
        Sale(
            id = "sale-3",
            receiptNumber = "REC-2026-0083",
            timestamp = now - (oneHour * 8),
            items = listOf(
                CartItem(initialProducts[0], 1),
                CartItem(initialProducts[1], 1)
            ),
            subtotal = 2230.0,
            discount = 0.0,
            total = 2230.0,
            paymentMethod = PaymentMethod.EMOLA,
            amountPaid = 2230.0,
            change = 0.0,
            operatorName = "Adélia Cossa",
            paymentReference = "EM-994102"
        ),
        Sale(
            id = "sale-4",
            receiptNumber = "REC-2026-0078",
            timestamp = now - (oneDay * 2),
            items = listOf(
                CartItem(initialProducts[0], 1),
                CartItem(initialProducts[9], 1)
            ),
            subtotal = 1805.0,
            discount = 0.0,
            total = 1805.0,
            paymentMethod = PaymentMethod.FIADO,
            amountPaid = 355.0,
            change = 0.0,
            customerId = "cli-1",
            customerName = "Dona Fátima Matusse",
            customerPhone = "+258 84 321 4567",
            operatorName = "Sr. Momade Chale",
            dueDate = now + (oneDay * 3)
        ),
        Sale(
            id = "sale-5",
            receiptNumber = "REC-2026-0065",
            timestamp = now - (oneDay * 6),
            items = listOf(
                CartItem(initialProducts[1], 4),
                CartItem(initialProducts[2], 2)
            ),
            subtotal = 3220.0,
            discount = 20.0,
            total = 3200.0,
            paymentMethod = PaymentMethod.FIADO,
            amountPaid = 0.0,
            change = 0.0,
            customerId = "cli-2",
            customerName = "Sr. Carlos Tembe",
            customerPhone = "+258 85 992 1100",
            operatorName = "Sr. Momade Chale",
            dueDate = now - (oneDay * 2)
        )
    )

    private val _sales = MutableStateFlow<List<Sale>>(emptyList())
    val sales: StateFlow<List<Sale>> = _sales.asStateFlow()

    // Debts
    private val initialDebts = listOf(
        DebtRecord(
            id = "debt-1",
            saleId = "sale-4",
            receiptNumber = "REC-2026-0078",
            clientId = "cli-1",
            clientName = "Dona Fátima Matusse",
            clientPhone = "+258 84 321 4567",
            originalAmount = 1450.0,
            remainingBalance = 1450.0,
            createdAt = now - (oneDay * 2),
            dueDate = now + (oneDay * 3),
            payments = emptyList()
        ),
        DebtRecord(
            id = "debt-2",
            saleId = "sale-5",
            receiptNumber = "REC-2026-0065",
            clientId = "cli-2",
            clientName = "Sr. Carlos Tembe",
            clientPhone = "+258 85 992 1100",
            originalAmount = 3200.0,
            remainingBalance = 3200.0,
            createdAt = now - (oneDay * 6),
            dueDate = now - (oneDay * 2),
            payments = emptyList()
        ),
        DebtRecord(
            id = "debt-3",
            saleId = "sale-old-3",
            receiptNumber = "REC-2026-0050",
            clientId = "cli-4",
            clientName = "Marta Nhantumbo",
            clientPhone = "+258 87 112 3344",
            originalAmount = 1850.0,
            remainingBalance = 850.0,
            createdAt = now - (oneDay * 12),
            dueDate = now + (oneDay * 5),
            payments = listOf(
                DebtPayment(
                    id = "pay-1",
                    date = now - (oneDay * 4),
                    amount = 1000.0,
                    paymentMethod = PaymentMethod.MPESA,
                    notes = "Pagamento parcial via M-Pesa"
                )
            )
        )
    )

    private val _debts = MutableStateFlow<List<DebtRecord>>(emptyList())
    val debts: StateFlow<List<DebtRecord>> = _debts.asStateFlow()

    // Stock Movements
    private val initialMovements = listOf(
        StockMovement(
            id = "mov-1",
            productId = "prod-1",
            productName = "Arroz Touro Branco 25kg",
            type = MovementType.ENTRADA,
            quantity = 20,
            previousStock = 0,
            newStock = 20,
            reason = "Compra fornecedor Grossista Maputo",
            operatorName = "Sr. Momade Chale",
            timestamp = now - (oneDay * 3)
        ),
        StockMovement(
            id = "mov-2",
            productId = "prod-6",
            productName = "Coca-Cola 330ml Lata",
            type = MovementType.ENTRADA,
            quantity = 72,
            previousStock = 6,
            newStock = 78,
            reason = "Entrada de 3 Caixas de 24 un",
            operatorName = "Sr. Momade Chale",
            timestamp = now - (oneDay * 2)
        )
    )

    private val _movements = MutableStateFlow<List<StockMovement>>(emptyList())
    val movements: StateFlow<List<StockMovement>> = _movements.asStateFlow()

    // Despesas operacionais
    private val initialExpenses = listOf(
        Expense(
            id = "exp-1",
            description = "Electricidade Loja EDM Credelec",
            amount = 2500.0,
            category = ExpenseCategory.ELECTRICIDADE,
            paymentMethod = PaymentMethod.MPESA,
            date = now - (oneDay * 1),
            notes = "Recarga mensal contador 100239",
            recordedBy = "Sr. Momade Chale"
        ),
        Expense(
            id = "exp-2",
            description = "Combustível carrinha de entrega",
            amount = 1200.0,
            category = ExpenseCategory.TRANSPORTE,
            paymentMethod = PaymentMethod.DINHEIRO,
            date = now - (oneDay * 3),
            notes = "Entrega de fardos aos clientes da Matola",
            recordedBy = "Sr. Momade Chale"
        ),
        Expense(
            id = "exp-3",
            description = "Renda do Imóvel Comercial",
            amount = 14000.0,
            category = ExpenseCategory.RENDA,
            paymentMethod = PaymentMethod.EMOLA,
            date = now - (oneDay * 10),
            notes = "Renda de Outubro/Novembro",
            recordedBy = "Sr. Momade Chale"
        )
    )

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    // Fornecedores
    private val initialSuppliers = listOf(
        Supplier(
            id = "sup-1",
            name = "Grossista Maputo Central SARL",
            phone = "+258 84 900 1122",
            address = "Baixa de Maputo, Rua da Ganda",
            suppliedCategories = "Arroz, Farinha, Açúcar e Óleo",
            totalDebt = 8500.0,
            notes = "Prazo 15 dias para fardos de arroz"
        ),
        Supplier(
            id = "sup-2",
            name = "Distribuidora Nacional de Bebidas (CDM)",
            phone = "+258 82 300 4455",
            address = "Zona Industrial da Machava",
            suppliedCategories = "Coca-Cola, 2M, Laurentina e Águas",
            totalDebt = 0.0,
            notes = "Entrega semanal às terças"
        ),
        Supplier(
            id = "sup-3",
            name = "Laticínios & Lanches do Sul",
            phone = "+258 87 222 9988",
            address = "Matola 700",
            suppliedCategories = "Leite Condensado, Iogurtes e Queijos",
            totalDebt = 2400.0,
            notes = "Pagamento via M-Pesa"
        )
    )

    private val _suppliers = MutableStateFlow<List<Supplier>>(emptyList())
    val suppliers: StateFlow<List<Supplier>> = _suppliers.asStateFlow()

    private val _supplierPurchases = MutableStateFlow<List<SupplierPurchase>>(emptyList())
    val supplierPurchases: StateFlow<List<SupplierPurchase>> = _supplierPurchases.asStateFlow()

    // Notificações locais do sistema
    private val initialNotifications = listOf(
        AppNotification(
            id = "notif-1",
            title = "Stock Esgotado!",
            message = "A Pasta de Dente Colgate Tripla Ação chegou a 0 unidades em stock.",
            type = NotificationType.ESGOTADO,
            timestamp = now - (oneHour * 3),
            isRead = false
        ),
        AppNotification(
            id = "notif-2",
            title = "Stock Baixo: Suco Santal",
            message = "Restam apenas 2 unidades de Suco Santal 1L (Mínimo: 6).",
            type = NotificationType.STOCK_BAIXO,
            timestamp = now - (oneHour * 5),
            isRead = false
        ),
        AppNotification(
            id = "notif-3",
            title = "Dívida em Atraso!",
            message = "O fiado do Sr. Carlos Tembe no valor de 3.200 MT venceu há 2 dias.",
            type = NotificationType.FIADO_VENCIDO,
            timestamp = now - (oneDay * 1),
            isRead = false
        )
    )

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // Subscrição e planos
    private val _subscription = MutableStateFlow(BusinessSubscription())
    val subscription: StateFlow<BusinessSubscription> = _subscription.asStateFlow()

    // Methods

    fun switchUser(user: User) {
        _currentUser.value = user
        val bizId = user.businessId.ifBlank { user.id }
        bindBusinessData(bizId)
    }

    fun addProduct(
        name: String,
        category: String,
        description: String,
        unitType: UnitType,
        unitsPerPackage: Int,
        packagesCount: Int,
        costPrice: Double,
        sellingPrice: Double,
        minStock: Int,
        barcode: String,
        iconEmoji: String,
        isActive: Boolean = true,
        supplierName: String = "",
        packageEnabled: Boolean = false,
        packagePrice: Double = 0.0,
        packageName: String = "Caixa"
    ): Product {
        val calculatedStock = if (packageEnabled && unitsPerPackage > 1) {
            packagesCount * unitsPerPackage
        } else {
            packagesCount
        }

        val newProduct = Product(
            id = "prod-${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            category = category.trim(),
            description = description.trim(),
            unitType = unitType,
            unitsPerPackage = unitsPerPackage.coerceAtLeast(1),
            costPrice = costPrice,
            sellingPrice = sellingPrice,
            currentStock = calculatedStock,
            minStock = minStock.coerceAtLeast(1),
            barcode = barcode.trim(),
            iconEmoji = iconEmoji.ifBlank { "📦" },
            isActive = isActive,
            supplierName = supplierName.trim(),
            packageEnabled = packageEnabled,
            packagePrice = packagePrice,
            packageName = packageName.trim()
        )

        val bizId = currentBusinessId()
        _products.value = listOf(newProduct) + _products.value.filter { it.id != newProduct.id }

        database?.let { db ->
            repoScope.launch {
                db.productDao().insertOrUpdate(newProduct.toEntity(bizId))
                db.syncQueueDao().enqueue(
                    SyncQueueEntity(
                        businessId = bizId,
                        entityType = "PRODUCT",
                        entityId = newProduct.id,
                        operation = "UPSERT",
                        payloadJson = ""
                    )
                )
                if (_isOnline.value) {
                    syncManager?.drainQueue(bizId)
                }
            }
        }

        addAuditLog(
            action = AuditAction.CREATE_PRODUCT,
            targetItem = newProduct.name,
            details = "Produto criado por ${_currentUser.value.name} (${_currentUser.value.role.label}). Preço de venda: ${sellingPrice} MT, Stock inicial: $calculatedStock un."
        )

        if (calculatedStock > 0) {
            val movement = StockMovement(
                id = "mov-${UUID.randomUUID().toString().take(8)}",
                productId = newProduct.id,
                productName = newProduct.name,
                type = MovementType.ENTRADA,
                quantity = calculatedStock,
                previousStock = 0,
                newStock = calculatedStock,
                reason = "Cadastro inicial de produto",
                operatorName = _currentUser.value.name,
                timestamp = System.currentTimeMillis()
            )
            _movements.value = listOf(movement) + _movements.value
            database?.let { db ->
                repoScope.launch {
                    db.stockMovementDao().insert(movement.toEntity(bizId))
                }
            }
        }

        return newProduct
    }

    fun updateProduct(product: Product) {
        val oldProduct = _products.value.find { it.id == product.id }
        _products.value = _products.value.map {
            if (it.id == product.id) product else it
        }

        val bizId = currentBusinessId()
        database?.let { db ->
            repoScope.launch {
                db.productDao().insertOrUpdate(product.toEntity(bizId))
                db.syncQueueDao().enqueue(
                    SyncQueueEntity(
                        businessId = bizId,
                        entityType = "PRODUCT",
                        entityId = product.id,
                        operation = "UPSERT",
                        payloadJson = ""
                    )
                )
                if (_isOnline.value) {
                    syncManager?.drainQueue(bizId)
                }
            }
        }

        val priceChanged = oldProduct != null && (oldProduct.sellingPrice != product.sellingPrice || oldProduct.costPrice != product.costPrice)
        val action = if (priceChanged) AuditAction.PRICE_CHANGE else AuditAction.UPDATE_PRODUCT

        addAuditLog(
            action = action,
            targetItem = product.name,
            details = "Produto atualizado por ${_currentUser.value.name}. Venda: ${product.sellingPrice} MT, Custo: ${product.costPrice} MT, Stock: ${product.currentStock} un."
        )
    }

    fun deleteProduct(productId: String) {
        val target = _products.value.find { it.id == productId }
        _products.value = _products.value.filter { it.id != productId }

        val bizId = currentBusinessId()
        database?.let { db ->
            repoScope.launch {
                db.productDao().deleteProduct(bizId, productId)
                db.syncQueueDao().enqueue(
                    SyncQueueEntity(
                        businessId = bizId,
                        entityType = "PRODUCT",
                        entityId = productId,
                        operation = "DELETE",
                        payloadJson = ""
                    )
                )
                if (_isOnline.value) {
                    syncManager?.drainQueue(bizId)
                }
            }
        }

        if (target != null) {
            addAuditLog(
                action = AuditAction.DELETE_PRODUCT,
                targetItem = target.name,
                details = "Produto eliminado por ${_currentUser.value.name} (${_currentUser.value.role.label})."
            )
        }
    }

    fun adjustStock(productId: String, adjustmentQuantity: Int, type: MovementType, reason: String) {
        val currentProduct = _products.value.find { it.id == productId } ?: return
        val previousStock = currentProduct.currentStock
        val newStock = when (type) {
            MovementType.ENTRADA -> previousStock + adjustmentQuantity
            MovementType.SAIDA, MovementType.AJUSTE, MovementType.DEVOLUCAO -> (previousStock - adjustmentQuantity).coerceAtLeast(0)
            MovementType.VENDA -> (previousStock - adjustmentQuantity).coerceAtLeast(0)
        }

        val updatedProduct = currentProduct.copy(currentStock = newStock)
        updateProduct(updatedProduct)

        val bizId = currentBusinessId()
        val movement = StockMovement(
            id = "mov-${UUID.randomUUID().toString().take(8)}",
            productId = productId,
            productName = currentProduct.name,
            type = type,
            quantity = adjustmentQuantity,
            previousStock = previousStock,
            newStock = newStock,
            reason = reason,
            operatorName = _currentUser.value.name,
            timestamp = System.currentTimeMillis()
        )
        _movements.value = listOf(movement) + _movements.value
        database?.let { db ->
            repoScope.launch {
                db.stockMovementDao().insert(movement.toEntity(bizId))
            }
        }

        // Check stock thresholds to trigger notification if needed
        if (newStock == 0) {
            addSystemNotification(
                title = "Artigo Esgotado",
                message = "${currentProduct.name} esgotou completamente no stock.",
                type = NotificationType.ESGOTADO
            )
        } else if (newStock <= currentProduct.minStock) {
            addSystemNotification(
                title = "Alerta de Stock Baixo",
                message = "${currentProduct.name} atingiu $newStock unidades (mínimo de segurança: ${currentProduct.minStock}).",
                type = NotificationType.STOCK_BAIXO
            )
        }
    }

    fun registerSale(
        items: List<CartItem>,
        discount: Double,
        paymentMethod: PaymentMethod,
        amountPaid: Double,
        client: Client?,
        paymentRef: String?,
        dueDate: Long?,
        notes: String
    ): Sale {
        val subtotal = items.sumOf { it.subtotal }
        val finalTotal = (subtotal - discount).coerceAtLeast(0.0)
        val change = if (paymentMethod == PaymentMethod.DINHEIRO) {
            (amountPaid - finalTotal).coerceAtLeast(0.0)
        } else {
            0.0
        }

        val receiptNumber = "REC-2026-${(1000..9999).random()}"
        val saleTimestamp = System.currentTimeMillis()

        val sale = Sale(
            id = "sale-${UUID.randomUUID().toString().take(8)}",
            receiptNumber = receiptNumber,
            timestamp = saleTimestamp,
            items = items,
            subtotal = subtotal,
            discount = discount,
            total = finalTotal,
            paymentMethod = paymentMethod,
            amountPaid = if (paymentMethod == PaymentMethod.FIADO) amountPaid else (if (amountPaid > 0) amountPaid else finalTotal),
            change = change,
            customerId = client?.id,
            customerName = client?.name,
            customerPhone = client?.phone,
            operatorName = _currentUser.value.name,
            paymentReference = paymentRef,
            dueDate = dueDate,
            notes = notes
        )

        val bizId = currentBusinessId()
        _sales.value = listOf(sale) + _sales.value

        database?.let { db ->
            repoScope.launch {
                db.saleDao().insertSale(sale.toEntity(bizId))
                db.syncQueueDao().enqueue(
                    SyncQueueEntity(
                        businessId = bizId,
                        entityType = "SALE",
                        entityId = sale.id,
                        operation = "UPSERT",
                        payloadJson = ""
                    )
                )
                if (_isOnline.value) {
                    syncManager?.drainQueue(bizId)
                }
            }
        }

        items.forEach { item ->
            val p = _products.value.find { it.id == item.product.id }
            if (p != null) {
                val oldStock = p.currentStock
                val newStock = (oldStock - item.quantity).coerceAtLeast(0)
                updateProduct(p.copy(currentStock = newStock))

                val mov = StockMovement(
                    id = "mov-${UUID.randomUUID().toString().take(8)}",
                    productId = p.id,
                    productName = p.name,
                    type = MovementType.VENDA,
                    quantity = item.quantity,
                    previousStock = oldStock,
                    newStock = newStock,
                    reason = "Venda #$receiptNumber",
                    operatorName = _currentUser.value.name,
                    timestamp = saleTimestamp
                )
                _movements.value = listOf(mov) + _movements.value
                database?.let { db ->
                    repoScope.launch {
                        db.stockMovementDao().insert(mov.toEntity(bizId))
                    }
                }

                if (newStock == 0) {
                    addSystemNotification(
                        title = "Artigo Esgotado",
                        message = "${p.name} esgotou no stock após venda recente.",
                        type = NotificationType.ESGOTADO
                    )
                } else if (newStock <= p.minStock) {
                    addSystemNotification(
                        title = "Alerta de Stock Baixo",
                        message = "${p.name} tem agora $newStock un (mínimo: ${p.minStock}).",
                        type = NotificationType.STOCK_BAIXO
                    )
                }
            }
        }

        if (paymentMethod == PaymentMethod.FIADO && client != null) {
            val debtAmount = (finalTotal - amountPaid).coerceAtLeast(0.0)
            if (debtAmount > 0.0) {
                val newDebt = DebtRecord(
                    id = "debt-${UUID.randomUUID().toString().take(8)}",
                    saleId = sale.id,
                    receiptNumber = receiptNumber,
                    clientId = client.id,
                    clientName = client.name,
                    clientPhone = client.phone,
                    originalAmount = debtAmount,
                    remainingBalance = debtAmount,
                    createdAt = saleTimestamp,
                    dueDate = dueDate ?: (saleTimestamp + (86400_000L * 7)),
                    payments = emptyList()
                )
                _debts.value = listOf(newDebt) + _debts.value

                _clients.value = _clients.value.map { c ->
                    if (c.id == client.id) c.copy(totalDebt = c.totalDebt + debtAmount) else c
                }

                database?.let { db ->
                    repoScope.launch {
                        db.debtDao().insertOrUpdate(newDebt.toEntity(bizId))
                        db.clientDao().updateDebt(bizId, client.id, client.totalDebt + debtAmount)
                        db.syncQueueDao().enqueue(
                            SyncQueueEntity(
                                businessId = bizId,
                                entityType = "DEBT",
                                entityId = newDebt.id,
                                operation = "UPSERT",
                                payloadJson = ""
                            )
                        )
                        db.syncQueueDao().enqueue(
                            SyncQueueEntity(
                                businessId = bizId,
                                entityType = "CLIENT",
                                entityId = client.id,
                                operation = "UPSERT",
                                payloadJson = ""
                            )
                        )
                        if (_isOnline.value) {
                            syncManager?.drainQueue(bizId)
                        }
                    }
                }
            }
        }

        return sale
    }

    fun recordDebtPayment(debtId: String, amount: Double, paymentMethod: PaymentMethod, notes: String) {
        val debt = _debts.value.find { it.id == debtId } ?: return
        val payment = DebtPayment(
            id = "pay-${UUID.randomUUID().toString().take(8)}",
            date = System.currentTimeMillis(),
            amount = amount,
            paymentMethod = paymentMethod,
            notes = notes
        )

        val newBalance = (debt.remainingBalance - amount).coerceAtLeast(0.0)
        val updatedDebt = debt.copy(
            remainingBalance = newBalance,
            payments = debt.payments + payment
        )

        _debts.value = _debts.value.map {
            if (it.id == debtId) updatedDebt else it
        }

        _clients.value = _clients.value.map { c ->
            if (c.id == debt.clientId) {
                c.copy(totalDebt = (c.totalDebt - amount).coerceAtLeast(0.0))
            } else c
        }

        val bizId = currentBusinessId()
        database?.let { db ->
            repoScope.launch {
                db.debtDao().updateBalanceAndPayments(
                    businessId = bizId,
                    id = debtId,
                    balance = newBalance,
                    paymentsJson = com.example.data.local.converters.RoomMappers.serializeDebtPayments(updatedDebt.payments)
                )
                val c = _clients.value.find { it.id == debt.clientId }
                if (c != null) {
                    db.clientDao().updateDebt(bizId, c.id, c.totalDebt)
                }
                db.syncQueueDao().enqueue(
                    SyncQueueEntity(
                        businessId = bizId,
                        entityType = "DEBT",
                        entityId = debtId,
                        operation = "UPSERT",
                        payloadJson = ""
                    )
                )
                if (_isOnline.value) {
                    syncManager?.drainQueue(bizId)
                }
            }
        }

        addSystemNotification(
            title = "Pagamento de Fiado Recebido",
            message = "Recebido ${amount} MT de ${debt.clientName} via ${paymentMethod.displayName}.",
            type = NotificationType.PAGAMENTO_RECEBIDO
        )
    }

    fun addClient(name: String, phone: String, address: String, creditLimit: Double, notes: String): Client {
        val newClient = Client(
            id = "cli-${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            phone = phone.trim(),
            address = address.trim(),
            creditLimit = creditLimit,
            totalDebt = 0.0,
            notes = notes.trim(),
            createdAt = System.currentTimeMillis()
        )
        _clients.value = listOf(newClient) + _clients.value.filter { it.id != newClient.id }

        val bizId = currentBusinessId()
        database?.let { db ->
            repoScope.launch {
                db.clientDao().insertOrUpdate(newClient.toEntity(bizId))
                db.syncQueueDao().enqueue(
                    SyncQueueEntity(
                        businessId = bizId,
                        entityType = "CLIENT",
                        entityId = newClient.id,
                        operation = "UPSERT",
                        payloadJson = ""
                    )
                )
                if (_isOnline.value) {
                    syncManager?.drainQueue(bizId)
                }
            }
        }
        return newClient
    }

    fun updateClient(client: Client) {
        _clients.value = _clients.value.map {
            if (it.id == client.id) client else it
        }

        val bizId = currentBusinessId()
        database?.let { db ->
            repoScope.launch {
                db.clientDao().insertOrUpdate(client.toEntity(bizId))
                db.syncQueueDao().enqueue(
                    SyncQueueEntity(
                        businessId = bizId,
                        entityType = "CLIENT",
                        entityId = client.id,
                        operation = "UPSERT",
                        payloadJson = ""
                    )
                )
                if (_isOnline.value) {
                    syncManager?.drainQueue(bizId)
                }
            }
        }
    }

    fun deleteClient(clientId: String) {
        _clients.value = _clients.value.filter { it.id != clientId }

        val bizId = currentBusinessId()
        database?.let { db ->
            repoScope.launch {
                db.clientDao().deleteClient(bizId, clientId)
                db.syncQueueDao().enqueue(
                    SyncQueueEntity(
                        businessId = bizId,
                        entityType = "CLIENT",
                        entityId = clientId,
                        operation = "DELETE",
                        payloadJson = ""
                    )
                )
                if (_isOnline.value) {
                    syncManager?.drainQueue(bizId)
                }
            }
        }
    }

    // EXPENSES METHODS (Module 8)
    fun addExpense(
        description: String,
        amount: Double,
        category: ExpenseCategory,
        paymentMethod: PaymentMethod,
        date: Long,
        notes: String
    ): Expense {
        val expense = Expense(
            id = "exp-${UUID.randomUUID().toString().take(8)}",
            description = description.trim(),
            amount = amount,
            category = category,
            paymentMethod = paymentMethod,
            date = date,
            notes = notes.trim(),
            recordedBy = _currentUser.value.name
        )
        _expenses.value = listOf(expense) + _expenses.value.filter { it.id != expense.id }

        val bizId = currentBusinessId()
        database?.let { db ->
            repoScope.launch {
                db.expenseDao().insert(expense.toEntity(bizId))
                db.syncQueueDao().enqueue(
                    SyncQueueEntity(
                        businessId = bizId,
                        entityType = "EXPENSE",
                        entityId = expense.id,
                        operation = "UPSERT",
                        payloadJson = ""
                    )
                )
                if (_isOnline.value) {
                    syncManager?.drainQueue(bizId)
                }
            }
        }

        return expense
    }

    fun deleteExpense(id: String) {
        _expenses.value = _expenses.value.filter { it.id != id }

        val bizId = currentBusinessId()
        database?.let { db ->
            repoScope.launch {
                db.expenseDao().delete(bizId, id)
                db.syncQueueDao().enqueue(
                    SyncQueueEntity(
                        businessId = bizId,
                        entityType = "EXPENSE",
                        entityId = id,
                        operation = "DELETE",
                        payloadJson = ""
                    )
                )
                if (_isOnline.value) {
                    syncManager?.drainQueue(bizId)
                }
            }
        }
    }

    // SUPPLIER METHODS (Module 9)
    fun addSupplier(name: String, phone: String, address: String, categories: String, notes: String): Supplier {
        val supplier = Supplier(
            id = "sup-${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            phone = phone.trim(),
            address = address.trim(),
            suppliedCategories = categories.ifBlank { "Geral" },
            totalDebt = 0.0,
            notes = notes.trim(),
            createdAt = System.currentTimeMillis()
        )
        _suppliers.value = listOf(supplier) + _suppliers.value.filter { it.id != supplier.id }

        val bizId = currentBusinessId()
        database?.let { db ->
            repoScope.launch {
                db.supplierDao().insertOrUpdate(supplier.toEntity(bizId))
                db.syncQueueDao().enqueue(
                    SyncQueueEntity(
                        businessId = bizId,
                        entityType = "SUPPLIER",
                        entityId = supplier.id,
                        operation = "UPSERT",
                        payloadJson = ""
                    )
                )
                if (_isOnline.value) {
                    syncManager?.drainQueue(bizId)
                }
            }
        }
        return supplier
    }

    fun registerSupplierPurchase(
        supplier: Supplier,
        items: List<PurchaseItem>,
        amountPaid: Double,
        paymentMethod: PaymentMethod,
        invoiceNumber: String,
        notes: String
    ): SupplierPurchase {
        val totalCost = items.sumOf { it.subtotal }
        val debt = (totalCost - amountPaid).coerceAtLeast(0.0)

        val purchase = SupplierPurchase(
            id = "pur-${UUID.randomUUID().toString().take(8)}",
            supplierId = supplier.id,
            supplierName = supplier.name,
            date = System.currentTimeMillis(),
            items = items,
            totalCost = totalCost,
            amountPaid = amountPaid,
            debtRemaining = debt,
            paymentMethod = paymentMethod,
            receiptOrInvoiceNumber = invoiceNumber,
            notes = notes
        )
        _supplierPurchases.value = listOf(purchase) + _supplierPurchases.value

        val bizId = currentBusinessId()
        database?.let { db ->
            repoScope.launch {
                db.supplierDao().insertPurchase(purchase.toEntity(bizId))
                db.syncQueueDao().enqueue(
                    SyncQueueEntity(
                        businessId = bizId,
                        entityType = "PURCHASE",
                        entityId = purchase.id,
                        operation = "UPSERT",
                        payloadJson = ""
                    )
                )
                if (_isOnline.value) {
                    syncManager?.drainQueue(bizId)
                }
            }
        }

        // Increase stock automatically!
        items.forEach { item ->
            val p = _products.value.find { it.id == item.productId }
            if (p != null) {
                val oldStock = p.currentStock
                val newStock = oldStock + item.quantity
                updateProduct(p.copy(currentStock = newStock, costPrice = item.unitCost))

                val mov = StockMovement(
                    id = "mov-${UUID.randomUUID().toString().take(8)}",
                    productId = p.id,
                    productName = p.name,
                    type = MovementType.ENTRADA,
                    quantity = item.quantity,
                    previousStock = oldStock,
                    newStock = newStock,
                    reason = "Compra ao fornecedor ${supplier.name}",
                    operatorName = _currentUser.value.name,
                    timestamp = System.currentTimeMillis()
                )
                _movements.value = listOf(mov) + _movements.value
                database?.let { db ->
                    repoScope.launch {
                        db.stockMovementDao().insert(mov.toEntity(bizId))
                    }
                }
            }
        }

        // If debt remained, increase supplier total debt
        if (debt > 0.0) {
            val newTotalDebt = supplier.totalDebt + debt
            _suppliers.value = _suppliers.value.map { s ->
                if (s.id == supplier.id) s.copy(totalDebt = newTotalDebt) else s
            }
            database?.let { db ->
                repoScope.launch {
                    val sEntity = db.supplierDao().getSuppliers(bizId).find { it.id == supplier.id }
                    if (sEntity != null) {
                        db.supplierDao().insertOrUpdate(sEntity.copy(totalDebt = newTotalDebt, updatedAt = System.currentTimeMillis()))
                    }
                }
            }
            addSystemNotification(
                title = "Nova Conta a Pagar",
                message = "Dívida de ${debt} MT registada para o fornecedor ${supplier.name}.",
                type = NotificationType.FORNECEDOR_PENDENTE
            )
        }

        return purchase
    }

    fun paySupplierDebt(supplierId: String, amount: Double, paymentMethod: PaymentMethod) {
        val s = _suppliers.value.find { it.id == supplierId } ?: return
        val newDebt = (s.totalDebt - amount).coerceAtLeast(0.0)
        _suppliers.value = _suppliers.value.map {
            if (it.id == supplierId) it.copy(totalDebt = newDebt) else it
        }

        val bizId = currentBusinessId()
        database?.let { db ->
            repoScope.launch {
                val sEntity = db.supplierDao().getSuppliers(bizId).find { it.id == supplierId }
                if (sEntity != null) {
                    db.supplierDao().insertOrUpdate(sEntity.copy(totalDebt = newDebt, updatedAt = System.currentTimeMillis()))
                }
            }
        }

        // Also add as an operational expense so it reflects on finances
        addExpense(
            description = "Pagamento Dívida Fornecedor: ${s.name}",
            amount = amount,
            category = ExpenseCategory.OUTRAS,
            paymentMethod = paymentMethod,
            date = System.currentTimeMillis(),
            notes = "Amortização de fornecedor"
        )
    }

    // NOTIFICATIONS METHODS (Module 12)
    fun addSystemNotification(title: String, message: String, type: NotificationType) {
        val notif = AppNotification(
            id = "notif-${UUID.randomUUID().toString().take(8)}",
            title = title,
            message = message,
            type = type,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        _notifications.value = listOf(notif) + _notifications.value

        val bizId = currentBusinessId()
        database?.let { db ->
            repoScope.launch {
                db.notificationDao().insert(notif.toEntity(bizId))
            }
        }
    }

    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        val bizId = currentBusinessId()
        database?.let { db ->
            repoScope.launch {
                db.notificationDao().markAsRead(bizId, id)
            }
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun deleteNotification(id: String) {
        _notifications.value = _notifications.value.filter { it.id != id }
    }

    // CONFIGURABLE PAYMENT ACCOUNTS (M-Pesa, mKesh & e-Mola) & WHATSAPP SUPPORT
    private val _platformPaymentAccounts = MutableStateFlow<List<PlatformPaymentAccount>>(
        listOf(
            PlatformPaymentAccount(
                id = "acc-mpesa",
                method = PaymentMethod.MPESA,
                accountNumber = "84 682 1128",
                accountHolder = "Robson Manuel"
            ),
            PlatformPaymentAccount(
                id = "acc-mkesh",
                method = PaymentMethod.MKESH,
                accountNumber = "83 568 1467",
                accountHolder = "Robson Rebo"
            )
        )
    )
    val platformPaymentAccounts: StateFlow<List<PlatformPaymentAccount>> = _platformPaymentAccounts.asStateFlow()

    private val _whatsappSupportNumber = MutableStateFlow("+258 83 568 1467")
    val whatsappSupportNumber: StateFlow<String> = _whatsappSupportNumber.asStateFlow()

    private val _mpesaPaymentNumber = MutableStateFlow("84 682 1128")
    val mpesaPaymentNumber: StateFlow<String> = _mpesaPaymentNumber.asStateFlow()

    private val _emolaPaymentNumber = MutableStateFlow("86 752 9000")
    val emolaPaymentNumber: StateFlow<String> = _emolaPaymentNumber.asStateFlow()

    private val _paymentAccountHolder = MutableStateFlow("Robson Manuel")
    val paymentAccountHolder: StateFlow<String> = _paymentAccountHolder.asStateFlow()

    fun updatePlatformPaymentAccounts(newAccounts: List<PlatformPaymentAccount>, whatsappSupport: String) {
        _platformPaymentAccounts.value = newAccounts
        _whatsappSupportNumber.value = whatsappSupport

        newAccounts.find { it.method == PaymentMethod.MPESA }?.let {
            _mpesaPaymentNumber.value = it.accountNumber
            _paymentAccountHolder.value = it.accountHolder
        }
        newAccounts.find { it.method == PaymentMethod.EMOLA }?.let {
            _emolaPaymentNumber.value = it.accountNumber
        }

        addAuditLog(
            action = AuditAction.CHANGE_PERMISSIONS,
            targetItem = "Contas de Pagamento e Suporte",
            details = "Contas de pagamento (M-Pesa, mKesh, e-Mola) e número de suporte WhatsApp ($whatsappSupport) atualizados com registo de auditoria."
        )
    }

    fun updatePaymentAccounts(mpesa: String, emola: String, holder: String) {
        _mpesaPaymentNumber.value = mpesa
        _emolaPaymentNumber.value = emola
        _paymentAccountHolder.value = holder
        _platformPaymentAccounts.value = listOf(
            PlatformPaymentAccount("acc-mpesa", PaymentMethod.MPESA, mpesa, holder),
            PlatformPaymentAccount("acc-mkesh", PaymentMethod.MKESH, "83 568 1467", "Robson Rebo"),
            PlatformPaymentAccount("acc-emola", PaymentMethod.EMOLA, emola, holder)
        )
        addAuditLog(
            action = AuditAction.CHANGE_PERMISSIONS,
            targetItem = "Configurações de Pagamento",
            details = "Números M-Pesa ($mpesa) e e-Mola ($emola) actualizados."
        )
    }

    // AUDIT LOGS (Module 16)
    private val _auditLogs = MutableStateFlow<List<AuditLog>>(
        listOf(
            AuditLog(
                id = "log-1",
                businessId = "store-matriz-1",
                userId = "usr-owner-1",
                userName = "Sr. Momade Chale",
                userRole = "DONO",
                action = AuditAction.CREATE_PRODUCT,
                targetItem = "Arroz Touro Branco 25kg",
                details = "Produto cadastrado com stock inicial de 18 un.",
                timestamp = System.currentTimeMillis() - 86400_000L * 3
            ),
            AuditLog(
                id = "log-2",
                businessId = "store-matriz-1",
                userId = "usr-owner-1",
                userName = "Sr. Momade Chale",
                userRole = "DONO",
                action = AuditAction.PURCHASE_STOCK,
                targetItem = "Fornecedor Grossista",
                details = "Entrada de mercadorias no valor de 35.000 MT.",
                timestamp = System.currentTimeMillis() - 86400_000L * 2
            ),
            AuditLog(
                id = "log-3",
                businessId = "store-matriz-1",
                userId = "usr-cashier-1",
                userName = "Adélia Cossa",
                userRole = "CAIXA",
                action = AuditAction.STOCK_ADJUSTMENT,
                targetItem = "Venda #1021",
                details = "Abatimento de stock de 2 un após venda em dinheiro.",
                timestamp = System.currentTimeMillis() - 3600_000L * 5
            )
        )
    )
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    fun addAuditLog(action: AuditAction, targetItem: String, details: String) {
        val user = _currentUser.value
        val log = AuditLog(
            id = "audit-${UUID.randomUUID().toString().take(8)}",
            businessId = "store-matriz-1",
            userId = user.id,
            userName = user.name,
            userRole = user.role.name,
            action = action,
            targetItem = targetItem,
            details = details,
            timestamp = System.currentTimeMillis()
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }

    // AI BUSINESS INTELLIGENCE (Module 11)
    private val geminiService = GeminiBusinessService()

    private val _latestAiSummary = MutableStateFlow<AiBusinessSummary?>(null)
    val latestAiSummary: StateFlow<AiBusinessSummary?> = _latestAiSummary.asStateFlow()

    private val _aiStatusState = MutableStateFlow(
        AiStatusState(
            isConnected = true,
            modelName = GeminiBusinessService.MODEL_NAME,
            lastResponseTimestamp = System.currentTimeMillis()
        )
    )
    val aiStatusState: StateFlow<AiStatusState> = _aiStatusState.asStateFlow()

    private val _aiChatHistory = MutableStateFlow<List<AiChatMessage>>(
        listOf(
            AiChatMessage(
                id = "ai-msg-1",
                senderName = "Assistente Nexora",
                content = "Olá! Sou o teu Assistente Inteligente empresarial. Podes fazer-me perguntas sobre as tuas vendas, despesas, lucros ou que produtos deves encomendar hoje.",
                isFromAi = true,
                timestamp = System.currentTimeMillis()
            )
        )
    )
    val aiChatHistory: StateFlow<List<AiChatMessage>> = _aiChatHistory.asStateFlow()

    private val _monthlyAiUsage = MutableStateFlow(
        AiMonthlyUsage(monthYear = "2026-11", usedCount = 0, maxLimit = 100)
    )
    val monthlyAiUsage: StateFlow<AiMonthlyUsage> = _monthlyAiUsage.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    suspend fun testAiConnection(): Result<String> {
        _isAiLoading.value = true
        _aiError.value = null
        val result = geminiService.testAiConnection()
        _isAiLoading.value = false

        return result.onSuccess { text ->
            _aiStatusState.value = _aiStatusState.value.copy(
                isConnected = true,
                lastResponseTimestamp = System.currentTimeMillis(),
                lastTestResult = text,
                technicalErrorText = null
            )
            addAuditLog(
                action = AuditAction.EXPORT_BACKUP,
                targetItem = "Diagnóstico IA",
                details = "Teste de ligação com o modelo ${GeminiBusinessService.MODEL_NAME} bem-sucedido: '$text'."
            )
        }.onFailure { err ->
            val errorMsg = err.localizedMessage ?: err.toString()
            _aiStatusState.value = _aiStatusState.value.copy(
                isConnected = false,
                technicalErrorText = errorMsg
            )
            _aiError.value = "O assistente não está disponível agora. Tente mais tarde."
            addAuditLog(
                action = AuditAction.LOG_DATA_INCIDENT,
                targetItem = "Diagnóstico IA (Falha)",
                details = "Erro no teste do modelo ${GeminiBusinessService.MODEL_NAME}: $errorMsg"
            )
        }
    }

    suspend fun runBusinessAiAnalysis() {
        if (!_currentUser.value.canViewProfits) {
            _aiError.value = "Apenas o Proprietário (Dono) tem acesso às análises de inteligência artificial."
            return
        }

        val usage = _monthlyAiUsage.value
        val limit = _subscription.value.currentPlan.aiAnalysesLimit
        if (usage.usedCount >= limit) {
            _aiError.value = "Atingiste o limite mensal de análises de IA (${usage.usedCount}/$limit). Faz upgrade para o Plano Profissional."
            return
        }

        _isAiLoading.value = true
        _aiError.value = null

        val result = if (_sales.value.size < 10) {
            val localSummary = geminiService.generateRuleBasedSummary(
                storeName = _currentUser.value.storeName,
                products = _products.value,
                sales = _sales.value,
                expenses = _expenses.value,
                debts = _debts.value
            )
            Result.success(localSummary)
        } else {
            geminiService.analyzeBusinessData(
                storeName = _currentUser.value.storeName,
                products = _products.value,
                sales = _sales.value,
                expenses = _expenses.value,
                stockMovements = _movements.value
            )
        }

        _isAiLoading.value = false

        result.onSuccess { summary ->
            _latestAiSummary.value = summary
            _monthlyAiUsage.value = usage.copy(usedCount = usage.usedCount + 1, maxLimit = limit)
            _aiStatusState.value = _aiStatusState.value.copy(
                isConnected = true,
                lastResponseTimestamp = System.currentTimeMillis(),
                technicalErrorText = null
            )

            // Inject real-data based notification
            if (summary.alerts.isNotEmpty() && !summary.alerts.first().contains("Nenhum risco")) {
                addSystemNotification(
                    title = "Alerta de Negócio",
                    message = summary.alerts.first(),
                    type = NotificationType.IA_ALERTA
                )
            }
        }.onFailure { error ->
            val errorDetails = error.localizedMessage ?: error.toString()
            _aiError.value = "O assistente não está disponível agora. Tente mais tarde"
            _aiStatusState.value = _aiStatusState.value.copy(
                isConnected = false,
                technicalErrorText = errorDetails
            )
            // Plano B fallback on error
            val fallbackSummary = geminiService.generateRuleBasedSummary(
                storeName = _currentUser.value.storeName,
                products = _products.value,
                sales = _sales.value,
                expenses = _expenses.value,
                debts = _debts.value
            )
            _latestAiSummary.value = fallbackSummary
        }
    }

    suspend fun askBusinessAssistant(question: String) {
        if (question.isBlank()) return
        if (!_currentUser.value.canViewProfits) {
            _aiError.value = "Apenas o Dono pode consultar o Assistente Inteligente."
            return
        }

        val userMsg = AiChatMessage(
            senderName = _currentUser.value.name,
            content = question,
            isFromAi = false
        )
        _aiChatHistory.value = _aiChatHistory.value + userMsg

        _isAiLoading.value = true
        _aiError.value = null

        val result = geminiService.answerBusinessQuestion(
            question = question,
            storeName = _currentUser.value.storeName,
            products = _products.value,
            sales = _sales.value,
            expenses = _expenses.value,
            debts = _debts.value
        )

        _isAiLoading.value = false

        result.onSuccess { answer ->
            val aiMsg = AiChatMessage(
                senderName = "Assistente Nexora",
                content = answer,
                isFromAi = true
            )
            _aiChatHistory.value = _aiChatHistory.value + aiMsg
            _aiStatusState.value = _aiStatusState.value.copy(
                isConnected = true,
                lastResponseTimestamp = System.currentTimeMillis(),
                technicalErrorText = null
            )
        }.onFailure { err ->
            val errorMsg = err.localizedMessage ?: err.toString()
            _aiStatusState.value = _aiStatusState.value.copy(
                isConnected = false,
                technicalErrorText = errorMsg
            )
            val fallbackMsg = AiChatMessage(
                senderName = "Assistente Nexora",
                content = "O assistente não está disponível agora. Tente mais tarde.",
                isFromAi = true,
                isError = true
            )
            _aiChatHistory.value = _aiChatHistory.value + fallbackMsg
        }
    }

    // SUPER-ADMIN MANAGEMENT (Module 15)
    private val _allTenants = MutableStateFlow<List<BusinessTenant>>(emptyList())
    val allTenants: StateFlow<List<BusinessTenant>> = _allTenants.asStateFlow()

    private val _allPaymentProofs = MutableStateFlow<List<SubscriptionPaymentProof>>(emptyList())
    val allPaymentProofs: StateFlow<List<SubscriptionPaymentProof>> = _allPaymentProofs.asStateFlow()

    private val _supportTickets = MutableStateFlow<List<SupportTicket>>(emptyList())
    val supportTickets: StateFlow<List<SupportTicket>> = _supportTickets.asStateFlow()

    private val _pricingConfigs = MutableStateFlow<List<PlanPricingConfig>>(
        listOf(
            PlanPricingConfig(PlanTier.GRATUITO, 0.0, 50, 1, 5),
            PlanPricingConfig(PlanTier.BASICO, 1500.0, 300, 3, 25),
            PlanPricingConfig(PlanTier.PROFISSIONAL, 3500.0, 5000, 15, 100)
        )
    )
    val pricingConfigs: StateFlow<List<PlanPricingConfig>> = _pricingConfigs.asStateFlow()

    fun approvePaymentProof(proofId: String) {
        val proof = _allPaymentProofs.value.find { it.id == proofId } ?: return
        _allPaymentProofs.value = _allPaymentProofs.value.map {
            if (it.id == proofId) it.copy(status = PaymentProofStatus.APROVADO, reviewedAt = System.currentTimeMillis()) else it
        }

        // Activate business subscription automatically
        if (proof.businessId == "store-matriz-1" || proof.businessId == "tenant-1") {
            _subscription.value = _subscription.value.copy(
                currentPlan = proof.requestedPlan,
                status = SubscriptionStatus.ACTIVA,
                expiresAt = System.currentTimeMillis() + (30L * 86400_000L)
            )
            addSystemNotification(
                title = "Subscrição Aprovada!",
                message = "O teu comprovativo para o ${proof.requestedPlan.title} foi aprovado pela administração. Válido por 30 dias.",
                type = NotificationType.SISTEMA
            )
        }

        addAuditLog(
            action = AuditAction.APPROVE_PAYMENT,
            targetItem = "Comprovativo ${proof.transactionReference}",
            details = "Pagamento de ${proof.amountMT} MT aprovado para ${proof.businessName} (${proof.requestedPlan.title})."
        )
    }

    fun rejectPaymentProof(proofId: String, reason: String) {
        val proof = _allPaymentProofs.value.find { it.id == proofId } ?: return
        _allPaymentProofs.value = _allPaymentProofs.value.map {
            if (it.id == proofId) it.copy(status = PaymentProofStatus.REJEITADO, reviewedAt = System.currentTimeMillis(), rejectionReason = reason) else it
        }

        if (proof.businessId == "store-matriz-1" || proof.businessId == "tenant-1") {
            _subscription.value = _subscription.value.copy(
                status = SubscriptionStatus.EXPERIMENTAL,
                rejectionReason = reason
            )
            addSystemNotification(
                title = "Comprovativo Rejeitado",
                message = "O teu comprovativo foi rejeitado pelo seguinte motivo: $reason. Por favor submete uma referência válida.",
                type = NotificationType.SISTEMA
            )
        }

        addAuditLog(
            action = AuditAction.REJECT_PAYMENT,
            targetItem = "Comprovativo ${proof.transactionReference}",
            details = "Pagamento rejeitado para ${proof.businessName}. Motivo: $reason"
        )
    }

    fun toggleTenantStatus(tenantId: String) {
        _allTenants.value = _allTenants.value.map { tenant ->
            if (tenant.id == tenantId) {
                val newStatus = if (tenant.status == TenantStatus.ATIVA) TenantStatus.SUSPENSA else TenantStatus.ATIVA
                addAuditLog(
                    action = if (newStatus == TenantStatus.SUSPENSA) AuditAction.SUSPEND_STORE else AuditAction.REACTIVATE_STORE,
                    targetItem = tenant.name,
                    details = "Estado da empresa alterado para ${newStatus.label}."
                )
                tenant.copy(status = newStatus)
            } else tenant
        }
    }

    fun updatePlanPrice(plan: PlanTier, newPriceMT: Double, maxProducts: Int, maxUsers: Int, aiLimit: Int) {
        _pricingConfigs.value = _pricingConfigs.value.map {
            if (it.plan == plan) it.copy(priceMT = newPriceMT, maxProducts = maxProducts, maxUsers = maxUsers, aiAnalysesPerMonth = aiLimit)
            else it
        }
        addAuditLog(
            action = AuditAction.CHANGE_PERMISSIONS,
            targetItem = "Tabela de Preços ${plan.title}",
            details = "Novo valor: $newPriceMT MT / mês, Produtos: $maxProducts, Limite IA: $aiLimit."
        )
    }

    fun submitSupportTicket(subject: String, message: String, phone: String) {
        val ticket = SupportTicket(
            businessId = "store-matriz-1",
            businessName = _currentUser.value.storeName,
            contactPhone = phone,
            subject = subject,
            message = message,
            status = TicketStatus.ABERTO,
            createdAt = System.currentTimeMillis()
        )
        _supportTickets.value = listOf(ticket) + _supportTickets.value
        addSystemNotification(
            title = "Ticket de Suporte Enviado",
            message = "O teu pedido foi registado com sucesso. A equipa NEXORA responderá por SMS ou chamada.",
            type = NotificationType.SISTEMA
        )
    }

    fun replySupportTicket(ticketId: String, replyText: String) {
        _supportTickets.value = _supportTickets.value.map {
            if (it.id == ticketId) it.copy(status = TicketStatus.RESOLVIDO, replyText = replyText) else it
        }
    }

    // SESSIONS & PHONE AUTH (Module 13)
    private val _phoneCredentials = MutableStateFlow(
        PhoneAuthCredentials(
            phoneNumber = "",
            isVerified = false,
            recoveryCodes = emptyList(),
            biometricEnabled = false
        )
    )
    val phoneCredentials: StateFlow<PhoneAuthCredentials> = _phoneCredentials.asStateFlow()

    private val realDeviceSession = UserSession(
        id = "sess-current",
        deviceModel = "${android.os.Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} ${android.os.Build.MODEL} (Este Dispositivo)",
        platform = "Android ${android.os.Build.VERSION.RELEASE} (NEXORA App)",
        ipAddressOrCity = "Rede Móvel / Wi-Fi Moçambique",
        lastActive = System.currentTimeMillis(),
        isCurrent = true
    )

    private val _activeSessions = MutableStateFlow<List<UserSession>>(listOf(realDeviceSession))
    val activeSessions: StateFlow<List<UserSession>> = _activeSessions.asStateFlow()

    fun terminateSession(sessionId: String) {
        _activeSessions.value = _activeSessions.value.filter { it.id != sessionId }
        addAuditLog(
            action = AuditAction.CHANGE_PERMISSIONS,
            targetItem = "Sessão de Dispositivo",
            details = "Sessão $sessionId terminada remotamente."
        )
    }

    fun terminateAllOtherSessions() {
        _activeSessions.value = _activeSessions.value.filter { it.isCurrent }
        addAuditLog(
            action = AuditAction.CHANGE_PERMISSIONS,
            targetItem = "Sessões Remotas",
            details = "Todas as outras sessões foram encerradas."
        )
    }

    fun toggleBiometric(enabled: Boolean) {
        _phoneCredentials.value = _phoneCredentials.value.copy(biometricEnabled = enabled)
    }

    // AUTHENTICATION & LOGIN FLOW (FASE 5)
    private val _isLoggedIn = MutableStateFlow(authService.isUserLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _authScreenMode = MutableStateFlow(
        if (authService.isUserLoggedIn) com.example.data.model.AuthScreenMode.MAIN_APP
        else com.example.data.model.AuthScreenMode.LANDING
    )
    val authScreenMode: StateFlow<com.example.data.model.AuthScreenMode> = _authScreenMode.asStateFlow()

    private val _loginFailedAttempts = MutableStateFlow(0)
    val loginFailedAttempts: StateFlow<Int> = _loginFailedAttempts.asStateFlow()

    private val _lockoutTimeRemainingSeconds = MutableStateFlow(0)
    val lockoutTimeRemainingSeconds: StateFlow<Int> = _lockoutTimeRemainingSeconds.asStateFlow()

    private var lockoutEndTime = 0L

    fun isLockedOut(): Boolean {
        val remaining = lockoutEndTime - System.currentTimeMillis()
        if (remaining > 0) {
            _lockoutTimeRemainingSeconds.value = (remaining / 1000).toInt()
            return true
        }
        _lockoutTimeRemainingSeconds.value = 0
        return false
    }

    fun navigateAuthScreen(mode: com.example.data.model.AuthScreenMode) {
        _authScreenMode.value = mode
    }

    fun logout() {
        syncManager?.stopCloudListeners()
        activeBusinessJobs.forEach { it.cancel() }
        activeBusinessJobs.clear()
        authService.signOut()
        _isLoggedIn.value = false
        _authScreenMode.value = com.example.data.model.AuthScreenMode.LANDING
        _currentUser.value = emptyUser
        _allUsers.value = listOf(emptyUser)
        _products.value = emptyList()
        _sales.value = emptyList()
        _clients.value = emptyList()
        _debts.value = emptyList()
        _movements.value = emptyList()
        _expenses.value = emptyList()
        _suppliers.value = emptyList()
        _notifications.value = emptyList()
        _pendingSyncOperations.value = 0
        addAuditLog(
            action = AuditAction.CHANGE_PERMISSIONS,
            targetItem = "Sessão de Utilizador",
            details = "Terminou sessão no Firebase Authentication."
        )
    }

    suspend fun registerOwnerFirebase(
        name: String,
        phone: String,
        password: String,
        storeName: String,
        businessType: String,
        city: String,
        nuit: String,
        pin: String,
        acceptedTerms: Boolean,
        acceptedPrivacy: Boolean,
        recoveryCodes: List<String>
    ): Result<User> {
        return try {
            val authResult = authService.createAccountWithPhoneAndPassword(phone, password)
            if (authResult.isFailure) {
                return Result.failure(authResult.exceptionOrNull() ?: Exception("Falha ao criar conta no Firebase."))
            }
            val firebaseUser = authResult.getOrThrow()
            val uid = firebaseUser.uid
            val businessId = uid
            val cleanPhone = SecurityUtils.formatMozambiquePhone(phone)
            val hashedPin = SecurityUtils.hashPin(pin)
            val hashedPassword = if (password.isNotBlank()) SecurityUtils.hashPin(password) else ""

            val newOwner = User(
                id = uid,
                name = name.trim(),
                phone = cleanPhone,
                role = UserRole.DONO,
                pinHash = hashedPin,
                passwordHash = hashedPassword,
                recoveryCodes = recoveryCodes,
                storeName = storeName.trim(),
                storeLocation = city.trim(),
                businessType = businessType.ifBlank { "Comércio Geral" },
                businessId = businessId,
                nuit = nuit.trim(),
                currencySymbol = "MT",
                createdAt = System.currentTimeMillis()
            )

            // Reset operational data so new account begins 100% empty
            _products.value = emptyList()
            _sales.value = emptyList()
            _clients.value = emptyList()
            _debts.value = emptyList()
            _movements.value = emptyList()
            _expenses.value = emptyList()
            _suppliers.value = emptyList()
            _notifications.value = emptyList()

            // 14 days trial for Professional plan
            val trialSub = BusinessSubscription(
                currentPlan = PlanTier.PROFISSIONAL,
                status = SubscriptionStatus.EXPERIMENTAL,
                expiresAt = System.currentTimeMillis() + (14L * 86400_000L)
            )
            _subscription.value = trialSub

            _currentUser.value = newOwner
            _allUsers.value = listOf(newOwner)
            _isLoggedIn.value = true
            _authScreenMode.value = com.example.data.model.AuthScreenMode.MAIN_APP

            // Connect Room and Cloud observers for this new business tenant
            bindBusinessData(businessId)

            // Save Legal Consent
            saveUserConsent(
                acceptedTerms = acceptedTerms,
                acceptedPrivacy = acceptedPrivacy,
                acceptedAi = true,
                version = "1.0"
            )

            // Asynchronously sync multi-tenant document tree in Firestore
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val bizData = mapOf(
                    "id" to businessId,
                    "name" to storeName.trim(),
                    "ownerUid" to uid,
                    "ownerName" to name.trim(),
                    "phone" to cleanPhone,
                    "businessType" to businessType.ifBlank { "Comércio Geral" },
                    "city" to city.trim(),
                    "nuit" to nuit.trim(),
                    "currency" to "MT",
                    "createdAt" to System.currentTimeMillis()
                )
                db.collection("businesses").document(businessId).set(bizData)

                val memberData = mapOf(
                    "uid" to uid,
                    "name" to name.trim(),
                    "phone" to cleanPhone,
                    "role" to "owner",
                    "pinHash" to hashedPin,
                    "businessId" to businessId,
                    "createdAt" to System.currentTimeMillis()
                )
                db.collection("businesses").document(businessId)
                    .collection("members").document(uid).set(memberData)

                val subData = mapOf(
                    "currentPlan" to "PROFISSIONAL",
                    "status" to "EXPERIMENTAL",
                    "expiresAt" to trialSub.expiresAt,
                    "updatedAt" to System.currentTimeMillis()
                )
                db.collection("businesses").document(businessId)
                    .collection("subscription").document("current").set(subData)

                val consentData = mapOf(
                    "uid" to uid,
                    "businessId" to businessId,
                    "acceptedTerms" to acceptedTerms,
                    "acceptedPrivacy" to acceptedPrivacy,
                    "version" to "1.0",
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("businesses").document(businessId)
                    .collection("legal_consents").document("v1_0").set(consentData)
            } catch (e: Exception) {
                android.util.Log.w("NexoraRepository", "Firestore init sync: ${e.message}")
            }

            addAuditLog(
                action = AuditAction.CHANGE_PERMISSIONS,
                targetItem = "Conta Real Criada",
                details = "Comerciante $name registou $storeName com 14 dias de plano Profissional."
            )

            addSystemNotification(
                title = "Bem-vindo à NEXORA!",
                message = "Conta criada com sucesso. Tens 14 dias de teste do plano Profissional.",
                type = NotificationType.SISTEMA
            )

            Result.success(newOwner)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithFirebase(phone: String, secret: String, isPin: Boolean): Result<User> {
        if (isLockedOut()) {
            val mins = (_lockoutTimeRemainingSeconds.value / 60) + 1
            return Result.failure(Exception("Conta temporariamente bloqueada após 5 tentativas erradas. Aguarde $mins min."))
        }

        if (isPin) {
            val currUser = _currentUser.value
            val matchesPin = currUser.pinHash.isNotEmpty() && SecurityUtils.verifyPin(secret, currUser.pinHash)
            if (matchesPin) {
                _loginFailedAttempts.value = 0
                lockoutEndTime = 0L
                _lockoutTimeRemainingSeconds.value = 0
                _isLoggedIn.value = true
                _authScreenMode.value = com.example.data.model.AuthScreenMode.MAIN_APP
                bindBusinessData(currUser.businessId.ifBlank { currUser.id })
                addAuditLog(
                    action = AuditAction.DATA_ACCESS,
                    targetItem = "Autenticação PIN",
                    details = "Sessão validada por PIN local."
                )
                return Result.success(currUser)
            } else {
                _loginFailedAttempts.value += 1
                if (_loginFailedAttempts.value >= 5) {
                    lockoutEndTime = System.currentTimeMillis() + (15 * 60 * 1000L)
                    _lockoutTimeRemainingSeconds.value = 900
                    return Result.failure(Exception("5 tentativas erradas. Conta bloqueada por 15 minutos."))
                }
                return Result.failure(Exception("PIN incorrecto. Tentativa ${_loginFailedAttempts.value}/5."))
            }
        } else {
            val authResult = authService.signInWithPhoneAndPassword(phone, secret)
            if (authResult.isSuccess) {
                val fbUser = authResult.getOrThrow()
                _loginFailedAttempts.value = 0
                lockoutEndTime = 0L
                _lockoutTimeRemainingSeconds.value = 0

                val cleanPhone = SecurityUtils.formatMozambiquePhone(phone)
                val user = User(
                    id = fbUser.uid,
                    name = fbUser.displayName?.ifBlank { "Comerciante" } ?: "Comerciante",
                    phone = cleanPhone,
                    role = UserRole.DONO,
                    pinHash = "",
                    storeName = "NEXORA Loja",
                    storeLocation = "Moçambique",
                    businessId = fbUser.uid,
                    currencySymbol = "MT",
                    createdAt = System.currentTimeMillis()
                )
                _currentUser.value = user
                _allUsers.value = listOf(user)
                _isLoggedIn.value = true
                _authScreenMode.value = com.example.data.model.AuthScreenMode.MAIN_APP

                // Automatically restore/bind all business data from Room & Cloud
                bindBusinessData(user.businessId)

                addAuditLog(
                    action = AuditAction.DATA_ACCESS,
                    targetItem = "Autenticação Firebase",
                    details = "Login efetuado com sucesso para o telemóvel $cleanPhone."
                )
                return Result.success(user)
            } else {
                _loginFailedAttempts.value += 1
                if (_loginFailedAttempts.value >= 5) {
                    lockoutEndTime = System.currentTimeMillis() + (15 * 60 * 1000L)
                    _lockoutTimeRemainingSeconds.value = 900
                    return Result.failure(Exception("5 tentativas erradas. Conta bloqueada por 15 minutos."))
                }
                val ex = authResult.exceptionOrNull()
                return Result.failure(ex ?: Exception("Falha ao entrar na conta."))
            }
        }
    }

    fun registerOwner(
        name: String,
        phone: String,
        password: String,
        storeName: String,
        businessType: String,
        city: String,
        nuit: String,
        pin: String,
        acceptedTerms: Boolean,
        acceptedPrivacy: Boolean,
        recoveryCodes: List<String>
    ): User {
        val newBusinessId = "biz-${UUID.randomUUID().toString().take(8)}"
        val hashedPin = SecurityUtils.hashPin(pin)
        val newOwner = User(
            id = "usr-${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            phone = phone.trim(),
            role = UserRole.DONO,
            pinHash = hashedPin,
            passwordHash = if (password.isNotBlank()) SecurityUtils.hashPin(password) else "",
            recoveryCodes = recoveryCodes,
            storeName = storeName.trim(),
            storeLocation = city.trim(),
            businessType = businessType.ifBlank { "Mercearia" },
            businessId = newBusinessId,
            nuit = nuit.trim(),
            currencySymbol = "MT",
            createdAt = System.currentTimeMillis()
        )

        _currentUser.value = newOwner
        _allUsers.value = listOf(newOwner)
        _isLoggedIn.value = true
        _authScreenMode.value = com.example.data.model.AuthScreenMode.MAIN_APP

        // Bind multi-tenant data for newly created owner
        bindBusinessData(newOwner.businessId)

        // Provision 14-day free trial subscription
        _subscription.value = BusinessSubscription(
            currentPlan = PlanTier.GRATUITO,
            status = SubscriptionStatus.EXPERIMENTAL,
            expiresAt = System.currentTimeMillis() + (14L * 86400_000L)
        )

        // Save Legal Consent
        saveUserConsent(
            acceptedTerms = acceptedTerms,
            acceptedPrivacy = acceptedPrivacy,
            acceptedAi = true,
            version = "1.0"
        )

        addAuditLog(
            action = AuditAction.CHANGE_PERMISSIONS,
            targetItem = "Novo Comerciante Registado",
            details = "Empresa $storeName registada por $name com plano experimental de 14 dias."
        )

        addSystemNotification(
            title = "Bem-vindo à NEXORA!",
            message = "Conta criada com sucesso. Tens 14 dias de teste gratuito com todas as funcionalidades.",
            type = NotificationType.SISTEMA
        )

        return newOwner
    }

    fun loginUser(phone: String, method: com.example.ui.screens.auth.LoginMethod, credential: String): Result<User> {
        val formatted = SecurityUtils.formatMozambiquePhone(phone)
        val user = _allUsers.value.find { 
            SecurityUtils.formatMozambiquePhone(it.phone) == formatted || it.phone == phone 
        } ?: return Result.failure(Exception("Número não registado na NEXORA."))

        return when (method) {
            com.example.ui.screens.auth.LoginMethod.PASSWORD -> {
                val matchesPass = user.passwordHash.isNotEmpty() && SecurityUtils.verifyPin(credential, user.passwordHash)
                if (matchesPass) {
                    _currentUser.value = user
                    _isLoggedIn.value = true
                    _authScreenMode.value = com.example.data.model.AuthScreenMode.MAIN_APP
                    _loginFailedAttempts.value = 0
                    bindBusinessData(user.businessId.ifBlank { user.id })
                    Result.success(user)
                } else {
                    _loginFailedAttempts.value += 1
                    Result.failure(Exception("Palavra-passe incorrecta. Tentativa ${_loginFailedAttempts.value}/5."))
                }
            }
            com.example.ui.screens.auth.LoginMethod.PIN -> {
                val matchesPin = SecurityUtils.verifyPin(credential, user.pinHash)
                if (matchesPin) {
                    _currentUser.value = user
                    _isLoggedIn.value = true
                    _authScreenMode.value = com.example.data.model.AuthScreenMode.MAIN_APP
                    _loginFailedAttempts.value = 0
                    bindBusinessData(user.businessId.ifBlank { user.id })
                    Result.success(user)
                } else {
                    _loginFailedAttempts.value += 1
                    Result.failure(Exception("PIN incorrecto. Tentativa ${_loginFailedAttempts.value}/5."))
                }
            }
            com.example.ui.screens.auth.LoginMethod.BIOMETRIC -> {
                _currentUser.value = user
                _isLoggedIn.value = true
                _authScreenMode.value = com.example.data.model.AuthScreenMode.MAIN_APP
                _loginFailedAttempts.value = 0
                bindBusinessData(user.businessId.ifBlank { user.id })
                Result.success(user)
            }
        }
    }

    fun loginWithPinOrPassword(phone: String, secret: String): Result<User> {
        val formatted = SecurityUtils.formatMozambiquePhone(phone)
        val user = _allUsers.value.find { 
            SecurityUtils.formatMozambiquePhone(it.phone) == formatted || it.phone == phone 
        } ?: return Result.failure(Exception("Utilizador não encontrado com o telemóvel introduzido."))

        val matchesPin = SecurityUtils.verifyPin(secret, user.pinHash)
        val matchesPassword = user.passwordHash.isNotEmpty() && SecurityUtils.verifyPin(secret, user.passwordHash)

        if (matchesPin || matchesPassword) {
            _currentUser.value = user
            _isLoggedIn.value = true
            _authScreenMode.value = com.example.data.model.AuthScreenMode.MAIN_APP
            _loginFailedAttempts.value = 0
            bindBusinessData(user.businessId.ifBlank { user.id })
            addAuditLog(
                action = AuditAction.DATA_ACCESS,
                targetItem = "Autenticação",
                details = "Login efetuado com sucesso por ${user.name} (${user.role.label})."
            )
            return Result.success(user)
        } else {
            _loginFailedAttempts.value += 1
            if (_loginFailedAttempts.value >= 5) {
                _lockoutTimeRemainingSeconds.value = 900
            }
            return Result.failure(Exception("PIN ou Palavra-passe incorrecta. Tentativa ${_loginFailedAttempts.value}/5."))
        }
    }

    fun loginWithSmsVerified(phone: String): Result<User> {
        val formatted = SecurityUtils.formatMozambiquePhone(phone)
        val user = _allUsers.value.find { 
            SecurityUtils.formatMozambiquePhone(it.phone) == formatted || it.phone == phone 
        } ?: return Result.failure(Exception("Número não registado. Por favor clica em 'Criar conta'."))

        _currentUser.value = user
        _isLoggedIn.value = true
        _authScreenMode.value = com.example.data.model.AuthScreenMode.MAIN_APP
        bindBusinessData(user.businessId.ifBlank { user.id })
        return Result.success(user)
    }

    fun loginWithRecoveryCode(phone: String, code: String): Result<User> {
        val formatted = SecurityUtils.formatMozambiquePhone(phone)
        val user = _allUsers.value.find { 
            SecurityUtils.formatMozambiquePhone(it.phone) == formatted || it.phone == phone 
        } ?: return Result.failure(Exception("Utilizador não encontrado."))

        val cleanCode = code.trim().uppercase()
        if (user.recoveryCodes.contains(cleanCode) || _phoneCredentials.value.recoveryCodes.contains(cleanCode)) {
            val updatedCodes = user.recoveryCodes.filter { it != cleanCode }
            val updatedUser = user.copy(recoveryCodes = updatedCodes)
            _currentUser.value = updatedUser
            _allUsers.value = _allUsers.value.map { if (it.id == user.id) updatedUser else it }
            _isLoggedIn.value = true
            _authScreenMode.value = com.example.data.model.AuthScreenMode.MAIN_APP
            bindBusinessData(updatedUser.businessId.ifBlank { updatedUser.id })
            addAuditLog(
                action = AuditAction.DATA_ACCESS,
                targetItem = "Recuperação de Acesso",
                details = "Login efetuado utilizando Código de Emergência $cleanCode."
            )
            return Result.success(updatedUser)
        } else {
            return Result.failure(Exception("Código de recuperação inválido ou já utilizado."))
        }
    }

    fun resetPinWithSms(phone: String, smsCode: String = "", newPin: String): Result<Boolean> {
        val formatted = SecurityUtils.formatMozambiquePhone(phone)
        val user = _allUsers.value.find { 
            SecurityUtils.formatMozambiquePhone(it.phone) == formatted || it.phone == phone 
        } ?: return Result.failure(Exception("Utilizador não encontrado."))

        val hashedPin = SecurityUtils.hashPin(newPin)
        val updatedUser = user.copy(pinHash = hashedPin)
        _currentUser.value = updatedUser
        _allUsers.value = _allUsers.value.map { if (it.id == user.id) updatedUser else it }
        
        addAuditLog(
            action = AuditAction.CHANGE_PERMISSIONS,
            targetItem = "Alteração de PIN",
            details = "PIN redefinido com sucesso via SMS de verificação."
        )
        return Result.success(true)
    }

    fun changePhoneNumber(oldPhone: String, newPhone: String, smsCode: String = ""): Result<Boolean> {
        val formattedNew = SecurityUtils.formatMozambiquePhone(newPhone)
        val user = _currentUser.value
        val updatedUser = user.copy(phone = formattedNew)
        _currentUser.value = updatedUser
        _allUsers.value = _allUsers.value.map { if (it.id == user.id) updatedUser else it }
        _phoneCredentials.value = _phoneCredentials.value.copy(phoneNumber = formattedNew)

        addAuditLog(
            action = AuditAction.CHANGE_PERMISSIONS,
            targetItem = "Alteração de Telemóvel",
            details = "Número de telefone alterado de $oldPhone para $formattedNew."
        )
        return Result.success(true)
    }

    // BACKUP & EXPORT / RESTORE (Module 16)
    fun exportAllBusinessDataJson(): String {
        val root = JSONObject()
        root.put("version", "NEXORA_FASE_3_BACKUP")
        root.put("exportedAt", System.currentTimeMillis())
        root.put("storeName", _currentUser.value.storeName)

        val prodArr = JSONArray()
        _products.value.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("category", p.category)
            obj.put("costPrice", p.costPrice)
            obj.put("sellingPrice", p.sellingPrice)
            obj.put("currentStock", p.currentStock)
            obj.put("minStock", p.minStock)
            obj.put("unitType", p.unitType.name)
            prodArr.put(obj)
        }
        root.put("products", prodArr)

        val salesArr = JSONArray()
        _sales.value.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("total", s.total)
            obj.put("paymentMethod", s.paymentMethod.name)
            obj.put("timestamp", s.timestamp)
            salesArr.put(obj)
        }
        root.put("sales", salesArr)

        val expArr = JSONArray()
        _expenses.value.forEach { e ->
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("description", e.description)
            obj.put("amount", e.amount)
            obj.put("category", e.category.name)
            obj.put("date", e.date)
            expArr.put(obj)
        }
        root.put("expenses", expArr)

        addAuditLog(
            action = AuditAction.EXPORT_BACKUP,
            targetItem = "Backup Completo da Empresa",
            details = "Exportação de dados JSON (${_products.value.size} produtos, ${_sales.value.size} vendas, ${_expenses.value.size} despesas)."
        )

        return root.toString(2)
    }

    fun restoreBusinessDataFromJson(jsonStr: String): Result<Int> {
        return try {
            val root = JSONObject(jsonStr)
            val prodArr = root.optJSONArray("products") ?: JSONArray()
            val restoredList = mutableListOf<Product>()
            for (i in 0 until prodArr.length()) {
                val obj = prodArr.getJSONObject(i)
                restoredList.add(
                    Product(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        category = obj.optString("category", "Geral"),
                        description = "Restaurado de cópia de segurança",
                        unitType = try { UnitType.valueOf(obj.optString("unitType", "UNIDADE")) } catch (e: Exception) { UnitType.UNIDADE },
                        costPrice = obj.getDouble("costPrice"),
                        sellingPrice = obj.getDouble("sellingPrice"),
                        currentStock = obj.getInt("currentStock"),
                        minStock = obj.optInt("minStock", 5)
                    )
                )
            }
            if (restoredList.isNotEmpty()) {
                _products.value = restoredList
            }

            addAuditLog(
                action = AuditAction.RESTORE_BACKUP,
                targetItem = "Restauração de Dados",
                details = "Restaurados ${restoredList.size} produtos a partir do ficheiro de cópia de segurança."
            )

            Result.success(restoredList.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // SUBSCRIPTIONS METHODS (Module 14)
    fun submitSubscriptionPaymentProof(reference: String, method: PaymentMethod) {
        val plan = PlanTier.PROFISSIONAL
        val amount = plan.monthlyPriceMT
        val proof = SubscriptionPaymentProof(
            businessId = "store-matriz-1",
            businessName = _currentUser.value.storeName,
            requestedPlan = plan,
            amountMT = amount,
            paymentMethod = method,
            transactionReference = reference,
            destinationAccount = if (method == PaymentMethod.MPESA) _mpesaPaymentNumber.value else _emolaPaymentNumber.value,
            status = PaymentProofStatus.PENDENTE,
            submittedAt = System.currentTimeMillis()
        )

        _allPaymentProofs.value = listOf(proof) + _allPaymentProofs.value

        _subscription.value = _subscription.value.copy(
            status = SubscriptionStatus.PENDENTE_APROVACAO,
            paymentProofReference = reference,
            paymentProofMethod = method,
            proofSubmittedAt = System.currentTimeMillis(),
            rejectionReason = null
        )

        addSystemNotification(
            title = "Comprovativo Submetido",
            message = "O teu comprovativo de pagamento ($reference) foi enviado para o Super-Administrador NEXORA.",
            type = NotificationType.SISTEMA
        )

        addAuditLog(
            action = AuditAction.APPROVE_PAYMENT,
            targetItem = "Comprovativo Submetido",
            details = "Comprovativo $reference (${method.displayName}) enviado para validação."
        )
    }

    // LEGAL & PRIVACY COMPLIANCE (FASE 4 - Lei de Moçambique)

    private val _userConsents = MutableStateFlow<List<UserConsentRecord>>(emptyList())
    val userConsents: StateFlow<List<UserConsentRecord>> = _userConsents.asStateFlow()

    private val _privacySettings = MutableStateFlow(
        PrivacySettings(
            allowAiAnalysis = true,
            allowNotifications = true,
            acceptedConsentVersion = "1.0",
            aiDisclaimerAccepted = true
        )
    )
    val privacySettings: StateFlow<PrivacySettings> = _privacySettings.asStateFlow()

    private val _dataIncidents = MutableStateFlow<List<DataIncident>>(emptyList())
    val dataIncidents: StateFlow<List<DataIncident>> = _dataIncidents.asStateFlow()

    fun saveUserConsent(acceptedTerms: Boolean, acceptedPrivacy: Boolean, acceptedAi: Boolean, version: String = "1.0") {
        val user = _currentUser.value
        val newRecord = UserConsentRecord(
            id = "consent-${UUID.randomUUID().toString().take(8)}",
            userId = user.id,
            userPhone = user.phone,
            documentVersion = version,
            acceptedTerms = acceptedTerms,
            acceptedPrivacy = acceptedPrivacy,
            acceptedAiAnalysis = acceptedAi,
            acceptedNotifications = true,
            timestamp = System.currentTimeMillis()
        )
        _userConsents.value = listOf(newRecord) + _userConsents.value

        addAuditLog(
            action = AuditAction.UPDATE_PRIVACY,
            targetItem = "Consentimento Legal v$version",
            details = "Termos: $acceptedTerms, Privacidade: $acceptedPrivacy, IA: $acceptedAi"
        )
    }

    fun updatePrivacySettings(allowAiAnalysis: Boolean, allowNotifications: Boolean) {
        _privacySettings.value = PrivacySettings(
            allowAiAnalysis = allowAiAnalysis,
            allowNotifications = allowNotifications,
            acceptedConsentVersion = "1.0",
            aiDisclaimerAccepted = allowAiAnalysis
        )

        saveUserConsent(acceptedTerms = true, acceptedPrivacy = true, acceptedAi = allowAiAnalysis, version = "1.0")

        addAuditLog(
            action = AuditAction.UPDATE_PRIVACY,
            targetItem = "Definições de Privacidade",
            details = "IA: $allowAiAnalysis, Notificações: $allowNotifications"
        )
    }

    fun updateUserPersonalDetails(name: String, phone: String, storeName: String, storeLocation: String) {
        val current = _currentUser.value
        val updated = current.copy(
            name = name.trim(),
            phone = phone.trim(),
            storeName = storeName.trim(),
            storeLocation = storeLocation.trim()
        )
        _currentUser.value = updated
        _allUsers.value = _allUsers.value.map { if (it.id == updated.id) updated else it }

        addAuditLog(
            action = AuditAction.DATA_ACCESS,
            targetItem = "Dados Pessoais do Utilizador",
            details = "Nome: $name, Telefone: $phone, Loja: $storeName"
        )

        addSystemNotification(
            title = "Dados Pessoais Actualizados",
            message = "As tuas informações de perfil e identificação da loja foram actualizadas com sucesso.",
            type = NotificationType.SISTEMA
        )
    }

    fun exportPersonalAndStoreDataCsvJson(): String {
        val root = JSONObject()
        root.put("document_title", "Exportação Integrada de Dados Pessoais e Comerciais NEXORA")
        root.put("legal_disclaimer", "Conformidade com a Lei n.º 3/2017 e Constituição da República de Moçambique Art. 71")
        root.put("exported_at", System.currentTimeMillis())

        val userObj = JSONObject()
        val user = _currentUser.value
        userObj.put("user_id", user.id)
        userObj.put("name", user.name)
        userObj.put("phone", user.phone)
        userObj.put("role", user.role.name)
        userObj.put("store_name", user.storeName)
        userObj.put("store_location", user.storeLocation)
        userObj.put("currency_symbol", user.currencySymbol)
        root.put("user_profile", userObj)

        val consentArr = JSONArray()
        _userConsents.value.filter { it.userId == user.id }.forEach { c ->
            val cObj = JSONObject()
            cObj.put("version", c.documentVersion)
            cObj.put("acceptedTerms", c.acceptedTerms)
            cObj.put("acceptedPrivacy", c.acceptedPrivacy)
            cObj.put("acceptedAiAnalysis", c.acceptedAiAnalysis)
            cObj.put("timestamp", c.timestamp)
            consentArr.put(cObj)
        }
        root.put("consents_history", consentArr)

        val prodArr = JSONArray()
        _products.value.forEach { p ->
            val pObj = JSONObject()
            pObj.put("name", p.name)
            pObj.put("category", p.category)
            pObj.put("costPrice", p.costPrice)
            pObj.put("sellingPrice", p.sellingPrice)
            pObj.put("currentStock", p.currentStock)
            prodArr.put(pObj)
        }
        root.put("products_catalog", prodArr)

        val salesArr = JSONArray()
        _sales.value.forEach { s ->
            val sObj = JSONObject()
            sObj.put("receiptNumber", s.receiptNumber)
            sObj.put("total", s.total)
            sObj.put("paymentMethod", s.paymentMethod.displayName)
            sObj.put("customerName", s.customerName ?: "Consumidor Final")
            sObj.put("timestamp", s.timestamp)
            salesArr.put(sObj)
        }
        root.put("sales_records", salesArr)

        val debtsArr = JSONArray()
        _debts.value.forEach { d ->
            val dObj = JSONObject()
            dObj.put("clientName", d.clientName)
            dObj.put("remainingBalance", d.remainingBalance)
            dObj.put("dueDate", d.dueDate)
            debtsArr.put(dObj)
        }
        root.put("credit_debts", debtsArr)

        addAuditLog(
            action = AuditAction.DATA_ACCESS,
            targetItem = "Direito de Portabilidade de Dados",
            details = "Exportação de relatórios completos de dados pessoais e comerciais efetuada pelo utilizador."
        )

        return root.toString(2)
    }

    fun eraseAccountAndUserData(pin: String): Result<Boolean> {
        val user = _currentUser.value
        val isPinValid = SecurityUtils.verifyPin(pin, user.pinHash)
        if (!isPinValid) {
            return Result.failure(Exception("PIN incorrecto. A eliminação da conta requer confirmação por PIN."))
        }

        // Add audit log of account deletion prior to wiping state
        addAuditLog(
            action = AuditAction.ERASE_ACCOUNT,
            targetItem = "Conta de Utilizador ${user.name}",
            details = "Exercido o Direito ao Esquecimento. Dados pessoais apagados e registos comerciais anonimizados conforme retenção fiscal de 5 anos."
        )

        // Clear clients personal data, anonymize sales customer info
        _clients.value = emptyList()
        _sales.value = _sales.value.map { s ->
            s.copy(
                customerName = "Consumidor Anonimizado",
                customerPhone = null,
                notes = "Informação pessoal eliminada a pedido do utilizador."
            )
        }
        _debts.value = emptyList()

        // Reset user info to anonymized status
        _currentUser.value = user.copy(
            name = "Conta Eliminada",
            phone = "+258 00 000 0000",
            storeName = "Empresa Encerrada"
        )

        return Result.success(true)
    }

    fun logDataIncident(title: String, severity: IncidentSeverity, affectedCount: Int, description: String) {
        val user = _currentUser.value
        val newIncident = DataIncident(
            id = "inc-${UUID.randomUUID().toString().take(8)}",
            title = title.trim(),
            description = description.trim(),
            severity = severity,
            status = IncidentStatus.INVESTIGANDO,
            affectedCount = affectedCount,
            loggedBy = user.name,
            timestamp = System.currentTimeMillis(),
            isNotifiedToUsers = true
        )
        _dataIncidents.value = listOf(newIncident) + _dataIncidents.value

        addAuditLog(
            action = AuditAction.LOG_DATA_INCIDENT,
            targetItem = "Incidente de Segurança: $title",
            details = "Gravidade: ${severity.label}, Afectados: $affectedCount pessoas."
        )

        addSystemNotification(
            title = "Registo de Incidente de Dados",
            message = "Incidente de segurança registado pela administração: $title.",
            type = NotificationType.SISTEMA
        )
    }

    fun resolveDataIncident(incidentId: String) {
        _dataIncidents.value = _dataIncidents.value.map { inc ->
            if (inc.id == incidentId) {
                inc.copy(
                    status = IncidentStatus.MITIGADO
                )
            } else inc
        }
    }
}


