package com.example.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.privacy.LegalTab

@Composable
fun RegistrationConsentDialog(
    onAcceptAll: () -> Unit,
    onOpenLegalDocument: (LegalTab) -> Unit,
    onCancel: () -> Unit
) {
    var agreedPrivacy by remember { mutableStateOf(false) }
    var agreedTerms by remember { mutableStateOf(false) }

    val isFormValid = agreedPrivacy && agreedTerms

    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = Color(0xFF1E293B),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Conformidade Legal & Termos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Para criar conta ou associar uma empresa à plataforma NEXORA em Moçambique, deves aceitar os seguintes documentos legais:",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Privacy Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = agreedPrivacy,
                        onCheckedChange = { agreedPrivacy = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0066FF)),
                        modifier = Modifier.testTag("consent_privacy_checkbox")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Li e concordo com a ",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Política de Privacidade",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onOpenLegalDocument(LegalTab.PRIVACIDADE) }
                    )
                }

                // Terms Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = agreedTerms,
                        onCheckedChange = { agreedTerms = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0066FF)),
                        modifier = Modifier.testTag("consent_terms_checkbox")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Li e aceito os ",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Termos de Uso do NEXORA",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onOpenLegalDocument(LegalTab.TERMOS) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Versão 1.0 (07/11/2026) · Servidores Firebase fora de Moçambique",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAcceptAll,
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0066FF),
                    disabledContainerColor = Color(0xFF334155)
                ),
                modifier = Modifier.testTag("confirm_registration_consent_button")
            ) {
                Text("Aceitar & Prosseguir")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancelar", color = Color(0xFF94A3B8))
            }
        }
    )
}
