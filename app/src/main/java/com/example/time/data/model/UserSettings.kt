package com.example.time.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User Settings Entity - 用户设置实体
 * 存储用户配置和偏好设置
 */
@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val settingKey: String,
    
    // 设置值（JSON格式存储复杂对象）
    val settingValue: String,
    
    // 设置类型（用于序列化/反序列化）
    val valueType: String,
    
    // 最后更新时间
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * App Reminder Settings - 应用提醒设置
 */
data class AppReminderSettings(
    val packageName: String,
    val appName: String,
    val enabled: Boolean = true,
    val dailyLimit: Long = 0, // 每日限制（毫秒）
    val warningThresholds: List<Int> = listOf(80, 90, 100), // 警告阈值百分比
    val lastNotificationTime: Long = 0
)

/**
 * Global Reminder Settings - 全局提醒设置
 */
data class GlobalReminderSettings(
    val enabled: Boolean = true,
    val dailyLimit: Long = 0, // 每日总使用时长限制（毫秒）
    val restReminderEnabled: Boolean = false,
    val restReminderInterval: Long = 1800000, // 休息提醒间隔（默认30分钟）
    val doNotDisturbEnabled: Boolean = false,
    val doNotDisturbStart: String = "22:00", // HH:mm format
    val doNotDisturbEnd: String = "08:00",
    val doNotDisturbWhitelist: List<String> = emptyList() // 白名单应用包名
)

/**
 * Performance Mode Settings - 性能模式设置
 */
enum class PerformanceMode(val interval: Long) {
    STANDARD(5000),      // 标准模式：5秒间隔
    HIGH_PRECISION(1000), // 高精度模式：1秒间隔
    ENERGY_SAVING(30000)  // 节能模式：30秒间隔
}

data class PerformanceModeSettings(
    val currentMode: PerformanceMode = PerformanceMode.STANDARD,
    val autoSwitch: Boolean = true,
    val autoSwitchBatteryLevel: Int = 20 // 电量低于此值自动切换到节能模式
)

/**
 * Theme Settings - 主题设置
 */
data class ThemeSettings(
    val isDarkMode: Boolean = false,
    val useSystemTheme: Boolean = true,
    val accentColor: String = "#667eea" // 品牌色
)

/**
 * App Blacklist - 应用黑名单
 */
data class AppBlacklist(
    val packageName: String,
    val appName: String,
    val reason: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

