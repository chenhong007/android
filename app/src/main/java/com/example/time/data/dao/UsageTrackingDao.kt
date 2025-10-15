package com.example.time.data.dao

import androidx.room.*
import com.example.time.data.model.AppUsageSummary
import com.example.time.data.model.UsageTracking
import kotlinx.coroutines.flow.Flow

/**
 * Usage Tracking DAO - 使用跟踪数据访问对象
 */
@Dao
interface UsageTrackingDao {
    
    // 插入操作
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usage: UsageTracking): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(usages: List<UsageTracking>)
    
    // 更新操作
    @Update
    suspend fun update(usage: UsageTracking)
    
    // 删除操作
    @Delete
    suspend fun delete(usage: UsageTracking)
    
    @Query("DELETE FROM usage_tracking WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldRecords(beforeTimestamp: Long): Int
    
    // 查询单条记录
    @Query("SELECT * FROM usage_tracking WHERE id = :id")
    suspend fun getById(id: Long): UsageTracking?
    
    @Query("SELECT * FROM usage_tracking WHERE sessionId = :sessionId")
    suspend fun getBySessionId(sessionId: String): List<UsageTracking>
    
    // 查询指定时间范围的记录
    @Query("""
        SELECT * FROM usage_tracking 
        WHERE timestamp >= :startTime AND timestamp <= :endTime 
        ORDER BY timestamp DESC
    """)
    suspend fun getUsageInRange(startTime: Long, endTime: Long): List<UsageTracking>
    
    @Query("""
        SELECT * FROM usage_tracking 
        WHERE timestamp >= :startTime AND timestamp <= :endTime 
        ORDER BY timestamp DESC
    """)
    fun getUsageInRangeFlow(startTime: Long, endTime: Long): Flow<List<UsageTracking>>
    
    // 按应用汇总使用情况
    // 注意：sessionCount = COUNT(*) 表示打开次数（每条记录代表一次打开）
    @Query("""
        SELECT 
            packageName,
            appName,
            category,
            SUM(duration) as totalDuration,
            COUNT(*) as sessionCount,
            MAX(timestamp) as lastUsedTimestamp,
            0.0 as percentage
        FROM usage_tracking
        WHERE timestamp >= :startTime AND timestamp <= :endTime
        GROUP BY packageName
        ORDER BY totalDuration DESC
    """)
    suspend fun getAppUsageSummary(startTime: Long, endTime: Long): List<AppUsageSummary>
    
    @Query("""
        SELECT 
            packageName,
            appName,
            category,
            SUM(duration) as totalDuration,
            COUNT(*) as sessionCount,
            MAX(timestamp) as lastUsedTimestamp,
            0.0 as percentage
        FROM usage_tracking
        WHERE timestamp >= :startTime AND timestamp <= :endTime
        GROUP BY packageName
        ORDER BY totalDuration DESC
    """)
    fun getAppUsageSummaryFlow(startTime: Long, endTime: Long): Flow<List<AppUsageSummary>>
    
    // 获取特定应用的使用记录
    @Query("""
        SELECT * FROM usage_tracking 
        WHERE packageName = :packageName 
        AND timestamp >= :startTime AND timestamp <= :endTime
        ORDER BY timestamp DESC
    """)
    suspend fun getAppUsage(
        packageName: String,
        startTime: Long,
        endTime: Long
    ): List<UsageTracking>
    
    // 获取总使用时长
    @Query("""
        SELECT COALESCE(SUM(duration), 0) 
        FROM usage_tracking 
        WHERE timestamp >= :startTime AND timestamp <= :endTime
    """)
    suspend fun getTotalDuration(startTime: Long, endTime: Long): Long
    
    @Query("""
        SELECT COALESCE(SUM(duration), 0) 
        FROM usage_tracking 
        WHERE timestamp >= :startTime AND timestamp <= :endTime
    """)
    fun getTotalDurationFlow(startTime: Long, endTime: Long): Flow<Long>
    
    // 获取指定应用的总使用时长
    @Query("""
        SELECT COALESCE(SUM(duration), 0) 
        FROM usage_tracking 
        WHERE packageName = :packageName 
        AND timestamp >= :startTime AND timestamp <= :endTime
    """)
    suspend fun getAppTotalDuration(
        packageName: String,
        startTime: Long,
        endTime: Long
    ): Long
    
    // 获取打开次数总和（所有应用的总打开次数）
    @Query("""
        SELECT COUNT(*) 
        FROM usage_tracking 
        WHERE timestamp >= :startTime AND timestamp <= :endTime
    """)
    suspend fun getSessionCount(startTime: Long, endTime: Long): Int
    
    // 获取最常用的应用（Top N）
    // 注意：sessionCount = COUNT(*) 表示打开次数
    @Query("""
        SELECT 
            packageName,
            appName,
            category,
            SUM(duration) as totalDuration,
            COUNT(*) as sessionCount,
            MAX(timestamp) as lastUsedTimestamp,
            0.0 as percentage
        FROM usage_tracking
        WHERE timestamp >= :startTime AND timestamp <= :endTime
        GROUP BY packageName
        ORDER BY totalDuration DESC
        LIMIT :limit
    """)
    suspend fun getTopApps(startTime: Long, endTime: Long, limit: Int): List<AppUsageSummary>
    
    @Query("""
        SELECT 
            packageName,
            appName,
            category,
            SUM(duration) as totalDuration,
            COUNT(*) as sessionCount,
            MAX(timestamp) as lastUsedTimestamp,
            0.0 as percentage
        FROM usage_tracking
        WHERE timestamp >= :startTime AND timestamp <= :endTime
        GROUP BY packageName
        ORDER BY totalDuration DESC
        LIMIT :limit
    """)
    fun getTopAppsFlow(startTime: Long, endTime: Long, limit: Int): Flow<List<AppUsageSummary>>
    
    // 按类别汇总
    // 注意：sessionCount = COUNT(*) 表示打开次数
    @Query("""
        SELECT 
            category as packageName,
            category as appName,
            category,
            SUM(duration) as totalDuration,
            COUNT(*) as sessionCount,
            MAX(timestamp) as lastUsedTimestamp,
            0.0 as percentage
        FROM usage_tracking
        WHERE timestamp >= :startTime AND timestamp <= :endTime
        GROUP BY category
        ORDER BY totalDuration DESC
    """)
    suspend fun getCategoryUsageSummary(startTime: Long, endTime: Long): List<AppUsageSummary>
    
    // 获取所有记录（用于导出）
    @Query("SELECT * FROM usage_tracking ORDER BY timestamp DESC")
    suspend fun getAllRecords(): List<UsageTracking>
    
    // 获取记录总数
    @Query("SELECT COUNT(*) FROM usage_tracking")
    suspend fun getRecordCount(): Int
    
    // 清空所有数据
    @Query("DELETE FROM usage_tracking")
    suspend fun deleteAll()
}

