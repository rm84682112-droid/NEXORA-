package com.example.ui.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiBusinessSummary
import com.example.data.model.AiChatMessage
import com.example.data.model.AiStatusState
import com.example.data.model.User
import com.example.data.repository.NexoraRepository
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.NexoraAmberWarning
import com.example.ui.theme.NexoraBlueLight
import com.example.ui.theme.NexoraBluePrimary
import com.example.ui.theme.NexoraCyanAccent
import com.example.ui.theme.NexoraGreenSuccess
import com.example.ui.theme.NexoraPurple
import com.example.ui.theme.NexoraRedAlert
import com.example.ui.theme.TextFaint
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.launch

@Composable
fun AiAssistantScreen(
    currentUser: User,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val latestSummary by NexoraRepository.latestAiSummary.collectAsState()
    val chatHistory by NexoraRepository.aiChatHistory.collectAsState()
    val monthlyUsage by NexoraRepository.monthlyAiUsage.collectAsState()
    val isAiLoading by NexoraRepository.isAiLoading.collectAsState()
    val aiError by NexoraRepository.aiError.collectAsState()
    val subscription by NexoraRepository.subscription.collectAsState()
    val aiStatusState by NexoraRepository.aiStatusState.collectAsState()

    var userQuestion by remember { mutableStateOf("") }
    var isTestingAi by remember { mutableStateOf(false) }

    if (!currentUser.canViewProfits) {
        // Cashier restricted view
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
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(NexoraAmberWarning.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = NexoraAmberWarning,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Acesso Exclusivo ao Proprietário",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "A inteligência artificial NEXORA analisa dados estratégicos, margens de lucro e despesas da empresa. Apenas o Dono ou Gerente tem permissão para aceder.",
                        color = TextMuted,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Voltar ao Menu")
                    }
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Header with AI Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(NexoraPurple.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NexoraPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Assistente Nexora",
                            color = TextWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Inteligência Artificial Comercial (${aiStatusState.modelName})",
                        color = NexoraCyanAccent,
                        fontSize = 12.sp
                    )
                }

                // Refresh / Run Button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            NexoraRepository.runBusinessAiAnalysis()
                        }
                    },
                    enabled = !isAiLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NexoraPurple,
                        disabledContainerColor = NavySurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("ai_run_analysis_button")
                ) {
                    if (isAiLoading) {
                        CircularProgressIndicator(
                            color = TextWhite,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("A Analisar...", fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Actualizar", fontSize = 12.sp)
                    }
                }
            }
        }

        // Diagnostic Card: "Estado da IA"
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = NexoraCyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Estado da IA",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Status Badge
                        val isOnline = aiStatusState.isConnected
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isOnline) NexoraGreenSuccess.copy(alpha = 0.2f) else NexoraRedAlert.copy(alpha = 0.2f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isOnline) "Ligada" else "Com erro",
                                color = if (isOnline) NexoraGreenSuccess else NexoraRedAlert,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Modelo: ${aiStatusState.modelName}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (aiStatusState.lastResponseTimestamp != null) {
                                val sdf = java.text.SimpleDateFormat("HH:mm - dd/MM", java.util.Locale.getDefault())
                                "Última resposta: ${sdf.format(java.util.Date(aiStatusState.lastResponseTimestamp!!))}"
                            } else {
                                "Sem respostas recentes"
                            },
                            color = TextFaint,
                            fontSize = 11.sp
                        )
                    }

                    if (aiStatusState.lastTestResult != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = aiStatusState.lastTestResult!!,
                            color = NexoraCyanAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (!aiStatusState.technicalErrorText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NexoraRedAlert.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Erro técnico: ${aiStatusState.technicalErrorText}",
                                color = TextWhite,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isTestingAi = true
                            coroutineScope.launch {
                                NexoraRepository.testAiConnection()
                                isTestingAi = false
                            }
                        },
                        enabled = !isTestingAi && !isAiLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NexoraBluePrimary,
                            disabledContainerColor = NavySurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_ai_connection_button")
                    ) {
                        if (isTestingAi) {
                            CircularProgressIndicator(
                                color = TextWhite,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("A testar comunicação...", fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Testar IA agora", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Monthly Usage Quota Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Quota Mensal de Análises IA",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${monthlyUsage.usedCount} de ${subscription.currentPlan.aiAnalysesLimit} análises utilizadas",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(NexoraBluePrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = subscription.currentPlan.title,
                            color = NexoraCyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Error message banner if any
        if (aiError != null) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NexoraRedAlert.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = NexoraRedAlert,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = aiError ?: "",
                            color = TextWhite,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Latest Business Summary Cards
        latestSummary?.let { summary ->
            item {
                Text(
                    text = "Diagnóstico do Teu Negócio",
                    color = TextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Executive Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "✨", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = summary.title,
                                color = NexoraCyanAccent,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = summary.period,
                                color = TextFaint,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = summary.summaryText,
                            color = TextLight,
                            fontSize = 14.sp,
                            lineHeight = 21.sp
                        )
                    }
                }
            }

            // Sales & Products Insights
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🏆", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Mais Vendidos",
                                    color = NexoraGreenSuccess,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = summary.bestSellingInsights,
                                color = TextLight,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💤", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Pouca Rotação",
                                    color = NexoraAmberWarning,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = summary.underperformingInsights,
                                color = TextLight,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            // Restock Recommendations Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📦", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recomendações de Compra & Stock",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        summary.restockRecommendations.forEach { rec ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(text = "• ", color = NexoraCyanAccent, fontWeight = FontWeight.Bold)
                                Text(text = rec, color = TextLight, fontSize = 13.sp, lineHeight = 19.sp)
                            }
                        }
                    }
                }
            }

            // Expenses & Forecast
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔮", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Previsão & Tendência",
                                color = NexoraPurple,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (!summary.hasEnoughDataForForecast) {
                                Box(
                                    modifier = Modifier
                                        .background(NexoraAmberWarning.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Histórico Curto", color = NexoraAmberWarning, fontSize = 10.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = summary.salesForecast,
                            color = TextLight,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Despesas: ${summary.expenseAnomalies}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Natural Language Q&A Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Conversar com o Assistente",
                color = TextWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Faz perguntas sobre as tuas vendas, clientes, despesas ou compras em Moçambique.",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        // Quick Suggestion Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(
                    listOf(
                        "Quanto facturei até agora?",
                        "Que produtos estão a acabar?",
                        "Como posso cortar despesas?",
                        "Qual é o produto mais lucrativo?"
                    )
                ) { prompt ->
                    SuggestionChip(
                        onClick = {
                            userQuestion = prompt
                            coroutineScope.launch {
                                NexoraRepository.askBusinessAssistant(prompt)
                            }
                        },
                        label = { Text(prompt, fontSize = 12.sp, color = TextWhite) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = NavySurfaceVariant
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            borderColor = NavyCardBorder,
                            enabled = true
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }

        // Chat History List
        items(chatHistory) { msg ->
            ChatBubble(msg, onRetry = {
                coroutineScope.launch {
                    NexoraRepository.askBusinessAssistant(msg.content)
                }
            })
        }

        // Thinking indicator when AI is loading
        if (isAiLoading) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = NexoraCyanAccent,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "A pensar...",
                                color = NexoraCyanAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Input Field
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userQuestion,
                    onValueChange = { userQuestion = it },
                    placeholder = { Text("Escreve uma pergunta...", color = TextMuted) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_question_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = NavySurface,
                        unfocusedContainerColor = NavySurface,
                        focusedBorderColor = NexoraPurple,
                        unfocusedBorderColor = NavyCardBorder
                    ),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val q = userQuestion.trim()
                        if (q.isNotBlank()) {
                            userQuestion = ""
                            coroutineScope.launch {
                                NexoraRepository.askBusinessAssistant(q)
                            }
                        }
                    },
                    enabled = !isAiLoading && userQuestion.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (userQuestion.isNotBlank()) NexoraPurple else NavySurfaceVariant,
                            CircleShape
                        )
                        .testTag("ai_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar",
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChatBubble(message: AiChatMessage, onRetry: () -> Unit = {}) {
    val isAi = message.isFromAi
    val isError = message.isError
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isAi) 4.dp else 16.dp,
                bottomEnd = if (isAi) 16.dp else 4.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isError -> NexoraRedAlert.copy(alpha = 0.2f)
                    isAi -> NavyCardElevated
                    else -> NexoraBluePrimary
                }
            ),
            modifier = Modifier.fillMaxWidth(0.88f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isAi) {
                        Text(text = if (isError) "⚠️" else "✨", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = message.senderName,
                        color = when {
                            isError -> NexoraRedAlert
                            isAi -> NexoraCyanAccent
                            else -> TextWhite.copy(alpha = 0.8f)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = message.content,
                    color = TextWhite,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                if (isError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = NexoraRedAlert),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tentar de novo", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
