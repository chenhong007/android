package com.example.time.ui.statistics.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.time.ui.theme.*
import java.time.Duration
import java.time.format.DateTimeFormatter

/**
 * 增强型统计概览卡片
 * 使用2x2网格布局和渐变背景
 */
@Composable
fun EnhancedStatisticsOverviewCard(
    totalUsage: Duration,
    appCount: Int,
    dailyAverage: Duration,
    mostUsedApp: String?,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    val fadeIn by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "card_fade_in"
    )
    
    val slideIn by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 20.dp,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "card_slide_in"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(fadeIn)
            .offset(y = slideIn)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = BackgroundGradientBrush,
                    alpha = 0.9f
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp)
                )
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = Color.Black.copy(alpha = 0.15f)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "使用统计概览",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    // 刷新指示器
                    AnimatedRefreshIndicator()
                }
                
                // 2x2网格布局
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 左侧列
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 总使用时长
                        GradientStatItem(
                            title = "总使用时长",
                            value = formatDuration(totalUsage),
                            icon = Icons.Default.Schedule,
                            gradient = BrandGradientBrush,
                            animatedValue = true
                        )
                        
                        // 应用数量
                        GradientStatItem(
                            title = "应用数量",
                            value = appCount.toString(),
                            icon = Icons.Default.Apps,
                            gradient = BluePurpleGradientBrush,
                            animatedValue = true
                        )
                    }
                    
                    // 右侧列
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 日均使用
                        GradientStatItem(
                            title = "日均使用",
                            value = formatDuration(dailyAverage),
                            icon = Icons.Default.Today,
                            gradient = SuccessGradientBrush,
                            animatedValue = true
                        )
                        
                        // 最常用应用
                        GradientStatItem(
                            title = "最常用",
                            value = mostUsedApp ?: "暂无数据",
                            icon = Icons.Default.Star,
                            gradient = if (mostUsedApp != null) WarningGradientBrush else NeutralGradientBrush,
                            animatedValue = false
                        )
                    }
                }
                
                // 底部趋势指示器
                EnhancedTrendIndicator(
                    currentUsage = totalUsage,
                    previousUsage = totalUsage.minus(dailyAverage)
                )
            }
        }
    }
}

/**
 * 渐变统计项
 */
@Composable
private fun GradientStatItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush,
    animatedValue: Boolean,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    val fadeIn by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 200),
        label = "item_fade_in"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = tween(durationMillis = 600, delayMillis = 200),
        label = "item_scale"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = gradient,
                alpha = 0.2f
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f)
            )
            .alpha(fadeIn)
            .scale(scale)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 顶部图标和标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.6f))
                )
            }
            
            // 底部数值
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                
                if (animatedValue) {
                    AnimatedText(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

/**
 * 动画文本组件
 */
@Composable
private fun AnimatedText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    fontWeight: FontWeight,
    modifier: Modifier = Modifier
) {
    var animatedText by remember { mutableStateOf("") }
    
    LaunchedEffect(text) {
        text.forEachIndexed { index, _ ->
            animatedText = text.substring(0, index + 1)
            kotlinx.coroutines.delay(50)
        }
    }
    
    Text(
        text = animatedText,
        style = style,
        color = color,
        fontWeight = fontWeight,
        modifier = modifier
    )
}

/**
 * 动画刷新指示器
 */
@Composable
private fun AnimatedRefreshIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_indicator")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "indicator_rotation"
    )
    
    Icon(
        imageVector = Icons.Default.Refresh,
        contentDescription = "刷新",
        tint = BrandBlue.copy(alpha = 0.6f),
        modifier = Modifier
            .size(16.dp)
            .rotate(rotation)
    )
}

/**
 * 增强型趋势指示器
 */
@Composable
private fun EnhancedTrendIndicator(
    currentUsage: Duration,
    previousUsage: Duration,
    modifier: Modifier = Modifier
) {
    val trend = calculateTrend(currentUsage, previousUsage)
    val isPositive = trend > 0
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = if (isPositive) ErrorLight.copy(alpha = 0.1f) else SuccessLight.copy(alpha = 0.1f)
            )
            .border(
                width = 1.dp,
                color = if (isPositive) ErrorLight.copy(alpha = 0.3f) else SuccessLight.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
            contentDescription = null,
            tint = if (isPositive) ErrorLight else SuccessLight,
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(6.dp))
        
        Text(
            text = if (isPositive) "较上期增长 ${"%.1f".format(trend)}%" else "较上期减少 ${"%.1f".format(-trend)}%",
            style = MaterialTheme.typography.labelMedium,
            color = if (isPositive) ErrorLight else SuccessLight,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 计算趋势百分比
 */
private fun calculateTrend(current: Duration, previous: Duration): Float {
    if (previous.isZero) return 0f
    return ((current.toMinutes() - previous.toMinutes()).toFloat() / previous.toMinutes()) * 100f
}

/**
 * 格式化时长
 */
private fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "0m"
    }
}

// 预览函数
@Preview(showBackground = true)
@Composable
private fun EnhancedStatisticsOverviewCardPreview() {
    TimeTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnhancedStatisticsOverviewCard(
                totalUsage = Duration.ofHours(8).plusMinutes(30),
                appCount = 15,
                dailyAverage = Duration.ofHours(4).plusMinutes(15),
                mostUsedApp = "微信",
                modifier = Modifier.fillMaxWidth()
            )
            
            EnhancedStatisticsOverviewCard(
                totalUsage = Duration.ofHours(12).plusMinutes(45),
                appCount = 23,
                dailyAverage = Duration.ofHours(6).plusMinutes(30),
                mostUsedApp = "抖音",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}