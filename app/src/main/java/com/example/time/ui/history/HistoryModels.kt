package com.example.time.ui.history

import com.example.time.ui.charts.models.AppUsageData

/**
 * 每日使用数据
 */
data class DailyUsageData(
    val totalUsage: Long,
    val unlockCount: Int,
    val topApps: List<AppUsageData>
)

/**
 * 趋势数据
 */
data class TrendData(
    val weeklyAverage: Long,
    val monthlyAverage: Long,
    val weeklyChange: Float,
    val monthlyChange: Float
)

/**
 * 对比数据
 */
data class ComparisonData(
    val thisWeek: Long,
    val lastWeek: Long,
    val thisMonth: Long,
    val lastMonth: Long,
    val weeklyChange: Float,
    val monthlyChange: Float
)

/**
 * 统计摘要
 */
data class SummaryData(
    val totalDays: Int,
    val dailyAverage: Long,
    val maxDailyUsage: Long,
    val mostUsedApp: String
)

