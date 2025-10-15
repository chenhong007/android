package com.example.time.data.repository

import android.util.Log
import com.example.time.data.dao.UsageTrackingDao
import com.example.time.data.model.AppUsageSummary
import com.example.time.data.model.DailyUsageSummary
import com.example.time.data.model.UsageTracking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Usage Repository - 使用跟踪数据仓库
 * 负责管理应用使用数据的存取
 */
@Singleton
class UsageRepository @Inject constructor(
    private val usageTrackingDao: UsageTrackingDao,
    private val screenEventRepository: ScreenEventRepository
) {
    
    companion object {
        private const val TAG = "UsageRepository"
    }
    
    /**
     * 插入使用记录
     * 
     * ✅ 数据持久化：每次APP从前台切换到后台时调用
     * - 每条记录 = 一次完整的APP使用
     * - 包含：packageName, duration, sessionId, timestamp等
     * - 保存到SQLite数据库（Room）
     */
    suspend fun insertUsage(usage: UsageTracking): Long = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "💾 插入单条使用记录")
            Log.d(TAG, "   应用: ${usage.appName}")
            Log.d(TAG, "   时长: ${usage.duration/1000}秒")
            Log.d(TAG, "   SessionID: ${usage.sessionId}")
            
            val id = usageTrackingDao.insert(usage)
            
            Log.d(TAG, "✅ 数据库插入成功 (ID: $id)")
            id
        } catch (e: Exception) {
            Log.e(TAG, "❌ 数据库插入失败", e)
            throw e
        }
    }
    
    /**
     * 批量插入使用记录
     * 
     * ✅ 数据持久化：DataCollectionService每5秒批量保存
     * - 一次可以保存多条记录
     * - 事务性保存，保证数据一致性
     */
    suspend fun insertUsages(usages: List<UsageTracking>) = withContext(Dispatchers.IO) {
        try {
            if (usages.isEmpty()) {
                Log.d(TAG, "⚠️ 批量插入记录为空，跳过")
                return@withContext
            }
            
            Log.d(TAG, "💾 批量插入使用记录 (数量: ${usages.size})")
            usages.forEachIndexed { index, usage ->
                Log.d(TAG, "   [${index+1}] ${usage.appName}: ${usage.duration/1000}秒")
            }
            
            usageTrackingDao.insertAll(usages)
            
            Log.d(TAG, "✅ 批量插入成功 (${usages.size} 条记录)")
        } catch (e: Exception) {
            Log.e(TAG, "❌ 批量插入失败", e)
            throw e
        }
    }
    
    /**
     * 更新使用记录
     */
    suspend fun updateUsage(usage: UsageTracking) = withContext(Dispatchers.IO) {
        usageTrackingDao.update(usage)
    }
    
    /**
     * 获取指定时间范围的使用记录
     */
    suspend fun getUsageInRange(startTime: Long, endTime: Long): List<UsageTracking> =
        withContext(Dispatchers.IO) {
            usageTrackingDao.getUsageInRange(startTime, endTime)
        }
    
    /**
     * 获取指定时间范围的使用记录（Flow）
     */
    fun getUsageInRangeFlow(startTime: Long, endTime: Long): Flow<List<UsageTracking>> =
        usageTrackingDao.getUsageInRangeFlow(startTime, endTime)
    
    /**
     * 获取应用使用汇总
     * 
     * @param totalDuration 可选参数，如果已知总时长，传入以避免重复查询
     */
    suspend fun getAppUsageSummary(
        startTime: Long, 
        endTime: Long,
        totalDuration: Long? = null
    ): List<AppUsageSummary> =
        withContext(Dispatchers.IO) {
            val summary = usageTrackingDao.getAppUsageSummary(startTime, endTime)
            
            // ✅ 性能优化：优先使用传入的总时长
            // 其次从已有的汇总数据中计算（避免额外查询）
            // 最后才查询数据库（兜底逻辑）
            val total = totalDuration 
                ?: summary.sumOf { it.totalDuration }  // ← 从汇总数据计算，避免查询
                ?: 0L
            
            // 计算每个应用的使用百分比
            summary.map { app ->
                app.copy(
                    percentage = if (total > 0) {
                        (app.totalDuration.toFloat() / total * 100)
                    } else {
                        0f
                    }
                )
            }
        }
    
    /**
     * 获取应用使用汇总（Flow）
     */
    fun getAppUsageSummaryFlow(startTime: Long, endTime: Long): Flow<List<AppUsageSummary>> =
        usageTrackingDao.getAppUsageSummaryFlow(startTime, endTime)
    
    /**
     * 获取特定应用的使用记录
     */
    suspend fun getAppUsage(
        packageName: String,
        startTime: Long,
        endTime: Long
    ): List<UsageTracking> = withContext(Dispatchers.IO) {
        usageTrackingDao.getAppUsage(packageName, startTime, endTime)
    }
    
    /**
     * 获取总使用时长
     * 
     * ✅ 从数据库查询：SUM(duration)
     * - 统计指定时间范围内所有APP的使用时长总和
     * - 单位：毫秒
     */
    suspend fun getTotalDuration(startTime: Long, endTime: Long): Long =
        withContext(Dispatchers.IO) {
            usageTrackingDao.getTotalDuration(startTime, endTime)
        }
    
    /**
     * 获取总使用时长（Flow）
     */
    fun getTotalDurationFlow(startTime: Long, endTime: Long): Flow<Long> =
        usageTrackingDao.getTotalDurationFlow(startTime, endTime)
    
    /**
     * 获取特定应用的总使用时长
     */
    suspend fun getAppTotalDuration(
        packageName: String,
        startTime: Long,
        endTime: Long
    ): Long = withContext(Dispatchers.IO) {
        usageTrackingDao.getAppTotalDuration(packageName, startTime, endTime)
    }
    
    /**
     * 获取会话数量
     */
    suspend fun getSessionCount(startTime: Long, endTime: Long): Int =
        withContext(Dispatchers.IO) {
            usageTrackingDao.getSessionCount(startTime, endTime)
        }
    
    /**
     * 获取最常用的应用（Top N）
     * 
     * ✅ 从数据库查询：
     * - 打开次数：COUNT(*) = 记录条数
     * - 使用时长：SUM(duration)
     * - 百分比：在内存中计算（使用时长 / 总时长 * 100）
     * 
     * @param totalDuration 可选参数，如果已知总时长，传入以避免重复查询
     */
    suspend fun getTopApps(
        startTime: Long, 
        endTime: Long, 
        limit: Int = 5,
        totalDuration: Long? = null
    ): List<AppUsageSummary> =
        withContext(Dispatchers.IO) {
            // 步骤1：从数据库查询Top N应用
            val apps = usageTrackingDao.getTopApps(startTime, endTime, limit)
            
            // 步骤2：获取总时长（使用传入的值或查询数据库）
            val total = totalDuration ?: usageTrackingDao.getTotalDuration(startTime, endTime)
            
            // 步骤3：计算百分比（内存计算）
            apps.map { app ->
                app.copy(
                    percentage = if (total > 0) {
                        (app.totalDuration.toFloat() / total * 100)
                    } else {
                        0f
                    }
                )
            }
        }
    
    /**
     * 获取最常用的应用（Top N，Flow）
     */
    fun getTopAppsFlow(startTime: Long, endTime: Long, limit: Int = 5): Flow<List<AppUsageSummary>> =
        usageTrackingDao.getTopAppsFlow(startTime, endTime, limit)
    
    /**
     * 按类别获取使用汇总
     */
    suspend fun getCategoryUsageSummary(startTime: Long, endTime: Long): List<AppUsageSummary> =
        withContext(Dispatchers.IO) {
            val summary = usageTrackingDao.getCategoryUsageSummary(startTime, endTime)
            
            // ✅ 性能优化：从汇总数据计算总时长，避免额外查询
            val totalDuration = summary.sumOf { it.totalDuration }
            
            // 计算百分比
            summary.map { category ->
                category.copy(
                    percentage = if (totalDuration > 0) {
                        (category.totalDuration.toFloat() / totalDuration * 100)
                    } else {
                        0f
                    }
                )
            }
        }
    
    /**
     * 删除旧记录（数据清理）
     */
    suspend fun deleteOldRecords(beforeTimestamp: Long): Int = withContext(Dispatchers.IO) {
        usageTrackingDao.deleteOldRecords(beforeTimestamp)
    }
    
    /**
     * 获取所有记录（用于导出）
     */
    suspend fun getAllRecords(): List<UsageTracking> = withContext(Dispatchers.IO) {
        usageTrackingDao.getAllRecords()
    }
    
    /**
     * 获取记录总数
     */
    suspend fun getRecordCount(): Int = withContext(Dispatchers.IO) {
        usageTrackingDao.getRecordCount()
    }
    
    /**
     * 清空所有数据（谨慎使用）
     */
    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        usageTrackingDao.deleteAll()
    }
    
    /**
     * 获取应用使用汇总列表（用于图表）
     */
    suspend fun getAppUsageSummaries(startTime: Long, endTime: Long): List<AppUsageSummary> =
        withContext(Dispatchers.IO) {
            getAppUsageSummary(startTime, endTime)
        }
    
    /**
     * 获取每日使用汇总列表（用于图表）
     */
    suspend fun getDailyUsageSummaries(startTime: Long, endTime: Long): List<DailyUsageSummary> =
        withContext(Dispatchers.IO) {
            val usages = usageTrackingDao.getUsageInRange(startTime, endTime)
            
            val summaries: List<DailyUsageSummary> = usages
                .groupBy { usage ->
                    val calendar = java.util.Calendar.getInstance()
                    calendar.timeInMillis = usage.timestamp
                    String.format(
                        "%04d-%02d-%02d",
                        calendar.get(java.util.Calendar.YEAR),
                        calendar.get(java.util.Calendar.MONTH) + 1,
                        calendar.get(java.util.Calendar.DAY_OF_MONTH)
                    )
                }
                .map { (date, dayUsages) ->
                    val totalDuration = dayUsages.sumOf { it.duration }
                    
                    // 解析日期字符串以获取当天的起止时间
                    val dateParts = date.split("-")
                    val calendar = java.util.Calendar.getInstance()
                    calendar.set(dateParts[0].toInt(), dateParts[1].toInt() - 1, dateParts[2].toInt())
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                    calendar.set(java.util.Calendar.MINUTE, 0)
                    calendar.set(java.util.Calendar.SECOND, 0)
                    calendar.set(java.util.Calendar.MILLISECOND, 0)
                    val dayStart = calendar.timeInMillis
                    
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
                    calendar.set(java.util.Calendar.MINUTE, 59)
                    calendar.set(java.util.Calendar.SECOND, 59)
                    calendar.set(java.util.Calendar.MILLISECOND, 999)
                    val dayEnd = calendar.timeInMillis
                    
                    // 获取真实的解锁次数（从ScreenEventRepository）
                    val unlockCount = screenEventRepository.getUnlockCount(dayStart, dayEnd)
                    
                    // 获取真实的屏幕开启时长（从ScreenEventRepository）
                    val screenOnDuration = screenEventRepository.calculateScreenOnDuration(dayStart, dayEnd)
                    
                    val topApps: List<AppUsageSummary> = dayUsages
                        .groupBy { it.packageName }
                        .map { (pkg, usagesForApp) ->
                            val totalDur = usagesForApp.sumOf { it.duration }
                            val appName = usagesForApp.first().appName
                            val category = usagesForApp.first().category
                            val lastUsed = usagesForApp.maxOf { it.timestamp }
                            AppUsageSummary(
                                packageName = pkg,
                                appName = appName,
                                category = category,
                                totalDuration = totalDur,
                                // ✅ 修复：打开次数 = 记录条数，不应该去重 sessionId
                                sessionCount = usagesForApp.size,
                                lastUsedTimestamp = lastUsed
                            )
                        }
                        .sortedByDescending { it.totalDuration }
                        .take(5)
                    
                    DailyUsageSummary(
                        date = date,
                        totalDuration = totalDuration,
                        unlockCount = unlockCount,
                        screenOnDuration = screenOnDuration,
                        topApps = topApps
                    )
                }
            
            summaries
        }
    
    /**
     * 获取时间范围内的使用记录（用于图表）
     */
    suspend fun getUsageInTimeRange(startTime: Long, endTime: Long): List<UsageTracking> =
        withContext(Dispatchers.IO) {
            usageTrackingDao.getUsageInRange(startTime, endTime)
        }
}

