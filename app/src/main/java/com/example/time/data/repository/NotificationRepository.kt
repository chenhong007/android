package com.example.time.data.repository

import com.example.time.data.dao.NotificationRecordDao
import com.example.time.data.model.NotificationRecord
import com.example.time.data.model.NotificationStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NotificationRepository - 通知记录仓库
 * 
 * 封装 NotificationRecordDao，提供通知记录的业务逻辑层
 * 包含通知统计、响应率计算等功能
 */
@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationRecordDao
) {
    
    // ==================== 基础 CRUD 操作 ====================
    
    /**
     * 插入单个通知记录
     */
    suspend fun insertNotification(record: NotificationRecord): Long {
        return notificationDao.insert(record)
    }
    
    /**
     * 批量插入通知记录
     */
    suspend fun insertNotifications(records: List<NotificationRecord>) {
        notificationDao.insertAll(records)
    }
    
    /**
     * 更新通知记录
     */
    suspend fun updateNotification(record: NotificationRecord) {
        notificationDao.update(record)
    }
    
    /**
     * 删除通知记录
     */
    suspend fun deleteNotification(record: NotificationRecord) {
        notificationDao.delete(record)
    }
    
    /**
     * 删除指定时间之前的旧记录
     * @param beforeTimestamp 时间戳阈值
     * @return 删除的记录数量
     */
    suspend fun deleteOldRecords(beforeTimestamp: Long): Int {
        return notificationDao.deleteOldRecords(beforeTimestamp)
    }
    
    /**
     * 清空所有通知记录
     */
    suspend fun deleteAllRecords() {
        notificationDao.deleteAll()
    }
    
    // ==================== 查询操作 ====================
    
    /**
     * 根据 ID 获取通知记录
     */
    suspend fun getNotificationById(id: Long): NotificationRecord? {
        return notificationDao.getById(id)
    }
    
    /**
     * 根据通知键获取记录
     */
    suspend fun getNotificationByKey(key: String): NotificationRecord? {
        return notificationDao.getByKey(key)
    }
    
    /**
     * 获取指定时间范围内的通知记录
     */
    suspend fun getNotificationsInRange(startTime: Long, endTime: Long): List<NotificationRecord> {
        return notificationDao.getNotificationsInRange(startTime, endTime)
    }
    
    /**
     * 获取指定时间范围内的通知记录（Flow）
     */
    fun getNotificationsInRangeFlow(startTime: Long, endTime: Long): Flow<List<NotificationRecord>> {
        return notificationDao.getNotificationsInRangeFlow(startTime, endTime)
    }
    
    /**
     * 获取特定应用的通知记录
     */
    suspend fun getAppNotifications(
        packageName: String,
        startTime: Long,
        endTime: Long
    ): List<NotificationRecord> {
        return notificationDao.getAppNotifications(packageName, startTime, endTime)
    }
    
    /**
     * 获取所有记录（用于导出）
     */
    suspend fun getAllRecords(): List<NotificationRecord> {
        return notificationDao.getAllRecords()
    }
    
    /**
     * 获取记录总数
     */
    suspend fun getRecordCount(): Int {
        return notificationDao.getRecordCount()
    }
    
    // ==================== 统计功能 ====================
    
    /**
     * 获取通知统计信息
     */
    suspend fun getNotificationStatistics(
        startTime: Long,
        endTime: Long
    ): List<NotificationStatistics> {
        return notificationDao.getNotificationStatistics(startTime, endTime)
    }
    
    /**
     * 获取通知统计信息（Flow）
     */
    fun getNotificationStatisticsFlow(
        startTime: Long,
        endTime: Long
    ): Flow<List<NotificationStatistics>> {
        return notificationDao.getNotificationStatisticsFlow(startTime, endTime)
    }
    
    /**
     * 获取通知总数
     */
    suspend fun getNotificationCount(startTime: Long, endTime: Long): Int {
        return notificationDao.getNotificationCount(startTime, endTime)
    }
    
    /**
     * 获取通知总数（Flow）
     */
    fun getNotificationCountFlow(startTime: Long, endTime: Long): Flow<Int> {
        return notificationDao.getNotificationCountFlow(startTime, endTime)
    }
    
    /**
     * 获取特定应用的通知数量
     */
    suspend fun getAppNotificationCount(
        packageName: String,
        startTime: Long,
        endTime: Long
    ): Int {
        return notificationDao.getAppNotificationCount(packageName, startTime, endTime)
    }
    
    /**
     * 获取交互率最高的应用
     */
    suspend fun getTopInteractionApps(
        startTime: Long,
        endTime: Long,
        limit: Int = 10
    ): List<NotificationStatistics> {
        return notificationDao.getTopInteractionApps(startTime, endTime, limit)
    }
    
    // ==================== 业务逻辑方法 ====================
    
    /**
     * 计算总体通知响应率
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 响应率（0.0 - 1.0）
     */
    suspend fun calculateOverallResponseRate(startTime: Long, endTime: Long): Double {
        val statistics = getNotificationStatistics(startTime, endTime)
        if (statistics.isEmpty()) return 0.0
        
        val totalNotifications = statistics.sumOf { it.totalCount }
        val totalInteractions = statistics.sumOf { it.clickedCount }
        
        return if (totalNotifications > 0) {
            totalInteractions.toDouble() / totalNotifications.toDouble()
        } else {
            0.0
        }
    }
    
    /**
     * 获取今日通知统计
     */
    suspend fun getTodayNotificationStatistics(): List<NotificationStatistics> {
        val now = System.currentTimeMillis()
        val startOfDay = now - (now % (24 * 60 * 60 * 1000))
        return getNotificationStatistics(startOfDay, now)
    }
    
    /**
     * 获取今日通知统计（Flow）
     */
    fun getTodayNotificationStatisticsFlow(): Flow<List<NotificationStatistics>> {
        val now = System.currentTimeMillis()
        val startOfDay = now - (now % (24 * 60 * 60 * 1000))
        return getNotificationStatisticsFlow(startOfDay, now)
    }
    
    /**
     * 获取本周通知统计
     */
    suspend fun getWeeklyNotificationStatistics(): List<NotificationStatistics> {
        val now = System.currentTimeMillis()
        val weekAgo = now - (7 * 24 * 60 * 60 * 1000)
        return getNotificationStatistics(weekAgo, now)
    }
    
    /**
     * 获取应用通知频率排名
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 返回数量限制
     * @return 按通知数量排序的统计列表
     */
    suspend fun getNotificationFrequencyRanking(
        startTime: Long,
        endTime: Long,
        limit: Int = 10
    ): List<NotificationStatistics> {
        return getNotificationStatistics(startTime, endTime).take(limit)
    }
    
    /**
     * 计算平均响应时间
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 平均响应时间（毫秒）
     */
    suspend fun calculateAverageResponseTime(startTime: Long, endTime: Long): Double {
        val statistics = getNotificationStatistics(startTime, endTime)
        if (statistics.isEmpty()) return 0.0
        
        val totalResponseTime = statistics.sumOf { it.averageResponseTime * it.clickedCount }
        val totalClicked = statistics.sumOf { it.clickedCount }
        
        return if (totalClicked > 0) {
            totalResponseTime / totalClicked.toDouble()
        } else {
            0.0
        }
    }
    
    /**
     * 获取通知趋势数据（按小时分组）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每小时通知数量的映射
     */
    suspend fun getNotificationTrends(startTime: Long, endTime: Long): Map<Int, Int> {
        val notifications = getNotificationsInRange(startTime, endTime)
        return notifications.groupBy { record ->
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = record.postedTime
            calendar.get(java.util.Calendar.HOUR_OF_DAY)
        }.mapValues { it.value.size }
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
     * 获取通知摘要信息
     */
    suspend fun getNotificationSummary(startTime: Long, endTime: Long): NotificationSummary {
        val statistics = getNotificationStatistics(startTime, endTime)
        val totalCount = statistics.sumOf { it.totalCount }
        val totalClicked = statistics.sumOf { it.clickedCount }
        val totalCancelled = statistics.sumOf { it.cancelledCount }
        val responseRate = if (totalCount > 0) totalClicked.toDouble() / totalCount else 0.0
        val averageResponseTime = calculateAverageResponseTime(startTime, endTime)
        
        return NotificationSummary(
            totalNotifications = totalCount,
            clickedNotifications = totalClicked,
            cancelledNotifications = totalCancelled,
            responseRate = responseRate,
            averageResponseTime = averageResponseTime,
            topApps = statistics.take(5)
        )
    }
}

/**
 * 通知摘要数据类
 */
data class NotificationSummary(
    val totalNotifications: Int,
    val clickedNotifications: Int,
    val cancelledNotifications: Int,
    val responseRate: Double,
    val averageResponseTime: Double,
    val topApps: List<NotificationStatistics>
)
