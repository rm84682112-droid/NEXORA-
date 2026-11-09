package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.firebase.configureAppCheck
import com.example.data.model.MovementType
import com.example.data.model.PaymentMethod
import com.example.data.model.UnitType
import com.example.data.repository.NexoraRepository
import com.example.ui.components.MainNavigationItem
import com.example.ui.components.NexoraBottomBar
import com.example.ui.components.NexoraTopBar
import com.example.ui.screens.admin.SuperAdminScreen
import com.example.ui.screens.ai.AiAssistantScreen
import com.example.ui.screens.clients.ClientsScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.debts.DebtsScreen
import com.example.ui.screens.expenses.ExpensesScreen
import com.example.ui.screens.more.MoreScreen
import com.example.ui.screens.notifications.NotificationsScreen
import com.example.ui.screens.pos.PosScreen
import com.example.ui.screens.products.ProductsScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.security.SecurityAndSyncScreen
import com.example.ui.screens.settings.PaymentAccountsConfigScreen
import com.example.ui.screens.stock.StockScreen
import com.example.ui.screens.subscription.SubscriptionScreen
import com.example.ui.screens.suppliers.SuppliersScreen
import com.example.ui.screens.team.TeamManagementScreen
import com.example.ui.screens.privacy.LegalDocumentViewerScreen
import com.example.ui.screens.privacy.LegalTab
import com.example.ui.screens.privacy.PrivacyAndDataScreen
import com.example.data.model.AuthScreenMode
import com.example.data.model.UserRole
import com.example.ui.theme.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.auth.AccessRecoveryScreen
import com.example.ui.screens.auth.AuthLandingScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.OwnerRegistrationWizardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyBackground

enum class SubScreen {
    NONE,
    CLIENTS,
    DEBTS,
    EXPENSES,
    SUPPLIERS,
    REPORTS,
    NOTIFICATIONS,
    SUBSCRIPTION,
    AI_ASSISTANT,
    SUPER_ADMIN,
    SECURITY_SYNC,
    PAYMENT_ACCOUNTS_CONFIG,
    TEAM_MANAGEMENT,
    PRIVACY_DATA,
    LEGAL_VIEWER
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NexoraApp()
            }
        }
    }
}

@Composable
fun NexoraApp() {
    val isLoggedIn by NexoraRepository.isLoggedIn.collectAsState()
    val authScreenMode by NexoraRepository.authScreenMode.collectAsState()
    val currentUser by NexoraRepository.currentUser.collectAsState()
    val allUsers by NexoraRepository.allUsers.collectAsState()
    val products by NexoraRepository.products.collectAsState()
    val clients by NexoraRepository.clients.collectAsState()
    val sales by NexoraRepository.sales.collectAsState()
    val debts by NexoraRepository.debts.collectAsState()
    val movements by NexoraRepository.movements.collectAsState()
    val expenses by NexoraRepository.expenses.collectAsState()
    val suppliers by NexoraRepository.suppliers.collectAsState()
    val notifications by NexoraRepository.notifications.collectAsState()
    val subscription by NexoraRepository.subscription.collectAsState()
    val isOnline by NexoraRepository.isOnline.collectAsState()
    val pendingSyncCount by NexoraRepository.pendingSyncOperations.collectAsState()
    val hasCompletedTour by NexoraRepository.hasCompletedTour.collectAsState()
    val helpVideosConfig by NexoraRepository.helpVideosConfig.collectAsState()

    var currentTab by remember { mutableStateOf(MainNavigationItem.DASHBOARD) }
    var currentSubScreen by remember { mutableStateOf(SubScreen.NONE) }
    var selectedLegalTab by remember { mutableStateOf(LegalTab.PRIVACIDADE) }
    var showScreenHelp by remember { mutableStateOf(false) }

    // Render Authentication flow if user is not logged in or authScreenMode is active
    if (!isLoggedIn || authScreenMode != AuthScreenMode.MAIN_APP) {
        when (authScreenMode) {
            AuthScreenMode.LANDING -> {
                AuthLandingScreen(
                    onNavigateToRegister = { NexoraRepository.navigateAuthScreen(AuthScreenMode.REGISTER_WIZARD) },
                    onNavigateToLogin = { NexoraRepository.navigateAuthScreen(AuthScreenMode.LOGIN) },
                    onNavigateToLegalViewer = { tab ->
                        selectedLegalTab = tab
                        currentSubScreen = SubScreen.LEGAL_VIEWER
                        NexoraRepository.navigateAuthScreen(AuthScreenMode.MAIN_APP)
                    }
                )
            }
            AuthScreenMode.REGISTER_WIZARD -> {
                OwnerRegistrationWizardScreen(
                    onBack = { NexoraRepository.navigateAuthScreen(AuthScreenMode.LANDING) },
                    onCompleteRegistration = {
                        currentTab = MainNavigationItem.DASHBOARD
                    },
                    onNavigateToLegalViewer = { tab ->
                        selectedLegalTab = tab
                        currentSubScreen = SubScreen.LEGAL_VIEWER
                        NexoraRepository.navigateAuthScreen(AuthScreenMode.MAIN_APP)
                    }
                )
            }
            AuthScreenMode.LOGIN -> {
                LoginScreen(
                    onBack = { NexoraRepository.navigateAuthScreen(AuthScreenMode.LANDING) },
                    onLoginSuccess = {
                        val user = NexoraRepository.currentUser.value
                        if (user.role == UserRole.CAIXA) {
                            currentTab = MainNavigationItem.VENDAS
                        } else {
                            currentTab = MainNavigationItem.DASHBOARD
                        }
                    },
                    onNavigateToRecovery = { NexoraRepository.navigateAuthScreen(AuthScreenMode.ACCESS_RECOVERY) },
                    onNavigateToRegister = { NexoraRepository.navigateAuthScreen(AuthScreenMode.REGISTER_WIZARD) }
                )
            }
            AuthScreenMode.ACCESS_RECOVERY -> {
                AccessRecoveryScreen(
                    onBack = { NexoraRepository.navigateAuthScreen(AuthScreenMode.LOGIN) },
                    onRecoverySuccess = {
                        val user = NexoraRepository.currentUser.value
                        if (user.role == UserRole.CAIXA) {
                            currentTab = MainNavigationItem.VENDAS
                        } else {
                            currentTab = MainNavigationItem.DASHBOARD
                        }
                    }
                )
            }
            AuthScreenMode.MAIN_APP -> {
                AuthLandingScreen(
                    onNavigateToRegister = { NexoraRepository.navigateAuthScreen(AuthScreenMode.REGISTER_WIZARD) },
                    onNavigateToLogin = { NexoraRepository.navigateAuthScreen(AuthScreenMode.LOGIN) },
                    onNavigateToLegalViewer = { tab ->
                        selectedLegalTab = tab
                        currentSubScreen = SubScreen.LEGAL_VIEWER
                        NexoraRepository.navigateAuthScreen(AuthScreenMode.MAIN_APP)
                    }
                )
            }
        }
        return
    }

    // Intercept Back button when inside a subscreen
    BackHandler(enabled = currentSubScreen != SubScreen.NONE) {
        currentSubScreen = SubScreen.NONE
    }

    val lowStockCount = remember(products) {
        products.count { it.isLowStock || it.isOutOfStock }
    }
    val activeDebtsCount = remember(debts) {
        debts.count { it.remainingBalance > 0 }
    }
    val unreadNotificationsCount = remember(notifications) {
        notifications.count { !it.isRead }
    }

    // Onboarding Tour for Owners on first entrance
    if (com.example.config.OnboardingConfig.ONBOARDING_TOUR && !hasCompletedTour && currentUser.role == com.example.data.model.UserRole.DONO) {
        com.example.ui.screens.onboarding.OnboardingTourDialog(
            onDismiss = { NexoraRepository.completeTour() },
            onComplete = { NexoraRepository.completeTour() }
        )
    }

    // Screen Help Dialog
    if (showScreenHelp) {
        val (helpTitle, helpBullets, helpKey) = when {
            currentSubScreen == SubScreen.NOTIFICATIONS -> Triple(
                "Notificações",
                listOf(
                    "Receba alertas de produtos com stock em nível crítico.",
                    "Consulte avisos de cobranças e faturas pendentes.",
                    "Marque notificações como lidas para organizar a lista."
                ),
                "notifications"
            )
            currentSubScreen == SubScreen.REPORTS -> Triple(
                "Relatórios Financeiros",
                listOf(
                    "Acompanhe o Demonstrativo de Resultados (DRE) e CMV.",
                    "Veja o lucro líquido real descontando despesas operacionais.",
                    "Consulte gráficos de vendas diárias e métodos de pagamento."
                ),
                "reports"
            )
            currentSubScreen == SubScreen.EXPENSES -> Triple(
                "Gestão de Despesas",
                listOf(
                    "Registe custos operacionais como renda, energia, salários ou água.",
                    "Associe despesas a categorias para análise financeira.",
                    "O total de despesas é deduzido automaticamente no lucro líquido."
                ),
                "expenses"
            )
            currentSubScreen == SubScreen.DEBTS -> Triple(
                "Fiado e Cobranças",
                listOf(
                    "Veja a lista de clientes com saldo devedor pendente.",
                    "Consulte prazos de vencimento e histórico de amortizações.",
                    "Envie mensagens amigáveis de cobrança directamente pelo WhatsApp."
                ),
                "debts"
            )
            currentSubScreen == SubScreen.CLIENTS -> Triple(
                "Gestão de Clientes",
                listOf(
                    "Cadastre clientes com nome e número de telemóvel (+258).",
                    "Defina limites de crédito para vendas a fiado.",
                    "Consulte o histórico de compras de cada cliente."
                ),
                "clients"
            )
            currentTab == MainNavigationItem.VENDAS -> Triple(
                "Ponto de Venda (PDV)",
                listOf(
                    "Selecione os produtos pretendidos para adicionar ao carrinho.",
                    "Escolha o método de pagamento: Dinheiro, M-Pesa, e-Mola ou mKesh.",
                    "Confirme a venda e envie o recibo por WhatsApp."
                ),
                "pos"
            )
            currentTab == MainNavigationItem.PRODUTOS -> Triple(
                "Catálogo de Produtos",
                listOf(
                    "Cadastre produtos com nome, preço de venda e stock inicial.",
                    "Expanda 'Mais opções' para cadastrar preço de compra e categoria.",
                    "A Nexora alerta automaticamente quando o stock estiver a acabar."
                ),
                "products"
            )
            currentTab == MainNavigationItem.STOCK -> Triple(
                "Gestão de Stock",
                listOf(
                    "Consulte as quantidades disponíveis de cada artigo.",
                    "Veja sugestões automáticas de compras e reposição.",
                    "Registe ajustes de inventário por entrada, perda ou quebra."
                ),
                "stock"
            )
            else -> Triple(
                "Painel Comercial",
                listOf(
                    "Acompanhe o total de vendas, lucros e gráfico diário.",
                    "Conclua os 'Primeiros passos' no topo para configurar a loja.",
                    "Utilize o menu Mais para aceder a relatórios e definições."
                ),
                "dashboard"
            )
        }

        com.example.ui.components.ScreenHelpDialog(
            screenTitle = helpTitle,
            bulletPoints = helpBullets,
            storeName = currentUser.storeName,
            videoHelpUrl = helpVideosConfig[helpKey],
            onDismiss = { showScreenHelp = false }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = NavyBackground,
        topBar = {
            NexoraTopBar(
                currentUser = currentUser,
                allUsers = allUsers,
                onSwitchUser = { user ->
                    NexoraRepository.switchUser(user)
                },
                isOnline = isOnline,
                pendingSyncCount = pendingSyncCount,
                onToggleOnline = {
                    NexoraRepository.toggleOnlineMode()
                },
                unreadNotificationsCount = unreadNotificationsCount,
                onOpenNotifications = {
                    currentSubScreen = SubScreen.NOTIFICATIONS
                },
                onOpenHelp = {
                    showScreenHelp = true
                },
                onLogout = {
                    NexoraRepository.logout()
                }
            )
        },
        bottomBar = {
            NexoraBottomBar(
                selectedItem = currentTab,
                onItemSelected = { tab ->
                    currentTab = tab
                    currentSubScreen = SubScreen.NONE
                },
                lowStockCount = lowStockCount,
                activeDebtsCount = activeDebtsCount
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(NavyBackground)
        ) {
            when (currentSubScreen) {
                SubScreen.CLIENTS -> {
                    ClientsScreen(
                        clients = clients,
                        sales = sales,
                        onAddClient = { name, phone, address, creditLimit, notes ->
                            NexoraRepository.addClient(name, phone, address, creditLimit, notes)
                        },
                        onUpdateClient = { updated ->
                            NexoraRepository.updateClient(updated)
                        },
                        onDeleteClient = { id ->
                            NexoraRepository.deleteClient(id)
                        },
                        onNavigateToDebts = {
                            currentSubScreen = SubScreen.DEBTS
                        }
                    )
                }

                SubScreen.DEBTS -> {
                    DebtsScreen(
                        debts = debts,
                        onRecordPayment = { debtId, amount, method, notes ->
                            NexoraRepository.recordDebtPayment(debtId, amount, method, notes)
                        }
                    )
                }

                SubScreen.EXPENSES -> {
                    ExpensesScreen(
                        currentUser = currentUser,
                        expenses = expenses,
                        onAddExpense = { desc, amt, cat, method, date, notes ->
                            NexoraRepository.addExpense(desc, amt, cat, method, date, notes)
                        },
                        onDeleteExpense = { id ->
                            NexoraRepository.deleteExpense(id)
                        }
                    )
                }

                SubScreen.SUPPLIERS -> {
                    SuppliersScreen(
                        suppliers = suppliers,
                        products = products,
                        onAddSupplier = { name, phone, address, cat, notes ->
                            NexoraRepository.addSupplier(name, phone, address, cat, notes)
                        },
                        onRegisterPurchase = { supplier, items, paid, method, invoice, notes ->
                            NexoraRepository.registerSupplierPurchase(supplier, items, paid, method, invoice, notes)
                        },
                        onPaySupplierDebt = { supplierId, amount, method ->
                            NexoraRepository.paySupplierDebt(supplierId, amount, method)
                        }
                    )
                }

                SubScreen.REPORTS -> {
                    ReportsScreen(
                        currentUser = currentUser,
                        sales = sales,
                        expenses = expenses,
                        clients = clients,
                        suppliers = suppliers,
                        products = products
                    )
                }

                SubScreen.NOTIFICATIONS -> {
                    NotificationsScreen(
                        notifications = notifications,
                        onMarkAsRead = { id ->
                            NexoraRepository.markNotificationAsRead(id)
                        },
                        onMarkAllAsRead = {
                            NexoraRepository.markAllNotificationsAsRead()
                        },
                        onDeleteNotification = { id ->
                            NexoraRepository.deleteNotification(id)
                        }
                    )
                }

                SubScreen.SUBSCRIPTION -> {
                    SubscriptionScreen(
                        subscription = subscription,
                        onSubmitProof = { ref, method ->
                            NexoraRepository.submitSubscriptionPaymentProof(ref, method)
                        }
                    )
                }

                SubScreen.AI_ASSISTANT -> {
                    AiAssistantScreen(
                        currentUser = currentUser,
                        onBack = { currentSubScreen = SubScreen.NONE }
                    )
                }

                SubScreen.SUPER_ADMIN -> {
                    if (currentUser.isSuperAdmin) {
                        SuperAdminScreen(
                            currentUser = currentUser,
                            onBack = { currentSubScreen = SubScreen.NONE }
                        )
                    } else {
                        NotFoundScreen(onBack = { currentSubScreen = SubScreen.NONE })
                    }
                }

                SubScreen.SECURITY_SYNC -> {
                    SecurityAndSyncScreen(
                        currentUser = currentUser,
                        onBack = { currentSubScreen = SubScreen.NONE }
                    )
                }

                SubScreen.PAYMENT_ACCOUNTS_CONFIG -> {
                    if (currentUser.isSuperAdmin) {
                        PaymentAccountsConfigScreen(
                            onBack = { currentSubScreen = SubScreen.NONE }
                        )
                    } else {
                        NotFoundScreen(onBack = { currentSubScreen = SubScreen.NONE })
                    }
                }

                SubScreen.TEAM_MANAGEMENT -> {
                    TeamManagementScreen(
                        currentUser = currentUser,
                        onBack = { currentSubScreen = SubScreen.NONE }
                    )
                }

                SubScreen.PRIVACY_DATA -> {
                    PrivacyAndDataScreen(
                        onBack = { currentSubScreen = SubScreen.NONE },
                        onNavigateToLegalViewer = { tab ->
                            selectedLegalTab = tab
                            currentSubScreen = SubScreen.LEGAL_VIEWER
                        }
                    )
                }

                SubScreen.LEGAL_VIEWER -> {
                    LegalDocumentViewerScreen(
                        initialTab = selectedLegalTab,
                        onBack = { currentSubScreen = SubScreen.PRIVACY_DATA }
                    )
                }

                SubScreen.NONE -> {
                    when (currentTab) {
                        MainNavigationItem.DASHBOARD -> {
                            DashboardScreen(
                                currentUser = currentUser,
                                sales = sales,
                                products = products,
                                expenses = expenses,
                                onNavigateToPos = { currentTab = MainNavigationItem.VENDAS },
                                onNavigateToProducts = { currentTab = MainNavigationItem.PRODUTOS },
                                onNavigateToStock = { currentTab = MainNavigationItem.STOCK },
                                onNavigateToDebts = {
                                    currentSubScreen = SubScreen.DEBTS
                                },
                                onNavigateToAi = {
                                    currentSubScreen = SubScreen.AI_ASSISTANT
                                }
                            )
                        }

                        MainNavigationItem.PRODUTOS -> {
                            ProductsScreen(
                                currentUser = currentUser,
                                products = products,
                                suppliers = suppliers,
                                onAddProduct = { name, cat, desc, unitType, perPkg, pkgCount, cost, sell, min, barcode, emoji, isActive, supplierName, pkgEnabled, pkgPrice, pkgName ->
                                    NexoraRepository.addProduct(
                                        name = name,
                                        category = cat,
                                        description = desc,
                                        unitType = unitType,
                                        unitsPerPackage = perPkg,
                                        packagesCount = pkgCount,
                                        costPrice = cost,
                                        sellingPrice = sell,
                                        minStock = min,
                                        barcode = barcode,
                                        iconEmoji = emoji,
                                        isActive = isActive,
                                        supplierName = supplierName,
                                        packageEnabled = pkgEnabled,
                                        packagePrice = pkgPrice,
                                        packageName = pkgName
                                    )
                                },
                                onUpdateProduct = { updated ->
                                    NexoraRepository.updateProduct(updated)
                                },
                                onDeleteProduct = { id ->
                                    NexoraRepository.deleteProduct(id)
                                },
                                onNavigateToPos = {
                                    currentTab = MainNavigationItem.VENDAS
                                    currentSubScreen = SubScreen.NONE
                                }
                            )
                        }

                        MainNavigationItem.VENDAS -> {
                            PosScreen(
                                currentUser = currentUser,
                                products = products,
                                clients = clients,
                                onCompleteSale = { items, disc, method, amountPaid, client, ref, due, notes ->
                                    NexoraRepository.registerSale(
                                        items, disc, method, amountPaid, client, ref, due, notes
                                    )
                                }
                            )
                        }

                        MainNavigationItem.STOCK -> {
                            StockScreen(
                                currentUser = currentUser,
                                products = products,
                                movements = movements,
                                onAdjustStock = { productId, qty, type, reason ->
                                    NexoraRepository.adjustStock(productId, qty, type, reason)
                                }
                            )
                        }

                        MainNavigationItem.MAIS -> {
                            MoreScreen(
                                currentUser = currentUser,
                                allUsers = allUsers,
                                onSwitchUser = { user ->
                                    NexoraRepository.switchUser(user)
                                },
                                onNavigateToClients = {
                                    currentSubScreen = SubScreen.CLIENTS
                                },
                                onNavigateToDebts = {
                                    currentSubScreen = SubScreen.DEBTS
                                },
                                onNavigateToExpenses = {
                                    currentSubScreen = SubScreen.EXPENSES
                                },
                                onNavigateToSuppliers = {
                                    currentSubScreen = SubScreen.SUPPLIERS
                                },
                                onNavigateToReports = {
                                    currentSubScreen = SubScreen.REPORTS
                                },
                                onNavigateToNotifications = {
                                    currentSubScreen = SubScreen.NOTIFICATIONS
                                },
                                onNavigateToSubscription = {
                                    currentSubScreen = SubScreen.SUBSCRIPTION
                                },
                                onNavigateToAi = {
                                    currentSubScreen = SubScreen.AI_ASSISTANT
                                },
                                onNavigateToSuperAdmin = {
                                    currentSubScreen = SubScreen.SUPER_ADMIN
                                },
                                onNavigateToSecuritySync = {
                                    currentSubScreen = SubScreen.SECURITY_SYNC
                                },
                                onNavigateToPaymentAccounts = {
                                    currentSubScreen = SubScreen.PAYMENT_ACCOUNTS_CONFIG
                                },
                                onNavigateToTeam = {
                                    currentSubScreen = SubScreen.TEAM_MANAGEMENT
                                },
                                onNavigateToPrivacy = {
                                    currentSubScreen = SubScreen.PRIVACY_DATA
                                },
                                onNavigateToLegalViewer = { tab ->
                                    selectedLegalTab = tab
                                    currentSubScreen = SubScreen.LEGAL_VIEWER
                                },
                                activeDebtsCount = activeDebtsCount,
                                clientsCount = clients.size,
                                unreadNotificationsCount = unreadNotificationsCount,
                                onReopenTour = {
                                    NexoraRepository.resetTour()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotFoundScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Página não encontrada",
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "O endereço solicitado não existe ou foi removido.",
                    color = TextMuted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onBack,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Voltar ao Início", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
