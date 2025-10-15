package com.example.time.service

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.example.time.data.model.UsageTracking
import com.example.time.data.repository.UsageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Usage Stats Collector - 使用统计数据收集器
 * 使用 UsageStatsManager 收集应用使用数据
 * 
 * 统计逻辑：
 * 1. 每次APP从后台切换到前台（MOVE_TO_FOREGROUND）→ 打开次数+1，生成新sessionId
 * 2. 每次APP从前台切换到后台（MOVE_TO_BACKGROUND）→ 保存本次使用时长
 * 3. 打开次数 = 数据库记录条数 = COUNT(*)
 * 4. 使用时长 = 所有前台时长总和 = SUM(duration)
 * 5. 后台运行时间不计入使用时间
 */
class UsageStatsCollector(
    private val context: Context,
    private val usageRepository: UsageRepository
) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager
    
    private val TAG = "UsageStatsCollector"
    
    /**
     * 收集指定时间范围的使用数据
     * 
     * 核心逻辑：
     * 1. 监听 MOVE_TO_FOREGROUND 事件 → 记录前台开始时间，生成新sessionId
     * 2. 监听 MOVE_TO_BACKGROUND 事件 → 计算使用时长并保存记录
     * 3. 每次前台切换都算一次打开，每次后台切换都保存一条记录
     */
    suspend fun collectUsageData(startTime: Long, endTime: Long) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "┌───────────────────────────────────────────────────────┐")
            Log.d(TAG, "│ 开始收集应用使用数据                                    │")
            Log.d(TAG, "├───────────────────────────────────────────────────────┤")
            Log.d(TAG, "│ 开始时间: ${Date(startTime)}")
            Log.d(TAG, "│ 结束时间: ${Date(endTime)}")
            Log.d(TAG, "│ 时间跨度: ${(endTime - startTime)/1000}秒")
            Log.d(TAG, "└───────────────────────────────────────────────────────┘")
            
            val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
            val usageRecords = mutableListOf<UsageTracking>()
            val sessionMap = mutableMapOf<String, SessionInfo>()
            
            var eventCount = 0
            var foregroundCount = 0
            var backgroundCount = 0
            val event = UsageEvents.Event()
            
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                eventCount++
                
                when (event.eventType) {
                    UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                        foregroundCount++
                        // 应用移至前台
                        handleMoveToForeground(event, sessionMap, usageRecords)
                    }
                    UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                        backgroundCount++
                        // 应用移至后台
                        handleMoveToBackground(event, sessionMap, usageRecords)
                    }
                }
            }
            
            Log.d(TAG, "┌───────────────────────────────────────────────────────┐")
            Log.d(TAG, "│ 事件统计汇总                                           │")
            Log.d(TAG, "├───────────────────────────────────────────────────────┤")
            Log.d(TAG, "│ 总事件数: $eventCount")
            Log.d(TAG, "│ 前台切换: $foregroundCount 次")
            Log.d(TAG, "│ 后台切换: $backgroundCount 次")
            Log.d(TAG, "└───────────────────────────────────────────────────────┘")
            
            // 处理未结束的会话（仍在前台的应用）
            sessionMap.values.forEach { session ->
                if (session.startTime > 0) {
                    val duration = endTime - session.startTime
                    if (duration > 0) {
                        Log.d(TAG, "结束未完成会话: ${session.packageName}, 时长: ${duration}ms")
                        usageRecords.add(createUsageRecord(session, endTime, duration))
                    }
                }
            }
            
            // ✅ 关键步骤：保存到数据库
            // 每条记录代表一次完整的APP使用（从前台到后台）
            // 打开次数 = 数据库记录条数
            // 使用时长 = duration字段的总和
            if (usageRecords.isNotEmpty()) {
                Log.d(TAG, "═══════════════════════════════════════════")
                Log.d(TAG, "💾 保存数据到数据库")
                Log.d(TAG, "   记录数量: ${usageRecords.size} 条")
                usageRecords.forEachIndexed { index, record ->
                    Log.d(TAG, "   [${index + 1}] ${record.appName}: ${record.duration/1000}秒")
                }
                usageRepository.insertUsages(usageRecords)
                Log.d(TAG, "   ✅ 数据已持久化到 Room Database")
                Log.d(TAG, "═══════════════════════════════════════════")
            } else {
                Log.d(TAG, "没有使用记录需要保存")
            }
            
            usageRecords.size
        } catch (e: Exception) {
            Log.e(TAG, "收集使用数据失败", e)
            e.printStackTrace()
            0
        }
    }
    
    /**
     * 处理应用移至前台事件
     * 
     * 核心逻辑：
     * 1. 每次前台切换都生成新的 sessionId（每次打开都是新记录）
     * 2. 记录前台开始时间
     * 3. 不再使用会话阈值判断
     */
    private fun handleMoveToForeground(
        event: UsageEvents.Event,
        sessionMap: MutableMap<String, SessionInfo>,
        usageRecords: MutableList<UsageTracking>
    ) {
        val packageName = event.packageName
        val currentTime = event.timeStamp
        val appName = getAppName(packageName)
        
        // ✅ 增强日志：显示清晰的前台切换标识
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "📱 应用切换到前台")
        Log.d(TAG, "   应用名: $appName")
        Log.d(TAG, "   包名: $packageName")
        Log.d(TAG, "   时间: ${Date(currentTime)}")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        
        // 获取或创建会话信息
        val session = sessionMap.getOrPut(packageName) {
            SessionInfo(
                packageName = packageName,
                sessionId = UUID.randomUUID().toString()
            )
        }
        
        // ✅ 新逻辑：每次前台切换都生成新的 sessionId
        // 这样每次打开APP都会创建一条新记录
        session.sessionId = UUID.randomUUID().toString()
        session.startTime = currentTime
        session.lastEventTime = currentTime
        
        Log.d(TAG, "   SessionID: ${session.sessionId}")
    }
    
    /**
     * 处理应用移至后台事件
     * 
     * 核心逻辑：
     * 1. 计算本次前台使用时长（后台时间 - 前台时间）
     * 2. 保存使用记录到数据库
     * 3. 每次后台切换都保存一条记录
     */
    private fun handleMoveToBackground(
        event: UsageEvents.Event,
        sessionMap: MutableMap<String, SessionInfo>,
        usageRecords: MutableList<UsageTracking>
    ) {
        val packageName = event.packageName
        val currentTime = event.timeStamp
        val appName = getAppName(packageName)
        
        // ✅ 增强日志：显示清晰的后台切换标识
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "🔙 应用切换到后台")
        Log.d(TAG, "   应用名: $appName")
        Log.d(TAG, "   包名: $packageName")
        Log.d(TAG, "   时间: ${Date(currentTime)}")
        
        val session = sessionMap[packageName]
        if (session == null) {
            Log.w(TAG, "   ⚠️ 未找到会话信息")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            return
        }
        
        if (session.startTime > 0) {
            val duration = currentTime - session.startTime
            val durationSeconds = duration / 1000
            val durationMinutes = durationSeconds / 60
            val remainingSeconds = durationSeconds % 60
            
            Log.d(TAG, "   使用时长: ${durationMinutes}分${remainingSeconds}秒 (${duration}ms)")
            
            // 只记录有意义的使用时长（大于1秒）
            if (duration > 1000) {
                val record = createUsageRecord(session, currentTime, duration)
                usageRecords.add(record)
                Log.d(TAG, "   ✅ 记录已添加")
                Log.d(TAG, "   SessionID: ${session.sessionId}")
            } else {
                Log.d(TAG, "   ⏭️ 时长太短，已忽略 (${duration}ms < 1秒)")
            }
            
            // 重置开始时间
            session.startTime = 0
            session.lastEventTime = currentTime
        } else {
            Log.d(TAG, "   ⚠️ 会话未开始")
        }
        
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
    
    /**
     * 创建使用记录
     * 
     * @param session 会话信息（包含packageName和sessionId）
     * @param endTime 结束时间（切换到后台的时间）
     * @param duration 使用时长（毫秒）
     * @return UsageTracking 使用记录对象
     */
    private fun createUsageRecord(
        session: SessionInfo,
        endTime: Long,
        duration: Long
    ): UsageTracking {
        val appName = getAppName(session.packageName)
        val category = getAppCategory(session.packageName)
        
        val record = UsageTracking(
            packageName = session.packageName,
            appName = appName,
            category = category,
            sessionId = session.sessionId,
            timestamp = session.startTime,
            endTimestamp = endTime,
            duration = duration,
            isForeground = true,
            screenState = "ON",
            collectionMode = "standard"
        )
        
        Log.d(TAG, "📝 创建使用记录: $appName ($category), 时长: ${duration/1000}秒, sessionId: ${session.sessionId}")
        
        return record
    }
    
    /**
     * 获取应用名称
     */
    private fun getAppName(packageName: String): String {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }
    
    /**
     * 获取应用类别
     */
    private fun getAppCategory(packageName: String): String {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            when (applicationInfo.category) {
                android.content.pm.ApplicationInfo.CATEGORY_GAME -> "游戏"
                android.content.pm.ApplicationInfo.CATEGORY_AUDIO -> "音频"
                android.content.pm.ApplicationInfo.CATEGORY_VIDEO -> "视频"
                android.content.pm.ApplicationInfo.CATEGORY_IMAGE -> "图片"
                android.content.pm.ApplicationInfo.CATEGORY_SOCIAL -> "社交"
                android.content.pm.ApplicationInfo.CATEGORY_NEWS -> "新闻"
                android.content.pm.ApplicationInfo.CATEGORY_MAPS -> "地图"
                android.content.pm.ApplicationInfo.CATEGORY_PRODUCTIVITY -> "效率"
                else -> {
                    // 根据包名猜测类别
                    when {
                        packageName.contains("browser") || packageName.contains("chrome") -> "浏览器"
                        packageName.contains("message") || packageName.contains("chat") -> "社交"
                        packageName.contains("video") || packageName.contains("youtube") -> "视频"
                        packageName.contains("music") || packageName.contains("spotify") -> "音乐"
                        packageName.contains("game") -> "游戏"
                        packageName.contains("camera") || packageName.contains("photo") -> "图片"
                        else -> "其他"
                    }
                }
            }
        } catch (e: Exception) {
            "其他"
        }
    }
    
    /**
     * 会话信息
     * 用于临时存储APP的使用状态
     * 
     * @property packageName 应用包名
     * @property sessionId 会话ID（每次前台切换生成新ID）
     * @property startTime 本次前台开始时间
     * @property lastEventTime 最后事件时间
     */
    private data class SessionInfo(
        val packageName: String,
        var sessionId: String,
        var startTime: Long = 0,
        var lastEventTime: Long = 0
    )
}

