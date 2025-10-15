package com.example.time.ui.statistics.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.time.ui.charts.models.BarChartData
import com.example.time.ui.components.LoadingIndicator
import com.example.time.ui.components.ErrorMessage
import com.example.time.ui.theme.*
import kotlin.math.roundToInt

/**
 * 渐变柱状图组件
 * 支持动画效果、交互和自定义样式
 */
@Composable
fun GradientBarChart(
    data: List<BarChartItem>,
    modifier: Modifier = Modifier,
    barWidth: Dp = 24.dp,
    barSpacing: Dp = 8.dp,
    cornerRadius: Dp = 4.dp,
    animationDuration: Int = 1000,
    showLabels: Boolean = true,
    showValues: Boolean = true,
    onBarClick: (BarChartItem) -> Unit = {},
    selectedBar: String? = null
) {
    val animatedProgress = remember { Animatable(0f) }
    var selectedBarIndex by remember { mutableStateOf(-1) }
    
    LaunchedEffect(Unit) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = animationDuration,
                easing = FastOutSlowInEasing
            )
        )
    }
    
    if (data.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无数据",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        return
    }
    
    val maxValue = data.maxOf { it.value }
    val totalWidth = (barWidth + barSpacing) * data.size - barSpacing
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 图表区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (showLabels) 32.dp else 0.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val startX = (canvasWidth - totalWidth.toPx()) / 2f
                val labelHeightPx = if (showLabels) 32.dp.toPx() else 0f
                val maxBarHeight = canvasHeight - labelHeightPx
                
                data.forEachIndexed { index, item ->
                    val x = startX + index * (barWidth + barSpacing).toPx()
                    val barHeight = (item.value / maxValue) * maxBarHeight * animatedProgress.value
                    val y = canvasHeight - barHeight - labelHeightPx
                    
                    // 创建渐变
                    val gradient = Brush.verticalGradient(
                        colors = listOf(
                            item.gradientStart,
                            item.gradientEnd
                        ),
                        startY = y,
                        endY = y + barHeight
                    )
                    
                    // 绘制柱子
                    drawRoundRect(
                        brush = gradient,
                        topLeft = Offset(x, y),
                        size = Size(barWidth.toPx(), barHeight),
                        cornerRadius = CornerRadius(cornerRadius.toPx()),
                        alpha = if (selectedBar == item.label) 1f else 0.9f
                    )
                    
                    // 选中效果
                    if (selectedBar == item.label) {
                        val strokeWidth = 2.dp.toPx()
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.3f),
                            topLeft = Offset(x - strokeWidth, y - strokeWidth),
                            size = Size(barWidth.toPx() + strokeWidth * 2, barHeight + strokeWidth * 2),
                            cornerRadius = CornerRadius(cornerRadius.toPx() + strokeWidth),
                            style = Stroke(width = strokeWidth)
                        )
                    }
                    
                    // 数值标签
                    if (showValues) {
                        val valueLabelOffset = 8.dp.toPx()
                        drawContext.canvas.nativeCanvas.apply {
                            drawText(
                                item.formattedValue,
                                x + barWidth.toPx() / 2,
                                y - valueLabelOffset,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.WHITE
                                    textSize = 10.dp.toPx()
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isFakeBoldText = true
                                }
                            )
                        }
                    }
                    
                    // 底部标签
                    if (showLabels) {
                        val bottomLabelOffset = 8.dp.toPx()
                        drawContext.canvas.nativeCanvas.apply {
                            drawText(
                                item.label,
                                x + barWidth.toPx() / 2,
                                canvasHeight - bottomLabelOffset,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.GRAY
                                    textSize = 12.dp.toPx()
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                            )
                        }
                    }
                }
            }
            
            // 点击区域
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (showLabels) 32.dp else 0.dp),
                horizontalArrangement = Arrangement.spacedBy(barSpacing)
            ) {
                val localDensity = LocalDensity.current
                val barWidthPx = with(localDensity) { barWidth.toPx() }
                
                data.forEachIndexed { index, item ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { 
                                    selectedBarIndex = index
                                    onBarClick(item)
                                }
                            )
                    )
                }
            }
        }
        
        // 图例
        if (data.isNotEmpty() && data.size > 1) {
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(data.size) { index ->
                    val item = data[index]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(item.gradientStart, item.gradientEnd)
                                    ),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * 增强的柱状图组件，包含标题和交互
 */
@Composable
fun EnhancedGradientBarChart(
    title: String,
    data: List<BarChartItem>,
    modifier: Modifier = Modifier,
    showHeader: Boolean = true,
    onBarClick: (BarChartItem) -> Unit = {},
    selectedBar: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        if (showHeader) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                IconButton(
                    onClick = { /* 更多选项 */ },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多选项",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        GradientBarChart(
            data = data,
            onBarClick = onBarClick,
            selectedBar = selectedBar
        )
    }
}

/**
 * 柱状图数据项
 */
data class BarChartItem(
    val label: String,
    val value: Float,
    val formattedValue: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    val data: Any? = null
)

/**
 * 创建示例数据
 */
fun createSampleBarChartData(): List<BarChartItem> {
    return listOf(
        BarChartItem(
            label = "微信",
            value = 180f,
            formattedValue = "3小时",
            gradientStart = WeChatGreen,
            gradientEnd = WeChatGreenDark
        ),
        BarChartItem(
            label = "抖音",
            value = 150f,
            formattedValue = "2.5小时",
            gradientStart = InstagramPurple,
            gradientEnd = InstagramPurpleDark
        ),
        BarChartItem(
            label = "Chrome",
            value = 120f,
            formattedValue = "2小时",
            gradientStart = ChromeBlue,
            gradientEnd = ChromeBlueDark
        ),
        BarChartItem(
            label = "YouTube",
            value = 90f,
            formattedValue = "1.5小时",
            gradientStart = YouTubeRed,
            gradientEnd = YouTubeRedDark
        ),
        BarChartItem(
            label = "其他",
            value = 60f,
            formattedValue = "1小时",
            gradientStart = SuccessGreen,
            gradientEnd = SuccessGreenLight
        )
    )
}

// 预览函数
@Preview(showBackground = true)
@Composable
private fun GradientBarChartPreview() {
    TimeTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GradientBarChart(
                data = createSampleBarChartData(),
                modifier = Modifier.fillMaxWidth()
            )
            
            EnhancedGradientBarChart(
                title = "应用使用排行",
                data = createSampleBarChartData(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}