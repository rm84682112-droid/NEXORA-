package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.NexoraAmberWarning
import com.example.ui.theme.NexoraBlueLight
import com.example.ui.theme.NexoraBluePrimary
import com.example.ui.theme.NexoraCyanAccent
import com.example.ui.theme.NexoraRedAlert
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

enum class MainNavigationItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "nav_dashboard"),
    PRODUTOS("Produtos", Icons.Default.ShoppingBag, "nav_products"),
    VENDAS("Vendas", Icons.Default.PointOfSale, "nav_pos"),
    STOCK("Stock", Icons.Default.Inventory2, "nav_stock"),
    MAIS("Mais", Icons.Default.MoreHoriz, "nav_more")
}

@Composable
fun NexoraBottomBar(
    selectedItem: MainNavigationItem,
    onItemSelected: (MainNavigationItem) -> Unit,
    lowStockCount: Int = 0,
    activeDebtsCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = NavySurface,
        tonalElevation = 12.dp,
        border = androidx.compose.foundation.BorderStroke(0.8.dp, NavyCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainNavigationItem.values().forEach { item ->
                val isSelected = item == selectedItem
                val hasBadge = (item == MainNavigationItem.STOCK && lowStockCount > 0) ||
                        (item == MainNavigationItem.MAIS && activeDebtsCount > 0)

                val badgeText = when (item) {
                    MainNavigationItem.STOCK -> lowStockCount.toString()
                    MainNavigationItem.MAIS -> activeDebtsCount.toString()
                    else -> ""
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onItemSelected(item) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag(item.testTag),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        BadgedBox(
                            badge = {
                                if (hasBadge) {
                                    Badge(
                                        containerColor = if (item == MainNavigationItem.STOCK) NexoraAmberWarning else NexoraRedAlert,
                                        contentColor = NavyBackground
                                    ) {
                                        Text(
                                            text = badgeText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 36.dp else 28.dp)
                                    .background(
                                        if (isSelected) NexoraBluePrimary.copy(alpha = 0.2f) else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = if (isSelected) NexoraCyanAccent else TextMuted,
                                    modifier = Modifier.size(if (isSelected) 24.dp else 22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = item.title,
                            color = if (isSelected) TextWhite else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
