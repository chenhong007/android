package com.example.time.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.time.ui.components.*
import com.example.time.ui.theme.*
import com.example.time.ui.utils.formatDuration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 历史数据页面
 * 严格按照 UI/历史数据页面.html 实现
 */
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = BackgroundGradientBrush)
            .padding(horizontal = Spacing.ExtraLarge),
        verticalArrangement = Arrangement.spacedBy(Spacing.Large)
    ) {
        // 顶部间距
        item {
            Spacer(modifier = Modifier.height(Spacing.Large))
        }
        
        // 头部标题
        item {
            Column {
                Text(
                    text = "历史数据",
                    style = MaterialTheme.typography.displayLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(Spacing.Small))
                Text(
                    text = "查看您的使用历史和趋势",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextSecondary
                )
            }
        }
        
        // 时间范围选择
        item {
            TimeRangeCard(
                currentMonth = uiState.currentMonth.toString(),
                selectedPeriod = "",
                onPeriodSelected = { },
                onPreviousMonth = { viewModel.navigateToPreviousMonth() },
                onNextMonth = { viewModel.navigateToNextMonth() }
            )
        }
        
        // 日历热力图
        item {
            CalendarHeatmapCard(
                monthData = uiState.monthlyData,
                selectedDate = uiState.selectedDate,
                onDateSelected = { date -> viewModel.selectDate(date) }
            )
        }
        
        // 使用趋势
        item {
            val trendData = uiState.trendData
            if (trendData != null) {
                UsageTrendCard(
                    weeklyAverage = formatDuration(trendData.weeklyAverage),
                    monthlyAverage = formatDuration(trendData.monthlyAverage),
                    weeklyChange = trendData.weeklyChange,
                    monthlyChange = trendData.monthlyChange,
                    activeDays = uiState.summaryData?.totalDays ?: 0
                )
            }
        }
        
        // 时段对比
        item {
            val comparisonData = uiState.comparisonData
            if (comparisonData != null) {
                val periodComparison = PeriodComparison(
                    title = "使用时长对比",
                    currentLabel = "本周",
                    currentValue = formatDuration(comparisonData.thisWeek),
                    currentProgress = 1.0f,
                    previousLabel = "上周",
                    previousValue = formatDuration(comparisonData.lastWeek),
                    previousProgress = if (comparisonData.thisWeek > 0) comparisonData.lastWeek.toFloat() / comparisonData.thisWeek.toFloat() else 0f,
                    changePercent = comparisonData.weeklyChange
                )
                PeriodComparisonCard(
                    comparisons = listOf(periodComparison)
                )
            }
        }
        
        // 底部间距
        item {
            Spacer(modifier = Modifier.height(ComponentSize.BottomNavHeight))
        }
    }
}

/**
 * 时间范围卡片
 */
@Composable
private fun TimeRangeCard(
    currentMonth: String,
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
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
                    text = "时间范围",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
                ) {
                    IconButton(
                        onClick = onPreviousMonth,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = BackgroundLightSecondary,
                                shape = RoundedCornerShape(CornerRadius.Small)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "上一月",
                            tint = TextSecondary,
                            modifier = Modifier.size(IconSize.Small)
                        )
                    }
                    
                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = BackgroundLightSecondary,
                                shape = RoundedCornerShape(CornerRadius.Small)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "下一月",
                            tint = TextSecondary,
                            modifier = Modifier.size(IconSize.Small)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.Large))
            
            // 时间段按钮
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                item {
                    FilterButton(
                        text = "本周",
                        selected = selectedPeriod == "本周",
                        onClick = { onPeriodSelected("本周") }
                    )
                }
                item {
                    FilterButton(
                        text = "本月",
                        selected = selectedPeriod == "本月",
                        onClick = { onPeriodSelected("本月") }
                    )
                }
                item {
                    FilterButton(
                        text = "上月",
                        selected = selectedPeriod == "上月",
                        onClick = { onPeriodSelected("上月") }
                    )
                }
                item {
                    FilterButton(
                        text = "近3月",
                        selected = selectedPeriod == "近3月",
                        onClick = { onPeriodSelected("近3月") }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.Large))
            
            // 当前月份显示
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentMonth,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = "共31天数据",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }
    }
}

/**
 * 日历热力图卡片
 */
@Composable
private fun CalendarHeatmapCard(
    monthData: Map<LocalDate, Long>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
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
                    text = "使用热力图",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                
                // 图例
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = SuccessGreen, label = "少")
                    LegendItem(color = WarningOrange, label = "中")
                    LegendItem(color = ErrorRed, label = "多")
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.Large))
            
            // 星期标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.Small))
            
            // 日历网格（简化版本）
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                horizontalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall),
                verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall),
                modifier = Modifier.height(240.dp)
            ) {
                items(31) { day ->
                    CalendarDayItem(
                        day = day + 1,
                        usage = monthData[LocalDate.now().withDayOfMonth(day + 1)] ?: 0L,
                        isSelected = selectedDate?.dayOfMonth == day + 1,
                        isToday = LocalDate.now().dayOfMonth == day + 1,
                        onClick = { /* onDateSelected */ }
                    )
                }
            }
        }
    }
}

/**
 * 日历日期项
 */
@Composable
private fun CalendarDayItem(
    day: Int,
    usage: Long,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isToday -> BrandBlue
        usage == 0L -> BackgroundLightSecondary
        usage < 3_600_000 -> SuccessGreen.copy(alpha = 0.2f)
        usage < 7_200_000 -> Color(0xFFFFEE58).copy(alpha = 0.3f)
        else -> ErrorRed.copy(alpha = 0.2f)
    }
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(CornerRadius.Small))
            .background(backgroundColor)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = BrandBlue,
                        shape = RoundedCornerShape(CornerRadius.Small)
                    )
                } else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = if (isToday) Color.White else TextPrimary,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * 图例项
 */
@Composable
private fun RowScope.LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, shape = RoundedCornerShape(CornerRadius.Small))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

/**
 * 使用趋势卡片
 */
@Composable
private fun UsageTrendCard(
    weeklyAverage: String,
    monthlyAverage: String,
    weeklyChange: Float,
    monthlyChange: Float,
    activeDays: Int
) {
    GlassmorphismCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = CornerRadius.Medium
    ) {
        Column(
            modifier = Modifier.padding(Spacing.Large)
        ) {
            Text(
                text = "使用趋势",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(Spacing.Large))
            
            // 简单的趋势线图（用柱状图代替）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                BrandBlue.copy(alpha = 0.1f),
                                BrandPurple.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(CornerRadius.Medium)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Spacing.Small, vertical = Spacing.Small),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    repeat(7) { index ->
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight((40 + index * 10) / 100f)
                                .background(
                                    color = BrandBlue.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                                )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.Large))
            
            // 统计数据
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = weeklyAverage,
                    label = "平均时长"
                )
                StatItem(
                    value = "${if (weeklyChange > 0) "+" else ""}$weeklyChange%",
                    label = "环比变化",
                    valueColor = if (weeklyChange < 0) SuccessGreen else ErrorRed
                )
                StatItem(
                    value = activeDays.toString(),
                    label = "活跃天数"
                )
            }
        }
    }
}

/**
 * 统计项
 */
@Composable
private fun StatItem(
    value: String,
    label: String,
    valueColor: Color = TextPrimary
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

/**
 * 时段对比卡片
 */
@Composable
private fun PeriodComparisonCard(
    comparisons: List<PeriodComparison>
) {
    GlassmorphismCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = CornerRadius.Medium
    ) {
        Column(
            modifier = Modifier.padding(Spacing.Large)
        ) {
            Text(
                text = "时段对比",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(Spacing.Large))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                comparisons.forEach { comparison ->
                    ComparisonItem(comparison = comparison)
                }
            }
        }
    }
}

/**
 * 对比项
 */
@Composable
private fun ComparisonItem(
    comparison: PeriodComparison
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.Medium),
        color = Color.White.copy(alpha = 0.8f)
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
                    text = comparison.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "${if (comparison.changePercent > 0) "↑" else "↓"} ${kotlin.math.abs(comparison.changePercent)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (comparison.changePercent < 0) SuccessGreen else ErrorRed,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.Medium))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Large)
            ) {
                // 当前时段
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = comparison.currentLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = comparison.currentValue,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.ExtraSmall))
                    LinearProgressIndicator(
                        progress = comparison.currentProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ComponentSize.ProgressBarHeight)
                            .clip(RoundedCornerShape(CornerRadius.Small)),
                        color = ChromeBlue,
                        trackColor = NeutralGray.copy(alpha = 0.2f)
                    )
                }
                
                // 对比时段
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = comparison.previousLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = comparison.previousValue,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.ExtraSmall))
                    LinearProgressIndicator(
                        progress = comparison.previousProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ComponentSize.ProgressBarHeight)
                            .clip(RoundedCornerShape(CornerRadius.Small)),
                        color = NeutralGray,
                        trackColor = NeutralGray.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

// Data classes

data class PeriodComparison(
    val title: String,
    val currentLabel: String,
    val currentValue: String,
    val currentProgress: Float,
    val previousLabel: String,
    val previousValue: String,
    val previousProgress: Float,
    val changePercent: Float
)
