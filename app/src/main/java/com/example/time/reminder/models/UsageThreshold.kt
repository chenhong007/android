package com.example.time.reminder.models

import kotlinx.serialization.Serializable

/**
 * UsageThreshold - 使用阈值模型
 * 
 * 定义阈值配置数据结构，支持全局和每应用阈值
 * 包含提醒级别（警告、严重）和通知设置
 */
@Serializable
data class UsageThreshold(
    val id: String,
    val packageName: String? = null, // null 表示全局阈值
    val appName: String? = null,
    val dailyLimitMinutes: Int, // 每日限制（分钟）
    val warningPercentages: List<Int> = listOf(80, 90, 100), // 提醒阈值百分比
    val isEnabled: Boolean = true,
    val notificationSound: Boolean = true,
    val vibration: Boolean = true,
    val priority: ThresholdPriority = ThresholdPriority.NORMAL,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 是否为全局阈值
     */
    val isGlobal: Boolean
        get() = packageName == null
    
    /**
     * 获取限制时长（毫秒）
     */
    val dailyLimitMs: Long
        get() = dailyLimitMinutes * 60 * 1000L
    
    /**
     * 检查当前使用量是否触发阈值
     */
    fun checkThreshold(currentUsageMs: Long): ThresholdResult? {
        if (!isEnabled || dailyLimitMs <= 0) return null
        
        val currentPercentage = (currentUsageMs * 100.0 / dailyLimitMs).toInt()
        val triggeredPercentage = warningPercentages
            .filter { it <= currentPercentage }
            .maxOrNull()
        
        return if (triggeredPercentage != null) {
            ThresholdResult(
                threshold = this,
                currentUsageMs = currentUsageMs,
                triggeredPercentage = triggeredPercentage,
                severity = when (triggeredPercentage) {
                    in 0..79 -> ThresholdSeverity.INFO
                    in 80..89 -> ThresholdSeverity.WARNING
                    in 90..99 -> ThresholdSeverity.CRITICAL
                    else -> ThresholdSeverity.EXCEEDED
                }
            )
        } else {
            null
        }
    }
    
    /**
     * 获取下一个阈值百分比
     */
    fun getNextThreshold(currentPercentage: Int): Int? {
        return warningPercentages
            .filter { it > currentPercentage }
            .minOrNull()
    }
    
    /**
     * 获取剩余时间（毫秒）
     */
    fun getRemainingTime(currentUsageMs: Long): Long {
        return maxOf(0L, dailyLimitMs - currentUsageMs)
    }
    
    /**
     * 获取使用百分比
     */
    fun getUsagePercentage(currentUsageMs: Long): Double {
        return if (dailyLimitMs > 0) {
            (currentUsageMs * 100.0 / dailyLimitMs).coerceAtMost(100.0)
        } else {
            0.0
        }
    }
}

/**
 * 阈值优先级
 */
@Serializable
enum class ThresholdPriority(val displayName: String, val value: Int) {
    LOW("低", 1),
    NORMAL("普通", 2),
    HIGH("高", 3),
    CRITICAL("紧急", 4)
}

/**
 * 阈值严重程度
 */
enum class ThresholdSeverity(val displayName: String, val color: String) {
    INFO("信息", "#2196F3"),
    WARNING("警告", "#FF9800"),
    CRITICAL("严重", "#F44336"),
    EXCEEDED("超限", "#9C27B0")
}

/**
 * 阈值检查结果
 */
data class ThresholdResult(
    val threshold: UsageThreshold,
    val currentUsageMs: Long,
    val triggeredPercentage: Int,
    val severity: ThresholdSeverity,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * 获取使用百分比
     */
    val usagePercentage: Double
        get() = threshold.getUsagePercentage(currentUsageMs)
    
    /**
     * 获取剩余时间
     */
    val remainingTimeMs: Long
        get() = threshold.getRemainingTime(currentUsageMs)
    
    /**
     * 是否已超限
     */
    val isExceeded: Boolean
        get() = currentUsageMs >= threshold.dailyLimitMs
    
    /**
     * 获取超限时间
     */
    val exceededTimeMs: Long
        get() = maxOf(0L, currentUsageMs - threshold.dailyLimitMs)
    
    /**
     * 获取格式化的使用时间
     */
    fun getFormattedUsageTime(): String {
        return formatDuration(currentUsageMs)
    }
    
    /**
     * 获取格式化的剩余时间
     */
    fun getFormattedRemainingTime(): String {
        return formatDuration(remainingTimeMs)
    }
    
    /**
     * 获取格式化的超限时间
     */
    fun getFormattedExceededTime(): String {
        return formatDuration(exceededTimeMs)
    }
    
    private fun formatDuration(milliseconds: Long): String {
        val totalMinutes = milliseconds / (1000 * 60)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        
        return when {
            hours > 0 -> "${hours}小时${minutes}分钟"
            minutes > 0 -> "${minutes}分钟"
            else -> "不到1分钟"
        }
    }
}

/**
 * 阈值配置构建器
 */
class UsageThresholdBuilder {
    private var id: String = ""
    private var packageName: String? = null
    private var appName: String? = null
    private var dailyLimitMinutes: Int = 60
    private var warningPercentages: List<Int> = listOf(80, 90, 100)
    private var isEnabled: Boolean = true
    private var notificationSound: Boolean = true
    private var vibration: Boolean = true
    private var priority: ThresholdPriority = ThresholdPriority.NORMAL
    
    fun id(id: String) = apply { this.id = id }
    fun packageName(packageName: String?) = apply { this.packageName = packageName }
    fun appName(appName: String?) = apply { this.appName = appName }
    fun dailyLimitMinutes(minutes: Int) = apply { this.dailyLimitMinutes = minutes }
    fun warningPercentages(percentages: List<Int>) = apply { this.warningPercentages = percentages }
    fun enabled(enabled: Boolean) = apply { this.isEnabled = enabled }
    fun notificationSound(sound: Boolean) = apply { this.notificationSound = sound }
    fun vibration(vibration: Boolean) = apply { this.vibration = vibration }
    fun priority(priority: ThresholdPriority) = apply { this.priority = priority }
    
    fun build(): UsageThreshold {
        require(id.isNotEmpty()) { "ID cannot be empty" }
        require(dailyLimitMinutes > 0) { "Daily limit must be positive" }
        require(warningPercentages.isNotEmpty()) { "Warning percentages cannot be empty" }
        require(warningPercentages.all { it in 1..100 }) { "Warning percentages must be between 1 and 100" }
        
        return UsageThreshold(
            id = id,
            packageName = packageName,
            appName = appName,
            dailyLimitMinutes = dailyLimitMinutes,
            warningPercentages = warningPercentages.sorted(),
            isEnabled = isEnabled,
            notificationSound = notificationSound,
            vibration = vibration,
            priority = priority
        )
    }
}

/**
 * 预定义阈值模板
 */
object ThresholdTemplates {
    
    /**
     * 创建全局阈值模板
     */
    fun createGlobalThreshold(dailyLimitHours: Int): UsageThreshold {
        return UsageThresholdBuilder()
            .id("global_threshold")
            .dailyLimitMinutes(dailyLimitHours * 60)
            .warningPercentages(listOf(75, 90, 100))
            .priority(ThresholdPriority.HIGH)
            .build()
    }
    
    /**
     * 创建应用阈值模板
     */
    fun createAppThreshold(
        packageName: String,
        appName: String,
        dailyLimitMinutes: Int,
        priority: ThresholdPriority = ThresholdPriority.NORMAL
    ): UsageThreshold {
        return UsageThresholdBuilder()
            .id("app_${packageName}")
            .packageName(packageName)
            .appName(appName)
            .dailyLimitMinutes(dailyLimitMinutes)
            .warningPercentages(listOf(80, 90, 100))
            .priority(priority)
            .build()
    }
    
    /**
     * 创建社交应用阈值模板
     */
    fun createSocialAppThreshold(packageName: String, appName: String): UsageThreshold {
        return createAppThreshold(packageName, appName, 120, ThresholdPriority.HIGH) // 2小时
    }
    
    /**
     * 创建游戏应用阈值模板
     */
    fun createGameAppThreshold(packageName: String, appName: String): UsageThreshold {
        return createAppThreshold(packageName, appName, 90, ThresholdPriority.HIGH) // 1.5小时
    }
    
    /**
     * 创建工作应用阈值模板
     */
    fun createWorkAppThreshold(packageName: String, appName: String): UsageThreshold {
        return createAppThreshold(packageName, appName, 480, ThresholdPriority.LOW) // 8小时
    }
}
