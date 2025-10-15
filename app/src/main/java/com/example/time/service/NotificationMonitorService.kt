package com.example.time.service

import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.time.data.database.TimeDatabase
import com.example.time.data.model.NotificationRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Notification Monitor Service - 通知监听服务
 * 监听系统通知事件，记录通知的发布、移除和用户交互
 */
class NotificationMonitorService : NotificationListenerService() {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var database: TimeDatabase
    
    override fun onCreate() {
        super.onCreate()
        database = TimeDatabase.getInstance(applicationContext)
    }
    
    /**
     * 通知发布时调用
     */
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return
        
        serviceScope.launch {
            try {
                val notification = sbn.notification ?: return@launch
                val extras = notification.extras
                
                val record = NotificationRecord(
                    notificationKey = sbn.key,
                    packageName = sbn.packageName,
                    appName = getAppName(applicationContext, sbn.packageName),
                    title = extras.getCharSequence("android.title")?.toString(),
                    text = extras.getCharSequence("android.text")?.toString(),
                    category = notification.category,
                    postedTime = sbn.postTime,
                    removedTime = null,
                    interactedTime = null,
                    responseTime = null,
                    wasClicked = false,
                    wasCancelled = false,
                    isGroup = notification.group != null,
                    priority = notification.priority
                )
                
                database.notificationRecordDao().insert(record)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 通知移除时调用
     */
    override fun onNotificationRemoved(sbn: StatusBarNotification?, rankingMap: RankingMap?, reason: Int) {
        super.onNotificationRemoved(sbn, rankingMap, reason)
        sbn ?: return
        
        serviceScope.launch {
            try {
                val existingRecord = database.notificationRecordDao().getByKey(sbn.key)
                if (existingRecord != null) {
                    val updatedRecord = existingRecord.copy(
                        removedTime = System.currentTimeMillis(),
                        wasCancelled = reason == REASON_CANCEL || reason == REASON_CANCEL_ALL,
                        wasClicked = reason == REASON_CLICK
                    )
                    
                    // 计算响应时间
                    if (updatedRecord.wasClicked) {
                        val responseTime = (updatedRecord.removedTime ?: 0L) - updatedRecord.postedTime
                        database.notificationRecordDao().update(
                            updatedRecord.copy(
                                interactedTime = updatedRecord.removedTime,
                                responseTime = responseTime
                            )
                        )
                    } else {
                        database.notificationRecordDao().update(updatedRecord)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    override fun onListenerConnected() {
        super.onListenerConnected()
        // 服务连接成功
    }
    
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        // 服务断开连接，尝试重新请求绑定
        requestRebind(android.content.ComponentName(this, NotificationMonitorService::class.java))
    }
    
    override fun onDestroy() {
        super.onDestroy()
    }
    
    /**
     * 获取应用名称
     */
    private fun getAppName(context: Context, packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
}

