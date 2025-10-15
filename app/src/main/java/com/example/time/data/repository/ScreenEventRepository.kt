package com.example.time.data.repository

import com.example.time.data.dao.ScreenEventDao
import com.example.time.data.model.ScreenEvent
import com.example.time.data.model.ScreenEventType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ScreenEventRepository - 屏幕事件仓库
 * 
 * 封装 ScreenEventDao，提供屏幕事件的业务逻辑层
 * 包含解锁次数计算、屏幕开启时长统计、使用模式分析等功能
 */
@Singleton
class ScreenEventRepository @Inject constructor(
    private val screenEventDao: ScreenEventDao
) {
    
    // ==================== 基础 CRUD 操作 ====================
    
    /**
     * 插入屏幕事件
     */
    suspend fun insertScreenEvent(event: ScreenEvent): Long {
        return screenEventDao.insert(event)
    }
    
    /**
     * 批量插入屏幕事件
     */
    suspend fun insertScreenEvents(events: List<ScreenEvent>) {
        screenEventDao.insertAll(events)
    }
    
    /**
     * 更新屏幕事件
     */
    suspend fun updateScreenEvent(event: ScreenEvent) {
        screenEventDao.update(event)
    }
    
    /**
     * 删除屏幕事件
     */
    suspend fun deleteScreenEvent(event: ScreenEvent) {
        screenEventDao.delete(event)
    }
    
    /**
     * 删除指定时间之前的旧记录
     * @param beforeTimestamp 时间戳阈值
     * @return 删除的记录数量
     */
    suspend fun deleteOldRecords(beforeTimestamp: Long): Int {
        return screenEventDao.deleteOldRecords(beforeTimestamp)
    }
    
    /**
     * 清空所有屏幕事件记录
     */
    suspend fun deleteAllRecords() {
        screenEventDao.deleteAll()
    }
    
    // ==================== 查询操作 ====================
    
    /**
     * 根据 ID 获取屏幕事件
     */
    suspend fun getScreenEventById(id: Long): ScreenEvent? {
        return screenEventDao.getById(id)
    }
    
    /**
     * 获取指定时间范围内的屏幕事件
     */
    suspend fun getEventsInRange(startTime: Long, endTime: Long): List<ScreenEvent> {
        return screenEventDao.getEventsInRange(startTime, endTime)
    }
    
    /**
     * 获取指定时间范围内的屏幕事件（Flow）
     */
    fun getEventsInRangeFlow(startTime: Long, endTime: Long): Flow<List<ScreenEvent>> {
        return screenEventDao.getEventsInRangeFlow(startTime, endTime)
    }
    
    /**
     * 按事件类型获取屏幕事件
     */
    suspend fun getEventsByType(
        eventType: ScreenEventType,
        startTime: Long,
        endTime: Long
    ): List<ScreenEvent> {
        return screenEventDao.getEventsByType(eventType, startTime, endTime)
    }
    
    /**
     * 获取所有记录（用于导出）
     */
    suspend fun getAllRecords(): List<ScreenEvent> {
        return screenEventDao.getAllRecords()
    }
    
    /**
     * 获取记录总数
     */
    suspend fun getRecordCount(): Int {
        return screenEventDao.getRecordCount()
    }
    
    // ==================== 解锁次数统计 ====================
    
    /**
     * 获取解锁次数
     */
    suspend fun getUnlockCount(startTime: Long, endTime: Long): Int {
        return screenEventDao.getUnlockCount(startTime, endTime)
    }
    
    /**
     * 获取解锁次数（Flow）
     */
    fun getUnlockCountFlow(startTime: Long, endTime: Long): Flow<Int> {
        return screenEventDao.getUnlockCountFlow(startTime, endTime)
    }
    
    /**
     * 获取今日解锁次数
     */
    suspend fun getTodayUnlockCount(): Int {
        val now = System.currentTimeMillis()
        val startOfDay = now - (now % (24 * 60 * 60 * 1000))
        return getUnlockCount(startOfDay, now)
    }
    
    /**
     * 获取今日解锁次数（Flow）
     */
    fun getTodayUnlockCountFlow(): Flow<Int> {
        val now = System.currentTimeMillis()
        val startOfDay = now - (now % (24 * 60 * 60 * 1000))
        return getUnlockCountFlow(startOfDay, now)
    }
    
    /**
     * 获取本周解锁次数
     */
    suspend fun getWeeklyUnlockCount(): Int {
        val now = System.currentTimeMillis()
        val weekAgo = now - (7 * 24 * 60 * 60 * 1000)
        return getUnlockCount(weekAgo, now)
    }
    
    // ==================== 屏幕开启时长统计 ====================
    
    /**
     * 计算屏幕开启总时长
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 屏幕开启时长（毫秒）
     */
    suspend fun calculateScreenOnDuration(startTime: Long, endTime: Long): Long {
        val events = screenEventDao.getScreenOnOffEvents(startTime, endTime)
        return calculateScreenOnDurationFromEvents(events, startTime, endTime)
    }
    
    /**
     * 获取屏幕开启时长（Flow）
     */
    fun getScreenOnDurationFlow(startTime: Long, endTime: Long): Flow<Long> {
        return getEventsInRangeFlow(startTime, endTime).map { events ->
            val screenOnOffEvents = events.filter { 
                it.eventType == ScreenEventType.SCREEN_ON || it.eventType == ScreenEventType.SCREEN_OFF 
            }.sortedBy { it.timestamp }
            calculateScreenOnDurationFromEvents(screenOnOffEvents, startTime, endTime)
        }
    }
    
    /**
     * 从事件列表计算屏幕开启时长
     */
    private fun calculateScreenOnDurationFromEvents(
        events: List<ScreenEvent>,
        rangeStart: Long,
        rangeEnd: Long
    ): Long {
        if (events.isEmpty()) return 0L
        
        var totalDuration = 0L
        var screenOnTime: Long? = null
        
        // 检查范围开始时屏幕是否已经开启
        val eventsBeforeRange = events.filter { it.timestamp < rangeStart }
        val lastEventBeforeRange = eventsBeforeRange.maxByOrNull { it.timestamp }
        if (lastEventBeforeRange?.eventType == ScreenEventType.SCREEN_ON) {
            screenOnTime = rangeStart
        }
        
        // 处理范围内的事件
        for (event in events.filter { it.timestamp in rangeStart..rangeEnd }) {
            when (event.eventType) {
                ScreenEventType.SCREEN_ON -> {
                    screenOnTime = event.timestamp
                }
                ScreenEventType.SCREEN_OFF -> {
                    screenOnTime?.let { onTime ->
                        totalDuration += event.timestamp - onTime
                        screenOnTime = null
                    }
                }
                else -> { /* 忽略其他事件类型 */ }
            }
        }
        
        // 如果范围结束时屏幕仍然开启，计算到范围结束的时长
        screenOnTime?.let { onTime ->
            totalDuration += rangeEnd - onTime
        }
        
        return totalDuration
    }
    
    /**
     * 获取今日屏幕开启时长
     */
    suspend fun getTodayScreenOnDuration(): Long {
        val now = System.currentTimeMillis()
        val startOfDay = now - (now % (24 * 60 * 60 * 1000))
        return calculateScreenOnDuration(startOfDay, now)
    }
    
    /**
     * 获取今日屏幕开启时长（Flow）
     */
    fun getTodayScreenOnDurationFlow(): Flow<Long> {
        val now = System.currentTimeMillis()
        val startOfDay = now - (now % (24 * 60 * 60 * 1000))
        return getScreenOnDurationFlow(startOfDay, now)
    }
    
    /**
     * 获取本周屏幕开启时长
     */
    suspend fun getWeeklyScreenOnDuration(): Long {
        val now = System.currentTimeMillis()
        val weekAgo = now - (7 * 24 * 60 * 60 * 1000)
        return calculateScreenOnDuration(weekAgo, now)
    }
    
    // ==================== 使用模式分析 ====================
    
    /**
     * 屏幕使用会话数据类
     */
    data class ScreenSession(
        val startTime: Long,
        val endTime: Long,
        val duration: Long
    )
    
    /**
     * 获取屏幕使用会话列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 会话列表
     */
    suspend fun getScreenSessions(startTime: Long, endTime: Long): List<ScreenSession> {
        val events = screenEventDao.getScreenOnOffEvents(startTime, endTime)
        return calculateScreenSessionsFromEvents(events, startTime, endTime)
    }
    
    /**
     * 从事件列表计算屏幕使用会话
     */
    private fun calculateScreenSessionsFromEvents(
        events: List<ScreenEvent>,
        rangeStart: Long,
        rangeEnd: Long
    ): List<ScreenSession> {
        if (events.isEmpty()) return emptyList()
        
        val sessions = mutableListOf<ScreenSession>()
        var sessionStartTime: Long? = null
        
        // 检查范围开始时屏幕是否已经开启
        val eventsBeforeRange = events.filter { it.timestamp < rangeStart }
        val lastEventBeforeRange = eventsBeforeRange.maxByOrNull { it.timestamp }
        if (lastEventBeforeRange?.eventType == ScreenEventType.SCREEN_ON) {
            sessionStartTime = rangeStart
        }
        
        // 处理范围内的事件
        for (event in events.filter { it.timestamp in rangeStart..rangeEnd }) {
            when (event.eventType) {
                ScreenEventType.SCREEN_ON -> {
                    sessionStartTime = event.timestamp
                }
                ScreenEventType.SCREEN_OFF -> {
                    sessionStartTime?.let { startTime ->
                        val duration = event.timestamp - startTime
                        if (duration > 0) {
                            sessions.add(ScreenSession(startTime, event.timestamp, duration))
                        }
                        sessionStartTime = null
                    }
                }
                else -> { /* 忽略其他事件类型 */ }
            }
        }
        
        // 如果范围结束时屏幕仍然开启，创建到范围结束的会话
        sessionStartTime?.let { startTime ->
            val duration = rangeEnd - startTime
            if (duration > 0) {
                sessions.add(ScreenSession(startTime, rangeEnd, duration))
            }
        }
        
        return sessions
    }
    
    /**
     * 获取平均会话时长
     */
    suspend fun getAverageSessionDuration(startTime: Long, endTime: Long): Long {
        val sessions = getScreenSessions(startTime, endTime)
        return if (sessions.isNotEmpty()) {
            sessions.map { it.duration }.average().toLong()
        } else {
            0L
        }
    }
    
    /**
     * 获取最长会话时长
     */
    suspend fun getLongestSessionDuration(startTime: Long, endTime: Long): Long {
        val sessions = getScreenSessions(startTime, endTime)
        return sessions.maxOfOrNull { it.duration } ?: 0L
    }
    
    /**
     * 获取会话次数
     */
    suspend fun getSessionCount(startTime: Long, endTime: Long): Int {
        return getScreenSessions(startTime, endTime).size
    }
    
    // ==================== 使用时间分布 ====================
    
    /**
     * 获取每小时使用时长分布
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每小时使用时长的映射（小时 -> 时长毫秒）
     */
    suspend fun getHourlyUsageDistribution(startTime: Long, endTime: Long): Map<Int, Long> {
        val sessions = getScreenSessions(startTime, endTime)
        val hourlyUsage = mutableMapOf<Int, Long>()
        
        for (session in sessions) {
            val startCalendar = java.util.Calendar.getInstance().apply {
                timeInMillis = session.startTime
            }
            val endCalendar = java.util.Calendar.getInstance().apply {
                timeInMillis = session.endTime
            }
            
            // 如果会话跨越多个小时，需要按小时分割
            var currentTime = session.startTime
            while (currentTime < session.endTime) {
                val calendar = java.util.Calendar.getInstance().apply {
                    timeInMillis = currentTime
                }
                val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                
                // 计算当前小时内的时长
                val nextHourStart = calendar.apply {
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                    add(java.util.Calendar.HOUR_OF_DAY, 1)
                }.timeInMillis
                
                val hourEndTime = minOf(nextHourStart, session.endTime)
                val durationInHour = hourEndTime - currentTime
                
                hourlyUsage[hour] = (hourlyUsage[hour] ?: 0L) + durationInHour
                currentTime = nextHourStart
            }
        }
        
        return hourlyUsage
    }
    
    /**
     * 获取解锁时间分布
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每小时解锁次数的映射（小时 -> 解锁次数）
     */
    suspend fun getUnlockTimeDistribution(startTime: Long, endTime: Long): Map<Int, Int> {
        val unlockEvents = getEventsByType(ScreenEventType.USER_PRESENT, startTime, endTime)
        return unlockEvents.groupBy { event ->
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = event.timestamp
            calendar.get(java.util.Calendar.HOUR_OF_DAY)
        }.mapValues { it.value.size }
    }
    
    // ==================== 其他统计功能 ====================
    
    /**
     * 获取第一次解锁时间
     */
    suspend fun getFirstUnlockTime(startTime: Long, endTime: Long): Long? {
        return screenEventDao.getFirstUnlockTime(startTime, endTime)
    }
    
    /**
     * 获取最后一次屏幕关闭时间
     */
    suspend fun getLastScreenOffTime(startTime: Long, endTime: Long): Long? {
        return screenEventDao.getLastScreenOffTime(startTime, endTime)
    }
    
    /**
     * 获取今日第一次解锁时间
     */
    suspend fun getTodayFirstUnlockTime(): Long? {
        val now = System.currentTimeMillis()
        val startOfDay = now - (now % (24 * 60 * 60 * 1000))
        return getFirstUnlockTime(startOfDay, now)
    }
    
    /**
     * 获取今日最后一次屏幕关闭时间
     */
    suspend fun getTodayLastScreenOffTime(): Long? {
        val now = System.currentTimeMillis()
        val startOfDay = now - (now % (24 * 60 * 60 * 1000))
        return getLastScreenOffTime(startOfDay, now)
    }
    
    /**
     * 数据清理 - 删除超过指定天数的记录
     * @param daysToKeep 保留天数
     * @return 删除的记录数量
     */
    suspend fun cleanupOldData(daysToKeep: Int = 30): Int {
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        return deleteOldRecords(cutoffTime)
    }
    
    /**
     * 获取屏幕使用摘要
     */
    suspend fun getScreenUsageSummary(startTime: Long, endTime: Long): ScreenUsageSummary {
        val unlockCount = getUnlockCount(startTime, endTime)
        val screenOnDuration = calculateScreenOnDuration(startTime, endTime)
        val sessions = getScreenSessions(startTime, endTime)
        val averageSessionDuration = if (sessions.isNotEmpty()) {
            sessions.map { it.duration }.average().toLong()
        } else {
            0L
        }
        val longestSession = sessions.maxOfOrNull { it.duration } ?: 0L
        val firstUnlock = getFirstUnlockTime(startTime, endTime)
        val lastScreenOff = getLastScreenOffTime(startTime, endTime)
        
        return ScreenUsageSummary(
            unlockCount = unlockCount,
            screenOnDuration = screenOnDuration,
            sessionCount = sessions.size,
            averageSessionDuration = averageSessionDuration,
            longestSessionDuration = longestSession,
            firstUnlockTime = firstUnlock,
            lastScreenOffTime = lastScreenOff
        )
    }
}

/**
 * 屏幕使用摘要数据类
 */
data class ScreenUsageSummary(
    val unlockCount: Int,
    val screenOnDuration: Long,
    val sessionCount: Int,
    val averageSessionDuration: Long,
    val longestSessionDuration: Long,
    val firstUnlockTime: Long?,
    val lastScreenOffTime: Long?
)
