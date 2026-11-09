package com.example.ui.screens.notifications

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNotification
import com.example.data.model.NotificationType
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.NexoraAmberWarning
import com.example.ui.theme.NexoraBlueLight
import com.example.ui.theme.NexoraBluePrimary
import com.example.ui.theme.NexoraCyanAccent
import com.example.ui.theme.NexoraGreenSuccess
import com.example.ui.theme.NexoraPurple
import com.example.ui.theme.NexoraRedAlert
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.Formatters

@Composable
fun NotificationsScreen(
    notifications: List<AppNotification>,
    onMarkAsRead: (String) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onDeleteNotification: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val unreadCount = notifications.count { !it.isRead }

    Box(modifier = modifier.fillMaxSize().background(NavyBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
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
                        Text("Centro de Notificações", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("$unreadCount avisos por ler", color = TextMuted, fontSize = 12.sp)
                    }

                    if (unreadCount > 0) {
                        TextButton(onClick = onMarkAllAsRead) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, tint = NexoraCyanAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ler Todas", color = NexoraCyanAccent, fontSize = 12.sp)
                        }
                    }
                }
            }

            if (notifications.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔔", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Sem notificações", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Tudo sob controlo na sua mercearia.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }

            items(notifications, key = { it.id }) { notif ->
                NotificationItemCard(
                    notification = notif,
                    onMarkRead = { onMarkAsRead(notif.id) },
                    onDelete = { onDeleteNotification(notif.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun NotificationItemCard(
    notification: AppNotification,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit
) {
    val badgeColor = when (notification.type) {
        NotificationType.ESGOTADO -> NexoraRedAlert
        NotificationType.STOCK_BAIXO -> NexoraAmberWarning
        NotificationType.FIADO_VENCIDO -> NexoraAmberWarning
        NotificationType.FORNECEDOR_PENDENTE -> NexoraBlueLight
        NotificationType.PAGAMENTO_RECEBIDO -> NexoraGreenSuccess
        NotificationType.IA_ALERTA -> NexoraPurple
        NotificationType.SISTEMA -> NexoraCyanAccent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMarkRead() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead) NavyCardElevated else NavySurfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (!notification.isRead) badgeColor.copy(alpha = 0.5f) else NavyCardBorder
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(badgeColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(notification.type.iconEmoji, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = notification.title,
                            color = TextWhite,
                            fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        if (!notification.isRead) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.size(6.dp).background(badgeColor, CircleShape))
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))
                    Text(text = notification.message, color = TextLight, fontSize = 12.sp, lineHeight = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = Formatters.formatDateTime(notification.timestamp), color = TextMuted, fontSize = 10.sp)
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = TextMuted, modifier = Modifier.size(15.dp))
            }
        }
    }
}
