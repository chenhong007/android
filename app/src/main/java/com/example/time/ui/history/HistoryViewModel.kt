package com.example.time.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.time.data.repository.UsageRepository
import com.example.time.data.repository.ScreenEventRepository
import com.example.time.ui.charts.models.AppUsageData
import com.example.time.ui.charts.models.AppCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

/**
 * 历史数据 ViewModel
 * 管理历史数据加载、月份导航、日期选择、趋势计算等
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val usageRepository: UsageRepository,
    private val screenEventRepository: ScreenEventRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HistoryState())
    val uiState: StateFlow<HistoryState> = _uiState.asStateFlow()
    
    init {
        loadCurrentMonthData()
    }
    
    /**
     * 加载当前月份数据
     */
    private fun loadCurrentMonthData() {
        val currentMonth = YearMonth.now()
        _uiState.value = _uiState.value.copy(currentMonth = currentMonth)
        loadMonthData(currentMonth)
    }
    
    /**
     * 加载指定月份的数据
     */
    private fun loadMonthData(yearMonth: YearMonth) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val startDate = yearMonth.atDay(1)
                val endDate = yearMonth.atEndOfMonth()
                val startTimestamp = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endTimestamp = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                
                // 加载每日使用数据
                val monthlyData = mutableMapOf<LocalDate, Long>()
                
                // 遍历月份中的每一天
                var currentDate = startDate
                while (!currentDate.isAfter(endDate)) {
                    val dayStart = currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val dayEnd = currentDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    
                    // 获取当天的使用数据
                    val dayUsage = try {
                        val usageRecords = usageRepository.getUsageInRange(dayStart, dayEnd)
                        usageRecords.sumOf { it.duration }
                    } catch (e: Exception) {
                        0L
                    }
                    
                    monthlyData[currentDate] = dayUsage
                    currentDate = currentDate.plusDays(1)
                }
                
                // 加载趋势数据
                val trendData = calculateTrendData(yearMonth)
                
                // 加载对比数据
                val comparisonData = calculateComparisonData(yearMonth)
                
                // 加载统计摘要
                val summaryData = calculateSummaryData(yearMonth, monthlyData)
                
                _uiState.value = _uiState.value.copy(
                    monthlyData = monthlyData,
                    trendData = trendData,
                    comparisonData = comparisonData,
                    summaryData = summaryData,
                    isLoading = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "加载历史数据失败: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * 选择日期
     */
    fun selectDate(date: LocalDate) {
        viewModelScope.launch {
            try {
                val dayStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val dayEnd = date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                
                // 获取当天的详细使用数据
                val usageRecords = usageRepository.getUsageInRange(dayStart, dayEnd)
                val totalUsage = usageRecords.sumOf { it.duration }
                
                // 获取解锁次数
                val unlockCount = screenEventRepository.getUnlockCount(dayStart, dayEnd)
                
                // 按应用汇总使用数据
                val appUsageMap = mutableMapOf<String, AppUsageData>()
                usageRecords.forEach { record ->
                    val existing = appUsageMap[record.packageName]
                    if (existing != null) {
                        appUsageMap[record.packageName] = existing.copy(
                            totalUsageTime = existing.totalUsageTime + record.duration,
                            usageCount = existing.usageCount + 1
                        )
                    } else {
                        appUsageMap[record.packageName] = AppUsageData(
                            packageName = record.packageName,
                            appName = record.packageName, // 临时使用包名作为应用名
                            totalUsageTime = record.duration,
                            usageCount = 1,
                            category = AppCategory.OTHER // 默认分类
                        )
                    }
                }
                
                val topApps = appUsageMap.values
                    .sortedByDescending { it.totalUsageTime }
                    .take(10)
                
                val dailyData = DailyUsageData(
                    totalUsage = totalUsage,
                    unlockCount = unlockCount,
                    topApps = topApps
                )
                
                _uiState.value = _uiState.value.copy(
                    selectedDate = date,
                    selectedDateData = dailyData
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "加载日期详情失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 导航到上个月
     */
    fun navigateToPreviousMonth() {
        val previousMonth = _uiState.value.currentMonth.minusMonths(1)
        _uiState.value = _uiState.value.copy(
            currentMonth = previousMonth,
            selectedDate = null,
            selectedDateData = null
        )
        loadMonthData(previousMonth)
    }
    
    /**
     * 导航到下个月
     */
    fun navigateToNextMonth() {
        val nextMonth = _uiState.value.currentMonth.plusMonths(1)
        if (nextMonth <= YearMonth.now()) {
            _uiState.value = _uiState.value.copy(
                currentMonth = nextMonth,
                selectedDate = null,
                selectedDateData = null
            )
            loadMonthData(nextMonth)
        }
    }
    
    /**
     * 选择月份
     */
    fun selectMonth(yearMonth: YearMonth) {
        if (yearMonth != _uiState.value.currentMonth) {
            _uiState.value = _uiState.value.copy(
                currentMonth = yearMonth,
                selectedDate = null,
                selectedDateData = null
            )
            loadMonthData(yearMonth)
        }
    }
    
    /**
     * 计算趋势数据
     */
    private suspend fun calculateTrendData(yearMonth: YearMonth): TrendData {
        try {
            val currentMonthStart = yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val currentMonthEnd = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            val previousMonthStart = yearMonth.minusMonths(1).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val previousMonthEnd = yearMonth.minusMonths(1).atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            // 计算本月数据
            val currentMonthUsage = usageRepository.getUsageInRange(currentMonthStart, currentMonthEnd)
            val currentMonthTotal = currentMonthUsage.sumOf { it.duration }
            val currentMonthDays = yearMonth.lengthOfMonth()
            val currentMonthAverage = if (currentMonthDays > 0) currentMonthTotal / currentMonthDays else 0L
            
            // 计算上月数据
            val previousMonthUsage = usageRepository.getUsageInRange(previousMonthStart, previousMonthEnd)
            val previousMonthTotal = previousMonthUsage.sumOf { it.duration }
            val previousMonthDays = yearMonth.minusMonths(1).lengthOfMonth()
            val previousMonthAverage = if (previousMonthDays > 0) previousMonthTotal / previousMonthDays else 0L
            
            // 计算本周数据（最近7天）
            val now = System.currentTimeMillis()
            val weekAgo = now - (7 * 24 * 60 * 60 * 1000L)
            val weekUsage = usageRepository.getUsageInRange(weekAgo, now)
            val weekTotal = weekUsage.sumOf { it.duration }
            val weekAverage = weekTotal / 7
            
            // 计算上周数据
            val twoWeeksAgo = weekAgo - (7 * 24 * 60 * 60 * 1000L)
            val lastWeekUsage = usageRepository.getUsageInRange(twoWeeksAgo, weekAgo)
            val lastWeekTotal = lastWeekUsage.sumOf { it.duration }
            val lastWeekAverage = lastWeekTotal / 7
            
            // 计算变化百分比
            val weeklyChange = if (lastWeekAverage > 0) {
                ((weekAverage - lastWeekAverage).toFloat() / lastWeekAverage.toFloat()) * 100
            } else {
                0f
            }
            
            // 计算本周和上周的对比数据
            val thisWeekStart = now - (now % (7 * 24 * 60 * 60 * 1000L))
            val thisWeekEnd = thisWeekStart + (7 * 24 * 60 * 60 * 1000L)
            val thisWeekUsage = usageRepository.getUsageInRange(thisWeekStart, thisWeekEnd)
            val thisWeekTotal = thisWeekUsage.sumOf { it.duration }
            
            val lastWeekStart = thisWeekStart - (7 * 24 * 60 * 60 * 1000L)
            val lastWeekEnd = thisWeekStart
            val lastWeekUsageForComparison = usageRepository.getUsageInRange(lastWeekStart, lastWeekEnd)
            val lastWeekTotalForComparison = lastWeekUsageForComparison.sumOf { it.duration }
            
            val thisMonthStart = yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val thisMonthEnd = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val thisMonthUsage = usageRepository.getUsageInRange(thisMonthStart, thisMonthEnd)
            val thisMonthTotal = thisMonthUsage.sumOf { it.duration }
            
            val lastMonthStart = yearMonth.minusMonths(1).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val lastMonthEnd = yearMonth.minusMonths(1).atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val lastMonthUsage = usageRepository.getUsageInRange(lastMonthStart, lastMonthEnd)
            val lastMonthTotal = lastMonthUsage.sumOf { it.duration }
            
            // 计算月度变化百分比
            val monthlyChange = if (lastMonthTotal > 0) {
                ((thisMonthTotal - lastMonthTotal).toFloat() / lastMonthTotal.toFloat()) * 100
            } else {
                0f
            }
            
            return TrendData(
                weeklyAverage = weekAverage,
                monthlyAverage = currentMonthAverage,
                weeklyChange = weeklyChange,
                monthlyChange = monthlyChange
            )
            
        } catch (e: Exception) {
            return TrendData(0L, 0L, 0f, 0f)
        }
    }
    
    /**
     * 计算对比数据
     */
    private suspend fun calculateComparisonData(yearMonth: YearMonth): ComparisonData? {
        return try {
            val now = System.currentTimeMillis()
            
            // 本周数据
            val weekAgo = now - (7 * 24 * 60 * 60 * 1000L)
            val thisWeekUsage = usageRepository.getUsageInRange(weekAgo, now)
            val thisWeekTotal = thisWeekUsage.sumOf { it.duration }
            
            // 上周数据
            val twoWeeksAgo = weekAgo - (7 * 24 * 60 * 60 * 1000L)
            val lastWeekUsage = usageRepository.getUsageInRange(twoWeeksAgo, weekAgo)
            val lastWeekTotal = lastWeekUsage.sumOf { it.duration }
            
            // 本月数据
            val currentMonthStart = yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val currentMonthEnd = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val thisMonthUsage = usageRepository.getUsageInRange(currentMonthStart, currentMonthEnd)
            val thisMonthTotal = thisMonthUsage.sumOf { it.duration }
            
            // 上月数据
            val previousMonthStart = yearMonth.minusMonths(1).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val previousMonthEnd = yearMonth.minusMonths(1).atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val lastMonthUsage = usageRepository.getUsageInRange(previousMonthStart, previousMonthEnd)
            val lastMonthTotal = lastMonthUsage.sumOf { it.duration }
            
            // 计算变化百分比
            val weeklyChange = if (lastWeekTotal > 0) {
                ((thisWeekTotal - lastWeekTotal).toFloat() / lastWeekTotal.toFloat()) * 100
            } else {
                0f
            }
            
            val monthlyChange = if (lastMonthTotal > 0) {
                ((thisMonthTotal - lastMonthTotal).toFloat() / lastMonthTotal.toFloat()) * 100
            } else {
                0f
            }
            
            ComparisonData(
                thisWeek = thisWeekTotal,
                lastWeek = lastWeekTotal,
                thisMonth = thisMonthTotal,
                lastMonth = lastMonthTotal,
                weeklyChange = weeklyChange,
                monthlyChange = monthlyChange
            )
            
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 计算统计摘要
     */
    private suspend fun calculateSummaryData(
        yearMonth: YearMonth,
        monthlyData: Map<LocalDate, Long>
    ): SummaryData {
        return try {
            val totalDays = monthlyData.values.count { it > 0 }
            val totalUsage = monthlyData.values.sum()
            val dailyAverage = if (totalDays > 0) totalUsage / totalDays else 0L
            val maxDailyUsage = monthlyData.values.maxOrNull() ?: 0L
            
            // 获取最常用应用
            val monthStart = yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val monthEnd = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val allUsage = usageRepository.getUsageInRange(monthStart, monthEnd)
            
            val appUsageMap = mutableMapOf<String, Long>()
            allUsage.forEach { record ->
                appUsageMap[record.packageName] = (appUsageMap[record.packageName] ?: 0L) + record.duration
            }
            
            val mostUsedApp = appUsageMap.maxByOrNull { it.value }?.key ?: "无"
            
            SummaryData(
                totalDays = totalDays,
                dailyAverage = dailyAverage,
                maxDailyUsage = maxDailyUsage,
                mostUsedApp = mostUsedApp
            )
            
        } catch (e: Exception) {
            SummaryData(0, 0L, 0L, "无")
        }
    }
    
    /**
     * 切换对比模式
     */
    fun toggleComparison() {
        val currentState = _uiState.value
        if (currentState.comparisonData == null) {
            // 加载对比数据
            viewModelScope.launch {
                val comparisonData = calculateComparisonData(currentState.currentMonth)
                _uiState.value = currentState.copy(comparisonData = comparisonData)
            }
        } else {
            // 隐藏对比数据
            _uiState.value = currentState.copy(comparisonData = null)
        }
    }
    
    /**
     * 显示导出对话框
     */
    fun showExportDialog() {
        _uiState.value = _uiState.value.copy(showExportDialog = true)
    }
    
    /**
     * 隐藏导出对话框
     */
    fun hideExportDialog() {
        _uiState.value = _uiState.value.copy(showExportDialog = false)
    }
    
    /**
     * 导出数据
     */
    fun exportData(format: String) {
        viewModelScope.launch {
            try {
                // 这里实现实际的导出逻辑
                // 暂时只是隐藏对话框
                _uiState.value = _uiState.value.copy(
                    showExportDialog = false,
                    error = "导出功能将在后续版本中实现"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "导出失败: ${e.message}",
                    showExportDialog = false
                )
            }
        }
    }
    
    /**
     * 显示应用详情
     */
    fun showAppDetail(appUsage: AppUsageData) {
        _uiState.value = _uiState.value.copy(
            showAppDetailDialog = true,
            selectedAppUsage = appUsage
        )
    }
    
    /**
     * 隐藏应用详情
     */
    fun hideAppDetail() {
        _uiState.value = _uiState.value.copy(
            showAppDetailDialog = false,
            selectedAppUsage = null
        )
    }
    
    /**
     * 刷新数据
     */
    fun refreshData() {
        loadMonthData(_uiState.value.currentMonth)
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * 历史数据页面状态
 */
data class HistoryState(
    val currentMonth: YearMonth = YearMonth.now(),
    val monthlyData: Map<LocalDate, Long> = emptyMap(),
    val selectedDate: LocalDate? = null,
    val selectedDateData: DailyUsageData? = null,
    val trendData: TrendData? = null,
    val comparisonData: ComparisonData? = null,
    val summaryData: SummaryData? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showExportDialog: Boolean = false,
    val showAppDetailDialog: Boolean = false,
    val selectedAppUsage: AppUsageData? = null
)
