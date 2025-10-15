package com.example.time.ui.utils

/**
 * 时间格式化工具类
 * 提供统一的时长格式化函数
 */

/**
 * 格式化时长（毫秒）为可读字符串
 * @param millis 毫秒数
 * @return 格式化后的字符串，如"3小时20分钟"
 */
fun formatDuration(millis: Long): String {
    val hours = millis / (1000 * 60 * 60)
    val minutes = (millis / (1000 * 60)) % 60
    return when {
        hours > 0 -> "${hours}小时${if (minutes > 0) "${minutes}分钟" else ""}"
        minutes > 0 -> "${minutes}分钟"
        else -> "少于1分钟"
    }
}

/**
 * 格式化时长（分钟）为可读字符串
 * @param minutes 分钟数
 * @return 格式化后的字符串，如"3小时20分钟"
 */
fun formatDurationMinutes(minutes: Long): String {
    return when {
        minutes < 60 -> "${minutes}分钟"
        minutes < 1440 -> {
            val hours = minutes / 60
            val mins = minutes % 60
            "${hours}小时${if (mins > 0) "${mins}分钟" else ""}"
        }
        else -> {
            val days = minutes / 1440
            val hours = (minutes % 1440) / 60
            "${days}天${if (hours > 0) "${hours}小时" else ""}"
        }
    }
}

/**
 * 格式化时长（Duration）为可读字符串
 * @param duration java.time.Duration对象
 * @return 格式化后的字符串
 */
fun formatDuration(duration: java.time.Duration): String {
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    return when {
        hours > 0 -> "${hours}小时${if (minutes > 0) "${minutes}分钟" else ""}"
        minutes > 0 -> "${minutes}分钟"
        else -> "少于1分钟"
    }
}
