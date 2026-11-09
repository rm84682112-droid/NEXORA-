package com.example.data.model

import java.util.UUID

data class AiBusinessSummary(
    val title: String,
    val period: String,
    val summaryText: String,
    val bestSellingInsights: String,
    val underperformingInsights: String,
    val expenseAnomalies: String,
    val salesTrends: String,
    val restockRecommendations: List<String>,
    val alerts: List<String>,
    val salesForecast: String,
    val hasEnoughDataForForecast: Boolean,
    val generatedAt: Long = System.currentTimeMillis()
)

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderName: String,
    val content: String,
    val isFromAi: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

data class AiMonthlyUsage(
    val monthYear: String, // e.g. "2026-11"
    val usedCount: Int = 0,
    val maxLimit: Int = 5 // according to plan: 5 for Free, 25 for Basic, 100 for Pro
)

data class AiStatusState(
    val isConnected: Boolean = true,
    val modelName: String = "gemini-3.8-flash",
    val lastResponseTimestamp: Long? = null,
    val lastTestResult: String? = null,
    val technicalErrorText: String? = null
)
