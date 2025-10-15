package com.example.time.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Usage Tracking Entity - 使用跟踪实体
 * 记录应用使用情况，精确到毫秒
 */
@Entity(
    tableName = "usage_tracking",
    indices = [
        // 单列索引：用于单一条件查询
        Index(value = ["packageName"], name = "idx_package_name"),
        Index(value = ["timestamp"], name = "idx_timestamp"),
        Index(value = ["sessionId"], name = "idx_session_id"),
        
        // 组合索引：用于常见的组合查询，大幅提升性能
        // 用于: WHERE packageName = ? AND timestamp >= ? AND timestamp <= ?
        Index(value = ["packageName", "timestamp"], name = "idx_package_timestamp"),
        
        // 用于: WHERE timestamp >= ? AND timestamp <= ? ORDER BY timestamp DESC
        // 覆盖索引可以避免回表查询
        Index(value = ["timestamp", "duration"], name = "idx_timestamp_duration")
    ]
)
data class UsageTracking(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 应用包名
    val packageName: String,
    
    // 应用名称
    val appName: String,
    
    // 应用类别（娱乐、社交、工具等）
    val category: String = "Other",
    
    // 会话ID（用于关联连续使用记录）
    val sessionId: String,
    
    // 开始时间戳（毫秒）
    val timestamp: Long,
    
    // 结束时间戳（毫秒）
    val endTimestamp: Long,
    
    // 使用时长（毫秒）
    val duration: Long,
    
    // 是否为前台应用
    val isForeground: Boolean = true,
    
    // 屏幕状态（开/关）
    val screenState: String = "ON",
    
    // 数据收集模式（standard, high-precision, energy-saving）
    val collectionMode: String = "standard",
    
    // 记录创建时间
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * App Usage Summary - 应用使用汇总
 * 用于统计查询的数据类
 * 
 * @property packageName 应用包名
 * @property appName 应用名称
 * @property category 应用类别
 * @property totalDuration 总使用时长（毫秒）
 * @property sessionCount 打开次数（每次从后台切换到前台算一次）
 * @property lastUsedTimestamp 最后使用时间戳
 * @property percentage 使用时长占比（百分比）
 */
data class AppUsageSummary(
    val packageName: String,
    val appName: String,
    val category: String,
    val totalDuration: Long,
    val sessionCount: Int, // 注意：虽然字段名是sessionCount，但实际表示打开次数
    val lastUsedTimestamp: Long,
    val percentage: Float = 0f
)

/**
 * Daily Usage Summary - 每日使用汇总
 */
data class DailyUsageSummary(
    val date: String, // YYYY-MM-DD format
    val totalDuration: Long,
    val unlockCount: Int,
    val screenOnDuration: Long,
    val topApps: List<AppUsageSummary>
)

