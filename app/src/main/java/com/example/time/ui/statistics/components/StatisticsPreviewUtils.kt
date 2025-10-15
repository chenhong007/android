package com.example.time.ui.statistics.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.time.ui.theme.TimeTheme
import java.time.Duration
import java.time.LocalDate

/**
 * 预览数据生成器
 * 为所有组件提供一致的预览数据
 */
object StatisticsPreviewData {
    
    // 应用使用数据
    val appUsageData = listOf(
        AppUsageData("微信", 180, "社交"),
        AppUsageData("抖音", 150, "娱乐"),
        AppUsageData("淘宝", 90, "购物"),
        AppUsageData("支付宝", 60, "金融"),
        AppUsageData("网易云音乐", 45, "音乐"),
        AppUsageData("百度地图", 30, "导航"),
        AppUsageData("美团", 25, "外卖"),
        AppUsageData("知乎", 20, "阅读")
    )
    
    // 热力图数据
    val heatmapData = buildMap {
        val today = LocalDate.now()
        for (i in 0..29) {
            val date = today.minusDays(i.toLong())
            val usage = (Math.random() * 480 + 60).toFloat() // 1-9小时
            put(date, usage)
        }
    }
    
    // 时间线数据
    val timelineData = listOf(
        TimelineData("08:00", "微信", Duration.ofMinutes(15)),
        TimelineData("08:15", "抖音", Duration.ofMinutes(30)),
        TimelineData("08:45", "淘宝", Duration.ofMinutes(20)),
        TimelineData("09:05", "支付宝", Duration.ofMinutes(5)),
        TimelineData("09:10", "网易云音乐", Duration.ofMinutes(45)),
        TimelineData("09:55", "百度地图", Duration.ofMinutes(15))
    )
    
    // 统计概览数据
    val totalUsage = Duration.ofHours(8).plusMinutes(30)
    val appCount = 15
    val dailyAverage = Duration.ofHours(4).plusMinutes(15)
    val mostUsedApp = "微信"
    
    // 柱状图数据
    val barChartItems = appUsageData.map { data ->
        val color = getPreviewColor(data.appName)
        BarChartItem(
            label = data.appName,
            value = data.usageMinutes.toFloat(),
            formattedValue = "${data.usageMinutes / 60}小时${data.usageMinutes % 60}分",
            gradientStart = color,
            gradientEnd = color.copy(alpha = 0.7f)
        )
    }
    
    // 数据类
    data class AppUsageData(
        val appName: String,
        val usageMinutes: Long,
        val category: String
    )
    
    data class TimelineData(
        val time: String,
        val appName: String,
        val duration: Duration
    )
    
    // 获取预览颜色
    private fun getPreviewColor(appName: String): androidx.compose.ui.graphics.Color {
        val colors = listOf(
            com.example.time.ui.theme.BrandBlue,
            com.example.time.ui.theme.BrandPurple,
            com.example.time.ui.theme.SuccessLight,
            com.example.time.ui.theme.WarningLight,
            com.example.time.ui.theme.ErrorLight
        )
        return colors[appName.hashCode() % colors.size]
    }
}

/**
 * 性能优化配置
 */
object StatisticsPerformanceConfig {
    // 动画持续时间
    const val ANIMATION_DURATION_SHORT = 200
    const val ANIMATION_DURATION_MEDIUM = 400
    const val ANIMATION_DURATION_LONG = 800
    
    // 图表更新频率
    const val CHART_UPDATE_INTERVAL = 1000L // 1秒
    
    // 内存优化
    const val MAX_VISIBLE_CHART_ITEMS = 20
    const val CHART_ITEM_POOL_SIZE = 50
    
    // 渲染优化
    const val USE_COMPOSITION_LOCAL = true
    const val ENABLE_GPU_ACCELERATION = true
    
    // 数据缓存
    const val CACHE_EXPIRY_TIME = 5 * 60 * 1000L // 5分钟
    const val MAX_CACHE_SIZE = 100
}

/**
 * 性能监控工具
 */
@Composable
fun StatisticsPerformanceMonitor(
    content: @Composable () -> Unit
) {
    var renderCount by remember { mutableStateOf(0) }
    var lastRenderTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        renderCount++
        val currentTime = System.currentTimeMillis()
        val renderTime = currentTime - lastRenderTime
        lastRenderTime = currentTime
        
        // 记录性能指标
        if (renderTime > StatisticsPerformanceConfig.ANIMATION_DURATION_LONG) {
            println("⚠️ 渲染时间过长: ${renderTime}ms")
        }
    }
    
    content()
}

/**
 * 优化的可组合函数
 * 使用remember和derivedStateOf优化性能
 */
@Composable
fun OptimizedStatisticsContent(
    data: List<BarChartItem>,
    modifier: Modifier = Modifier
) {
    // 使用remember缓存计算结果
    val maxValue by remember(data) {
        derivedStateOf { data.maxOfOrNull { it.value } ?: 0f }
    }
    
    val totalValue by remember(data) {
        derivedStateOf { data.sumOf { it.value.toDouble() }.toFloat() }
    }
    
    // 使用key优化重组
    key(data.hashCode()) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "最大值: ${maxValue}",
                style = MaterialTheme.typography.bodySmall
            )
            
            Text(
                text = "总计: ${totalValue}",
                style = MaterialTheme.typography.bodySmall
            )
            
            // 只渲染可见项目
            val visibleItems = remember(data) {
                data.take(StatisticsPerformanceConfig.MAX_VISIBLE_CHART_ITEMS)
            }
            
            visibleItems.forEach { item ->
                Text(
                    text = "${item.label}: ${item.value}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * 内存优化的图表组件
 */
@Composable
fun MemoryOptimizedChart(
    data: List<BarChartItem>,
    modifier: Modifier = Modifier
) {
    // 使用rememberSaveable保持状态
    val visibleRange by rememberSaveable { mutableStateOf(0..9) }
    
    // 只渲染可见范围的数据
    val visibleData by remember(data, visibleRange) {
        derivedStateOf { data.slice(visibleRange) }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        visibleData.forEach { item ->
            // 使用key优化列表项重组
            key(item.label) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodySmall
                        )
                        
                        Text(
                            text = item.value.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

/**
 * 延迟加载的图表组件
 */
@Composable
fun LazyLoadedChart(
    dataProvider: () -> List<BarChartItem>,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    // 延迟加载数据
    val data by remember(isVisible) {
        derivedStateOf {
            if (isVisible) dataProvider() else emptyList()
        }
    }
    
    LaunchedEffect(Unit) {
        // 模拟延迟加载
        kotlinx.coroutines.delay(100)
        isVisible = true
    }
    
    if (data.isNotEmpty()) {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            data.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodySmall
                        )
                        
                        Text(
                            text = item.value.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "加载中...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// 预览函数
@Preview(showBackground = true)
@Composable
private fun StatisticsPerformanceMonitorPreview() {
    TimeTheme {
        StatisticsPerformanceMonitor {
            OptimizedStatisticsContent(
                data = StatisticsPreviewData.barChartItems,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MemoryOptimizedChartPreview() {
    TimeTheme {
        MemoryOptimizedChart(
            data = StatisticsPreviewData.barChartItems,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LazyLoadedChartPreview() {
    TimeTheme {
        LazyLoadedChart(
            dataProvider = { StatisticsPreviewData.barChartItems },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp)
        )
    }
}