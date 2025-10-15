package com.example.time.ui.statistics

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.time.ui.charts.models.ChartType as ChartChartType
import com.example.time.ui.statistics.components.*
import com.example.time.ui.statistics.components.TimeFilter as ComponentTimeFilter
import com.example.time.ui.statistics.TimeFilter as ViewModelTimeFilter
import com.example.time.ui.theme.*
import com.example.time.ui.utils.formatDuration
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate

/**
 * 完整的统计页面
 * 集成所有图表组件和数据可视化功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreenComplete(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // 时间筛选器
        EnhancedTimeFilterSection(
            selectedFilter = convertToComponentTimeFilter(uiState.timeFilter),
            onFilterSelected = { componentFilter -> 
                viewModel.setTimeFilter(convertToViewModelTimeFilter(componentFilter))
            },
            customDateRange = uiState.customDateRange,
            onCustomDateRangeSelected = { startDate, endDate ->
                viewModel.setCustomDateRange(startDate, endDate)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 统计概览卡片
        EnhancedStatisticsOverviewCard(
            totalUsage = Duration.ofMillis(uiState.totalUsage),
            appCount = uiState.appCount,
            dailyAverage = Duration.ofMillis(uiState.dailyAverage),
            mostUsedApp = uiState.mostUsedApp ?: "无"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 图表类型选择器 - 使用简化版本
        Text(
            text = "图表类型: ${getChartTypeName(uiState.selectedChartType)}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 根据选择的图表类型显示对应内容 - 简化版本
        when (uiState.selectedChartType) {
            ChartChartType.BAR_CHART -> {
                // 应用使用排行
                Text(
                    text = "应用使用排行图表",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            ChartChartType.HEATMAP, ChartChartType.CALENDAR_HEATMAP -> {
                // 日历热力图
                Text(
                    text = "日历热力图",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            ChartChartType.TIMELINE -> {
                // 时间线
                Text(
                    text = "时间线图表",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            ChartChartType.LINE_CHART -> {
                // 折线图
                Text(
                    text = "趋势折线图",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            ChartChartType.COMPARISON -> {
                Text(
                    text = "对比图表功能开发中...",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            ChartChartType.PIE_CHART -> {
                Text(
                    text = "饼图功能开发中...",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * 获取图表类型名称
 */
private fun getChartTypeName(chartType: ChartChartType): String {
    return when (chartType) {
        ChartChartType.BAR_CHART -> "柱状图"
        ChartChartType.LINE_CHART -> "折线图"
        ChartChartType.PIE_CHART -> "饼图"
        ChartChartType.HEATMAP, ChartChartType.CALENDAR_HEATMAP -> "热力图"
        ChartChartType.TIMELINE -> "时间线"
        ChartChartType.COMPARISON -> "对比图"
    }
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