package com.example.ui.screens.clients

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Client
import com.example.data.model.Sale
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
import com.example.ui.theme.NexoraRedAlert
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    clients: List<Client>,
    sales: List<Sale>,
    onAddClient: (name: String, phone: String, address: String, creditLimit: Double, notes: String) -> Unit,
    onUpdateClient: (Client) -> Unit,
    onDeleteClient: (String) -> Unit,
    onNavigateToDebts: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddSheet by remember { mutableStateOf(false) }
    var clientToEdit by remember { mutableStateOf<Client?>(null) }
    var clientDetailsToShow by remember { mutableStateOf<Client?>(null) }

    val filteredClients = remember(clients, searchQuery) {
        if (searchQuery.isBlank()) clients
        else clients.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.phone.contains(searchQuery, ignoreCase = true) ||
                    it.address.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalClientsDebt = remember(clients) { clients.sumOf { it.totalDebt } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Gestão de Clientes",
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${clients.size} clientes cadastrados",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { showAddSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("add_client_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Novo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total em Fiado a Receber", color = TextMuted, fontSize = 11.sp)
                            Text(
                                text = Formatters.formatMoney(totalClientsDebt),
                                color = if (totalClientsDebt > 0) NexoraAmberWarning else NexoraGreenSuccess,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Button(
                            onClick = onNavigateToDebts,
                            colors = ButtonDefaults.buttonColors(containerColor = NexoraCardBorderColor()),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Ver Fiado", color = NexoraCyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Pesquisar cliente por nome ou telemóvel...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = NexoraCyanAccent)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = TextMuted)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("client_search_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NavySurfaceVariant,
                        unfocusedContainerColor = NavySurfaceVariant,
                        focusedBorderColor = NexoraBlueLight,
                        unfocusedBorderColor = NavyCardBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextLight
                    ),
                    singleLine = true
                )
            }

            if (filteredClients.isEmpty()) {
                item {
                    com.example.ui.components.EducationalEmptyState(
                        iconEmoji = "👥",
                        title = "Ainda não tem clientes registados",
                        description = "Adicione o primeiro cliente para registar vendas a fiado e contactos.",
                        actionLabel = "Novo Cliente",
                        onAction = { showAddSheet = true }
                    )
                }
            } else {
                // Client Cards
                items(filteredClients, key = { it.id }) { client ->
                    ClientCard(
                        client = client,
                        onClick = { clientDetailsToShow = client },
                        onEdit = { clientToEdit = client }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal: Add or Edit Client
    if (showAddSheet || clientToEdit != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val isEditing = clientToEdit != null

        ModalBottomSheet(
            onDismissRequest = {
                showAddSheet = false
                clientToEdit = null
            },
            sheetState = sheetState,
            containerColor = NavySurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            ClientFormSheet(
                existingClient = clientToEdit,
                onDismiss = {
                    showAddSheet = false
                    clientToEdit = null
                },
                onSave = { name, phone, address, creditLimit, notes ->
                    if (isEditing && clientToEdit != null) {
                        onUpdateClient(
                            clientToEdit!!.copy(
                                name = name,
                                phone = phone,
                                address = address,
                                creditLimit = creditLimit,
                                notes = notes
                            )
                        )
                    } else {
                        onAddClient(name, phone, address, creditLimit, notes)
                    }
                    showAddSheet = false
                    clientToEdit = null
                }
            )
        }
    }

    // Modal: Client Profile Details
    clientDetailsToShow?.let { client ->
        val clientSales = remember(sales, client) { sales.filter { it.customerId == client.id } }

        ClientDetailDialog(
            client = client,
            sales = clientSales,
            onDismiss = { clientDetailsToShow = null },
            onEdit = {
                clientToEdit = client
                clientDetailsToShow = null
            }
        )
    }
}

@Composable
private fun NexoraCardBorderColor() = NavyCardElevated

@Composable
private fun ClientCard(
    client: Client,
    onClick: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("client_card_${client.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(NexoraBluePrimary.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = NexoraCyanAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = client.name,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${client.phone} • ${client.address}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (client.hasDebt) {
                    Box(
                        modifier = Modifier
                            .background(NexoraRedAlert.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = Formatters.formatMoney(client.totalDebt),
                            color = NexoraRedAlert,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Fiado em aberto", color = TextMuted, fontSize = 9.sp)
                } else {
                    Box(
                        modifier = Modifier
                            .background(NexoraGreenSuccess.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Em dia",
                            color = NexoraGreenSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientFormSheet(
    existingClient: Client?,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, address: String, creditLimit: Double, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(existingClient?.name ?: "") }
    var phone by remember { mutableStateOf(existingClient?.phone ?: "+258 8") }
    var address by remember { mutableStateOf(existingClient?.address ?: "") }
    var creditLimitStr by remember { mutableStateOf(existingClient?.creditLimit?.toString() ?: "5000") }
    var notes by remember { mutableStateOf(existingClient?.notes ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (existingClient == null) "Registar Novo Cliente" else "Editar Dados do Cliente",
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome Completo") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = clientFormColors()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Telemóvel (+258 84 / 85 / 86 / 87)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = clientFormColors()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Bairro / Endereço (ex: Bairro do Jardim)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = clientFormColors()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = creditLimitStr,
            onValueChange = { creditLimitStr = it },
            label = { Text("Limite de Crédito Fiado (MT)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = clientFormColors()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notas / Observações") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = clientFormColors()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val limit = creditLimitStr.toDoubleOrNull() ?: 5000.0
                onSave(name.ifBlank { "Cliente Sem Nome" }, phone, address, limit, notes)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NexoraBluePrimary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (existingClient == null) "Guardar Cliente" else "Atualizar Dados",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ClientDetailDialog(
    client: Client,
    sales: List<Sale>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = client.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = NexoraCyanAccent, modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "📞 ${client.phone}", color = TextLight, fontSize = 13.sp)
                Text(text = "📍 ${client.address}", color = TextMuted, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavyCardElevated, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Dívida Atual", color = TextMuted, fontSize = 10.sp)
                        Text(
                            text = Formatters.formatMoney(client.totalDebt),
                            color = if (client.hasDebt) NexoraRedAlert else NexoraGreenSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Limite Autorizado", color = TextMuted, fontSize = 10.sp)
                        Text(
                            text = Formatters.formatMoney(client.creditLimit),
                            color = TextLight,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(text = "Histórico Recente de Compras (${sales.size})", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                if (sales.isEmpty()) {
                    Text(text = "Nenhuma compra associada a este cliente.", color = TextMuted, fontSize = 11.sp)
                } else {
                    sales.take(4).forEach { sale ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${sale.receiptNumber} (${Formatters.formatDate(sale.timestamp)})", color = TextLight, fontSize = 11.sp)
                            Text(text = Formatters.formatMoney(sale.total), color = NexoraCyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = NexoraBlueLight)
            }
        },
        containerColor = NavySurface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun clientFormColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = NavySurfaceVariant,
    unfocusedContainerColor = NavySurfaceVariant,
    focusedBorderColor = NexoraBlueLight,
    unfocusedBorderColor = NavyCardBorder,
    focusedTextColor = TextWhite,
    unfocusedTextColor = TextLight,
    focusedLabelColor = NexoraCyanAccent,
    unfocusedLabelColor = TextMuted
)
