package com.example.time.ui.statistics

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.time.ui.charts.models.BarChartData
import com.example.time.ui.charts.models.DateRange
import com.example.time.ui.components.*
import com.example.time.ui.theme.*
import com.example.time.ui.utils.formatDuration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 统计分析页面
 * 严格按照 UI/统计页面.html 实现
 */
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = BackgroundGradientBrush)
            .padding(horizontal = Spacing.ExtraLarge),
        verticalArrangement = Arrangement.spacedBy(Spacing.ExtraLarge)
    ) {
        // 顶部间距
        item {
            Spacer(modifier = Modifier.height(Spacing.Large))
        }
        
        // 时间筛选区域
        item {
            TimeFilterCard(
                selectedFilter = uiState.timeFilter,
                onFilterSelected = { filter -> viewModel.setTimeFilter(filter) },
                customDateRange = uiState.customDateRange,
                onCustomDateRangeSelected = { startDate, endDate ->
                    viewModel.setCustomDateRange(startDate, endDate)
                }
            )
        }
        
        // 今日总览
        item {
            StatisticsOverviewCard(
                totalUsage = formatDuration(uiState.totalUsage),
                unlockCount = uiState.unlockCount,
                appCount = uiState.appCount
            )
        }
        
        // 应用使用排行
        item {
            AppUsageRankingCard(
                appUsageList = uiState.appUsageRanking?.entries?.mapIndexed { index, entry ->
                    AppUsageItem(
                        rank = index + 1,
                        appName = entry.label,
                        // entry.value 单位是分钟（已在ChartDataProvider中转换）
                        duration = formatDurationFromMinutes(entry.value.toLong()),
                        percentage = if (uiState.appUsageRanking?.entries?.sumOf { it.value.toDouble() } ?: 0.0 > 0) 
                            (entry.value / (uiState.appUsageRanking?.entries?.sumOf { it.value.toDouble() } ?: 1.0)).toFloat()
                        else 0f
                    )
                } ?: emptyList(),
                isLoading = uiState.isLoading
            )
        }
        
        // 本周使用趋势
        item {
            WeeklyTrendCard(
                weeklyData = uiState.weeklyTrend?.entries?.map { entry ->
                    val totalValue = uiState.weeklyTrend?.entries?.sumOf { it.value.toDouble() } ?: 1.0
                    DayUsageData(
                        dayName = entry.label,
                        usagePercentage = if (totalValue > 0) (entry.value / totalValue).toFloat() else 0f
                    )
                } ?: emptyList(),
                averageUsage = formatDuration(uiState.dailyAverage)
            )
        }
        
        // 底部间距
        item {
            Spacer(modifier = Modifier.height(ComponentSize.BottomNavHeight + Spacing.ExtraLarge))
        }
    }
}

/**
 * 时间筛选卡片
 */
@Composable
private fun TimeFilterCard(
    selectedFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit,
    customDateRange: DateRange?,
    onCustomDateRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    var showCustomDateDialog by remember { mutableStateOf(false) }
    
    GlassmorphismCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = CornerRadius.Large
    ) {
        Column(
            modifier = Modifier.padding(Spacing.ExtraLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "使用统计",
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "日历",
                    tint = TextTertiary,
                    modifier = Modifier.size(IconSize.Large)
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.Large))
            
            // 时间范围按钮
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterButton(
                        text = "今天",
                        selected = selectedFilter == TimeFilter.TODAY,
                        onClick = { onFilterSelected(TimeFilter.TODAY) }
                    )
                }
                item {
                    FilterButton(
                        text = "昨天",
                        selected = selectedFilter == TimeFilter.YESTERDAY,
                        onClick = { onFilterSelected(TimeFilter.YESTERDAY) }
                    )
                }
                item {
                    FilterButton(
                        text = "本周",
                        selected = selectedFilter == TimeFilter.THIS_WEEK,
                        onClick = { onFilterSelected(TimeFilter.THIS_WEEK) }
                    )
                }
                item {
                    FilterButton(
                        text = "本月",
                        selected = selectedFilter == TimeFilter.THIS_MONTH,
                        onClick = { onFilterSelected(TimeFilter.THIS_MONTH) }
                    )
                }
                item {
                    FilterButton(
                        text = "自定义",
                        selected = selectedFilter == TimeFilter.CUSTOM,
                        onClick = { showCustomDateDialog = true }
                    )
                }
            }
            
            // 当前选择的时间范围显示
            Spacer(modifier = Modifier.height(Spacing.Large))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(CornerRadius.Medium),
                color = BackgroundLightSecondary
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.Medium),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(IconSize.Small)
                        )
                        Spacer(modifier = Modifier.width(Spacing.Small))
                        Text(
                            text = getTimeRangeText(selectedFilter, customDateRange),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    
                    Text(
                        text = getDataCountText(selectedFilter, customDateRange),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
        }
    }
    
    if (showCustomDateDialog) {
        // TODO: 实现自定义日期选择对话框
        showCustomDateDialog = false
    }
}

/**
 * 统计概览卡片
 */
@Composable
private fun StatisticsOverviewCard(
    totalUsage: String,
    unlockCount: Int,
    appCount: Int
) {
    GlassmorphismCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = CornerRadius.Large
    ) {
        Column(
            modifier = Modifier.padding(Spacing.ExtraLarge)
        ) {
            Text(
                text = "今日总览",
                style = MaterialTheme.typography.displaySmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(Spacing.ExtraLarge))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 使用时长
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                brush = BrandGradientBrush,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // 这里可以添加进度环组件
                    }
                    Spacer(modifier = Modifier.height(Spacing.Medium))
                    Text(
                        text = totalUsage,
                        style = MaterialTheme.typography.displayMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "使用时长",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                
                // 解锁次数
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                brush = BluePurpleGradientBrush,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(IconSize.Large)
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.Medium))
                    Text(
                        text = unlockCount.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "解锁次数",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                
                // 应用数量
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                brush = GreenTealGradientBrush,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(IconSize.Large)
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.Medium))
                    Text(
                        text = appCount.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "应用数量",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * 应用使用排行卡片
 */
@Composable
private fun AppUsageRankingCard(
    appUsageList: List<AppUsageItem>,
    isLoading: Boolean
) {
    GlassmorphismCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = CornerRadius.Medium
    ) {
        Column(
            modifier = Modifier.padding(Spacing.Large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "应用使用排行",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
                ) {
                    IconButton(
                        onClick = { /* 切换到柱状图 */ },
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = BackgroundLightSecondary,
                                shape = RoundedCornerShape(CornerRadius.Small)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "柱状图",
                            tint = TextSecondary,
                            modifier = Modifier.size(IconSize.Small)
                        )
                    }
                    
                    IconButton(
                        onClick = { /* 切换到饼图 */ },
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = BackgroundLightSecondary,
                                shape = RoundedCornerShape(CornerRadius.Small)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = "饼图",
                            tint = TextSecondary,
                            modifier = Modifier.size(IconSize.Small)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.Large))
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandBlue)
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
                ) {
                    appUsageList.take(5).forEach { app ->
                        AppUsageRow(app)
                    }
                }
            }
        }
    }
}

/**
 * 应用使用行项
 */
@Composable
private fun AppUsageRow(app: AppUsageItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.Medium),
        color = when {
            app.rank == 1 -> Color(0xFFFEF2F2)
            app.rank == 2 -> Color(0xFFF0FDF4)
            app.rank == 3 -> Color(0xFFEFF6FF)
            else -> Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier.padding(Spacing.Large),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 应用图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = when {
                            app.rank == 1 -> YouTubeGradientBrush
                            app.rank == 2 -> WeChatGradientBrush
                            app.rank == 3 -> ChromeGradientBrush
                            else -> BrandGradientBrush
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        app.rank == 1 -> Icons.Default.PlayArrow
                        app.rank == 2 -> Icons.Default.Message
                        app.rank == 3 -> Icons.Default.Language
                        else -> Icons.Default.Apps
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(IconSize.Large)
                )
            }
            
            Spacer(modifier = Modifier.width(Spacing.Large))
            
            // 应用信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                
                // 进度条
                Spacer(modifier = Modifier.height(Spacing.Small))
                LinearProgressIndicator(
                    progress = app.percentage / 100f,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(ComponentSize.ProgressBarHeight)
                        .clip(RoundedCornerShape(CornerRadius.Small)),
                    color = when {
                        app.rank == 1 -> YouTubeRed
                        app.rank == 2 -> WeChatGreen
                        app.rank == 3 -> ChromeBlue
                        else -> BrandBlue
                    },
                    trackColor = NeutralGray.copy(alpha = 0.2f)
                )
            }
            
            // 使用时长和百分比
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = app.duration,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${app.percentage.toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        app.rank == 1 -> YouTubeRed
                        app.rank == 2 -> WeChatGreen
                        app.rank == 3 -> ChromeBlue
                        else -> BrandBlue
                    },
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 本周使用趋势卡片
 */
@Composable
private fun WeeklyTrendCard(
    weeklyData: List<DayUsageData>,
    averageUsage: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.Large),
        color = Color.White,
        shadowElevation = Elevation.Small
    ) {
        Column(
            modifier = Modifier.padding(Spacing.ExtraLarge)
        ) {
            Text(
                text = "本周使用趋势",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(Spacing.Large))
            
            // 简单的柱状图
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEach { day ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .fillMaxHeight(day.usagePercentage / 100f)
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(
                                            BrandBlue.copy(alpha = 0.6f),
                                            BrandPurple.copy(alpha = 0.8f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(Spacing.Small))
                        Text(
                            text = day.dayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.Large))
            
            Text(
                text = "平均每日使用时长：$averageUsage",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

// Helper functions

/**
 * 获取时间范围文本（使用真实的系统日期）
 */
private fun getTimeRangeText(filter: TimeFilter, customRange: DateRange?): String {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
    
    return when (filter) {
        TimeFilter.TODAY -> "${today.format(formatter)} (今天)"
        TimeFilter.YESTERDAY -> {
            val yesterday = today.minusDays(1)
            "${yesterday.format(formatter)} (昨天)"
        }
        TimeFilter.THIS_WEEK -> {
            val weekStart = today.with(java.time.DayOfWeek.MONDAY)
            val weekEnd = today.with(java.time.DayOfWeek.SUNDAY)
            "${weekStart.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))} - ${weekEnd.format(DateTimeFormatter.ofPattern("MM月dd日"))} (本周)"
        }
        TimeFilter.THIS_MONTH -> {
            val monthStart = today.withDayOfMonth(1)
            val monthEnd = today.withDayOfMonth(today.lengthOfMonth())
            "${monthStart.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))} - ${monthEnd.format(DateTimeFormatter.ofPattern("MM月dd日"))} (本月)"
        }
        TimeFilter.CUSTOM -> customRange?.let {
            "${it.startDate.format(formatter)} - " +
            "${it.endDate.format(formatter)}"
        } ?: "自定义"
    }
}

/**
 * 获取数据统计天数文本（使用真实的月份天数）
 */
private fun getDataCountText(filter: TimeFilter, customRange: DateRange? = null): String {
    return when (filter) {
        TimeFilter.TODAY, TimeFilter.YESTERDAY -> "共1天数据"
        TimeFilter.THIS_WEEK -> "共7天数据"
        TimeFilter.THIS_MONTH -> {
            val today = LocalDate.now()
            val daysInMonth = today.lengthOfMonth()
            "共${daysInMonth}天数据"
        }
        TimeFilter.CUSTOM -> customRange?.let {
            val days = ChronoUnit.DAYS.between(it.startDate, it.endDate) + 1
            "共${days}天数据"
        } ?: "自定义范围"
    }
}

/**
 * 从分钟数格式化为可读时长字符串
 * @param minutes 分钟数
 * @return 格式化后的字符串，如"3小时20分钟"或"45分钟"
 */
private fun formatDurationFromMinutes(minutes: Long): String {
    return when {
        minutes < 1 -> "少于1分钟"
        minutes < 60 -> "${minutes}分钟"
        minutes < 1440 -> { // 少于24小时
            val hours = minutes / 60
            val mins = minutes % 60
            if (mins > 0) "${hours}小时${mins}分钟" else "${hours}小时"
        }
        else -> { // 超过24小时
            val days = minutes / 1440
            val hours = (minutes % 1440) / 60
            if (hours > 0) "${days}天${hours}小时" else "${days}天"
        }
    }
}

// Data classes

data class AppUsageItem(
    val rank: Int,
    val appName: String,
    val duration: String,
    val percentage: Float
)

data class DayUsageData(
    val dayName: String,
    val usagePercentage: Float
)

enum class TimeFilter {
    TODAY,
    YESTERDAY,
    THIS_WEEK,
    THIS_MONTH,
    CUSTOM
}

// DateRange 在 com.example.time.ui.charts.models.DateRange 中已定义
