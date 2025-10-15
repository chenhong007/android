package com.example.time.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Screen Event Entity - 屏幕事件实体
 * 记录屏幕开关和解锁事件
 */
@Entity(
    tableName = "screen_events",
    indices = [
        Index(value = ["eventType"]),
        Index(value = ["timestamp"])
    ]
)
data class ScreenEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 事件类型：SCREEN_ON, SCREEN_OFF, USER_PRESENT
    val eventType: ScreenEventType,
    
    // 事件时间戳（毫秒）
    val timestamp: Long,
    
    // 记录创建时间
    val createdAt: Long = System.currentTimeMillis()
)

enum class ScreenEventType {
    SCREEN_ON,    // 屏幕开启
    SCREEN_OFF,   // 屏幕关闭
    USER_PRESENT  // 用户解锁
}

/**
 * Screen Time Summary - 屏幕时间汇总
 */
data class ScreenTimeSummary(
    val date: String, // YYYY-MM-DD format
    val unlockCount: Int,
    val screenOnDuration: Long, // 毫秒
    val firstUnlockTime: Long,
    val lastScreenOffTime: Long
)

