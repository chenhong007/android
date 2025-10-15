package com.example.time.ui.charts.data

import com.example.time.data.model.UsageTracking
import com.example.time.data.model.AppUsageSummary
import com.example.time.data.model.DailyUsageSummary
import com.example.time.data.repository.UsageRepository
import com.example.time.ui.charts.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 图表数据提供者
 * 负责将数据库数据转换为图表可用的格式
 */
@Singleton
class ChartDataProvider @Inject constructor(
    private val usageRepository: UsageRepository
) {
    
    companion object {
        private const val DEFAULT_TOP_APPS_COUNT = 10
        private const val MAX_SESSIONS_PER_DAY = 100
        private const val SAMPLING_THRESHOLD = 1000 // 数据采样阈值
    }

    /**
     * 获取应用使用排行数据
     */
    suspend fun getAppUsageRanking(
        startTime: Long,
        endTime: Long,
        limit: Int = DEFAULT_TOP_APPS_COUNT,
        categoryFilter: AppCategory? = null
    ): ChartDataResult<BarChartData> = withContext(Dispatchers.IO) {
        try {
            // 获取应用使用汇总数据
            val summaries = usageRepository.getAppUsageSummaries(startTime, endTime)
            
            // 过滤分类
            val filteredSummaries = categoryFilter?.let { category ->
                summaries.filter { it.category == category.name }
            } ?: summaries
            
            // 按使用时长排序
            val topApps = filteredSummaries
                .sortedByDescending { it.totalDuration }
                .take(limit)
            
            // 转换为图表数据
            val chartData = convertToBarChartData(topApps, "应用使用排行")
            
            ChartDataResult(
                data = chartData,
                success = true,
                message = "数据获取成功"
            )
        } catch (e: Exception) {
            ChartDataResult(
                data = BarChartData(),
                success = false,
                message = "数据获取失败: ${e.message}"
            )
        }
    }

    /**
     * 获取时间序列数据
     */
    suspend fun getTimeSeriesData(
        startTime: Long,
        endTime: Long,
        granularity: TimeGranularity,
        appFilter: String? = null
    ): ChartDataResult<List<TimeSeriesData>> = withContext(Dispatchers.IO) {
        try {
            // 获取每日使用汇总数据
            val dailySummaries = usageRepository.getDailyUsageSummaries(startTime, endTime)
            
            // 应用过滤
            val filteredSummaries = appFilter?.let { packageName ->
                // DailyUsageSummary 不包含 packageName，按 topApps 中是否存在指定包名过滤
                dailySummaries.filter { summary ->
                    summary.topApps.any { it.packageName == packageName }
                }
            } ?: dailySummaries
            
            // 按时间粒度聚合数据
            val timeSeriesData = when (granularity) {
                TimeGranularity.MINUTE -> aggregateByHour(filteredSummaries, startTime, endTime) // 降级到小时
                TimeGranularity.HOUR -> aggregateByHour(filteredSummaries, startTime, endTime)
                TimeGranularity.DAY -> aggregateByDay(filteredSummaries, startTime, endTime)
                TimeGranularity.WEEK -> aggregateByWeek(filteredSummaries, startTime, endTime)
                TimeGranularity.MONTH -> aggregateByMonth(filteredSummaries, startTime, endTime)
            }
            
            ChartDataResult(
                data = timeSeriesData,
                success = true,
                message = "数据获取成功"
            )
        } catch (e: Exception) {
            ChartDataResult(
                data = emptyList(),
                success = false,
                message = "数据获取失败: ${e.message}"
            )
        }
    }

    /**
     * 获取日历热力图数据
     */
    suspend fun getCalendarHeatmapData(
        startDate: LocalDate,
        endDate: LocalDate,
        appFilter: String? = null
    ): ChartDataResult<HeatmapData> = withContext(Dispatchers.IO) {
        try {
            // 获取日期范围的时间戳
            val startTime = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endTime = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            // 获取每日使用汇总数据
            val dailySummaries = usageRepository.getDailyUsageSummaries(startTime, endTime)
            
            // 应用过滤
            val filteredSummaries = appFilter?.let { packageName ->
                dailySummaries.filter { summary -> summary.topApps.any { it.packageName == packageName } }
            } ?: dailySummaries
            
            // 转换为热力图数据
            val heatmapData = convertToHeatmapData(filteredSummaries, startDate, endDate)
            
            ChartDataResult(
                data = heatmapData,
                success = true,
                message = "数据获取成功"
            )
        } catch (e: Exception) {
            ChartDataResult(
                data = HeatmapData(),
                success = false,
                message = "数据获取失败: ${e.message}"
            )
        }
    }

    /**
     * 获取时间线会话数据
     */
    suspend fun getTimelineSessions(
        date: LocalDate,
        appFilter: String? = null
    ): ChartDataResult<List<AppSession>> = withContext(Dispatchers.IO) {
        try {
            // 获取日期范围的时间戳
            val startTime = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endTime = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            // 获取使用记录
            val usageRecords = usageRepository.getUsageInTimeRange(startTime, endTime)
            
            // 应用过滤
            val filteredRecords = appFilter?.let { packageName ->
                usageRecords.filter { it.packageName == packageName }
            } ?: usageRecords
            
            // 转换为会话数据
            val sessions = convertToSessions(filteredRecords)
            
            // 数据采样（如果数据量过大）
            val sampledSessions = if (sessions.size > MAX_SESSIONS_PER_DAY) {
                sampleSessions(sessions)
            } else {
                sessions
            }
            
            ChartDataResult(
                data = sampledSessions,
                success = true,
                message = "数据获取成功"
            )
        } catch (e: Exception) {
            ChartDataResult(
                data = emptyList(),
                success = false,
                message = "数据获取失败: ${e.message}"
            )
        }
    }

    /**
     * 获取对比分析数据
     */
    suspend fun getComparisonData(
        timeRange1: DateRange,
        timeRange2: DateRange,
        comparisonType: ComparisonType,
        appFilter: String? = null
    ): ChartDataResult<ComparisonData> = withContext(Dispatchers.IO) {
        try {
            // 获取两个时间范围的数据
            val data1 = when (comparisonType) {
                ComparisonType.TIME_PERIOD -> getTimeSeriesData(
                    timeRange1.startTime, timeRange1.endTime, TimeGranularity.DAY, appFilter
                ).data
                ComparisonType.APP_COMPARISON -> getAppUsageRanking(
                    timeRange1.startTime, timeRange1.endTime, 10, null
                ).data?.entries?.map { entry ->
                    TimeSeriesData(
                        timestamp = timeRange1.startTime,
                        value = entry.value.toFloat(),
                        label = entry.label
                    )
                } ?: emptyList()
            }
            
            val data2 = when (comparisonType) {
                ComparisonType.TIME_PERIOD -> getTimeSeriesData(
                    timeRange2.startTime, timeRange2.endTime, TimeGranularity.DAY, appFilter
                ).data
                ComparisonType.APP_COMPARISON -> getAppUsageRanking(
                    timeRange2.startTime, timeRange2.endTime, 10, null
                ).data?.entries?.map { entry ->
                    TimeSeriesData(
                        timestamp = timeRange2.startTime,
                        value = entry.value.toFloat(),
                        label = entry.label
                    )
                } ?: emptyList()
            }
            
            val comparisonData = ComparisonData(
                baselineData = data1 ?: emptyList(),
                comparisonData = data2 ?: emptyList(),
                differences = calculateDifferences(data1 ?: emptyList(), data2 ?: emptyList()),
                labels = generateComparisonLabels(timeRange1, timeRange2)
            )
            
            ChartDataResult(
                data = comparisonData,
                success = true,
                message = "数据获取成功"
            )
        } catch (e: Exception) {
            ChartDataResult(
                data = ComparisonData(
                    baselineData = emptyList(),
                    comparisonData = emptyList(),
                    differences = emptyList(),
                    labels = emptyList()
                ),
                success = false,
                message = "数据获取失败: ${e.message}"
            )
        }
    }

    /**
     * 转换为条形图数据
     * @param summaries AppUsageSummary列表，其中totalDuration单位为毫秒
     * @param title 图表标题
     * @return BarChartData，value单位为分钟
     */
    private fun convertToBarChartData(
        summaries: List<AppUsageSummary>,
        title: String
    ): BarChartData {
        val entries = summaries.map { summary ->
            ChartEntry(
                label = summary.appName,
                // 将毫秒转换为分钟：totalDuration(毫秒) / (1000 * 60)
                value = (summary.totalDuration / (1000f * 60f)),
                color = getAppColor(summary.packageName)
            )
        }
        
        return BarChartData(
            entries = entries,
            title = title,
            xAxisLabel = "应用",
            yAxisLabel = "使用时长（分钟）"
        )
    }

    /**
     * 转换为热力图数据
     */
    private fun convertToHeatmapData(
        summaries: List<DailyUsageSummary>,
        startDate: LocalDate,
        endDate: LocalDate
    ): HeatmapData {
        val dateValues = mutableMapOf<LocalDate, Float>()
        
        // 初始化所有日期
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            dateValues[currentDate] = 0f
            currentDate = currentDate.plusDays(1)
        }
        
        // 填充数据 - summary.date 是字符串格式 "YYYY-MM-DD"
        summaries.forEach { summary ->
            try {
                val date = LocalDate.parse(summary.date)
                if (dateValues.containsKey(date)) {
                    dateValues[date] = (dateValues[date] ?: 0f) + summary.totalDuration.toFloat()
                }
            } catch (e: Exception) {
                // 忽略解析错误的日期
            }
        }
        
        // 生成颜色比例尺
        val colorScale = generateColorScale()
        
        return HeatmapData(
            dateValues = dateValues,
            minValue = dateValues.values.minOrNull() ?: 0f,
            maxValue = dateValues.values.maxOrNull() ?: 0f,
            colorScale = colorScale,
            startDate = startDate,
            endDate = endDate
        )
    }
    
    /**
     * 生成颜色比例尺
     */
    private fun generateColorScale(): List<Int> {
        return listOf(
            android.graphics.Color.parseColor("#ebedf0"), // 最浅
            android.graphics.Color.parseColor("#9be9a8"),
            android.graphics.Color.parseColor("#40c463"),
            android.graphics.Color.parseColor("#30a14e"),
            android.graphics.Color.parseColor("#216e39")  // 最深
        )
    }

    /**
     * 转换为会话数据
     */
    private fun convertToSessions(usageRecords: List<UsageTracking>): List<AppSession> {
        val sessions = mutableListOf<AppSession>()
        
        // 按应用分组
        val recordsByApp = usageRecords.groupBy { it.packageName }
        
        recordsByApp.forEach { (packageName, records) ->
            // 按时间排序
            val sortedRecords = records.sortedBy { it.timestamp }
            
            var currentSession: AppSession? = null
            
            sortedRecords.forEach { record ->
                if (currentSession == null) {
                    // 开始新会话
                    currentSession = AppSession(
                        packageName = packageName,
                        appName = record.appName,
                        startTime = record.timestamp,
                        endTime = record.endTimestamp,
                        duration = record.duration,
                        color = getAppColor(packageName)
                    )
                } else {
                    // 检查是否继续当前会话（间隔小于5分钟）
                    val timeGap = record.timestamp - currentSession!!.endTime
                    if (timeGap <= 5 * 60 * 1000) { // 5分钟
                        // 延长当前会话
                        currentSession = currentSession!!.copy(
                            endTime = record.endTimestamp,
                            duration = currentSession!!.duration + record.duration + timeGap
                        )
                    } else {
                        // 保存当前会话并开始新会话
                        sessions.add(currentSession!!)
                        currentSession = AppSession(
                            packageName = packageName,
                            appName = record.appName,
                            startTime = record.timestamp,
                            endTime = record.endTimestamp,
                            duration = record.duration,
                            color = getAppColor(packageName)
                        )
                    }
                }
            }
            
            // 保存最后一个会话
            currentSession?.let { sessions.add(it) }
        }
        
        return sessions.sortedBy { it.startTime }
    }

    /**
     * 按小时聚合数据
     */
    private fun aggregateByHour(
        summaries: List<DailyUsageSummary>,
        startTime: Long,
        endTime: Long
    ): List<TimeSeriesData> {
        val hourlyData = mutableMapOf<Long, Float>()
        
        // 初始化所有小时
        var currentTime = startTime
        while (currentTime < endTime) {
            hourlyData[currentTime] = 0f
            currentTime += 60 * 60 * 1000 // 1小时
        }
        
        // 填充数据
        summaries.forEach { summary ->
            // DailyUsageSummary.date 是 YYYY-MM-DD 字符串，将其对应整天的开始作为小时聚合的基础
            val date = LocalDate.parse(summary.date)
            val dayStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            hourlyData[dayStart] = hourlyData.getOrDefault(dayStart, 0f) + summary.totalDuration
        }
        
        return hourlyData.map { (timestamp, value) ->
            TimeSeriesData(
                timestamp = timestamp,
                value = value,
                label = ""
            )
        }.sortedBy { it.timestamp }
    }

    /**
     * 按天聚合数据
     */
    private fun aggregateByDay(
        summaries: List<DailyUsageSummary>,
        startTime: Long,
        endTime: Long
    ): List<TimeSeriesData> {
        val dailyData = mutableMapOf<Long, Float>()
        
        // 初始化所有天
        var currentDate = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(startTime),
            ZoneId.systemDefault()
        ).toLocalDate()
        val endDate = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(endTime),
            ZoneId.systemDefault()
        ).toLocalDate()
        
        while (!currentDate.isAfter(endDate)) {
            val dayStart = currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            dailyData[dayStart] = 0f
            currentDate = currentDate.plusDays(1)
        }
        
        // 填充数据
        summaries.forEach { summary ->
            val date = LocalDate.parse(summary.date)
            val dayStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            dailyData[dayStart] = dailyData.getOrDefault(dayStart, 0f) + summary.totalDuration
        }
        
        return dailyData.map { (timestamp, value) ->
            TimeSeriesData(
                timestamp = timestamp,
                value = value,
                label = ""
            )
        }.sortedBy { it.timestamp }
    }

    /**
     * 按周聚合数据
     */
    private fun aggregateByWeek(
        summaries: List<DailyUsageSummary>,
        startTime: Long,
        endTime: Long
    ): List<TimeSeriesData> {
        val weeklyData = mutableMapOf<Long, Float>()
        
        summaries.forEach { summary ->
            val date = LocalDate.parse(summary.date)
            val weekStart = date.with(java.time.DayOfWeek.MONDAY)
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            weeklyData[weekStart] = weeklyData.getOrDefault(weekStart, 0f) + summary.totalDuration
        }
        
        return weeklyData.map { (timestamp, value) ->
            TimeSeriesData(
                timestamp = timestamp,
                value = value,
                label = ""
            )
        }.sortedBy { it.timestamp }
    }

    /**
     * 按月聚合数据
     */
    private fun aggregateByMonth(
        summaries: List<DailyUsageSummary>,
        startTime: Long,
        endTime: Long
    ): List<TimeSeriesData> {
        val monthlyData = mutableMapOf<Long, Float>()
        
        summaries.forEach { summary ->
            val date = LocalDate.parse(summary.date)
            val monthStart = date.withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            monthlyData[monthStart] = monthlyData.getOrDefault(monthStart, 0f) + summary.totalDuration
        }
        
        return monthlyData.map { (timestamp, value) ->
            TimeSeriesData(
                timestamp = timestamp,
                value = value,
                label = ""
            )
        }.sortedBy { it.timestamp }
    }

    /**
     * 数据采样（当数据量过大时）
     */
    private fun sampleSessions(sessions: List<AppSession>): List<AppSession> {
        if (sessions.size <= SAMPLING_THRESHOLD) return sessions
        
        // 简单的随机采样
        val step = sessions.size.toDouble() / SAMPLING_THRESHOLD
        val sampled = mutableListOf<AppSession>()
        
        for (i in 0 until SAMPLING_THRESHOLD) {
            val index = (i * step).toInt().coerceIn(0, sessions.size - 1)
            sampled.add(sessions[index])
        }
        
        return sampled
    }

    /**
     * 获取应用颜色（基于包名生成）
     */
    private fun getAppColor(packageName: String): Int {
        // 基于包名生成颜色（简单的哈希算法）
        val hash = packageName.hashCode()
        val hue = (hash and 0xFF).toFloat() // 色相
        val saturation = 0.6f + (hash shr 8 and 0x3F) / 256f * 0.4f // 饱和度
        val lightness = 0.5f + (hash shr 16 and 0x3F) / 256f * 0.3f // 亮度
        
        return android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, lightness))
    }
    
    /**
     * 计算两组数据的差异百分比
     */
    private fun calculateDifferences(data1: List<Any>, data2: List<Any>): List<Float> {
        // 简化实现，实际应根据数据类型计算差异
        return emptyList()
    }
    
    /**
     * 生成对比标签
     */
    private fun generateComparisonLabels(timeRange1: DateRange, timeRange2: DateRange): List<String> {
        return listOf("时间段1", "时间段2")
    }
}