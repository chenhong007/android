package com.example.time.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.text.style.TextOverflow
import com.example.time.data.model.AppUsageSummary
import com.example.time.ui.components.*
import com.example.time.ui.theme.*
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import com.example.time.utils.AppNameMapper

/**
 * Home Screen - 首页
 * 严格按照 UI/首页.html 实现
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    HomeScreen(
        uiState = uiState,
        onNavigateToStats = { /* TODO: 导航到统计页面 */ },
        onNavigateToApps = { /* TODO: 导航到应用列表 */ },
        onNavigateToAppDetail = { /* TODO: 导航到应用详情 */ },
        modifier = modifier
    )
}

/**
 * Home Screen - 无 ViewModel 版本（便于预览和测试）
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onNavigateToStats: () -> Unit,
    onNavigateToApps: () -> Unit,
    onNavigateToAppDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(brush = BackgroundGradientBrush)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 问候语区域
        item {
            Spacer(modifier = Modifier.height(16.dp))
            GreetingSection(
                greeting = uiState.greeting,
                isLoading = uiState.isLoading
            )
        }
        
        // 今日使用时长主卡片
        item {
            TodayUsageCard(
                totalDuration = uiState.totalDuration,
                yesterdayComparison = uiState.yesterdayComparison,
                isDecrease = uiState.isDecrease
            )
        }
        
        // 快速统计区域
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickStatCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp),
                    icon = Icons.Default.Lock,
                    value = "${uiState.unlockCount}",
                    label = "解锁次数",
                    progress = uiState.getUnlockProgress(),
                    iconBrush = BluePurpleGradientBrush,
                    subLabel = "比昨天 ${if (uiState.unlockCountDiff >= 0) "+${uiState.unlockCountDiff}" else uiState.unlockCountDiff.toString()}"
                )
                
                QuickStatCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp),
                    icon = Icons.Default.WbSunny,
                    value = formatScreenOnDuration(uiState.screenOnDuration),
                    label = "屏幕时间",
                    progress = uiState.getScreenOnProgress(),
                    iconBrush = GreenTealGradientBrush,
                    subLabel = "比昨天 ${formatScreenOnDurationDiff(uiState.screenOnDurationDiff)}"
                )
            }
        }
        
        // 最常用应用列表（放在圆角卡片中）
        item {
            val context = LocalContext.current
            GlassmorphismCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 标题
                    Text(
                        text = "今日最常用应用",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    // 应用列表
                    uiState.topApps.take(3).forEach { app ->
                        // 转换应用名称
                        val displayName = AppNameMapper.getAppName(context, app.packageName, app.appName)
                        MostUsedAppItem(
                            app = app.copy(appName = displayName),
                            onClick = { onNavigateToAppDetail(app.packageName) }
                        )
                    }
                }
            }
        }
        
        // 底部间距
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * 问候语区域
 */
@Composable
private fun GreetingSection(
    greeting: String,
    isLoading: Boolean
) {
    Column {
        Text(
            text = greeting,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isLoading) "加载中..." else "让我们看看今天的使用情况",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 今日使用时长主卡片
 */
@Composable
private fun TodayUsageCard(
    totalDuration: Long,
    yesterdayComparison: String,
    isDecrease: Boolean
) {
    GradientCard(
            modifier = Modifier.fillMaxWidth(),
            gradient = DarkGradientBrush,
            cornerRadius = 20.dp,
            elevation = 8.dp
        ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // 标题和图标
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "今日使用时长",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.WatchLater,
                    contentDescription = "时钟",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 时长
            Text(
                text = formatDuration(totalDuration),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 对比
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isDecrease) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                    contentDescription = if (isDecrease) "减少" else "增加",
                    tint = if (isDecrease) SuccessGreenLight else ErrorRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "比昨天${if (isDecrease) "减少" else "增加"} $yesterdayComparison",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDecrease) SuccessGreenLight else ErrorRed
                )
            }
        }
    }
}

/**
 * 快速统计卡片
 */
@Composable
private fun QuickStatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    progress: Float,
    iconBrush: Brush,
    subLabel: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "scale"
    )
    
    GlassmorphismCard(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { /* 点击效果 */ },
        cornerRadius = 20.dp,
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            // 图标和数值
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 渐变图标容器
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(brush = iconBrush, shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Text(
                    text = value,
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 标签
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 进度条
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = SuccessGreen,
                trackColor = Color.Gray.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 子标签
            Text(
                text = subLabel,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
    }
}

/**
 * 最常用应用列表项（参考 UI/首页.html 设计）
 */
@Composable
private fun MostUsedAppItem(
    app: AppUsageSummary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val appBrush = getAppGradientBrush(app.appName)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "scale"
    )

    // 获取真实的应用图标
    val appIcon = remember(app.packageName) {
        try {
            context.packageManager.getApplicationIcon(app.packageName)
        } catch (e: Exception) {
            null
        }
    }
    
    // 计算使用占比的进度条宽度
    val progressWidth = (app.percentage / 100f).coerceIn(0f, 1f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .background(
                color = Color.Gray.copy(alpha = 0.03f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 应用图标（带渐变背景）
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    brush = appBrush,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (appIcon != null) {
                Image(
                    bitmap = appIcon.toBitmap(
                        width = 128,
                        height = 128
                    ).asImageBitmap(),
                    contentDescription = app.appName,
                    modifier = Modifier.size(28.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    imageVector = getAppIcon(app.appName),
                    contentDescription = app.appName,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 应用信息
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = app.appName,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = app.category,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 进度条（显示使用占比）
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.Gray.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(getAppColor(app.appName))
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 使用时长和占比
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = formatDuration(app.totalDuration),
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${String.format("%.0f", app.percentage)}%",
                style = MaterialTheme.typography.bodyMedium,
                color = getAppColor(app.appName),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Helper functions

private fun formatDuration(millis: Long): String {
    val hours = millis / (1000 * 60 * 60)
    val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
    
    return if (hours > 0) {
        "${hours}小时 ${minutes}分"
    } else if (minutes > 0) {
        "${minutes}分钟"
    } else {
        "少于1分钟"
    }
}

/**
 * 格式化屏幕开启时长（从毫秒转换为小时分钟格式）
 */
private fun formatScreenOnDuration(millis: Long): String {
    val hours = millis / (1000 * 60 * 60)
    val minutes = (millis / (1000 * 60)) % 60
    
    return if (hours > 0) {
        "${hours}h${minutes}m"
    } else {
        "${minutes}m"
    }
}

/**
 * 格式化屏幕开启时长差值（显示与昨天的对比）
 */
private fun formatScreenOnDurationDiff(millis: Long): String {
    val hours = kotlin.math.abs(millis) / (1000 * 60 * 60)
    val sign = if (millis >= 0) "+" else ""
    
    return if (hours > 0) {
        "${sign}${if (millis >= 0) hours else -hours}h"
    } else {
        val minutes = kotlin.math.abs(millis) / (1000 * 60)
        "${sign}${if (millis >= 0) minutes else -minutes}m"
    }
}

private fun getAppGradientBrush(appName: String): Brush {
    return when {
        appName.contains("YouTube", ignoreCase = true) -> YouTubeGradientBrush
        appName.contains("微信", ignoreCase = true) -> WeChatGradientBrush
        appName.contains("Chrome", ignoreCase = true) -> ChromeGradientBrush
        appName.contains("Instagram", ignoreCase = true) -> InstagramGradientBrush
        else -> BrandGradientBrush
    }
}

private fun getAppColor(appName: String): Color {
    return when {
        appName.contains("YouTube", ignoreCase = true) -> YouTubeRed
        appName.contains("微信", ignoreCase = true) -> WeChatGreen
        appName.contains("Chrome", ignoreCase = true) -> ChromeBlue
        appName.contains("Instagram", ignoreCase = true) -> InstagramPurple
        else -> BrandBlue
    }
}

private fun getAppIcon(appName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        appName.contains("YouTube", ignoreCase = true) -> Icons.Default.PlayArrow
        appName.contains("微信", ignoreCase = true) -> Icons.Default.Message
        appName.contains("Chrome", ignoreCase = true) -> Icons.Default.Language
        appName.contains("Instagram", ignoreCase = true) -> Icons.Default.CameraAlt
        else -> Icons.Default.Apps
    }
}

