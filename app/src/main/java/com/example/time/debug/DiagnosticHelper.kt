package com.example.time.debug

import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import android.util.Log
import com.example.time.data.repository.UsageRepository
import com.example.time.service.DataCollectionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * 诊断助手 - 帮助排查统计系统问题
 * 
 * 使用方法：
 * 1. 在 HomeViewModel 或 HomeScreen 中调用
 * 2. 查看 Logcat 日志（过滤 "Diagnostic"）
 */
object DiagnosticHelper {
    
    private const val TAG = "Diagnostic"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    /**
     * 完整诊断 - 检查所有关键点
     */
    suspend fun runFullDiagnostic(context: Context, usageRepository: UsageRepository) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "开始完整诊断")
        Log.d(TAG, "========================================")
        
        // 1. 检查权限
        checkPermissions(context)
        
        // 2. 检查服务状态
        checkServiceStatus(context)
        
        // 3. 检查数据库
        checkDatabase(usageRepository)
        
        // 4. 检查系统事件
        checkSystemEvents(context)
        
        // 5. 检查时间范围
        checkTimeRange()
        
        Log.d(TAG, "========================================")
        Log.d(TAG, "诊断完成")
        Log.d(TAG, "========================================")
    }
    
    /**
     * 检查1：权限状态
     */
    private fun checkPermissions(context: Context) {
        Log.d(TAG, "\n=== 检查1: 权限状态 ===")
        
        // 检查 PACKAGE_USAGE_STATS 权限
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        
        val hasUsageStatsPermission = mode == AppOpsManager.MODE_ALLOWED
        
        if (hasUsageStatsPermission) {
            Log.d(TAG, "✅ PACKAGE_USAGE_STATS 权限已授予")
        } else {
            Log.e(TAG, "❌ PACKAGE_USAGE_STATS 权限未授予")
            Log.e(TAG, "   请前往: 设置 → 应用 → Time → 使用权限访问")
        }
        
        // 检查通知权限（Android 13+）
        // 这里简化处理，实际应用中需要更详细的检查
        Log.d(TAG, "ℹ️  请确认已授予通知权限（前台服务需要）")
    }
    
    /**
     * 检查2：服务状态
     */
    private fun checkServiceStatus(context: Context) {
        Log.d(TAG, "\n=== 检查2: 服务状态 ===")
        
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = activityManager.getRunningServices(Int.MAX_VALUE)
        
        val serviceName = DataCollectionService::class.java.name
        val isServiceRunning = runningServices.any { it.service.className == serviceName }
        
        if (isServiceRunning) {
            Log.d(TAG, "✅ DataCollectionService 正在运行")
        } else {
            Log.e(TAG, "❌ DataCollectionService 未运行")
            Log.e(TAG, "   可能原因:")
            Log.e(TAG, "   1. 应用刚启动，服务尚未开始")
            Log.e(TAG, "   2. 服务已崩溃")
            Log.e(TAG, "   3. 服务被系统杀掉")
            Log.e(TAG, "   解决方法: 重启应用")
        }
    }
    
    /**
     * 检查3：数据库状态
     */
    private suspend fun checkDatabase(usageRepository: UsageRepository) = withContext(Dispatchers.IO) {
        Log.d(TAG, "\n=== 检查3: 数据库状态 ===")
        
        try {
            // 检查数据库中的总记录数
            val totalRecords = usageRepository.getRecordCount()
            Log.d(TAG, "数据库总记录数: $totalRecords 条")
            
            if (totalRecords == 0) {
                Log.w(TAG, "⚠️ 数据库为空！")
                Log.w(TAG, "   可能原因:")
                Log.w(TAG, "   1. 刚安装应用，还没有收集到数据")
                Log.w(TAG, "   2. 权限未授予，无法收集数据")
                Log.w(TAG, "   3. 服务未运行")
            } else {
                Log.d(TAG, "✅ 数据库包含 $totalRecords 条记录")
                
                // 显示最近5条记录
                val allRecords = usageRepository.getAllRecords()
                Log.d(TAG, "\n最近5条记录:")
                allRecords.take(5).forEachIndexed { index, record ->
                    Log.d(TAG, "  ${index + 1}. ${record.appName}")
                    Log.d(TAG, "     包名: ${record.packageName}")
                    Log.d(TAG, "     时长: ${record.duration / 1000}秒")
                    Log.d(TAG, "     开始: ${dateFormat.format(Date(record.timestamp))}")
                    Log.d(TAG, "     结束: ${dateFormat.format(Date(record.endTimestamp))}")
                    Log.d(TAG, "     SessionId: ${record.sessionId}")
                }
            }
            
            // 检查今日数据
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val todayStart = calendar.timeInMillis
            val todayEnd = System.currentTimeMillis()
            
            val todayRecords = usageRepository.getUsageInRange(todayStart, todayEnd)
            Log.d(TAG, "\n今日记录数: ${todayRecords.size} 条")
            
            if (todayRecords.isEmpty()) {
                Log.w(TAG, "⚠️ 今日没有记录！")
                Log.w(TAG, "   可能原因:")
                Log.w(TAG, "   1. 今天还没有使用其他应用（超过1秒）")
                Log.w(TAG, "   2. 时间范围计算错误")
                Log.w(TAG, "   3. 数据收集未启动")
            } else {
                Log.d(TAG, "✅ 今日有 ${todayRecords.size} 条记录")
                
                // 统计今日数据
                val todayDuration = usageRepository.getTotalDuration(todayStart, todayEnd)
                val topApps = usageRepository.getTopApps(todayStart, todayEnd, 5)
                
                Log.d(TAG, "\n今日统计:")
                Log.d(TAG, "  总时长: ${todayDuration / 1000 / 60}分钟")
                Log.d(TAG, "  Top 5应用:")
                topApps.forEachIndexed { index, app ->
                    Log.d(TAG, "    ${index + 1}. ${app.appName}: ${app.totalDuration / 1000 / 60}分钟, 打开${app.sessionCount}次")
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ 数据库检查失败", e)
        }
    }
    
    /**
     * 检查4：系统事件
     */
    private fun checkSystemEvents(context: Context) {
        Log.d(TAG, "\n=== 检查4: 系统事件 ===")
        
        try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            
            // 查询最近5分钟的事件
            val endTime = System.currentTimeMillis()
            val startTime = endTime - 5 * 60 * 1000 // 5分钟前
            
            val events = usageStatsManager.queryEvents(startTime, endTime)
            var eventCount = 0
            var foregroundCount = 0
            var backgroundCount = 0
            
            val event = android.app.usage.UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                eventCount++
                
                when (event.eventType) {
                    android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND -> foregroundCount++
                    android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND -> backgroundCount++
                }
            }
            
            Log.d(TAG, "最近5分钟系统事件:")
            Log.d(TAG, "  总事件数: $eventCount")
            Log.d(TAG, "  前台切换: $foregroundCount 次")
            Log.d(TAG, "  后台切换: $backgroundCount 次")
            
            if (eventCount == 0) {
                Log.w(TAG, "⚠️ 最近5分钟没有系统事件")
                Log.w(TAG, "   可能原因: 手机一直在当前应用中，没有切换")
            } else {
                Log.d(TAG, "✅ 系统事件正常")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ 系统事件检查失败", e)
            Log.e(TAG, "   可能原因: 权限未授予")
        }
    }
    
    /**
     * 检查5：时间范围
     */
    private fun checkTimeRange() {
        Log.d(TAG, "\n=== 检查5: 时间范围 ===")
        
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val todayEnd = calendar.timeInMillis
        
        val now = System.currentTimeMillis()
        
        Log.d(TAG, "今日时间范围:")
        Log.d(TAG, "  开始: ${dateFormat.format(Date(todayStart))}")
        Log.d(TAG, "  结束: ${dateFormat.format(Date(todayEnd))}")
        Log.d(TAG, "  当前: ${dateFormat.format(Date(now))}")
        
        if (now < todayStart || now > todayEnd) {
            Log.e(TAG, "❌ 时间范围异常！当前时间不在今日范围内")
            Log.e(TAG, "   可能原因: 系统时间设置错误")
        } else {
            Log.d(TAG, "✅ 时间范围正常")
        }
    }
    
    /**
     * 快速诊断 - 只检查关键问题
     */
    suspend fun quickDiagnostic(context: Context, usageRepository: UsageRepository): DiagnosticResult {
        val hasPermission = checkUsageStatsPermission(context)
        val isServiceRunning = checkServiceRunning(context)
        val recordCount = withContext(Dispatchers.IO) { usageRepository.getRecordCount() }
        
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis
        val todayEnd = System.currentTimeMillis()
        
        val todayRecordCount = withContext(Dispatchers.IO) {
            usageRepository.getUsageInRange(todayStart, todayEnd).size
        }
        
        return DiagnosticResult(
            hasPermission = hasPermission,
            isServiceRunning = isServiceRunning,
            totalRecordCount = recordCount,
            todayRecordCount = todayRecordCount
        )
    }
    
    private fun checkUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
    
    private fun checkServiceRunning(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = activityManager.getRunningServices(Int.MAX_VALUE)
        val serviceName = DataCollectionService::class.java.name
        return runningServices.any { it.service.className == serviceName }
    }
}

/**
 * 诊断结果
 */
data class DiagnosticResult(
    val hasPermission: Boolean,
    val isServiceRunning: Boolean,
    val totalRecordCount: Int,
    val todayRecordCount: Int
) {
    fun isHealthy(): Boolean {
        return hasPermission && isServiceRunning && totalRecordCount > 0
    }
    
    fun getErrorMessage(): String? {
        return when {
            !hasPermission -> "请授予使用权限访问"
            !isServiceRunning -> "数据收集服务未运行，请重启应用"
            totalRecordCount == 0 -> "数据库为空，请先使用其他应用几分钟"
            todayRecordCount == 0 -> "今日没有数据，请先使用其他应用几分钟"
            else -> null
        }
    }
}

