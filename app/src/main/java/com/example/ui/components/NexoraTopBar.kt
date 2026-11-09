package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NexoraAmberWarning
import com.example.ui.theme.NexoraBlueLight
import com.example.ui.theme.NexoraBluePrimary
import com.example.ui.theme.NexoraCyanAccent
import com.example.ui.theme.NexoraGreenSuccess
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun NexoraTopBar(
    currentUser: User,
    allUsers: List<User>,
    onSwitchUser: (User) -> Unit = {},
    isOnline: Boolean = true,
    pendingSyncCount: Int = 0,
    onToggleOnline: () -> Unit = {},
    unreadNotificationsCount: Int = 0,
    onOpenNotifications: () -> Unit = {},
    onOpenHelp: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = NavySurface,
        tonalElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, NavyCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand & Location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(NexoraBluePrimary, NexoraCyanAccent)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "N",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "NEXORA",
                            color = TextWhite,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))

                        // Offline / Online Indicator Pill (Interactive for testing)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onToggleOnline() }
                                .testTag("connectivity_indicator_pill"),
                            color = if (isOnline) NexoraGreenSuccess.copy(alpha = 0.15f) else NexoraAmberWarning.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(if (isOnline) NexoraGreenSuccess else NexoraAmberWarning, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isOnline) "Online" else "Offline ($pendingSyncCount)",
                                    color = if (isOnline) NexoraGreenSuccess else NexoraAmberWarning,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = currentUser.storeName,
                        color = TextMuted,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Help Button (?)
                IconButton(
                    onClick = onOpenHelp,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("top_bar_help_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(NexoraCyanAccent.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "?",
                            color = NexoraCyanAccent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Notification Bell
                IconButton(
                    onClick = onOpenNotifications,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("top_bar_notifications_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotificationsCount > 0) {
                                Badge(
                                    containerColor = NexoraAmberWarning,
                                    contentColor = Color.Black
                                ) {
                                    Text(unreadNotificationsCount.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notificações",
                            tint = if (unreadNotificationsCount > 0) NexoraCyanAccent else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
