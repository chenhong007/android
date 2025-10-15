package com.example.time.data.dao

import androidx.room.*
import com.example.time.data.model.NotificationRecord
import com.example.time.data.model.NotificationStatistics
import kotlinx.coroutines.flow.Flow

/**
 * Notification Record DAO - 通知记录数据访问对象
 */
@Dao
interface NotificationRecordDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: NotificationRecord): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<NotificationRecord>)
    
    @Update
    suspend fun update(record: NotificationRecord)
    
    @Delete
    suspend fun delete(record: NotificationRecord)
    
    @Query("DELETE FROM notification_records WHERE postedTime < :beforeTimestamp")
    suspend fun deleteOldRecords(beforeTimestamp: Long): Int
    
    // 查询操作
    @Query("SELECT * FROM notification_records WHERE id = :id")
    suspend fun getById(id: Long): NotificationRecord?
    
    @Query("SELECT * FROM notification_records WHERE notificationKey = :key")
    suspend fun getByKey(key: String): NotificationRecord?
    
    @Query("""
        SELECT * FROM notification_records 
        WHERE postedTime >= :startTime AND postedTime <= :endTime 
        ORDER BY postedTime DESC
    """)
    suspend fun getNotificationsInRange(startTime: Long, endTime: Long): List<NotificationRecord>
    
    @Query("""
        SELECT * FROM notification_records 
        WHERE postedTime >= :startTime AND postedTime <= :endTime 
        ORDER BY postedTime DESC
    """)
    fun getNotificationsInRangeFlow(startTime: Long, endTime: Long): Flow<List<NotificationRecord>>
    
    // 按应用查询
    @Query("""
        SELECT * FROM notification_records 
        WHERE packageName = :packageName 
        AND postedTime >= :startTime AND postedTime <= :endTime
        ORDER BY postedTime DESC
    """)
    suspend fun getAppNotifications(
        packageName: String,
        startTime: Long,
        endTime: Long
    ): List<NotificationRecord>
    
    // 统计信息
    @Query("""
        SELECT 
            packageName,
            appName,
            COUNT(*) as totalCount,
            SUM(CASE WHEN wasClicked THEN 1 ELSE 0 END) as clickedCount,
            SUM(CASE WHEN wasCancelled THEN 1 ELSE 0 END) as cancelledCount,
            AVG(COALESCE(responseTime, 0)) as averageResponseTime,
            CAST(SUM(CASE WHEN interactedTime IS NOT NULL THEN 1 ELSE 0 END) AS REAL) / COUNT(*) as interactionRate
        FROM notification_records
        WHERE postedTime >= :startTime AND postedTime <= :endTime
        GROUP BY packageName
        ORDER BY totalCount DESC
    """)
    suspend fun getNotificationStatistics(
        startTime: Long,
        endTime: Long
    ): List<NotificationStatistics>
    
    @Query("""
        SELECT 
            packageName,
            appName,
            COUNT(*) as totalCount,
            SUM(CASE WHEN wasClicked THEN 1 ELSE 0 END) as clickedCount,
            SUM(CASE WHEN wasCancelled THEN 1 ELSE 0 END) as cancelledCount,
            AVG(COALESCE(responseTime, 0)) as averageResponseTime,
            CAST(SUM(CASE WHEN interactedTime IS NOT NULL THEN 1 ELSE 0 END) AS REAL) / COUNT(*) as interactionRate
        FROM notification_records
        WHERE postedTime >= :startTime AND postedTime <= :endTime
        GROUP BY packageName
        ORDER BY totalCount DESC
    """)
    fun getNotificationStatisticsFlow(
        startTime: Long,
        endTime: Long
    ): Flow<List<NotificationStatistics>>
    
    // 获取通知总数
    @Query("""
        SELECT COUNT(*) 
        FROM notification_records 
        WHERE postedTime >= :startTime AND postedTime <= :endTime
    """)
    suspend fun getNotificationCount(startTime: Long, endTime: Long): Int
    
    @Query("""
        SELECT COUNT(*) 
        FROM notification_records 
        WHERE postedTime >= :startTime AND postedTime <= :endTime
    """)
    fun getNotificationCountFlow(startTime: Long, endTime: Long): Flow<Int>
    
    // 获取特定应用的通知数量
    @Query("""
        SELECT COUNT(*) 
        FROM notification_records 
        WHERE packageName = :packageName 
        AND postedTime >= :startTime AND postedTime <= :endTime
    """)
    suspend fun getAppNotificationCount(
        packageName: String,
        startTime: Long,
        endTime: Long
    ): Int
    
    // 获取交互率最高的应用
    @Query("""
        SELECT 
            packageName,
            appName,
            COUNT(*) as totalCount,
            SUM(CASE WHEN wasClicked THEN 1 ELSE 0 END) as clickedCount,
            SUM(CASE WHEN wasCancelled THEN 1 ELSE 0 END) as cancelledCount,
            AVG(COALESCE(responseTime, 0)) as averageResponseTime,
            CAST(SUM(CASE WHEN interactedTime IS NOT NULL THEN 1 ELSE 0 END) AS REAL) / COUNT(*) as interactionRate
        FROM notification_records
        WHERE postedTime >= :startTime AND postedTime <= :endTime
        GROUP BY packageName
        HAVING COUNT(*) >= 5
        ORDER BY interactionRate DESC
        LIMIT :limit
    """)
    suspend fun getTopInteractionApps(
        startTime: Long,
        endTime: Long,
        limit: Int
    ): List<NotificationStatistics>
    
    // 获取所有记录（用于导出）
    @Query("SELECT * FROM notification_records ORDER BY postedTime DESC")
    suspend fun getAllRecords(): List<NotificationRecord>
    
    @Query("SELECT COUNT(*) FROM notification_records")
    suspend fun getRecordCount(): Int
    
    @Query("DELETE FROM notification_records")
    suspend fun deleteAll()
}

