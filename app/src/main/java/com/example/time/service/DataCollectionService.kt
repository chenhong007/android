package com.example.time.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.time.R
import com.example.time.data.database.TimeDatabase
import com.example.time.data.repository.UsageRepository
import com.example.time.data.repository.ScreenEventRepository
import kotlinx.coroutines.*
import java.util.*

/**
 * Data Collection Service - 数据收集服务
 * 前台服务，持续收集使用数据
 */
class DataCollectionService : Service() {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var usageStatsCollector: UsageStatsCollector
    private lateinit var screenEventReceiver: ScreenEventReceiver
    private var collectionJob: Job? = null
    
    // 收集间隔（标准模式：5秒）
    private val collectionInterval = 5000L
    
    override fun onCreate() {
        super.onCreate()
        
        android.util.Log.d("DataCollectionService", "═══════════════════════════════════════════════════════")
        android.util.Log.d("DataCollectionService", "🚀 数据收集服务启动中...")
        android.util.Log.d("DataCollectionService", "═══════════════════════════════════════════════════════")
        
        // 初始化数据库和收集器
        val database = TimeDatabase.getInstance(applicationContext)
        val screenEventRepository = ScreenEventRepository(database.screenEventDao())
        val usageRepository = UsageRepository(database.usageTrackingDao(), screenEventRepository)
        usageStatsCollector = UsageStatsCollector(applicationContext, usageRepository)
        screenEventReceiver = ScreenEventReceiver(database.screenEventDao())
        
        android.util.Log.d("DataCollectionService", "✅ 数据库和收集器初始化完成")
        
        // 注册屏幕事件监听器
        try {
            registerReceiver(screenEventReceiver, ScreenEventReceiver.createIntentFilter())
            android.util.Log.d("DataCollectionService", "✅ 屏幕事件监听器注册成功")
            android.util.Log.d("DataCollectionService", "   监听事件: SCREEN_ON, SCREEN_OFF, USER_PRESENT")
        } catch (e: Exception) {
            android.util.Log.e("DataCollectionService", "❌ 屏幕事件监听器注册失败", e)
        }
        
        // 创建通知渠道
        createNotificationChannel()
        android.util.Log.d("DataCollectionService", "✅ 通知渠道创建完成")
        
        // 启动前台服务
        startForeground(NOTIFICATION_ID, createNotification())
        android.util.Log.d("DataCollectionService", "✅ 前台服务启动成功")
        
        // 开始数据收集
        startDataCollection()
        android.util.Log.d("DataCollectionService", "✅ 数据收集任务启动成功")
        android.util.Log.d("DataCollectionService", "   收集间隔: ${collectionInterval}ms (${collectionInterval/1000}秒)")
        android.util.Log.d("DataCollectionService", "═══════════════════════════════════════════════════════")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        android.util.Log.d("DataCollectionService", "═══════════════════════════════════════════════════════")
        android.util.Log.d("DataCollectionService", "🛑 数据收集服务停止中...")
        
        // 停止数据收集
        collectionJob?.cancel()
        android.util.Log.d("DataCollectionService", "✅ 数据收集任务已取消")
        
        // 取消注册接收器
        try {
            unregisterReceiver(screenEventReceiver)
            android.util.Log.d("DataCollectionService", "✅ 屏幕事件监听器已注销")
        } catch (e: Exception) {
            android.util.Log.e("DataCollectionService", "❌ 屏幕事件监听器注销失败", e)
            e.printStackTrace()
        }
        
        serviceScope.cancel()
        android.util.Log.d("DataCollectionService", "✅ 服务已完全停止")
        android.util.Log.d("DataCollectionService", "═══════════════════════════════════════════════════════")
    }
    
    /**
     * 开始数据收集
     */
    private fun startDataCollection() {
        collectionJob = serviceScope.launch {
            var lastCollectionTime = System.currentTimeMillis() - collectionInterval
            var collectionCount = 0
            
            android.util.Log.d("DataCollectionService", "🔄 数据收集循环启动")
            android.util.Log.d("DataCollectionService", "   初始时间: ${java.util.Date(lastCollectionTime)}")
            
            while (isActive) {
                try {
                    collectionCount++
                    val currentTime = System.currentTimeMillis()
                    
                    android.util.Log.d("DataCollectionService", "───────────────────────────────────────────────────────")
                    android.util.Log.d("DataCollectionService", "📊 第 $collectionCount 次数据收集")
                    android.util.Log.d("DataCollectionService", "   开始时间: ${java.util.Date(lastCollectionTime)}")
                    android.util.Log.d("DataCollectionService", "   结束时间: ${java.util.Date(currentTime)}")
                    android.util.Log.d("DataCollectionService", "   时间跨度: ${(currentTime - lastCollectionTime)/1000}秒")
                    
                    // 收集上一个间隔的数据
                    val recordCount = usageStatsCollector.collectUsageData(lastCollectionTime, currentTime)
                    
                    android.util.Log.d("DataCollectionService", "   收集到记录数: $recordCount 条")
                    android.util.Log.d("DataCollectionService", "───────────────────────────────────────────────────────")
                    
                    lastCollectionTime = currentTime
                    
                    // 等待下一个收集周期
                    delay(collectionInterval)
                } catch (e: Exception) {
                    android.util.Log.e("DataCollectionService", "❌ 数据收集过程出错", e)
                    e.printStackTrace()
                    delay(collectionInterval)
                }
            }
        }
    }
    
    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "数据收集服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "后台收集应用使用数据"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 创建通知
     */
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("时间都去哪了")
            .setContentText("正在记录您的使用数据")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
    
    companion object {
        private const val CHANNEL_ID = "data_collection_channel"
        private const val NOTIFICATION_ID = 1001
    }
}

