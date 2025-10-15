package com.example.time.data.dao

import androidx.room.*
import com.example.time.data.model.ScreenEvent
import com.example.time.data.model.ScreenEventType
import kotlinx.coroutines.flow.Flow

/**
 * Screen Event DAO - 屏幕事件数据访问对象
 */
@Dao
interface ScreenEventDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: ScreenEvent): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<ScreenEvent>)
    
    @Update
    suspend fun update(event: ScreenEvent)
    
    @Delete
    suspend fun delete(event: ScreenEvent)
    
    @Query("DELETE FROM screen_events WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldRecords(beforeTimestamp: Long): Int
    
    // 查询操作
    @Query("SELECT * FROM screen_events WHERE id = :id")
    suspend fun getById(id: Long): ScreenEvent?
    
    @Query("""
        SELECT * FROM screen_events 
        WHERE timestamp >= :startTime AND timestamp <= :endTime 
        ORDER BY timestamp DESC
    """)
    suspend fun getEventsInRange(startTime: Long, endTime: Long): List<ScreenEvent>
    
    @Query("""
        SELECT * FROM screen_events 
        WHERE timestamp >= :startTime AND timestamp <= :endTime 
        ORDER BY timestamp DESC
    """)
    fun getEventsInRangeFlow(startTime: Long, endTime: Long): Flow<List<ScreenEvent>>
    
    // 按事件类型查询
    @Query("""
        SELECT * FROM screen_events 
        WHERE eventType = :eventType 
        AND timestamp >= :startTime AND timestamp <= :endTime
        ORDER BY timestamp DESC
    """)
    suspend fun getEventsByType(
        eventType: ScreenEventType,
        startTime: Long,
        endTime: Long
    ): List<ScreenEvent>
    
    // 统计解锁次数
    @Query("""
        SELECT COUNT(*) 
        FROM screen_events 
        WHERE eventType = 'USER_PRESENT' 
        AND timestamp >= :startTime AND timestamp <= :endTime
    """)
    suspend fun getUnlockCount(startTime: Long, endTime: Long): Int
    
    @Query("""
        SELECT COUNT(*) 
        FROM screen_events 
        WHERE eventType = 'USER_PRESENT' 
        AND timestamp >= :startTime AND timestamp <= :endTime
    """)
    fun getUnlockCountFlow(startTime: Long, endTime: Long): Flow<Int>
    
    // 获取屏幕开启时长
    // 注意：这需要配对 SCREEN_ON 和 SCREEN_OFF 事件来计算
    @Query("""
        SELECT * FROM screen_events 
        WHERE eventType IN ('SCREEN_ON', 'SCREEN_OFF')
        AND timestamp >= :startTime AND timestamp <= :endTime
        ORDER BY timestamp ASC
    """)
    suspend fun getScreenOnOffEvents(startTime: Long, endTime: Long): List<ScreenEvent>
    
    // 获取第一次解锁时间
    @Query("""
        SELECT MIN(timestamp) 
        FROM screen_events 
        WHERE eventType = 'USER_PRESENT' 
        AND timestamp >= :startTime AND timestamp <= :endTime
    """)
    suspend fun getFirstUnlockTime(startTime: Long, endTime: Long): Long?
    
    // 获取最后一次屏幕关闭时间
    @Query("""
        SELECT MAX(timestamp) 
        FROM screen_events 
        WHERE eventType = 'SCREEN_OFF' 
        AND timestamp >= :startTime AND timestamp <= :endTime
    """)
    suspend fun getLastScreenOffTime(startTime: Long, endTime: Long): Long?
    
    // 获取所有记录（用于导出）
    @Query("SELECT * FROM screen_events ORDER BY timestamp DESC")
    suspend fun getAllRecords(): List<ScreenEvent>
    
    @Query("SELECT COUNT(*) FROM screen_events")
    suspend fun getRecordCount(): Int
    
    @Query("DELETE FROM screen_events")
    suspend fun deleteAll()
}

