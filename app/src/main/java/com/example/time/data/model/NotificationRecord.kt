package com.example.time.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Notification Record Entity - 通知记录实体
 * 记录通知事件和用户交互
 */
@Entity(
    tableName = "notification_records",
    indices = [
        Index(value = ["packageName"]),
        Index(value = ["postedTime"]),
        Index(value = ["notificationKey"])
    ]
)
data class NotificationRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 通知唯一标识
    val notificationKey: String,
    
    // 应用包名
    val packageName: String,
    
    // 应用名称
    val appName: String,
    
    // 通知标题
    val title: String?,
    
    // 通知内容
    val text: String?,
    
    // 通知类别
    val category: String?,
    
    // 发布时间（毫秒）
    val postedTime: Long,
    
    // 移除时间（毫秒，null表示未移除）
    val removedTime: Long? = null,
    
    // 用户交互时间（毫秒，null表示未交互）
    val interactedTime: Long? = null,
    
    // 响应时长（毫秒）
    val responseTime: Long? = null,
    
    // 是否被用户点击
    val wasClicked: Boolean = false,
    
    // 是否被用户取消
    val wasCancelled: Boolean = false,
    
    // 是否是分组通知
    val isGroup: Boolean = false,
    
    // 优先级
    val priority: Int = 0,
    
    // 记录创建时间
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Notification Statistics - 通知统计
 */
data class NotificationStatistics(
    val packageName: String,
    val appName: String,
    val totalCount: Int,
    val clickedCount: Int,
    val cancelledCount: Int,
    val averageResponseTime: Long,
    val interactionRate: Float
)

