package com.example.time.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.time.data.repository.UsageRepository
import com.example.time.ui.charts.data.ChartDataProvider
import com.example.time.ui.charts.models.*
import com.example.time.ui.charts.utils.ChartCacheManager
import com.example.time.ui.charts.utils.ChartPerformanceOptimizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * 统计页面ViewModel
 * 负责管理统计数据的状态和逻辑
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val chartDataProvider: ChartDataProvider
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<StatisticsEvent>()
    val events: SharedFlow<StatisticsEvent> = _events.asSharedFlow()
    
    private val cacheManager = ChartCacheManager
    private val performanceOptimizer = ChartPerformanceOptimizer

    init {
        // 初始化时加载默认数据
        loadInitialData()
    }
    
    /**
     * 设置时间筛选器
     */
    fun setTimeFilter(filter: TimeFilter) {
        _uiState.update { it.copy(timeFilter = filter) }
        
        // 计算时间范围
        val dateRange = calculateDateRange(filter)
        _uiState.update { it.copy(currentDateRange = dateRange) }
        
        // 加载数据
        loadChartData()
    }
    
    /**
     * 设置自定义日期范围
     */
    fun setCustomDateRange(startDate: LocalDate, endDate: LocalDate) {
        _uiState.update { 
            it.copy(
                customDateRange = DateRange(
                    startTime = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    endTime = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    startDate = startDate,
                    endDate = endDate
                )
            )
        }
        
        // 如果是自定义筛选，更新当前日期范围
        if (_uiState.value.timeFilter == TimeFilter.CUSTOM) {
            _uiState.update { it.copy(currentDateRange = it.customDateRange ?: it.currentDateRange) }
            loadChartData()
        }
    }
    
    /**
     * 切换图表类型
     */
    fun setChartType(chartType: ChartType) {
        _uiState.update { it.copy(selectedChartType = chartType) }
        loadChartData()
    }
    
    /**
     * 选择日期（用于热力图和时间线）
     */
    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        
        // 如果是时间线视图，重新加载时间线数据
        if (_uiState.value.selectedChartType == ChartType.TIMELINE) {
            viewModelScope.launch {
                loadTimelineData(date)
            }
        }
    }
    
    /**
     * 图表条目点击事件
     */
    fun onChartEntryClicked(entry: ChartEntry) {
        viewModelScope.launch {
            _events.emit(StatisticsEvent.ChartEntryClicked(entry))
        }
    }
    
    /**
     * 应用点击事件处理
     */
    fun onAppClick(appUsageData: AppUsageData) {
        // TODO: 实现应用点击处理逻辑，比如导航到应用详情页面
    }
    
    /**
     * 会话点击事件处理
     */
    fun onSessionClick(session: AppSession) {
        // TODO: 实现会话点击处理逻辑，比如显示会话详情
    }
    
    /**
     * 数据点击事件处理
     */
    fun onDataPointClick(dataPoint: TimeSeriesData) {
        // TODO: 实现数据点击处理逻辑，比如显示详细信息
    }
    
    /**
     * 加载初始数据
     */
    private fun loadInitialData() {
        // 设置默认筛选器
        setTimeFilter(TimeFilter.TODAY)
    }
    
    /**
     * 加载图表数据
     */
    private fun loadChartData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val currentRange = _uiState.value.currentDateRange
                
                when (_uiState.value.selectedChartType) {
                    ChartType.BAR_CHART -> {
                        loadAppUsageRanking(currentRange)
                    }
                    ChartType.CALENDAR_HEATMAP, ChartType.HEATMAP -> {
                        loadCalendarHeatmap(currentRange)
                    }
                    ChartType.TIMELINE -> {
                        loadTimelineData(_uiState.value.selectedDate)
                    }
                    ChartType.LINE_CHART, ChartType.COMPARISON, ChartType.PIE_CHART -> {
                        // 暂不支持的图表类型
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
                
                // 加载统计概览数据
                loadStatisticsOverview(currentRange)
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "数据加载失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 加载应用使用排行
     */
    private suspend fun loadAppUsageRanking(dateRange: DateRange) {
        try {
            // 检查缓存
            val cacheKey = cacheManager.generateCacheKey(
                ChartType.BAR_CHART,
                dateRange
            )
            
            val cachedData: BarChartData? = cacheManager.getCachedChartData(cacheKey)
            if (cachedData != null) {
                _uiState.update { 
                    it.copy(
                        appUsageRanking = cachedData,
                        isLoading = false
                    )
                }
                return
            }
            
            // 加载新数据
            val result = chartDataProvider.getAppUsageRanking(
                startTime = dateRange.startTime,
                endTime = dateRange.endTime,
                limit = 10
            )
            
            if (result.success && result.data != null) {
                // 缓存数据
                cacheManager.cacheChartData(cacheKey, result.data!!)
                
                _uiState.update { 
                    it.copy(
                        appUsageRanking = result.data,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { 
                    it.copy(
                        error = result.message.ifEmpty { "数据加载失败" },
                        isLoading = false
                    )
                }
            }
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(
                    error = "加载应用排行数据失败: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * 加载日历热力图
     */
    private suspend fun loadCalendarHeatmap(dateRange: DateRange) {
        val result = chartDataProvider.getCalendarHeatmapData(
            startDate = dateRange.startDate,
            endDate = dateRange.endDate
        )
        
        if (result.success && result.data != null) {
            _uiState.update { 
                it.copy(
                    calendarHeatmap = result.data,
                    isLoading = false
                )
            }
        } else {
            _uiState.update { 
                it.copy(
                    error = result.message.ifEmpty { "热力图数据加载失败" },
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * 加载时间线数据
     */
    private suspend fun loadTimelineData(date: LocalDate) {
        val result = chartDataProvider.getTimelineSessions(date)
        
        if (result.success && result.data != null) {
            _uiState.update { 
                it.copy(
                    timelineData = result.data,
                    isLoading = false
                )
            }
        } else {
            _uiState.update { 
                it.copy(
                    error = result.message.ifEmpty { "时间线数据加载失败" },
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * 加载统计概览
     */
    private suspend fun loadStatisticsOverview(dateRange: DateRange) {
        try {
            // 获取应用使用排行数据来计算概览
            val rankingResult = chartDataProvider.getAppUsageRanking(
                startTime = dateRange.startTime,
                endTime = dateRange.endTime,
                limit = 100 // 获取所有应用
            )
            
            if (rankingResult.success && rankingResult.data != null) {
                val entries = rankingResult.data!!.entries
                
                // 计算总使用时长
                val totalMinutes = entries.sumOf { it.value.toLong() }
                val totalUsageMillis = totalMinutes * 60000L // 转换为毫秒 (分钟 * 60 * 1000)
                
                // 计算应用数量
                val appCount = entries.size
                
                // 计算日均使用（按天数平均）
                val days = ChronoUnit.DAYS.between(dateRange.startDate, dateRange.endDate) + 1
                val dailyAverageMillis = if (days > 0) {
                    (totalMinutes / days) * 60000L
                } else {
                    0L
                }
                
                // 获取最常用应用
                val mostUsedApp = entries.firstOrNull()?.label
                
                _uiState.update { currentState ->
                    currentState.copy(
                        totalUsage = totalUsageMillis,
                        appCount = appCount,
                        dailyAverage = dailyAverageMillis,
                        mostUsedApp = mostUsedApp
                    )
                }
            }
        } catch (e: Exception) {
            // 概览数据加载失败不影响主要功能
            e.printStackTrace()
        }
    }
    
    /**
     * 计算日期范围
     */
    private fun calculateDateRange(filter: TimeFilter): DateRange {
        val now = LocalDate.now()
        
        return when (filter) {
            TimeFilter.TODAY -> {
                val startTime = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endTime = now.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                DateRange(startTime, endTime, now, now)
            }
            
            TimeFilter.YESTERDAY -> {
                val yesterday = now.minusDays(1)
                val startTime = yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endTime = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                DateRange(startTime, endTime, yesterday, yesterday)
            }
            
            TimeFilter.THIS_WEEK -> {
                val weekStart = now.with(java.time.DayOfWeek.MONDAY)
                val weekEnd = now.with(java.time.DayOfWeek.SUNDAY)
                val startTime = weekStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endTime = weekEnd.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                DateRange(startTime, endTime, weekStart, weekEnd)
            }
            
            TimeFilter.THIS_MONTH -> {
                val monthStart = now.withDayOfMonth(1)
                val monthEnd = now.withDayOfMonth(now.lengthOfMonth())
                val startTime = monthStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endTime = monthEnd.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                DateRange(startTime, endTime, monthStart, monthEnd)
            }
            
            TimeFilter.CUSTOM -> {
                _uiState.value.customDateRange ?: calculateDateRange(TimeFilter.TODAY)
            }
        }
    }
    
    /**
     * 格式化时长
     */
    private fun formatDuration(minutes: Long): String {
        return when {
            minutes < 60 -> "${minutes}分钟"
            minutes < 1440 -> "${minutes / 60}小时${minutes % 60}分钟"
            else -> "${minutes / 1440}天${(minutes % 1440) / 60}小时"
        }
    }
}

/**
 * 统计UI状态
 */
data class StatisticsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val timeFilter: TimeFilter = TimeFilter.TODAY,
    val customDateRange: DateRange? = null,
    val currentDateRange: DateRange = DateRange(),
    val selectedChartType: ChartType = ChartType.BAR_CHART,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedRange: String = "今天",
    
    // 统计概览数据
    val totalUsage: Long = 0L, // 改为Long类型，单位毫秒
    val unlockCount: Int = 0, // 解锁次数
    val appCount: Int = 0,
    val dailyAverage: Long = 0L, // 改为Long类型，单位毫秒
    val mostUsedApp: String? = null,
    
    // 图表数据
    val appUsageRanking: BarChartData? = null,
    val calendarHeatmap: HeatmapData? = null,
    val timelineData: List<AppSession> = emptyList(),
    val weeklyTrend: BarChartData? = null // 周趋势数据
)

/**
 * 统计事件
 */
sealed class StatisticsEvent {
    data class ChartEntryClicked(val entry: ChartEntry) : StatisticsEvent()
    data class DateSelected(val date: LocalDate) : StatisticsEvent()
    data class TimeFilterChanged(val filter: TimeFilter) : StatisticsEvent()
}