package com.example.time.ui.apps

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 应用信息数据类
 * 用于应用管理和提醒设置
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val category: String,
    val todayUsage: Long = 0L,
    val timeLimit: Int = 0, // 分钟
    val isBlacklisted: Boolean = false
)

/**
 * 应用项数据类
 * 用于应用管理列表显示
 */
data class AppItem(
    val name: String,
    val packageName: String,
    val icon: ImageVector,
    val iconGradient: Brush,
    val todayUsage: String,
    val size: String,
    val category: String,
    val progress: Float
)

/**
 * 排序类型
 */
enum class SortType {
    USAGE_TIME,
    FREQUENCY,
    SIZE,
    INSTALL_TIME,
    NAME
}

