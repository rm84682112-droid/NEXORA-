package com.example.data.firebase

import android.util.Log
import com.example.data.model.AiBusinessSummary
import com.example.data.model.DebtRecord
import com.example.data.model.Expense
import com.example.data.model.Product
import com.example.data.model.Sale
import com.example.data.model.StockMovement
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "GeminiBusinessService"

class GeminiBusinessService {

    companion object {
        const val MODEL_NAME = "gemini-3.8-flash"
        const val TIMEOUT_MS = 30_000L
    }

    private val generativeModel by lazy {
        Firebase.ai.generativeModel(MODEL_NAME)
    }

    // Ping rápido para verificar conectividade antes de enviar lotes maiores
    suspend fun testAiConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = withTimeout(TIMEOUT_MS) {
                generativeModel.generateContent("Responde apenas: OK")
            }
            val text = response.text?.trim() ?: throw IllegalStateException("Resposta vazia da API.")
            Log.d(TAG, "Conexão OK: $text")
            Result.success(text)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao testar endpoint do modelo", e)
            Result.failure(e)
        }
    }

    // Fallback offline: agrega os totais diretamente em memória se a rede falhar
    fun generateRuleBasedSummary(
        storeName: String,
        products: List<Product>,
        sales: List<Sale>,
        expenses: List<Expense>,
        debts: List<DebtRecord> = emptyList()
    ): AiBusinessSummary {
        val now = System.currentTimeMillis()
        val oneDayMs = 86400_000L
        val todayStart = now - (now % oneDayMs)

        val totalSalesCount = sales.size
        val totalRevenue = sales.sumOf { it.total }
        val todaySales = sales.filter { it.timestamp >= todayStart }
        val todayRevenue = todaySales.sumOf { it.total }

        val lowStockList = products.filter { it.isLowStock || it.isOutOfStock }
        val outOfStockCount = products.count { it.isOutOfStock }

        val totalExpenses = expenses.sumOf { it.amount }
        val totalDebts = debts.sumOf { it.remainingBalance }

        // Product sales ranking
        val productSalesMap = mutableMapOf<String, Int>()
        sales.flatMap { it.items }.forEach { item ->
            productSalesMap[item.product.name] = (productSalesMap[item.product.name] ?: 0) + item.quantity
        }
        val topProduct = productSalesMap.entries.maxByOrNull { it.value }
        val leastProduct = productSalesMap.entries.minByOrNull { it.value }

        val summaryText = if (sales.isEmpty()) {
            "Ainda não há vendas registadas para '$storeName'. Cadastre produtos e efetue a primeira venda no balcão."
        } else {
            "Facturação acumulada de ${String.format(java.util.Locale.US, "%.2f", totalRevenue)} MT em $totalSalesCount vendas. Vendas de hoje: ${String.format(java.util.Locale.US, "%.2f", todayRevenue)} MT (${todaySales.size} transacções)."
        }

        val bestInsights = if (topProduct != null) {
            "O produto mais vendido é '${topProduct.key}' com ${topProduct.value} unidades comercializadas."
        } else {
            "Sem histórico suficiente de vendas por artigo."
        }

        val underperforming = if (leastProduct != null && leastProduct.key != topProduct?.key) {
            "O artigo '${leastProduct.key}' apresenta a menor rotação com apenas ${leastProduct.value} unidades."
        } else {
            "Sem dados de baixa rotação identificados."
        }

        val expenseMsg = if (expenses.isNotEmpty()) {
            "Total de despesas operacionais registadas: ${String.format(java.util.Locale.US, "%.2f", totalExpenses)} MT."
        } else {
            "Nenhuma despesa operacional registada até ao momento."
        }

        val restockList = mutableListOf<String>()
        lowStockList.take(3).forEach { p ->
            if (p.isOutOfStock) {
                restockList.add("URGENTE: ${p.name} está ESGOTADO (0 ${p.unitType.label}). Recomenda-se encomenda imediata.")
            } else {
                restockList.add("ATENÇÃO: ${p.name} tem stock baixo (restam ${p.currentStock} ${p.unitType.label}, mínimo: ${p.minStock}).")
            }
        }
        if (restockList.isEmpty()) {
            restockList.add("Todos os produtos cadastrados estão com níveis de stock regulares.")
        }

        val alerts = mutableListOf<String>()
        if (outOfStockCount > 0) {
            alerts.add("Existem $outOfStockCount produtos esgotados no catálogo.")
        }
        if (totalDebts > 0) {
            alerts.add("Saldo pendente em fiado a cobrar de clientes: ${String.format(java.util.Locale.US, "%.2f", totalDebts)} MT.")
        }
        if (alerts.isEmpty()) {
            alerts.add("Operação comercial a decorrer sem alertas críticos.")
        }

        val forecastMsg = if (totalSalesCount >= 10) {
            "Com base nas $totalSalesCount vendas registadas, a média por transacção situa-se nos ${String.format(java.util.Locale.US, "%.2f", if (totalSalesCount > 0) totalRevenue / totalSalesCount else 0.0)} MT."
        } else {
            "São necessárias pelo menos 10 vendas registadas para gerar previsões de vendas automatizadas."
        }

        return AiBusinessSummary(
            title = "Análise Comercial Nexora (Motor Local)",
            period = "Actualizado agora",
            summaryText = summaryText,
            bestSellingInsights = bestInsights,
            underperformingInsights = underperforming,
            expenseAnomalies = expenseMsg,
            salesTrends = "Vendas registadas: $totalSalesCount no total.",
            restockRecommendations = restockList,
            alerts = alerts,
            salesForecast = forecastMsg,
            hasEnoughDataForForecast = totalSalesCount >= 10,
            generatedAt = System.currentTimeMillis()
        )
    }

    suspend fun analyzeBusinessData(
        storeName: String,
        products: List<Product>,
        sales: List<Sale>,
        expenses: List<Expense>,
        stockMovements: List<StockMovement>
    ): Result<AiBusinessSummary> = withContext(Dispatchers.IO) {
        if (sales.size < 10) {
            val localSummary = generateRuleBasedSummary(storeName, products, sales, expenses)
            return@withContext Result.success(localSummary)
        }

        try {
            val totalSalesCount = sales.size
            val totalRevenue = sales.sumOf { it.total }
            val totalDiscounts = sales.sumOf { it.discount }
            val totalExpenses = expenses.sumOf { it.amount }
            val lowStockProducts = products.filter { it.isLowStock }
            val outOfStockProducts = products.filter { it.isOutOfStock }

            val productSalesMap = mutableMapOf<String, Int>()
            val productRevenueMap = mutableMapOf<String, Double>()
            sales.forEach { sale ->
                sale.items.forEach { item ->
                    val pName = item.product.name
                    productSalesMap[pName] = (productSalesMap[pName] ?: 0) + item.quantity
                    productRevenueMap[pName] = (productRevenueMap[pName] ?: 0.0) + item.subtotal
                }
            }

            val topProducts = productSalesMap.entries.sortedByDescending { it.value }.take(5)
            val leastProducts = productSalesMap.entries.sortedBy { it.value }.take(5)

            val expensesByCategory = expenses.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }

            val dataContext = buildString {
                appendLine("DADOS REAIS DA EMPRESA '$storeName':")
                appendLine("- Total de produtos cadastrados: ${products.size}")
                appendLine("- Produtos com stock baixo: ${lowStockProducts.size} (${lowStockProducts.joinToString { "${it.name} (restam ${it.currentStock} ${it.unitType})" }})")
                appendLine("- Produtos esgotados: ${outOfStockProducts.size} (${outOfStockProducts.joinToString { it.name }})")
                appendLine("- Total de vendas registadas: $totalSalesCount")
                appendLine("- Facturação bruta total: $totalRevenue MT")
                appendLine("- Total de descontos concedidos: $totalDiscounts MT")
                appendLine("- Despesas operacionais totais: $totalExpenses MT")
                appendLine("- Despesas por categoria: ${expensesByCategory.entries.joinToString { "${it.key.label}: ${it.value} MT" }}")
                appendLine("- Produtos mais vendidos (qtd): ${topProducts.joinToString { "${it.key}: ${it.value} un (${productRevenueMap[it.key] ?: 0.0} MT)" }}")
                appendLine("- Produtos menos vendidos (qtd): ${leastProducts.joinToString { "${it.key}: ${it.value} un" }}")
            }

            val prompt = """
                És o assistente da Nexora. Responde só com base nos dados fornecidos.
                Se a informação não existir, diz 'Ainda não tenho esses dados'. Nunca inventes números, produtos ou percentagens.
                Responde em português de Moçambique, com frases curtas e valores em MT.
                
                Analisa os dados reais abaixo da empresa '$storeName' e responde estritamente no seguinte formato JSON:
                {
                  "summaryText": "Resumo geral...",
                  "bestSellingInsights": "Análise dos mais vendidos...",
                  "underperformingInsights": "Análise dos menos vendidos...",
                  "expenseAnomalies": "Análise de despesas...",
                  "salesTrends": "Tendência de vendas...",
                  "restockRecommendations": ["Recomendação 1..."],
                  "alerts": ["Alerta 1..."],
                  "salesForecast": "Previsão calculada...",
                  "hasEnoughDataForForecast": true
                }

                $dataContext
            """.trimIndent()

            val response = withTimeout(TIMEOUT_MS) {
                generativeModel.generateContent(prompt)
            }

            val responseText = response.text ?: throw IllegalStateException("O modelo não retornou resposta.")
            Log.d(TAG, "Gemini Response: $responseText")

            val cleanJson = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val jsonObj = JSONObject(cleanJson)
            val restockArray = jsonObj.optJSONArray("restockRecommendations")
            val restockList = mutableListOf<String>()
            if (restockArray != null) {
                for (i in 0 until restockArray.length()) {
                    restockList.add(restockArray.getString(i))
                }
            }

            val alertsArray = jsonObj.optJSONArray("alerts")
            val alertsList = mutableListOf<String>()
            if (alertsArray != null) {
                for (i in 0 until alertsArray.length()) {
                    alertsList.add(alertsArray.getString(i))
                }
            }

            val summary = AiBusinessSummary(
                title = "Análise Inteligente Nexora",
                period = "Actualizado agora",
                summaryText = jsonObj.optString("summaryText", "Análise do negócio gerada com base nos dados reais."),
                bestSellingInsights = jsonObj.optString("bestSellingInsights", "Sem dados de produtos mais vendidos."),
                underperformingInsights = jsonObj.optString("underperformingInsights", "Sem anomalias de produtos parados."),
                expenseAnomalies = jsonObj.optString("expenseAnomalies", "Despesas dentro dos parâmetros normais."),
                salesTrends = jsonObj.optString("salesTrends", "Vendas monitoradas activamente."),
                restockRecommendations = if (restockList.isNotEmpty()) restockList else listOf("Mantenha o stock dos produtos regularizado."),
                alerts = if (alertsList.isNotEmpty()) alertsList else listOf("Nenhum risco crítico detectado no momento."),
                salesForecast = jsonObj.optString("salesForecast", "Dados históricos insuficientes para projecção."),
                hasEnoughDataForForecast = jsonObj.optBoolean("hasEnoughDataForForecast", totalSalesCount >= 10)
            )

            Result.success(summary)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Falha na análise Gemini AI, a utilizar plano B local", e)
            val fallbackSummary = generateRuleBasedSummary(storeName, products, sales, expenses)
            Result.success(fallbackSummary)
        }
    }

    suspend fun answerBusinessQuestion(
        question: String,
        storeName: String,
        products: List<Product>,
        sales: List<Sale>,
        expenses: List<Expense>,
        debts: List<DebtRecord> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        if (products.isEmpty() && sales.isEmpty()) {
            return@withContext Result.success("Ainda não tenho dados suficientes. Registe as suas vendas e produtos.")
        }

        try {
            val now = System.currentTimeMillis()
            val oneDayMs = 86400_000L
            val todayStart = now - (now % oneDayMs)

            val totalSalesCount = sales.size
            val revenue = sales.sumOf { it.total }
            val todaySales = sales.filter { it.timestamp >= todayStart }
            val todayRevenue = todaySales.sumOf { it.total }
            val expensesTotal = expenses.sumOf { it.amount }
            val profitEstimate = (revenue - expensesTotal).coerceAtLeast(0.0)
            val totalDebts = debts.sumOf { it.remainingBalance }

            // Group sales by product for aggregated top selling
            val productSalesMap = mutableMapOf<String, Int>()
            sales.flatMap { it.items }.forEach { item ->
                productSalesMap[item.product.name] = (productSalesMap[item.product.name] ?: 0) + item.quantity
            }
            val topProducts = productSalesMap.entries.sortedByDescending { it.value }.take(5)

            // Group expenses by category
            val expensesByCategory = expenses.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }

            // Build strictly aggregated context without personal phone numbers or client addresses
            val aggregatedContext = buildString {
                appendLine("RESUMO DA EMPRESA '$storeName':")
                appendLine("- Vendas de Hoje: ${todaySales.size} vendas, total de ${String.format(java.util.Locale.US, "%.2f", todayRevenue)} MT.")
                appendLine("- Facturação Total Acumulada: ${String.format(java.util.Locale.US, "%.2f", revenue)} MT ($totalSalesCount vendas no total).")
                appendLine("- Despesas Totais Registadas: ${String.format(java.util.Locale.US, "%.2f", expensesTotal)} MT.")
                appendLine("- Estimativa de Lucro Operacional: ${String.format(java.util.Locale.US, "%.2f", profitEstimate)} MT.")
                appendLine("- Total de Fiado/Dívidas a Cobrar: ${String.format(java.util.Locale.US, "%.2f", totalDebts)} MT.")
                appendLine("- Produtos Cadastrados: ${products.size} (Esgotados: ${products.count { it.isOutOfStock }}, Stock Baixo: ${products.count { it.isLowStock }}).")
                appendLine("- Produtos com Stock Baixo/Esgotado: ${products.filter { it.isLowStock || it.isOutOfStock }.joinToString { "${it.name} (${it.currentStock} un)" }}")
                appendLine("- Produtos Mais Vendidos: ${topProducts.joinToString { "${it.key} (${it.value} un)" }}")
                appendLine("- Despesas por Categoria: ${expensesByCategory.entries.joinToString { "${it.key.label}: ${String.format(java.util.Locale.US, "%.2f", it.value)} MT" }}")
            }

            val prompt = """
                És o assistente da Nexora. Responde só com base nos dados fornecidos.
                Se a informação não existir, diz 'Ainda não tenho esses dados'. Nunca inventes números, produtos ou percentagens.
                Responde em português de Moçambique, com frases curtas e valores em MT.

                $aggregatedContext

                PERGUNTA DO PROPRIETÁRIO:
                "$question"
            """.trimIndent()

            val response = withTimeout(TIMEOUT_MS) {
                generativeModel.generateContent(prompt)
            }

            val text = response.text?.trim()
            if (text.isNullOrBlank()) {
                Result.success("Ainda não tenho dados suficientes para responder a essa pergunta.")
            } else {
                Result.success(text)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Falha na chamada Gemini AI, a utilizar resposta calculada localmente", e)
            
            // Fallback answer calculated strictly from code without hallucination
            val qLower = question.lowercase()
            val now = System.currentTimeMillis()
            val oneDayMs = 86400_000L
            val todayStart = now - (now % oneDayMs)
            val todaySales = sales.filter { it.timestamp >= todayStart }
            val todayRevenue = todaySales.sumOf { it.total }

            val fallbackAnswer = when {
                qLower.contains("vendi hoje") || qLower.contains("vendas de hoje") || qLower.contains("quanto vendi") -> {
                    if (todaySales.isEmpty()) {
                        "Hoje ainda não tem vendas registadas (0,00 MT)."
                    } else {
                        "Hoje vendeu o total de ${String.format(java.util.Locale.US, "%.2f", todayRevenue)} MT em ${todaySales.size} transacções."
                    }
                }
                qLower.contains("stock") || qLower.contains("acabar") || qLower.contains("esgotad") -> {
                    val low = products.filter { it.isLowStock || it.isOutOfStock }
                    if (low.isEmpty()) {
                        "Nenhum produto está com stock baixo ou esgotado."
                    } else {
                        "Existem ${low.size} produtos com atenção de stock: ${low.joinToString { "${it.name} (${it.currentStock} un)" }}."
                    }
                }
                qLower.contains("despesa") -> {
                    val totalExp = expenses.sumOf { it.amount }
                    "O total de despesas registadas é de ${String.format(java.util.Locale.US, "%.2f", totalExp)} MT em ${expenses.size} lançamentos."
                }
                else -> {
                    "O assistente não está disponível agora. Tente mais tarde."
                }
            }

            Result.success(fallbackAnswer)
        }
    }
}
