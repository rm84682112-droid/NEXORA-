package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.privacy.LegalTab

@Composable
fun AuthLandingScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToLegalViewer: (LegalTab) -> Unit
) {
    Scaffold(
        containerColor = Color(0xFF090E1A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Hero Brand Logo & Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF0066FF), Color(0xFF0D1527))
                            ),
                            shape = CircleShape
                        )
                        .border(2.dp, Color(0xFF38BDF8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = "NEXORA Logo",
                        tint = Color.White,
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "NEXORA",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Gestão Comercial Inteligente",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Plataforma móvel para pequenos, médios e grandes comerciantes em Moçambique.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Highlights list
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeatureRow("Funciona Online & Offline no mesmo telemóvel")
                    FeatureRow("Vendas em Meticais (MT) via M-Pesa, e-Mola e Fiado")
                    FeatureRow("Controlo de Stock, Despesas e Fornecedores")
                    FeatureRow("Painel para o Dono e Ponto de Venda para o Caixa")
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToRegister,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("landing_create_account_button")
                ) {
                    Text(
                        text = "Criar Conta de Comerciante",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }

                OutlinedButton(
                    onClick = onNavigateToLogin,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("landing_login_button")
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Já tenho uma conta — Entrar",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Legal links at bottom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Termos de Uso",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clickable { onNavigateToLegalViewer(LegalTab.TERMOS) }
                            .padding(4.dp)
                    )
                    Text(" · ", color = Color(0xFF64748B), fontSize = 12.sp)
                    Text(
                        text = "Política de Privacidade",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clickable { onNavigateToLegalViewer(LegalTab.PRIVACIDADE) }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, color = Color(0xFFE2E8F0), fontSize = 13.sp)
    }
}
