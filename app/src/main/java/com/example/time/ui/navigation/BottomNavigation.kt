package com.example.time.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.time.ui.theme.*

/**
 * Bottom Navigation Bar - 底部导航栏
 * 严格按照 UI 设计规范实现
 */
@Composable
fun TimeBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(ComponentSize.BottomNavHeight),
        color = Color.White.copy(alpha = 0.9f)
    ) {
        Column {
            // 顶部边框
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(NeutralGray.copy(alpha = 0.05f))
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = Spacing.Large),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItems.items.forEach { item ->
                    BottomNavItem(
                        item = item,
                        selected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) }
                    )
                }
            }
        }
    }
}

/**
 * Bottom Navigation Item - 底部导航项
 */
@Composable
private fun BottomNavItem(
    item: BottomNavItemData,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(vertical = Spacing.Small)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.icon,
                contentDescription = item.label,
                tint = if (selected) TextPrimary else TextTertiary,
                modifier = Modifier.size(IconSize.Medium)
            )
        }
        
        Spacer(modifier = Modifier.height(Spacing.ExtraSmall))
        
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) TextPrimary else TextTertiary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

/**
 * Bottom Navigation Items Data
 */
object BottomNavItems {
    val items = listOf(
        BottomNavItemData(
            route = "home",
            label = "首页",
            icon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home
        ),
        BottomNavItemData(
            route = "statistics",
            label = "统计",
            icon = Icons.Outlined.BarChart,
            selectedIcon = Icons.Filled.BarChart
        ),
        BottomNavItemData(
            route = "apps",
            label = "应用",
            icon = Icons.Outlined.Apps,
            selectedIcon = Icons.Filled.Apps
        ),
        BottomNavItemData(
            route = "reminders",
            label = "提醒",
            icon = Icons.Outlined.Notifications,
            selectedIcon = Icons.Filled.Notifications
        ),
        BottomNavItemData(
            route = "history",
            label = "历史",
            icon = Icons.Outlined.History,
            selectedIcon = Icons.Filled.History
        )
    )
}

data class BottomNavItemData(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

