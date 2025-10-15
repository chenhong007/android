package com.example.time.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.TypeConverter
import com.example.time.data.dao.*
import com.example.time.data.model.*

/**
 * Time Database - 时间管理应用数据库
 * 
 * 版本历史:
 * - v2: 添加性能优化索引（单列索引 + 组合索引）
 * - v1: 初始版本
 */
@Database(
    entities = [
        UsageTracking::class,
        NotificationRecord::class,
        UserSettings::class,
        ScreenEvent::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TimeDatabase : RoomDatabase() {
    
    abstract fun usageTrackingDao(): UsageTrackingDao
    abstract fun notificationRecordDao(): NotificationRecordDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun screenEventDao(): ScreenEventDao
    
    companion object {
        private const val DATABASE_NAME = "time_database.db"
        
        @Volatile
        private var INSTANCE: TimeDatabase? = null
        
        fun getInstance(context: Context): TimeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }
        
        private fun buildDatabase(context: Context): TimeDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                TimeDatabase::class.java,
                DATABASE_NAME
            )
                // 注意：开发阶段使用 fallbackToDestructiveMigration
                // 索引变更会在数据库重建时自动生效
                // 生产环境应提供迁移策略（addMigrations）
                .fallbackToDestructiveMigration()
                .build()
        }
        
        /**
         * 关闭数据库连接
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}

/**
 * Type Converters - 类型转换器
 * 用于 Room 数据库中的复杂类型转换
 */
class Converters {
    // List<String> 转换
    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        return value?.split(",")?.map { it }
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    // NotificationRecord 中 Long? 转换（Room支持 nullable Long，无需转换器）
    // ScreenEventType 枚举转换
    @TypeConverter
    fun fromScreenEventType(value: com.example.time.data.model.ScreenEventType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toScreenEventType(name: String?): com.example.time.data.model.ScreenEventType? {
        return name?.let { com.example.time.data.model.ScreenEventType.valueOf(it) }
    }
}

