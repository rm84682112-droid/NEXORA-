package com.example.ui.screens.team

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.repository.NexoraRepository
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.NexoraBluePrimary
import com.example.ui.theme.NexoraCyanAccent
import com.example.ui.theme.NexoraGreenSuccess
import com.example.ui.theme.NexoraRedAlert
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun TeamManagementScreen(
    currentUser: User,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allUsers by NexoraRepository.allUsers.collectAsState()

    var showAddMemberModal by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var pinInput by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.CAIXA) }

    if (!currentUser.canViewProfits) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = NexoraRedAlert, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Acesso Restrito ao Proprietário", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Apenas o Dono da empresa pode gerir os operadores de caixa.", color = TextMuted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBack) { Text("Voltar") }
                }
            }
        }
        return
    }

    Scaffold(
        containerColor = NavyBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddMemberModal = true },
                containerColor = NexoraBluePrimary,
                contentColor = TextWhite,
                modifier = Modifier.testTag("add_team_member_fab")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Adicionar Membro")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(NexoraBluePrimary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.People, contentDescription = null, tint = NexoraCyanAccent)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Gestão da Equipa Comercial",
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Adiciona e gere caixas e operadores com logins próprios",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(allUsers) { member ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        if (member.role == UserRole.DONO) NexoraBluePrimary else NavySurfaceVariant,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.name.take(1).uppercase(),
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = member.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(text = member.role.label, color = NexoraCyanAccent, fontSize = 12.sp)
                                Text(text = member.phone, color = TextMuted, fontSize = 11.sp)
                            }
                        }

                        if (member.role != UserRole.DONO) {
                            IconButton(
                                onClick = {
                                    NexoraRepository.removeTeamMember(member.id)
                                    Toast.makeText(context, "${member.name} removido da equipa.", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = NexoraRedAlert)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddMemberModal) {
        AlertDialog(
            onDismissRequest = { showAddMemberModal = false },
            title = { Text("Convidar / Adicionar Operador", color = TextWhite, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "O operador terá o seu próprio PIN de balcão. O Caixa tem acesso apenas a produtos, vendas e clientes (sem ver lucros ou despesas).",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        placeholder = { Text("Nome completo", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        placeholder = { Text("Telemóvel (+258 84/85/86/87)", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it },
                        placeholder = { Text("PIN de 4 dígitos", color = TextMuted) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank() && phoneInput.isNotBlank() && pinInput.length >= 4) {
                            NexoraRepository.inviteTeamMember(nameInput.trim(), phoneInput.trim(), UserRole.CAIXA, pinInput.trim())
                            Toast.makeText(context, "Operador adicionado com sucesso!", Toast.LENGTH_SHORT).show()
                            showAddMemberModal = false
                            nameInput = ""
                            phoneInput = ""
                            pinInput = ""
                        } else {
                            Toast.makeText(context, "Preenche todos os campos (PIN mínimo 4 dígitos)", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary)
                ) {
                    Text("Adicionar Operador")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberModal = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            },
            containerColor = NavyCardElevated
        )
    }
}
