package com.example.time.reminder

import android.content.Context
import androidx.work.*
import com.example.time.data.repository.SettingsRepository
import com.example.time.data.repository.UsageRepository
import com.example.time.data.repository.ScreenEventRepository
import com.example.time.reminder.models.UsageThreshold
import com.example.time.reminder.models.ReminderTrigger
import com.example.time.reminder.models.ReminderType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ReminderEngine - 提醒引擎核心类
 * 
 * 负责监控全局使用阈值、每应用使用阈值、计算使用百分比、触发提醒通知
 * 包含防重复提醒机制和与 DataCollectionService 的集成
 */
@Singleton
class ReminderEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val usageRepository: UsageRepository,
    private val screenEventRepository: ScreenEventRepository,
    private val notificationManager: ReminderNotificationManager,
    private val doNotDisturbScheduler: DoNotDisturbScheduler
) {
    
    companion object {
        private const val TAG = "ReminderEngine"
        private const val COOLDOWN_PERIOD_MS = 30 * 60 * 1000L // 30分钟冷却期
        private const val CHECK_INTERVAL_MS = 60 * 1000L // 1分钟检查间隔
        private const val WORK_NAME = "reminder_check_work"
    }
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val lastReminderTimes = mutableMapOf<String, Long>()
    private val workManager = WorkManager.getInstance(context)
    
    private var isRunning = false
    
    /**
     * 启动提醒引擎
     */
    fun start() {
        if (isRunning) return
        isRunning = true
        
        // 启动定期检查工作
        schedulePeriodicCheck()
        
        // 启动实时监控
        startRealtimeMonitoring()
    }
    
    /**
     * 停止提醒引擎
     */
    fun stop() {
        isRunning = false
        workManager.cancelUniqueWork(WORK_NAME)
        scope.cancel()
    }
    
    /**
     * 安排定期检查工作
     */
    private fun schedulePeriodicCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<ReminderCheckWorker>(
            15, TimeUnit.MINUTES // 每15分钟检查一次
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    /**
     * 启动实时监控
     */
    private fun startRealtimeMonitoring() {
        scope.launch {
            while (isRunning) {
                try {
                    performReminderCheck()
                    delay(CHECK_INTERVAL_MS)
                } catch (e: Exception) {
                    // 记录错误但继续运行
                    e.printStackTrace()
                    delay(CHECK_INTERVAL_MS)
                }
            }
        }
    }
    
    /**
     * 执行提醒检查
     */
    suspend fun performReminderCheck() {
        if (doNotDisturbScheduler.isInDoNotDisturbPeriod()) {
            return // 免打扰时段不发送提醒
        }
        
        val now = System.currentTimeMillis()
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        // 检查全局使用限制
        checkGlobalThreshold(startOfDay, now)
        
        // 检查应用特定限制
        checkAppThresholds(startOfDay, now)
        
        // 检查休息提醒
        checkRestReminder(now)
    }
    
    /**
     * 检查全局使用阈值
     */
    private suspend fun checkGlobalThreshold(startOfDay: Long, now: Long) {
        try {
            val globalSettings = settingsRepository.getGlobalReminderSettings()
            if (!globalSettings.enabled) return
            
            // 获取今日总使用时长
            val todayUsage = usageRepository.getUsageInRange(startOfDay, now)
            val totalUsageMs = todayUsage.sumOf { it.duration }
            val limitMs = globalSettings.dailyLimitMinutes * 60 * 1000L
            
            // 检查各个阈值
            globalSettings.warningPercentages.forEach { percentage ->
                val thresholdMs = (limitMs * percentage / 100.0).toLong()
                
                if (totalUsageMs >= thresholdMs) {
                    val trigger = ReminderTrigger(
                        id = "global_${percentage}",
                        type = ReminderType.GLOBAL_LIMIT,
                        appName = null,
                        packageName = null,
                        currentUsage = totalUsageMs,
                        limit = limitMs,
                        percentage = percentage,
                        timestamp = now
                    )
                    
                    if (shouldSendReminder(trigger)) {
                        sendReminder(trigger)
                    }
                }
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 检查应用特定阈值
     */
    private suspend fun checkAppThresholds(startOfDay: Long, now: Long) {
        try {
            val appReminders = settingsRepository.getAllAppReminderSettings()
            
            appReminders.filter { it.enabled }.forEach { appReminder ->
                // 获取应用今日使用时长
                val appUsage = usageRepository.getUsageInRange(startOfDay, now)
                    .filter { it.packageName == appReminder.packageName }
                val totalUsageMs = appUsage.sumOf { it.duration }
                val limitMs = appReminder.dailyLimitMinutes * 60 * 1000L
                
                // 检查各个阈值
                appReminder.warningPercentages.forEach { percentage ->
                    val thresholdMs = (limitMs * percentage / 100.0).toLong()
                    
                    if (totalUsageMs >= thresholdMs) {
                        val trigger = ReminderTrigger(
                            id = "${appReminder.packageName}_${percentage}",
                            type = ReminderType.APP_LIMIT,
                            appName = getAppName(appReminder.packageName),
                            packageName = appReminder.packageName,
                            currentUsage = totalUsageMs,
                            limit = limitMs,
                            percentage = percentage,
                            timestamp = now
                        )
                        
                        if (shouldSendReminder(trigger)) {
                            sendReminder(trigger)
                        }
                    }
                }
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 检查休息提醒
     */
    private suspend fun checkRestReminder(now: Long) {
        try {
            val globalSettings = settingsRepository.getGlobalReminderSettings()
            if (!globalSettings.restReminderEnabled) return
            
            val intervalMs = globalSettings.restIntervalMinutes * 60 * 1000L
            val lastRestReminder = lastReminderTimes["rest_reminder"] ?: 0L
            
            if (now - lastRestReminder >= intervalMs) {
                // 检查是否在连续使用中
                val screenOnDuration = screenEventRepository.getTodayScreenOnDuration()
                val continuousThreshold = 30 * 60 * 1000L // 连续使用30分钟以上才提醒休息
                
                if (screenOnDuration >= continuousThreshold) {
                    val trigger = ReminderTrigger(
                        id = "rest_reminder",
                        type = ReminderType.REST_BREAK,
                        appName = null,
                        packageName = null,
                        currentUsage = screenOnDuration,
                        limit = intervalMs,
                        percentage = 100,
                        timestamp = now
                    )
                    
                    sendReminder(trigger)
                }
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 检查是否应该发送提醒（防重复）
     */
    private fun shouldSendReminder(trigger: ReminderTrigger): Boolean {
        val lastTime = lastReminderTimes[trigger.id] ?: 0L
        val now = trigger.timestamp
        
        // 检查冷却期
        if (now - lastTime < COOLDOWN_PERIOD_MS) {
            return false
        }
        
        // 对于100%阈值，允许更频繁的提醒
        if (trigger.percentage >= 100) {
            val shortCooldown = 10 * 60 * 1000L // 10分钟
            return now - lastTime >= shortCooldown
        }
        
        return true
    }
    
    /**
     * 发送提醒
     */
    private suspend fun sendReminder(trigger: ReminderTrigger) {
        try {
            // 记录提醒时间
            lastReminderTimes[trigger.id] = trigger.timestamp
            
            // 获取通知设置
            val globalSettings = settingsRepository.getGlobalReminderSettings()
            val appSettings = trigger.packageName?.let { 
                settingsRepository.getAppReminderSettings(it) 
            }
            
            val soundEnabled = appSettings?.soundEnabled ?: globalSettings.soundEnabled
            val vibrationEnabled = appSettings?.vibrationEnabled ?: globalSettings.vibrationEnabled
            
            // 发送通知
            notificationManager.sendReminderNotification(
                trigger = trigger,
                soundEnabled = soundEnabled,
                vibrationEnabled = vibrationEnabled
            )
            
            // 记录提醒历史（如果需要）
            recordReminderHistory(trigger)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 记录提醒历史
     */
    private suspend fun recordReminderHistory(trigger: ReminderTrigger) {
        // 这里可以将提醒记录保存到数据库，用于统计分析
        // 暂时省略具体实现
    }
    
    /**
     * 获取应用名称
     */
    private fun getAppName(packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
    
    /**
     * 手动检查特定应用的阈值
     */
    suspend fun checkAppThreshold(packageName: String): ReminderTrigger? {
        try {
            val appSettings = settingsRepository.getAppReminderSettings(packageName)
            if (!appSettings.enabled) return null
            
            val now = System.currentTimeMillis()
            val today = LocalDate.now()
            val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            val appUsage = usageRepository.getUsageInRange(startOfDay, now)
                .filter { it.packageName == packageName }
            val totalUsageMs = appUsage.sumOf { it.duration }
            val limitMs = appSettings.dailyLimitMinutes * 60 * 1000L
            
            // 找到当前使用量对应的最高阈值
            val currentPercentage = if (limitMs > 0) {
                (totalUsageMs * 100.0 / limitMs).toInt()
            } else {
                0
            }
            
            val triggeredPercentage = appSettings.warningPercentages
                .filter { it <= currentPercentage }
                .maxOrNull()
            
            return if (triggeredPercentage != null) {
                ReminderTrigger(
                    id = "${packageName}_${triggeredPercentage}",
                    type = ReminderType.APP_LIMIT,
                    appName = getAppName(packageName),
                    packageName = packageName,
                    currentUsage = totalUsageMs,
                    limit = limitMs,
                    percentage = triggeredPercentage,
                    timestamp = now
                )
            } else {
                null
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * 获取使用统计摘要
     */
    suspend fun getUsageSummary(): UsageSummary {
        return try {
            val now = System.currentTimeMillis()
            val today = LocalDate.now()
            val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            val globalSettings = settingsRepository.getGlobalReminderSettings()
            val todayUsage = usageRepository.getUsageInRange(startOfDay, now)
            val totalUsageMs = todayUsage.sumOf { it.duration }
            val globalLimitMs = globalSettings.dailyLimitMinutes * 60 * 1000L
            
            val globalPercentage = if (globalLimitMs > 0) {
                (totalUsageMs * 100.0 / globalLimitMs).toInt()
            } else {
                0
            }
            
            // 获取超限应用
            val appReminders = settingsRepository.getAllAppReminderSettings()
            val exceededApps = mutableListOf<AppUsageStatus>()
            
            appReminders.filter { it.enabled }.forEach { appReminder ->
                val appUsage = usageRepository.getUsageInRange(startOfDay, now)
                    .filter { it.packageName == appReminder.packageName }
                val appTotalUsageMs = appUsage.sumOf { it.duration }
                val appLimitMs = appReminder.dailyLimitMinutes * 60 * 1000L
                
                val appPercentage = if (appLimitMs > 0) {
                    (appTotalUsageMs * 100.0 / appLimitMs).toInt()
                } else {
                    0
                }
                
                if (appPercentage >= 80) { // 80%以上算接近超限
                    exceededApps.add(
                        AppUsageStatus(
                            packageName = appReminder.packageName,
                            appName = getAppName(appReminder.packageName),
                            usageMs = appTotalUsageMs,
                            limitMs = appLimitMs,
                            percentage = appPercentage
                        )
                    )
                }
            }
            
            UsageSummary(
                totalUsageMs = totalUsageMs,
                globalLimitMs = globalLimitMs,
                globalPercentage = globalPercentage,
                exceededApps = exceededApps.sortedByDescending { it.percentage }
            )
            
        } catch (e: Exception) {
            e.printStackTrace()
            UsageSummary(0L, 0L, 0, emptyList())
        }
    }
}

/**
 * 使用摘要数据类
 */
data class UsageSummary(
    val totalUsageMs: Long,
    val globalLimitMs: Long,
    val globalPercentage: Int,
    val exceededApps: List<AppUsageStatus>
)

/**
 * 应用使用状态数据类
 */
data class AppUsageStatus(
    val packageName: String,
    val appName: String,
    val usageMs: Long,
    val limitMs: Long,
    val percentage: Int
)

/**
 * 提醒检查工作类
 */
class ReminderCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            // 这里需要通过依赖注入获取 ReminderEngine 实例
            // 由于 WorkManager 的限制，这里简化处理
            // 实际项目中需要使用 Hilt Worker 或其他方式
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
