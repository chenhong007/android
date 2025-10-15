package com.example.time.reminder.models

import kotlinx.serialization.Serializable

/**
 * ReminderTrigger - 提醒触发器
 * 
 * 表示一个提醒触发事件，包含触发条件、应用信息、使用数据等
 */
@Serializable
data class ReminderTrigger(
    val id: String,
    val type: ReminderType,
    val appName: String? = null,
    val packageName: String? = null,
    val currentUsage: Long, // 当前使用时长（毫秒）
    val limit: Long, // 限制时长（毫秒）
    val percentage: Int, // 触发百分比
    val timestamp: Long = System.currentTimeMillis(),
    val severity: ReminderSeverity = ReminderSeverity.fromPercentage(percentage),
    val message: String? = null,
    val actionType: ReminderActionType = ReminderActionType.NOTIFICATION
) {
    /**
     * 获取使用百分比
     */
    val usagePercentage: Double
        get() = if (limit > 0) (currentUsage * 100.0 / limit).coerceAtMost(100.0) else 0.0
    
    /**
     * 获取剩余时间
     */
    val remainingTime: Long
        get() = maxOf(0L, limit - currentUsage)
    
    /**
     * 获取超限时间
     */
    val exceededTime: Long
        get() = maxOf(0L, currentUsage - limit)
    
    /**
     * 是否已超限
     */
    val isExceeded: Boolean
        get() = currentUsage >= limit
    
    /**
     * 获取显示名称
     */
    val displayName: String
        get() = appName ?: when (type) {
            ReminderType.GLOBAL_LIMIT -> "全局使用"
            ReminderType.REST_BREAK -> "休息提醒"
            else -> "应用使用"
        }
    
    /**
     * 获取格式化的消息
     */
    fun getFormattedMessage(): String {
        return message ?: when (type) {
            ReminderType.GLOBAL_LIMIT -> {
                when {
                    isExceeded -> "您今天已使用设备${formatDuration(currentUsage)}，超出限制${formatDuration(exceededTime)}，建议适当休息"
                    percentage >= 90 -> "您今天已使用设备${formatDuration(currentUsage)}，即将达到每日限制，剩余${formatDuration(remainingTime)}"
                    else -> "您今天已使用设备${formatDuration(currentUsage)}，已达到限制的${percentage}%"
                }
            }
            ReminderType.APP_LIMIT -> {
                when {
                    isExceeded -> "您今天使用${displayName}已达${formatDuration(currentUsage)}，超出限制${formatDuration(exceededTime)}"
                    percentage >= 90 -> "您今天使用${displayName}已达${formatDuration(currentUsage)}，即将达到限制，剩余${formatDuration(remainingTime)}"
                    else -> "您今天使用${displayName}已达${formatDuration(currentUsage)}，已达到限制的${percentage}%"
                }
            }
            ReminderType.REST_BREAK -> {
                "您已连续使用设备${formatDuration(currentUsage)}，建议休息一下，保护视力健康"
            }
        }
    }
    
    /**
     * 获取通知标题
     */
    fun getNotificationTitle(): String {
        return when (type) {
            ReminderType.GLOBAL_LIMIT -> when {
                isExceeded -> "设备使用超限"
                percentage >= 90 -> "设备使用即将达限"
                else -> "设备使用提醒"
            }
            ReminderType.APP_LIMIT -> when {
                isExceeded -> "${displayName}使用超限"
                percentage >= 90 -> "${displayName}即将达限"
                else -> "${displayName}使用提醒"
            }
            ReminderType.REST_BREAK -> "休息提醒"
        }
    }
    
    /**
     * 获取通知图标资源
     */
    fun getNotificationIcon(): String {
        return when (type) {
            ReminderType.GLOBAL_LIMIT -> "ic_phone_android"
            ReminderType.APP_LIMIT -> "ic_apps"
            ReminderType.REST_BREAK -> "ic_self_care"
        }
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
 * 提醒类型
 */
@Serializable
enum class ReminderType(val displayName: String) {
    GLOBAL_LIMIT("全局限制"),
    APP_LIMIT("应用限制"),
    REST_BREAK("休息提醒")
}

/**
 * 提醒严重程度
 */
@Serializable
enum class ReminderSeverity(val displayName: String, val priority: Int) {
    INFO("信息", 1),
    WARNING("警告", 2),
    CRITICAL("严重", 3),
    EXCEEDED("超限", 4);
    
    companion object {
        fun fromPercentage(percentage: Int): ReminderSeverity {
            return when (percentage) {
                in 0..79 -> INFO
                in 80..89 -> WARNING
                in 90..99 -> CRITICAL
                else -> EXCEEDED
            }
        }
    }
}

/**
 * 提醒动作类型
 */
@Serializable
enum class ReminderActionType(val displayName: String) {
    NOTIFICATION("通知"),
    POPUP("弹窗"),
    SOUND("声音"),
    VIBRATION("振动"),
    BLOCK("阻止使用")
}

/**
 * 提醒历史记录
 */
@Serializable
data class ReminderHistory(
    val id: String = java.util.UUID.randomUUID().toString(),
    val trigger: ReminderTrigger,
    val sentAt: Long = System.currentTimeMillis(),
    val wasClicked: Boolean = false,
    val wasDismissed: Boolean = false,
    val clickedAt: Long? = null,
    val dismissedAt: Long? = null,
    val responseTime: Long? = null // 用户响应时间（毫秒）
) {
    /**
     * 计算响应时间
     */
    fun calculateResponseTime(): Long? {
        val responseTimestamp = clickedAt ?: dismissedAt
        return if (responseTimestamp != null) {
            responseTimestamp - sentAt
        } else {
            null
        }
    }
    
    /**
     * 获取响应状态
     */
    val responseStatus: ReminderResponseStatus
        get() = when {
            wasClicked -> ReminderResponseStatus.CLICKED
            wasDismissed -> ReminderResponseStatus.DISMISSED
            else -> ReminderResponseStatus.NO_RESPONSE
        }
}

/**
 * 提醒响应状态
 */
enum class ReminderResponseStatus(val displayName: String) {
    NO_RESPONSE("未响应"),
    CLICKED("已点击"),
    DISMISSED("已忽略")
}

/**
 * 提醒统计数据
 */
data class ReminderStatistics(
    val totalReminders: Int,
    val clickedReminders: Int,
    val dismissedReminders: Int,
    val noResponseReminders: Int,
    val averageResponseTime: Long,
    val remindersByType: Map<ReminderType, Int>,
    val remindersBySeverity: Map<ReminderSeverity, Int>,
    val clickRate: Double,
    val dismissRate: Double
) {
    companion object {
        fun fromHistory(history: List<ReminderHistory>): ReminderStatistics {
            val total = history.size
            val clicked = history.count { it.wasClicked }
            val dismissed = history.count { it.wasDismissed }
            val noResponse = total - clicked - dismissed
            
            val responseTimes = history.mapNotNull { it.calculateResponseTime() }
            val averageResponseTime = if (responseTimes.isNotEmpty()) {
                responseTimes.average().toLong()
            } else {
                0L
            }
            
            val byType = history.groupBy { it.trigger.type }
                .mapValues { it.value.size }
            
            val bySeverity = history.groupBy { it.trigger.severity }
                .mapValues { it.value.size }
            
            val clickRate = if (total > 0) clicked.toDouble() / total else 0.0
            val dismissRate = if (total > 0) dismissed.toDouble() / total else 0.0
            
            return ReminderStatistics(
                totalReminders = total,
                clickedReminders = clicked,
                dismissedReminders = dismissed,
                noResponseReminders = noResponse,
                averageResponseTime = averageResponseTime,
                remindersByType = byType,
                remindersBySeverity = bySeverity,
                clickRate = clickRate,
                dismissRate = dismissRate
            )
        }
    }
}
