package com.example.time.ui.charts.models

import android.graphics.Color
import android.graphics.drawable.Drawable
import java.time.LocalDate

/**
 * 图表数据模型定义
 * 根据技术架构文档定义核心数据模型
 */

// 应用使用数据 - 用于条形图
data class AppUsageData(
    val packageName: String,
    val appName: String,
    val totalUsageTime: Long, // 毫秒
    val usageCount: Int,
    val category: AppCategory,
    val icon: Drawable? = null,
    val color: Int = Color.BLUE
)

// 时间序列数据 - 用于趋势图
data class TimeSeriesData(
    val timestamp: Long,
    val value: Float,
    val label: String = ""
)

// 应用会话数据 - 用于时间线
data class AppSession(
    val packageName: String,
    val appName: String,
    val startTime: Long,
    val endTime: Long,
    val duration: Long,
    val color: Int = Color.BLUE
)

// 使用强度数据 - 用于热力图
data class UsageIntensity(
    val totalTime: Long,
    val appCount: Int,
    val unlockCount: Int,
    val intensityLevel: Float // 0.0 - 1.0
)

// 应用类别枚举
enum class AppCategory(val displayName: String) {
    SOCIAL("社交"),
    ENTERTAINMENT("娱乐"),
    PRODUCTIVITY("生产力"),
    COMMUNICATION("通讯"),
    GAMES("游戏"),
    NEWS("新闻"),
    SHOPPING("购物"),
    EDUCATION("教育"),
    HEALTH("健康"),
    TOOLS("工具"),
    OTHER("其他");

    companion object {
        fun fromString(category: String): AppCategory {
            return values().find { it.name.equals(category, ignoreCase = true) } ?: OTHER
        }
    }
}

// 图表类型枚举
enum class ChartType {
    BAR_CHART,      // 应用排行
    LINE_CHART,     // 趋势图
    HEATMAP,        // 日历热力图
    CALENDAR_HEATMAP, // 兼容别名
    TIMELINE,       // 时间线
    COMPARISON,     // 对比图
    PIE_CHART       // 饼图
}

// 时间范围
data class TimeRange(
    val startTime: Long,
    val endTime: Long
) {
    val duration: Long
        get() = endTime - startTime
}

// 日期范围
data class DateRange(
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now()
)

// 筛选条件集合
data class FilterSet(
    val categories: Set<AppCategory> = emptySet(),
    val searchQuery: String = "",
    val minDuration: Long = 0,
    val maxDuration: Long = Long.MAX_VALUE,
    val includeSystemApps: Boolean = false
)

// 对比类型
enum class ComparisonType {
    TIME_PERIOD,  // 时间段对比
    APP_COMPARISON // 应用对比
}

// 时间粒度
enum class TimeGranularity {
    MINUTE,
    HOUR,
    DAY,
    WEEK,
    MONTH
}

// 图表数据响应 (简化版本 - 用于兼容)
data class ChartDataResult<T>(
    val data: T? = null,
    val success: Boolean = false,
    val message: String = ""
)

// 通用图表数据
data class ChartData(
    val type: ChartType,
    val dataPoints: List<Any>,
    val labels: List<String> = emptyList(),
    val colors: List<Int> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

// 条形图数据
data class ChartEntry(
    val label: String,
    val value: Float,
    val color: Int = Color.BLUE
)
data class BarChartData(
    val entries: List<ChartEntry> = emptyList(),
    val title: String = "",
    val xAxisLabel: String = "",
    val yAxisLabel: String = ""
)

// 热力图数据
data class HeatmapData(
    val dateValues: Map<LocalDate, Float> = emptyMap(),
    val minValue: Float = 0f,
    val maxValue: Float = 0f,
    val colorScale: List<Int> = emptyList(),
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now()
)

// 时间线数据
data class TimelineData(
    val sessions: List<AppSession>,
    val startHour: Int = 0,
    val endHour: Int = 24,
    val pixelPerMinute: Float = 2f
)

// 对比数据
data class ComparisonData(
    val baselineData: List<Any>,
    val comparisonData: List<Any>,
    val differences: List<Float>, // 变化百分比
    val labels: List<String>
)

// 类型别名，用于向后兼容
// typealias ChartEntry = BarChartData.BarEntry

// 使用 ChartConfig.kt 中的 ColorScheme 和 ColorScale 枚举，移除重复定义