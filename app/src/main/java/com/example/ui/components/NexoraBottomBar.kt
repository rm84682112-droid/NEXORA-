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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import com.example.ui.theme.HairlineDivider
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NexoraAmberWarning
import com.example.ui.theme.NexoraBluePrimary
import com.example.ui.theme.NexoraRedAlert
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

enum class MainNavigationItem(
    val title: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Painel", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_dashboard"),
    PRODUTOS("Produtos", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag, "nav_products"),
    VENDAS("Balcão", Icons.Filled.PointOfSale, Icons.Outlined.PointOfSale, "nav_pos"),
    STOCK("Stock", Icons.Filled.Inventory2, Icons.Outlined.Inventory2, "nav_stock"),
    MAIS("Mais", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz, "nav_more");

    val icon: ImageVector get() = filledIcon
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
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = NavySurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, HairlineDivider)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainNavigationItem.values().forEach { item ->
                val isSelected = item == selectedItem
                val hasBadge = (item == MainNavigationItem.STOCK && lowStockCount > 0) ||
                        (item == MainNavigationItem.MAIS && activeDebtsCount > 0)

                val badgeText = when (item) {
                    MainNavigationItem.STOCK -> if (lowStockCount > 99) "99+" else lowStockCount.toString()
                    MainNavigationItem.MAIS -> if (activeDebtsCount > 99) "99+" else activeDebtsCount.toString()
                    else -> ""
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = NexoraBluePrimary.copy(alpha = 0.3f))
                        ) { onItemSelected(item) }
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
                                        contentColor = Color.Black
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
                                    .size(width = 44.dp, height = 28.dp)
                                    .background(
                                        if (isSelected) NexoraBluePrimary else Color.Transparent,
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                                    contentDescription = item.title,
                                    tint = if (isSelected) Color.White else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = item.title,
                            color = if (isSelected) TextWhite else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
