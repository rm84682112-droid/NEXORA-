package com.example.ui.screens.privacy

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.legal.LegalDocuments

enum class LegalTab(val label: String) {
    PRIVACIDADE("Privacidade"),
    TERMOS("Termos de Uso"),
    CLIENTES("Tratamento de Dados")
}

@Composable
fun LegalDocumentViewerScreen(
    initialTab: LegalTab = LegalTab.PRIVACIDADE,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D1527))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("legal_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Documentos Legais NEXORA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Versão ${LegalDocuments.DOCUMENT_VERSION} · Lei n.º 3/2017 Moçambique",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LegalTab.values().forEach { tab ->
                        FilterChip(
                            selected = (selectedTab == tab),
                            onClick = { selectedTab = tab },
                            label = { Text(tab.label, fontSize = 12.sp) },
                            leadingIcon = {
                                val icon = when (tab) {
                                    LegalTab.PRIVACIDADE -> Icons.Default.Shield
                                    LegalTab.TERMOS -> Icons.Default.Gavel
                                    LegalTab.CLIENTES -> Icons.Default.VerifiedUser
                                }
                                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0066FF),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFF090E1A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Mandatory Legal Disclaimer Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Aviso de Revisão Jurídica Opcional",
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF38BDF8),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = LegalDocuments.LEGAL_NOTICE_DISCLAIMER,
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Document Content
            val textContent = when (selectedTab) {
                LegalTab.PRIVACIDADE -> LegalDocuments.PRIVACY_POLICY
                LegalTab.TERMOS -> LegalDocuments.TERMS_OF_USE
                LegalTab.CLIENTES -> LegalDocuments.CUSTOMER_DATA_PROCESSING_CLAUSE
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = selectedTab.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Moçambique — Versão ${LegalDocuments.DOCUMENT_VERSION}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = textContent,
                        color = Color(0xFFE2E8F0),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}
