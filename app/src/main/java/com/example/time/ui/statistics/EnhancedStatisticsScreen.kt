package com.example.time.ui.statistics

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.time.ui.charts.models.DateRange
import com.example.time.ui.charts.models.ChartType as ChartChartType
import com.example.time.ui.charts.models.TimelineData as ChartTimelineData
import com.example.time.ui.statistics.components.*
import com.example.time.ui.statistics.components.TimeFilter as ComponentTimeFilter
import com.example.time.ui.statistics.components.ChartType as ComponentChartType
import com.example.time.ui.statistics.TimeFilter as ViewModelTimeFilter
import com.example.time.ui.theme.*
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource

/**
 * 重构后的统计页面
 * 使用玻璃拟态效果和渐变设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedStatisticsScreen(
    navController: NavController,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var isContentVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isContentVisible = true
    }
    
    Scaffold(
        topBar = {
            EnhancedStatisticsTopAppBar(
                title = "使用统计",
                onBackClick = { navController.navigateUp() }
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundGradientBrush)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // 时间筛选器
                    ChartContentTransition(
                        visible = isContentVisible,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        EnhancedTimeFilterSection(
                            selectedFilter = convertToComponentTimeFilter(uiState.timeFilter),
                            onFilterSelected = { componentFilter -> 
                                viewModel.setTimeFilter(convertToViewModelTimeFilter(componentFilter))
                            },
                            customDateRange = uiState.customDateRange,
                            onCustomDateRangeSelected = { start, end ->
                                viewModel.setCustomDateRange(start, end)
                            }
                        )
                    }
                }
                
                item {
                    // 统计概览卡片
                    ChartContentTransition(
                        visible = isContentVisible,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        EnhancedStatisticsOverviewCard(
                            totalUsage = Duration.ofMillis(uiState.totalUsage),
                            appCount = uiState.appCount,
                            dailyAverage = Duration.ofMillis(uiState.dailyAverage),
                            mostUsedApp = uiState.mostUsedApp ?: "无"
                        )
                    }
                }
                
                item {
                    // 图表类型切换器
                    ChartContentTransition(
                        visible = isContentVisible,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        EnhancedChartTypeSwitcher(
                            selectedChart = convertToComponentChartType(uiState.selectedChartType),
                            onChartSelected = { componentType ->
                                viewModel.setChartType(convertToChartChartType(componentType))
                            }
                        )
                    }
                }
                
                item {
                    // 图表内容区域
                    ChartSwitchAnimationContainer(
                        currentChart = convertToComponentChartType(uiState.selectedChartType),
                        onChartTypeChanged = { componentType ->
                            viewModel.setChartType(convertToChartChartType(componentType))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        when (uiState.selectedChartType) {
                            ChartChartType.BAR_CHART -> {
                                // 使用柱状图显示应用使用排名
                                uiState.appUsageRanking?.let { rankings ->
                                    EnhancedAppUsageRankingSection(
                                        appUsageData = convertToAppUsageDataList(rankings),
                                        onAppClick = { appName ->
                                            // navController.navigate("app_detail/$appName")
                                        }
                                    )
                                }
                            }
                            ChartChartType.CALENDAR_HEATMAP, ChartChartType.HEATMAP -> {
                                // 使用日历热力图
                                uiState.calendarHeatmap?.let { heatmap ->
                                    EnhancedCalendarHeatmapSection(
                                        heatmapData = convertToHeatmapData(heatmap),
                                        selectedDate = uiState.selectedDate,
                                        onDateSelected = { date ->
                                            viewModel.onDateSelected(date)
                                        }
                                    )
                                }
                            }
                            ChartChartType.TIMELINE -> {
                                // 使用时间线显示每日使用详情
                                if (uiState.timelineData.isNotEmpty()) {
                                    EnhancedTimelineSection(
                                        timelineData = convertToTimelineDataList(uiState.timelineData),
                                        selectedDate = uiState.selectedDate,
                                        onDateSelected = { date ->
                                            viewModel.onDateSelected(date)
                                        }
                                    )
                                }
                            }
                            else -> {
                                // 默认显示柱状图
                            }
                        }
                    }
                }
                
                // 底部间距
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
            
            // 加载状态
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    ChartLoadingAnimation(
                        message = "正在加载统计数据..."
                    )
                }
            }
        }
    }
}

/**
 * 增强型顶部应用栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedStatisticsTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "back_button_scale"
    )
    
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        navigationIcon = {
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = BackgroundGradientBrush,
                        alpha = 0.8f
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBackClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        actions = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 导出按钮
                GlassmorphismIconButton(
                    icon = Icons.Default.Share,
                    onClick = { /* TODO: 导出统计报告 */ },
                    modifier = Modifier.size(36.dp)
                )
                
                // 刷新按钮
                GlassmorphismIconButton(
                    icon = Icons.Default.Refresh,
                    onClick = { /* TODO: 刷新数据 */ },
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = TextPrimary
        ),
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = BackgroundGradientBrush,
                alpha = 0.95f
            )
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
    )
}

/**
 * 增强型应用使用排行部分
 */
@Composable
private fun EnhancedAppUsageRankingSection(
    appUsageData: List<AppUsageData>,
    onAppClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    val fadeIn by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "ranking_fade_in"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(fadeIn)
    ) {
        if (appUsageData.isNotEmpty()) {
            EnhancedGradientBarChart(
                title = "应用使用排行",
                data = appUsageData.map { data ->
                    val appColor = getAppColor(data.appName)
                    BarChartItem(
                        label = data.appName,
                        value = data.usageMinutes.toFloat(),
                        formattedValue = "${data.usageMinutes}分钟",
                        gradientStart = appColor,
                        gradientEnd = appColor.copy(alpha = 0.7f)
                    )
                },
                onBarClick = { item ->
                    onAppClick(item.label)
                }
            )
        } else {
            EmptyChartPlaceholder(
                icon = Icons.Default.BarChart,
                title = "暂无应用使用数据",
                message = "选择其他时间段查看数据"
            )
        }
    }
}

/**
 * 增强型日历热力图部分
 */
@Composable
private fun EnhancedCalendarHeatmapSection(
    heatmapData: Map<LocalDate, Float>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    val fadeIn by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "heatmap_fade_in"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(fadeIn)
    ) {
        if (heatmapData.isNotEmpty()) {
            // TODO: 实现增强型日历热力图组件
            PlaceholderChart(
                title = "日历热力图",
                message = "热力图组件开发中..."
            )
        } else {
            EmptyChartPlaceholder(
                icon = Icons.Default.CalendarMonth,
                title = "暂无热力图数据",
                message = "选择其他时间段查看数据"
            )
        }
    }
}

/**
 * 增强型时间线部分
 */
@Composable
private fun EnhancedTimelineSection(
    timelineData: List<TimelineData>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    val fadeIn by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "timeline_fade_in"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(fadeIn)
    ) {
        if (timelineData.isNotEmpty()) {
            // TODO: 实现增强型时间线组件
            PlaceholderChart(
                title = "时间线图表",
                message = "时间线组件开发中..."
            )
        } else {
            EmptyChartPlaceholder(
                icon = Icons.Default.Timeline,
                title = "暂无时间线数据",
                message = "选择其他时间段查看数据"
            )
        }
    }
}

/**
 * 玻璃拟态图标按钮
 */
@Composable
private fun GlassmorphismIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "icon_button_scale"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .background(
                brush = BackgroundGradientBrush,
                alpha = 0.6f
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * 空图表占位符
 */
@Composable
private fun EmptyChartPlaceholder(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = BackgroundGradientBrush,
                alpha = 0.3f
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary.copy(alpha = 0.6f),
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary.copy(alpha = 0.7f)
        )
    }
}

/**
 * 占位符图表
 */
@Composable
private fun PlaceholderChart(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = BrandGradientBrush,
                alpha = 0.1f
            )
            .border(
                width = 1.dp,
                color = BrandBlue.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Construction,
            contentDescription = null,
            tint = BrandBlue.copy(alpha = 0.6f),
            modifier = Modifier.size(32.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = BrandBlue,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = BrandBlue.copy(alpha = 0.7f)
        )
    }
}

/**
 * 获取应用颜色
 */
private fun getAppColor(appName: String): Color {
    val colors = listOf(
        BrandBlue,
        BrandPurple,
        SuccessLight,
        WarningLight,
        ErrorLight
    )
    return colors[appName.hashCode() % colors.size]
}

/**
 * 类型转换函数：TimeFilter
 */
private fun convertToComponentTimeFilter(viewModelFilter: ViewModelTimeFilter): ComponentTimeFilter {
    return when (viewModelFilter) {
        ViewModelTimeFilter.TODAY -> ComponentTimeFilter.TODAY
        ViewModelTimeFilter.YESTERDAY -> ComponentTimeFilter.YESTERDAY
        ViewModelTimeFilter.THIS_WEEK -> ComponentTimeFilter.THIS_WEEK
        ViewModelTimeFilter.THIS_MONTH -> ComponentTimeFilter.THIS_MONTH
        ViewModelTimeFilter.CUSTOM -> ComponentTimeFilter.CUSTOM
    }
}

private fun convertToViewModelTimeFilter(componentFilter: ComponentTimeFilter): ViewModelTimeFilter {
    return when (componentFilter) {
        ComponentTimeFilter.TODAY -> ViewModelTimeFilter.TODAY
        ComponentTimeFilter.YESTERDAY -> ViewModelTimeFilter.YESTERDAY
        ComponentTimeFilter.THIS_WEEK -> ViewModelTimeFilter.THIS_WEEK
        ComponentTimeFilter.THIS_MONTH -> ViewModelTimeFilter.THIS_MONTH
        ComponentTimeFilter.CUSTOM -> ViewModelTimeFilter.CUSTOM
    }
}

/**
 * 类型转换函数：ChartType
 */
private fun convertToComponentChartType(chartType: ChartChartType): ComponentChartType {
    return when (chartType) {
        ChartChartType.BAR_CHART -> ComponentChartType.BAR_CHART
        ChartChartType.CALENDAR_HEATMAP, ChartChartType.HEATMAP -> ComponentChartType.CALENDAR_HEATMAP
        ChartChartType.TIMELINE -> ComponentChartType.TIMELINE
        ChartChartType.LINE_CHART, ChartChartType.PIE_CHART, ChartChartType.COMPARISON -> ComponentChartType.BAR_CHART  // 默认使用柱状图
    }
}

private fun convertToChartChartType(componentType: ComponentChartType): ChartChartType {
    return when (componentType) {
        ComponentChartType.BAR_CHART -> ChartChartType.BAR_CHART
        ComponentChartType.CALENDAR_HEATMAP -> ChartChartType.CALENDAR_HEATMAP
        ComponentChartType.TIMELINE -> ChartChartType.TIMELINE
    }
}

/**
 * 转换BarChartData到AppUsageData列表
 */
private fun convertToAppUsageDataList(barChartData: com.example.time.ui.charts.models.BarChartData): List<AppUsageData> {
    return barChartData.entries.map { entry ->
        AppUsageData(
            appName = entry.label,
            usageMinutes = entry.value.toLong(),
            category = "未分类"
        )
    }
}

/**
 * 转换HeatmapData到Map<LocalDate, Float>
 */
private fun convertToHeatmapData(heatmapData: com.example.time.ui.charts.models.HeatmapData): Map<LocalDate, Float> {
    return heatmapData.dateValues
}

/**
 * 转换AppSession列表到TimelineData列表
 */
private fun convertToTimelineDataList(sessions: List<com.example.time.ui.charts.models.AppSession>): List<TimelineData> {
    return sessions.map { session ->
        TimelineData(
            time = session.startTime.toString(),
            appName = session.appName,
            duration = Duration.ofMillis(session.duration)
        )
    }
}

// 数据类
private data class AppUsageData(
    val appName: String,
    val usageMinutes: Long,
    val category: String
)

private data class TimelineData(
    val time: String,
    val appName: String,
    val duration: Duration
)

/**
 * 格式化时长为可读字符串
 */
private fun formatDuration(millis: Long): String {
    val hours = millis / (1000 * 60 * 60)
    val minutes = (millis / (1000 * 60)) % 60
    return when {
        hours > 0 -> "${hours}小时${if (minutes > 0) "${minutes}分钟" else ""}"
        minutes > 0 -> "${minutes}分钟"
        else -> "少于1分钟"
    }
}

// 预览函数
@Preview(showBackground = true)
@Composable
private fun EnhancedStatisticsScreenPreview() {
    TimeTheme {
        EnhancedStatisticsScreen(
            navController = rememberNavController()
        )
    }
}