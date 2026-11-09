package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NexoraBlueLight
import com.example.ui.theme.NexoraBluePrimary
import com.example.ui.theme.NexoraCyanAccent
import com.example.ui.theme.NexoraGreenSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun FirstStepsCard(
    hasProduct: Boolean,
    hasSale: Boolean,
    hasViewedProfit: Boolean,
    onAddProductClick: () -> Unit,
    onMakeSaleClick: () -> Unit,
    onViewProfitClick: () -> Unit,
    onDismissAllComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val completedCount = (if (hasProduct) 1 else 0) +
            (if (hasSale) 1 else 0) +
            (if (hasViewedProfit) 1 else 0)

    val isAllComplete = completedCount == 3

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("first_steps_checklist_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isAllComplete) NexoraGreenSuccess.copy(alpha = 0.5f) else NexoraCyanAccent.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title + Progress counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = if (isAllComplete) "🎉" else "🚀", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAllComplete) "Parabéns!" else "Primeiros passos",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (isAllComplete) NexoraGreenSuccess.copy(alpha = 0.2f) else NexoraBluePrimary.copy(alpha = 0.2f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$completedCount de 3",
                        color = if (isAllComplete) NexoraGreenSuccess else NexoraCyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { completedCount / 3f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isAllComplete) NexoraGreenSuccess else NexoraCyanAccent,
                trackColor = NavyCardElevated
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (isAllComplete) {
                Text(
                    text = "Completou todas as tarefas iniciais com sucesso! O seu negócio está a funcionar na Nexora.",
                    color = TextWhite,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            } else {
                Text(
                    text = "Complete estas 3 tarefas para começar a vender em menos de 3 minutos:",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Step 1: Add First Product
                    StepItem(
                        stepNumber = 1,
                        title = "Adicione o primeiro produto",
                        isCompleted = hasProduct,
                        onClick = onAddProductClick,
                        testTag = "first_step_add_product"
                    )

                    // Step 2: Make First Sale
                    StepItem(
                        stepNumber = 2,
                        title = "Faça a primeira venda",
                        isCompleted = hasSale,
                        onClick = onMakeSaleClick,
                        testTag = "first_step_make_sale"
                    )

                    // Step 3: View Today's Profit
                    StepItem(
                        stepNumber = 3,
                        title = "Veja o seu lucro de hoje",
                        isCompleted = hasViewedProfit,
                        onClick = onViewProfitClick,
                        testTag = "first_step_view_profit"
                    )
                }
            }
        }
    }
}

@Composable
private fun StepItem(
    stepNumber: Int,
    title: String,
    isCompleted: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) NexoraGreenSuccess.copy(alpha = 0.12f) else NavyCardElevated
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCompleted) NexoraGreenSuccess.copy(alpha = 0.4f) else NavyCardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            if (isCompleted) NexoraGreenSuccess else NexoraBluePrimary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Concluído",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = stepNumber.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isCompleted) NexoraGreenSuccess else TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
