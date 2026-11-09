package com.example.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NexoraBlueLight
import com.example.ui.theme.NexoraBluePrimary
import com.example.ui.theme.NexoraCyanAccent
import com.example.ui.theme.NexoraGreenSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

data class TourStep(
    val stepIndex: Int,
    val title: String,
    val description: String,
    val emojiBadge: String,
    val highlightBadges: List<String>
)

val tourSteps = listOf(
    TourStep(
        stepIndex = 1,
        title = "Venda em poucos toques",
        description = "Regista cada venda rapidamente e recebe em Dinheiro, M-Pesa, e-Mola ou mKesh.",
        emojiBadge = "📱💳",
        highlightBadges = listOf("Dinheiro", "M-Pesa", "e-Mola", "mKesh")
    ),
    TourStep(
        stepIndex = 2,
        title = "Saiba o que tem em stock",
        description = "A Nexora avisa automaticamente quando um produto está a acabar.",
        emojiBadge = "📦⚠️",
        highlightBadges = listOf("Alertas de Stock", "Controlo de Quantidades")
    ),
    TourStep(
        stepIndex = 3,
        title = "Controle o fiado",
        description = "Veja quem deve, quanto deve e envie lembretes de cobrança pelo WhatsApp.",
        emojiBadge = "📑💬",
        highlightBadges = listOf("Cobrança WhatsApp", "Histórico de Amortizações")
    ),
    TourStep(
        stepIndex = 4,
        title = "Veja o seu lucro",
        description = "Acompanhe os seus lucros e receitas todos os dias, sem contas no caderno.",
        emojiBadge = "📊📈",
        highlightBadges = listOf("Margens Reais", "Relatórios Automáticos")
    )
)

@Composable
fun OnboardingTourDialog(
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val step = tourSteps[currentStepIndex]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyBackground.copy(alpha = 0.96f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_tour_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, NexoraBlueLight.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Row: Step Indicator & Skip Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(NexoraBluePrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${step.stepIndex} de ${tourSteps.size}",
                                color = NexoraCyanAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        TextButton(
                            onClick = {
                                onComplete()
                                onDismiss()
                            },
                            modifier = Modifier.testTag("onboarding_skip_button")
                        ) {
                            Text("Saltar", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Animated Step Content
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tour_step_animation"
                    ) { currentStep ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Emoji / Animation Badge Container
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            listOf(NexoraBluePrimary.copy(alpha = 0.4f), NavyCardElevated)
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = currentStep.emojiBadge, fontSize = 42.sp)
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = currentStep.title,
                                color = TextWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = currentStep.description,
                                color = TextMuted,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Highlight Badges Row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                currentStep.highlightBadges.forEach { badgeText ->
                                    Box(
                                        modifier = Modifier
                                            .background(NavyCardElevated, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = badgeText,
                                            color = NexoraCyanAccent,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Step Dot Indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tourSteps.indices.forEach { index ->
                            val isActive = index == currentStepIndex
                            Box(
                                modifier = Modifier
                                    .size(if (isActive) 24.dp else 8.dp, 8.dp)
                                    .background(
                                        if (isActive) NexoraCyanAccent else TextMuted.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Button (Next or Begin)
                    val isLastStep = currentStepIndex == tourSteps.size - 1
                    Button(
                        onClick = {
                            if (isLastStep) {
                                onComplete()
                                onDismiss()
                            } else {
                                currentStepIndex++
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLastStep) NexoraGreenSuccess else NexoraBluePrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("onboarding_next_button")
                    ) {
                        Text(
                            text = if (isLastStep) "Começar" else "Seguinte",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (isLastStep) Icons.Default.Check else Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
