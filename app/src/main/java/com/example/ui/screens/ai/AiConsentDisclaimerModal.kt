package com.example.ui.screens.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.legal.LegalDocuments

@Composable
fun AiConsentDisclaimerModal(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDecline,
        containerColor = Color(0xFF1E293B),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Aviso de Privacidade da IA Nexora",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Antes de utilizar a Inteligência Artificial Gemini 3.8 Flash para análises empresariais, por favor confirma que compreendes como os teus dados são processados:",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp).padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dados Processados: Apenas totais de vendas, categorias de produtos, valores de despesas e níveis de stock da loja.",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp).padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sem Dados Pessoais de Clientes (PII): Nomes, telefones, moradas ou dados pessoais de terceiros NUNCA são transmitidos à IA.",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(16.dp).padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Apenas para o Proprietário: As recomendações e alertas financeiros da IA ficam exclusivamente visíveis para o Dono.",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Podes revogar a permissão de IA a qualquer momento em Mais > Privacidade e Dados.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                modifier = Modifier.testTag("accept_ai_consent_button")
            ) {
                Text("Compreendo e Aceito")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDecline,
                modifier = Modifier.testTag("decline_ai_consent_button")
            ) {
                Text("Agora Não", color = Color(0xFF94A3B8))
            }
        }
    )
}
