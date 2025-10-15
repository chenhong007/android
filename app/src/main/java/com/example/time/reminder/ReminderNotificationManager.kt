package com.example.time.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.time.MainActivity
import com.example.time.R
import com.example.time.reminder.models.ReminderTrigger
import com.example.time.reminder.models.ReminderType
import com.example.time.reminder.models.ReminderSeverity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ReminderNotificationManager - 提醒通知管理器
 * 
 * 负责创建通知渠道、构建提醒通知、显示通知、处理通知点击、通知优先级管理
 */
@Singleton
class ReminderNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        // 通知渠道ID
        private const val CHANNEL_GLOBAL_REMINDER = "global_reminder"
        private const val CHANNEL_APP_REMINDER = "app_reminder"
        private const val CHANNEL_REST_REMINDER = "rest_reminder"
        
        // 通知ID范围
        private const val NOTIFICATION_ID_GLOBAL = 1000
        private const val NOTIFICATION_ID_APP_BASE = 2000
        private const val NOTIFICATION_ID_REST = 3000
        
        // 请求代码
        private const val REQUEST_CODE_MAIN = 100
        private const val REQUEST_CODE_SETTINGS = 101
        private const val REQUEST_CODE_DISMISS = 102
    }
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    init {
        createNotificationChannels()
    }
    
    /**
     * 创建通知渠道
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                // 全局提醒渠道
                NotificationChannel(
                    CHANNEL_GLOBAL_REMINDER,
                    "全局使用提醒",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "设备整体使用时间提醒"
                    enableVibration(true)
                    setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                },
                
                // 应用提醒渠道
                NotificationChannel(
                    CHANNEL_APP_REMINDER,
                    "应用使用提醒",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "单个应用使用时间提醒"
                    enableVibration(true)
                },
                
                // 休息提醒渠道
                NotificationChannel(
                    CHANNEL_REST_REMINDER,
                    "休息提醒",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "定期休息提醒"
                    enableVibration(false)
                }
            )
            
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { channel ->
                systemNotificationManager.createNotificationChannel(channel)
            }
        }
    }
    
    /**
     * 发送提醒通知
     */
    fun sendReminderNotification(
        trigger: ReminderTrigger,
        soundEnabled: Boolean = true,
        vibrationEnabled: Boolean = true
    ) {
        try {
            val notification = buildNotification(trigger, soundEnabled, vibrationEnabled)
            val notificationId = getNotificationId(trigger)
            
            notificationManager.notify(notificationId, notification)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 构建通知
     */
    private fun buildNotification(
        trigger: ReminderTrigger,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean
    ): android.app.Notification {
        
        val channelId = getChannelId(trigger.type)
        val title = trigger.getNotificationTitle()
        val message = trigger.getFormattedMessage()
        
        // 主要动作 - 打开应用
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("trigger_id", trigger.id)
            putExtra("trigger_type", trigger.type.name)
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_MAIN,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 设置动作 - 打开设置页面
        val settingsIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_settings", true)
            putExtra("settings_type", trigger.type.name)
        }
        val settingsPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_SETTINGS,
            settingsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 忽略动作
        val dismissIntent = Intent(context, ReminderDismissReceiver::class.java).apply {
            putExtra("trigger_id", trigger.id)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_DISMISS,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(getNotificationIcon(trigger))
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(mainPendingIntent)
            .setAutoCancel(true)
            .setPriority(getNotificationPriority(trigger.severity))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        
        // 添加动作按钮
        when (trigger.type) {
            ReminderType.GLOBAL_LIMIT, ReminderType.APP_LIMIT -> {
                builder.addAction(
                    android.R.drawable.ic_menu_preferences,
                    "设置",
                    settingsPendingIntent
                )
                builder.addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "忽略",
                    dismissPendingIntent
                )
            }
            ReminderType.REST_BREAK -> {
                builder.addAction(
                    android.R.drawable.ic_dialog_info,
                    "知道了",
                    dismissPendingIntent
                )
            }
        }
        
        // 设置进度条（对于有限制的提醒）
        if (trigger.limit > 0 && trigger.type != ReminderType.REST_BREAK) {
            val progress = (trigger.usagePercentage).toInt()
            builder.setProgress(100, progress, false)
        }
        
        // 设置颜色
        builder.color = getNotificationColor(trigger.severity)
        
        // 声音设置
        if (!soundEnabled) {
            builder.setSound(null)
        }
        
        // 振动设置
        if (vibrationEnabled) {
            builder.setVibrate(getVibrationPattern(trigger.severity))
        } else {
            builder.setVibrate(null)
        }
        
        // 设置大图标（如果是应用提醒）
        if (trigger.packageName != null) {
            try {
                val packageManager = context.packageManager
                val appIcon = packageManager.getApplicationIcon(trigger.packageName)
                // 暂时移除图标设置，避免编译错误
                // builder.setLargeIcon(null)
            } catch (e: Exception) {
                // 忽略图标加载错误
            }
        }
        
        return builder.build()
    }
    
    /**
     * 获取通知ID
     */
    private fun getNotificationId(trigger: ReminderTrigger): Int {
        return when (trigger.type) {
            ReminderType.GLOBAL_LIMIT -> NOTIFICATION_ID_GLOBAL
            ReminderType.APP_LIMIT -> {
                NOTIFICATION_ID_APP_BASE + (trigger.packageName?.hashCode()?.and(0x7FFFFFFF) ?: 0) % 1000
            }
            ReminderType.REST_BREAK -> NOTIFICATION_ID_REST
        }
    }
    
    /**
     * 获取通知渠道ID
     */
    private fun getChannelId(type: ReminderType): String {
        return when (type) {
            ReminderType.GLOBAL_LIMIT -> CHANNEL_GLOBAL_REMINDER
            ReminderType.APP_LIMIT -> CHANNEL_APP_REMINDER
            ReminderType.REST_BREAK -> CHANNEL_REST_REMINDER
        }
    }
    
    /**
     * 获取通知图标
     */
    private fun getNotificationIcon(trigger: ReminderTrigger): Int {
        return when (trigger.type) {
            ReminderType.GLOBAL_LIMIT -> android.R.drawable.ic_menu_call
            ReminderType.APP_LIMIT -> android.R.drawable.ic_menu_manage
            ReminderType.REST_BREAK -> android.R.drawable.ic_dialog_info
        }
    }
    
    /**
     * 获取通知优先级
     */
    private fun getNotificationPriority(severity: ReminderSeverity): Int {
        return when (severity) {
            ReminderSeverity.INFO -> NotificationCompat.PRIORITY_LOW
            ReminderSeverity.WARNING -> NotificationCompat.PRIORITY_DEFAULT
            ReminderSeverity.CRITICAL -> NotificationCompat.PRIORITY_HIGH
            ReminderSeverity.EXCEEDED -> NotificationCompat.PRIORITY_MAX
        }
    }
    
    /**
     * 获取通知颜色
     */
    private fun getNotificationColor(severity: ReminderSeverity): Int {
        return when (severity) {
            ReminderSeverity.INFO -> 0xFF2196F3.toInt() // 蓝色
            ReminderSeverity.WARNING -> 0xFFFF9800.toInt() // 橙色
            ReminderSeverity.CRITICAL -> 0xFFF44336.toInt() // 红色
            ReminderSeverity.EXCEEDED -> 0xFF9C27B0.toInt() // 紫色
        }
    }
    
    /**
     * 获取振动模式
     */
    private fun getVibrationPattern(severity: ReminderSeverity): LongArray {
        return when (severity) {
            ReminderSeverity.INFO -> longArrayOf(0, 100, 100, 100)
            ReminderSeverity.WARNING -> longArrayOf(0, 200, 100, 200)
            ReminderSeverity.CRITICAL -> longArrayOf(0, 300, 100, 300, 100, 300)
            ReminderSeverity.EXCEEDED -> longArrayOf(0, 500, 200, 500, 200, 500)
        }
    }
    
    /**
     * 取消特定通知
     */
    fun cancelNotification(trigger: ReminderTrigger) {
        val notificationId = getNotificationId(trigger)
        notificationManager.cancel(notificationId)
    }
    
    /**
     * 取消所有提醒通知
     */
    fun cancelAllReminderNotifications() {
        // 取消全局提醒
        notificationManager.cancel(NOTIFICATION_ID_GLOBAL)
        
        // 取消休息提醒
        notificationManager.cancel(NOTIFICATION_ID_REST)
        
        // 取消应用提醒（这里只能取消已知的，实际项目中可能需要跟踪活动通知）
        for (i in 0..999) {
            notificationManager.cancel(NOTIFICATION_ID_APP_BASE + i)
        }
    }
    
    /**
     * 检查通知权限
     */
    fun areNotificationsEnabled(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }
    
    /**
     * 创建测试通知
     */
    fun sendTestNotification() {
        val testTrigger = ReminderTrigger(
            id = "test",
            type = ReminderType.GLOBAL_LIMIT,
            currentUsage = 4 * 60 * 60 * 1000L, // 4小时
            limit = 5 * 60 * 60 * 1000L, // 5小时
            percentage = 80
        )
        
        sendReminderNotification(testTrigger)
    }
}

/**
 * 提醒忽略接收器
 */
class ReminderDismissReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val triggerId = intent?.getStringExtra("trigger_id")
        // 这里可以记录用户忽略提醒的行为
        // 实际项目中需要通过依赖注入获取相关服务
    }
}
