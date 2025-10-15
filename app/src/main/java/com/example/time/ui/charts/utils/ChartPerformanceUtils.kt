package com.example.time.ui.charts.utils

import com.example.time.ui.charts.models.*
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.*

/**
 * 图表数据采样器
 * 用于优化大数据集的显示性能
 */
object ChartDataSampler {
    
    private val samplingCache = ConcurrentHashMap<String, List<*>>()
    
    /**
     * 对时间序列数据进行采样
     */
    fun sampleTimeSeriesData(
        data: List<TimeSeriesData>,
        maxPoints: Int = 100,
        timeGranularity: TimeGranularity = TimeGranularity.DAY
    ): List<TimeSeriesData> {
        if (data.size <= maxPoints) {
            return data
        }
        
        val cacheKey = "time_series_${data.hashCode()}_$maxPoints"
        return samplingCache.getOrPut(cacheKey) {
            when (timeGranularity) {
                TimeGranularity.MINUTE -> sampleByTimeWindow(data, maxPoints, 60 * 1000L) // 1分钟
                TimeGranularity.HOUR -> sampleByTimeWindow(data, maxPoints, 60 * 60 * 1000L) // 1小时
                TimeGranularity.DAY -> sampleByTimeWindow(data, maxPoints, 24 * 60 * 60 * 1000L) // 1天
                TimeGranularity.WEEK -> sampleByTimeWindow(data, maxPoints, 7 * 24 * 60 * 60 * 1000L) // 1周
                TimeGranularity.MONTH -> sampleByTimeWindow(data, maxPoints, 30 * 24 * 60 * 60 * 1000L) // 1月
            }
        } as List<TimeSeriesData>
    }
    
    /**
     * 对应用使用数据进行采样
     */
    fun sampleAppUsageData(
        data: List<AppUsageData>,
        maxApps: Int = 15,
        includeOthers: Boolean = true
    ): List<AppUsageData> {
        if (data.size <= maxApps) {
            return data
        }
        
        val cacheKey = "app_usage_${data.hashCode()}_$maxApps"
        return samplingCache.getOrPut(cacheKey) {
            val sortedData = data.sortedByDescending { it.totalUsageTime }
            val topApps = sortedData.take(maxApps - if (includeOthers) 1 else 0)
            
            if (includeOthers && sortedData.size > maxApps) {
                val othersDuration = sortedData.drop(maxApps - 1).sumOf { it.totalUsageTime }
                val othersCount = sortedData.drop(maxApps - 1).sumOf { it.usageCount }
                val othersApp = AppUsageData(
                    packageName = "others",
                    appName = "其他",
                    totalUsageTime = othersDuration,
                    usageCount = othersCount,
                    category = AppCategory.OTHER,
                    icon = null,
                    color = android.graphics.Color.GRAY
                )
                topApps + othersApp
            } else {
                topApps
            }
        } as List<AppUsageData>
    }
    
    /**
     * 对会话数据进行采样
     */
    fun sampleSessionData(
        sessions: List<AppSession>,
        maxSessions: Int = 100,
        timeWindowMs: Long = 24 * 60 * 60 * 1000L // 24小时
    ): List<AppSession> {
        if (sessions.size <= maxSessions) {
            return sessions
        }
        
        val cacheKey = "sessions_${sessions.hashCode()}_$maxSessions"
        return samplingCache.getOrPut(cacheKey) {
            val sortedSessions = sessions.sortedBy { it.startTime }
            val timeWindowCount = max(1, timeWindowMs / (60 * 1000L)) // 每分钟一个窗口
            val windowSize = max(1, sortedSessions.size / maxSessions)
            
            sortedSessions.filterIndexed { index, _ ->
                index % windowSize == 0
            }
        } as List<AppSession>
    }
    
    /**
     * 按时间窗口采样
     */
    private fun sampleByTimeWindow(
        data: List<TimeSeriesData>,
        maxPoints: Int,
        windowSize: Long
    ): List<TimeSeriesData> {
        val result = mutableListOf<TimeSeriesData>()
        var currentWindowStart = data.firstOrNull()?.timestamp ?: return emptyList()
        var currentWindowEnd = currentWindowStart + windowSize
        var windowSum = 0f
        var windowCount = 0
        
        data.forEach { point ->
            if (point.timestamp < currentWindowEnd) {
                windowSum += point.value
                windowCount++
            } else {
                if (windowCount > 0) {
                    result.add(
                        TimeSeriesData(
                            timestamp = currentWindowStart + windowSize / 2,
                            value = windowSum / windowCount.toFloat(),
                            label = "采样数据"
                        )
                    )
                }
                currentWindowStart = point.timestamp
                currentWindowEnd = currentWindowStart + windowSize
                windowSum = point.value
                windowCount = 1
            }
        }
        
        if (windowCount > 0) {
            result.add(
                TimeSeriesData(
                    timestamp = currentWindowStart + windowSize / 2,
                    value = windowSum / windowCount.toFloat(),
                    label = "采样数据"
                )
            )
        }
        
        return result
    }
    
    /**
     * 清理缓存
     */
    fun clearCache() {
        samplingCache.clear()
    }
}

/**
 * 图表性能优化器
 */
object ChartPerformanceOptimizer {
    // no changes needed here for enum names in this snippet
}

/**
 * 图表缓存管理器
 */
object ChartCacheManager {
    
    private val dataCache = ConcurrentHashMap<String, CachedChartData>()
    private const val DEFAULT_CACHE_SIZE = 50
    private const val DEFAULT_CACHE_DURATION = 5 * 60 * 1000L // 5分钟
    
    /**
     * 缓存图表数据
     */
    fun <T : Any> cacheChartData(
        key: String,
        data: T,
        duration: Long = DEFAULT_CACHE_DURATION
    ) {
        val cachedData = CachedChartData(
            data = data as Any,
            timestamp = System.currentTimeMillis(),
            duration = duration
        )
        dataCache[key] = cachedData
        
        // 清理过期缓存
        cleanupExpiredCache()
        
        // 限制缓存大小
        if (dataCache.size > DEFAULT_CACHE_SIZE) {
            removeOldestCache()
        }
    }
    
    /**
     * 获取缓存的图表数据
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getCachedChartData(key: String): T? {
        val cachedData = dataCache[key] ?: return null
        
        if (isCacheExpired(cachedData)) {
            dataCache.remove(key)
            return null
        }
        
        return cachedData.data as? T
    }
    
    /**
     * 检查缓存是否存在且有效
     */
    fun isCacheValid(key: String): Boolean {
        val cachedData = dataCache[key] ?: return false
        return !isCacheExpired(cachedData)
    }
    
    /**
     * 生成缓存键
     */
    fun generateCacheKey(
        chartType: ChartType,
        timeRange: DateRange,
        filters: FilterSet? = null
    ): String {
        return "${chartType}_${timeRange.startTime}_${timeRange.endTime}_${filters?.hashCode() ?: 0}"
    }
    
    /**
     * 清理过期缓存
     */
    private fun cleanupExpiredCache() {
        val currentTime = System.currentTimeMillis()
        dataCache.entries.removeIf { entry ->
            currentTime - entry.value.timestamp > entry.value.duration
        }
    }
    
    /**
     * 移除最旧的缓存
     */
    private fun removeOldestCache() {
        val oldestEntry = dataCache.entries.minByOrNull { it.value.timestamp }
        oldestEntry?.key?.let { dataCache.remove(it) }
    }
    
    /**
     * 检查缓存是否过期
     */
    private fun isCacheExpired(cachedData: CachedChartData): Boolean {
        return System.currentTimeMillis() - cachedData.timestamp > cachedData.duration
    }
    
    /**
     * 清空所有缓存
     */
    fun clearAllCache() {
        dataCache.clear()
    }
    
    /**
     * 获取缓存统计信息
     */
    fun getCacheStats(): CacheStats {
        return CacheStats(
            totalEntries = dataCache.size,
            totalMemory = estimateMemoryUsage(),
            oldestEntry = dataCache.values.minByOrNull { it.timestamp }?.timestamp ?: 0L,
            newestEntry = dataCache.values.maxByOrNull { it.timestamp }?.timestamp ?: 0L
        )
    }
    
    /**
     * 估算内存使用量（字节）
     */
    private fun estimateMemoryUsage(): Long {
        return dataCache.values.sumOf { cachedData ->
            when (val data = cachedData.data) {
                is List<*> -> data.size * 64L // 估算每个数据点64字节
                is Map<*, *> -> data.size * 128L // 估算每个键值对128字节
                else -> 256L // 默认估算
            }
        }
    }
}

/**
 * 缓存数据结构
 */
data class CachedChartData(
    val data: Any,
    val timestamp: Long,
    val duration: Long
)

/**
 * 缓存统计信息
 */
data class CacheStats(
    val totalEntries: Int,
    val totalMemory: Long, // 字节
    val oldestEntry: Long,
    val newestEntry: Long
)