package com.example.time.reminder

import com.example.time.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DoNotDisturbScheduler - 免打扰调度器
 * 
 * 负责免打扰时段管理、基于时间的规则、星期选择、应用白名单管理、
 * 检查当前是否在免打扰时段、过滤通知
 */
@Singleton
class DoNotDisturbScheduler @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    
    /**
     * 检查当前是否在免打扰时段
     */
    suspend fun isInDoNotDisturbPeriod(): Boolean {
        return try {
            val dndSettings = settingsRepository.getJson(
                "do_not_disturb_settings",
                SettingsRepository.DoNotDisturbSettings()
            )
            
            if (!dndSettings.enabled) return false
            
            val now = LocalDateTime.now()
            val currentTime = now.toLocalTime()
            val currentDayOfWeek = now.dayOfWeek.value // 1=Monday, 7=Sunday
            
            // 检查是否在启用的星期
            if (!dndSettings.enabledDays.contains(currentDayOfWeek)) {
                return false
            }
            
            // 检查时间范围
            val startTime = LocalTime.of(dndSettings.startHour, dndSettings.startMinute)
            val endTime = LocalTime.of(dndSettings.endHour, dndSettings.endMinute)
            
            return isTimeInRange(currentTime, startTime, endTime)
            
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 检查指定时间是否在免打扰时段
     */
    suspend fun isInDoNotDisturbPeriod(dateTime: LocalDateTime): Boolean {
        return try {
            val dndSettings = settingsRepository.getJson(
                "do_not_disturb_settings",
                SettingsRepository.DoNotDisturbSettings()
            )
            
            if (!dndSettings.enabled) return false
            
            val time = dateTime.toLocalTime()
            val dayOfWeek = dateTime.dayOfWeek.value
            
            // 检查是否在启用的星期
            if (!dndSettings.enabledDays.contains(dayOfWeek)) {
                return false
            }
            
            // 检查时间范围
            val startTime = LocalTime.of(dndSettings.startHour, dndSettings.startMinute)
            val endTime = LocalTime.of(dndSettings.endHour, dndSettings.endMinute)
            
            return isTimeInRange(time, startTime, endTime)
            
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 检查应用是否在白名单中
     */
    suspend fun isAppInWhitelist(packageName: String): Boolean {
        return try {
            val dndSettings = settingsRepository.getJson(
                "do_not_disturb_settings",
                SettingsRepository.DoNotDisturbSettings()
            )
            
            dndSettings.whitelistApps.contains(packageName)
            
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 检查是否应该过滤通知
     */
    suspend fun shouldFilterNotification(packageName: String? = null): Boolean {
        if (!isInDoNotDisturbPeriod()) return false
        
        // 如果应用在白名单中，不过滤
        if (packageName != null && isAppInWhitelist(packageName)) {
            return false
        }
        
        return true
    }
    
    /**
     * 获取免打扰状态流
     */
    fun getDoNotDisturbStatusFlow(): Flow<DoNotDisturbStatus> {
        return settingsRepository.getJsonFlow(
            "do_not_disturb_settings",
            SettingsRepository.DoNotDisturbSettings()
        ).map { dndSettings ->
            val isActive = if (dndSettings.enabled) {
                val now = LocalDateTime.now()
                val currentTime = now.toLocalTime()
                val currentDayOfWeek = now.dayOfWeek.value
                
                val isEnabledDay = dndSettings.enabledDays.contains(currentDayOfWeek)
                val startTime = LocalTime.of(dndSettings.startHour, dndSettings.startMinute)
                val endTime = LocalTime.of(dndSettings.endHour, dndSettings.endMinute)
                val isInTimeRange = isTimeInRange(currentTime, startTime, endTime)
                
                isEnabledDay && isInTimeRange
            } else {
                false
            }
            
            DoNotDisturbStatus(
                isEnabled = dndSettings.enabled,
                isActive = isActive,
                settings = dndSettings,
                nextToggleTime = calculateNextToggleTime(dndSettings)
            )
        }
    }
    
    /**
     * 添加应用到白名单
     */
    suspend fun addToWhitelist(packageName: String) {
        try {
            val dndSettings = settingsRepository.getJson(
                "do_not_disturb_settings",
                SettingsRepository.DoNotDisturbSettings()
            )
            
            val updatedSettings = dndSettings.copy(
                whitelistApps = dndSettings.whitelistApps + packageName
            )
            
            settingsRepository.setJson("do_not_disturb_settings", updatedSettings)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 从白名单移除应用
     */
    suspend fun removeFromWhitelist(packageName: String) {
        try {
            val dndSettings = settingsRepository.getJson(
                "do_not_disturb_settings",
                SettingsRepository.DoNotDisturbSettings()
            )
            
            val updatedSettings = dndSettings.copy(
                whitelistApps = dndSettings.whitelistApps - packageName
            )
            
            settingsRepository.setJson("do_not_disturb_settings", updatedSettings)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 获取白名单应用列表
     */
    suspend fun getWhitelistApps(): Set<String> {
        return try {
            val dndSettings = settingsRepository.getJson(
                "do_not_disturb_settings",
                SettingsRepository.DoNotDisturbSettings()
            )
            
            dndSettings.whitelistApps
            
        } catch (e: Exception) {
            e.printStackTrace()
            emptySet()
        }
    }
    
    /**
     * 临时禁用免打扰（指定时长）
     */
    suspend fun temporaryDisable(durationMinutes: Int) {
        // 这里可以实现临时禁用逻辑
        // 例如设置一个临时标记，在指定时间后自动恢复
    }
    
    /**
     * 获取免打扰时段描述
     */
    suspend fun getScheduleDescription(): String {
        return try {
            val dndSettings = settingsRepository.getJson(
                "do_not_disturb_settings",
                SettingsRepository.DoNotDisturbSettings()
            )
            
            if (!dndSettings.enabled) {
                return "免打扰已关闭"
            }
            
            val startTime = String.format("%02d:%02d", dndSettings.startHour, dndSettings.startMinute)
            val endTime = String.format("%02d:%02d", dndSettings.endHour, dndSettings.endMinute)
            
            val daysText = when {
                dndSettings.enabledDays.size == 7 -> "每天"
                dndSettings.enabledDays == setOf(1, 2, 3, 4, 5) -> "工作日"
                dndSettings.enabledDays == setOf(6, 7) -> "周末"
                else -> {
                    val dayNames = mapOf(
                        1 to "周一", 2 to "周二", 3 to "周三", 4 to "周四",
                        5 to "周五", 6 to "周六", 7 to "周日"
                    )
                    dndSettings.enabledDays.sorted().joinToString("、") { dayNames[it] ?: "" }
                }
            }
            
            "${daysText} ${startTime} - ${endTime}"
            
        } catch (e: Exception) {
            e.printStackTrace()
            "获取失败"
        }
    }
    
    /**
     * 检查时间是否在指定范围内
     */
    private fun isTimeInRange(current: LocalTime, start: LocalTime, end: LocalTime): Boolean {
        return if (start <= end) {
            // 同一天内的时间段，例如 09:00 - 17:00
            current >= start && current <= end
        } else {
            // 跨天的时间段，例如 22:00 - 08:00
            current >= start || current <= end
        }
    }
    
    /**
     * 计算下次切换时间
     */
    private fun calculateNextToggleTime(dndSettings: SettingsRepository.DoNotDisturbSettings): LocalDateTime? {
        if (!dndSettings.enabled) return null
        
        val now = LocalDateTime.now()
        val currentTime = now.toLocalTime()
        val currentDayOfWeek = now.dayOfWeek.value
        
        val startTime = LocalTime.of(dndSettings.startHour, dndSettings.startMinute)
        val endTime = LocalTime.of(dndSettings.endHour, dndSettings.endMinute)
        
        // 如果当前在免打扰时段，返回结束时间
        if (dndSettings.enabledDays.contains(currentDayOfWeek) && 
            isTimeInRange(currentTime, startTime, endTime)) {
            
            return if (startTime <= endTime) {
                // 同一天结束
                now.toLocalDate().atTime(endTime)
            } else {
                // 次日结束
                if (currentTime >= startTime) {
                    now.toLocalDate().plusDays(1).atTime(endTime)
                } else {
                    now.toLocalDate().atTime(endTime)
                }
            }
        }
        
        // 如果当前不在免打扰时段，返回下次开始时间
        // 查找下一个启用的日期
        for (i in 0..7) {
            val checkDate = now.toLocalDate().plusDays(i.toLong())
            val checkDayOfWeek = checkDate.dayOfWeek.value
            
            if (dndSettings.enabledDays.contains(checkDayOfWeek)) {
                val startDateTime = checkDate.atTime(startTime)
                
                if (i == 0 && currentTime < startTime) {
                    // 今天还没到开始时间
                    return startDateTime
                } else if (i > 0) {
                    // 未来的日期
                    return startDateTime
                }
            }
        }
        
        return null
    }
    
    /**
     * 获取免打扰统计信息
     */
    suspend fun getStatistics(): DoNotDisturbStatistics {
        return try {
            val dndSettings = settingsRepository.getJson(
                "do_not_disturb_settings",
                SettingsRepository.DoNotDisturbSettings()
            )
            
            val now = LocalDateTime.now()
            val isCurrentlyActive = isInDoNotDisturbPeriod()
            val nextToggle = calculateNextToggleTime(dndSettings)
            
            // 计算每周免打扰时长
            val weeklyHours = if (dndSettings.enabled) {
                calculateWeeklyDndHours(dndSettings)
            } else {
                0.0
            }
            
            DoNotDisturbStatistics(
                isEnabled = dndSettings.enabled,
                isCurrentlyActive = isCurrentlyActive,
                enabledDaysCount = dndSettings.enabledDays.size,
                whitelistAppsCount = dndSettings.whitelistApps.size,
                weeklyDndHours = weeklyHours,
                nextToggleTime = nextToggle
            )
            
        } catch (e: Exception) {
            e.printStackTrace()
            DoNotDisturbStatistics(false, false, 0, 0, 0.0, null)
        }
    }
    
    /**
     * 计算每周免打扰时长
     */
    private fun calculateWeeklyDndHours(dndSettings: SettingsRepository.DoNotDisturbSettings): Double {
        val startTime = LocalTime.of(dndSettings.startHour, dndSettings.startMinute)
        val endTime = LocalTime.of(dndSettings.endHour, dndSettings.endMinute)
        
        val dailyMinutes = if (startTime <= endTime) {
            // 同一天内
            java.time.Duration.between(startTime, endTime).toMinutes()
        } else {
            // 跨天
            val minutesToMidnight = java.time.Duration.between(startTime, LocalTime.MIDNIGHT).toMinutes()
            val minutesFromMidnight = java.time.Duration.between(LocalTime.MIDNIGHT, endTime).toMinutes()
            minutesToMidnight + minutesFromMidnight
        }
        
        val weeklyMinutes = dailyMinutes * dndSettings.enabledDays.size
        return weeklyMinutes / 60.0
    }
}

/**
 * 免打扰状态
 */
data class DoNotDisturbStatus(
    val isEnabled: Boolean,
    val isActive: Boolean,
    val settings: SettingsRepository.DoNotDisturbSettings,
    val nextToggleTime: LocalDateTime?
)

/**
 * 免打扰统计信息
 */
data class DoNotDisturbStatistics(
    val isEnabled: Boolean,
    val isCurrentlyActive: Boolean,
    val enabledDaysCount: Int,
    val whitelistAppsCount: Int,
    val weeklyDndHours: Double,
    val nextToggleTime: LocalDateTime?
)
