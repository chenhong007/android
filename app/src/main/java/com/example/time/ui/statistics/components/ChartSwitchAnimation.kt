package com.example.time.ui.statistics.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

/**
 * 图表切换动画容器
 * 提供平滑的图表切换过渡效果
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ChartSwitchAnimationContainer(
    currentChart: ChartType,
    onChartTypeChanged: (ChartType) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var previousChart by remember { mutableStateOf(currentChart) }
    
    AnimatedContent(
        targetState = currentChart,
        modifier = modifier,
        transitionSpec = {
            val direction = if (targetState.ordinal > initialState.ordinal) {
                AnimatedContentTransitionScope.SlideDirection.Left
            } else {
                AnimatedContentTransitionScope.SlideDirection.Right
            }
            
            slideIntoContainer(direction) + fadeIn() with
            slideOutOfContainer(direction) + fadeOut()
        },
        label = "chart_switch_animation"
    ) { chartType ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

/**
 * 增强型图表类型切换器
 * 使用渐变按钮和动画效果
 */
@Composable
fun EnhancedChartTypeSwitcher(
    selectedChart: ChartType,
    onChartSelected: (ChartType) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChartType.values().forEach { chartType ->
            val isSelected = selectedChart == chartType
            
            AnimatedChartTypeButton(
                chartType = chartType,
                selected = isSelected,
                onClick = { onChartSelected(chartType) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 动画图表类型按钮
 */
@Composable
private fun AnimatedChartTypeButton(
    chartType: ChartType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "button_scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (selected) 6.dp else 3.dp,
        animationSpec = tween(durationMillis = 200),
        label = "button_elevation"
    )
    
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (selected) 0.9f else 0.3f,
        animationSpec = tween(durationMillis = 200),
        label = "background_alpha"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = getChartTypeGradient(chartType),
                    alpha = backgroundAlpha
                )
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) {
                        Color.White.copy(alpha = 0.8f)
                    } else {
                        Color.White.copy(alpha = 0.4f)
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .shadow(
                    elevation = elevation,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = Color.Black.copy(alpha = 0.2f)
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedIcon(
                    icon = getChartTypeIcon(chartType),
                    selected = selected,
                    modifier = Modifier.size(18.dp)
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                AnimatedVisibility(
                    visible = selected,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Text(
                        text = getChartTypeLabel(chartType),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * 动画图标
 */
@Composable
private fun AnimatedIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val tint by animateColorAsState(
        targetValue = if (selected) Color.White else TextSecondary,
        animationSpec = tween(durationMillis = 200),
        label = "icon_tint"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "icon_scale"
    )
    
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = modifier
            .scale(scale)
            .rotate(if (selected) 360f else 0f)
    )
}

/**
 * 图表内容切换动画
 */
@Composable
fun ChartContentTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        ) + fadeIn(
            animationSpec = tween(durationMillis = 400)
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it / 2 },
            animationSpec = tween(durationMillis = 400)
        ) + fadeOut(
            animationSpec = tween(durationMillis = 300)
        )
    ) {
        content()
    }
}

/**
 * 图表加载动画
 */
@Composable
fun ChartLoadingAnimation(
    modifier: Modifier = Modifier,
    message: String = "正在加载图表数据..."
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_animation")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading_rotation"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loading_alpha"
    )
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    brush = BrandGradientBrush,
                    alpha = alpha
                )
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.6f),
                    shape = CircleShape
                )
                .rotate(rotation),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Analytics,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 图表数据更新动画
 */
@Composable
fun ChartDataUpdateAnimation(
    dataUpdated: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = dataUpdated,
        modifier = modifier,
        enter = scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(durationMillis = 300)
        ) + fadeIn(),
        exit = scaleOut(
            targetScale = 1.2f,
            animationSpec = tween(durationMillis = 200)
        ) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(SuccessLight)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.8f),
                    shape = CircleShape
                )
        )
    }
}

/**
 * 图表类型枚举
 */
enum class ChartType {
    BAR_CHART,
    CALENDAR_HEATMAP,
    TIMELINE
}

/**
 * 获取图表类型渐变
 */
private fun getChartTypeGradient(chartType: ChartType): Brush {
    return when (chartType) {
        ChartType.BAR_CHART -> BrandGradientBrush
        ChartType.CALENDAR_HEATMAP -> BluePurpleGradientBrush
        ChartType.TIMELINE -> SuccessGradientBrush
    }
}

/**
 * 获取图表类型图标
 */
private fun getChartTypeIcon(chartType: ChartType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (chartType) {
        ChartType.BAR_CHART -> Icons.Default.BarChart
        ChartType.CALENDAR_HEATMAP -> Icons.Default.CalendarMonth
        ChartType.TIMELINE -> Icons.Default.Timeline
    }
}

/**
 * 获取图表类型标签
 */
private fun getChartTypeLabel(chartType: ChartType): String {
    return when (chartType) {
        ChartType.BAR_CHART -> "柱状图"
        ChartType.CALENDAR_HEATMAP -> "热力图"
        ChartType.TIMELINE -> "时间线"
    }
}

// 预览函数
@Preview(showBackground = true)
@Composable
private fun EnhancedChartTypeSwitcherPreview() {
    TimeTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var selectedChart by remember { mutableStateOf(ChartType.BAR_CHART) }
            
            EnhancedChartTypeSwitcher(
                selectedChart = selectedChart,
                onChartSelected = { selectedChart = it }
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { selectedChart = ChartType.BAR_CHART },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("柱状图")
                }
                
                Button(
                    onClick = { selectedChart = ChartType.CALENDAR_HEATMAP },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("热力图")
                }
                
                Button(
                    onClick = { selectedChart = ChartType.TIMELINE },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("时间线")
                }
            }
            
            // 显示当前选中图表
            ChartContentTransition(
                visible = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = BackgroundGradientBrush,
                        alpha = 0.3f
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = "当前图表: ${getChartTypeLabel(selectedChart)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChartLoadingAnimationPreview() {
    TimeTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = BackgroundGradientBrush,
                    alpha = 0.2f
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            ChartLoadingAnimation()
        }
    }
}