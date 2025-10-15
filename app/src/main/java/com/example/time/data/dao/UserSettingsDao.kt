package com.example.time.data.dao

import androidx.room.*
import com.example.time.data.model.UserSettings
import kotlinx.coroutines.flow.Flow

/**
 * User Settings DAO - 用户设置数据访问对象
 */
@Dao
interface UserSettingsDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: UserSettings)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(settings: List<UserSettings>)
    
    @Update
    suspend fun update(settings: UserSettings)
    
    @Delete
    suspend fun delete(settings: UserSettings)
    
    // 查询操作
    @Query("SELECT * FROM user_settings WHERE settingKey = :key")
    suspend fun getSetting(key: String): UserSettings?
    
    @Query("SELECT * FROM user_settings WHERE settingKey = :key")
    fun getSettingFlow(key: String): Flow<UserSettings?>
    
    @Query("SELECT settingValue FROM user_settings WHERE settingKey = :key")
    suspend fun getSettingValue(key: String): String?
    
    @Query("SELECT * FROM user_settings")
    suspend fun getAllSettings(): List<UserSettings>
    
    @Query("SELECT * FROM user_settings")
    fun getAllSettingsFlow(): Flow<List<UserSettings>>
    
    // 批量查询
    @Query("SELECT * FROM user_settings WHERE settingKey IN (:keys)")
    suspend fun getSettings(keys: List<String>): List<UserSettings>
    
    // 删除设置
    @Query("DELETE FROM user_settings WHERE settingKey = :key")
    suspend fun deleteSetting(key: String)
    
    @Query("DELETE FROM user_settings WHERE settingKey IN (:keys)")
    suspend fun deleteSettings(keys: List<String>)
    
    @Query("DELETE FROM user_settings")
    suspend fun deleteAll()
    
    // 检查设置是否存在
    @Query("SELECT EXISTS(SELECT 1 FROM user_settings WHERE settingKey = :key)")
    suspend fun exists(key: String): Boolean
}

/**
 * Settings Keys - 设置键常量
 */
object SettingsKeys {
    // 全局提醒设置
    const val GLOBAL_REMINDER_SETTINGS = "global_reminder_settings"
    
    // 应用提醒设置（前缀）
    const val APP_REMINDER_PREFIX = "app_reminder_"
    
    // 性能模式设置
    const val PERFORMANCE_MODE_SETTINGS = "performance_mode_settings"
    
    // 主题设置
    const val THEME_SETTINGS = "theme_settings"
    
    // 应用黑名单
    const val APP_BLACKLIST = "app_blacklist"
    
    // 语言设置
    const val LANGUAGE_SETTING = "language_setting"
    
    // 数据保留天数
    const val DATA_RETENTION_DAYS = "data_retention_days"
    
    // 引导已完成标记
    const val ONBOARDING_COMPLETED = "onboarding_completed"
    
    // 权限已授予标记
    const val PERMISSIONS_GRANTED = "permissions_granted"
    
    // 生成应用提醒设置键
    fun appReminderKey(packageName: String) = "$APP_REMINDER_PREFIX$packageName"
}

