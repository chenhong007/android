package com.example.time.data.repository

import com.example.time.data.dao.SettingsKeys
import com.example.time.data.dao.UserSettingsDao
import com.example.time.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SettingsRepository - 用户设置仓库
 * 
 * 封装 UserSettingsDao，提供用户设置的业务逻辑层
 * 包含主题管理、语言设置、性能模式管理、提醒配置管理等功能
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDao: UserSettingsDao
) {
    
    @PublishedApi
    internal val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    // ==================== 基础 CRUD 操作 ====================
    
    /**
     * 设置字符串值
     */
    suspend fun setString(key: String, value: String) {
        val setting = UserSettings(
            settingKey = key,
            settingValue = value,
            valueType = "string",
            updatedAt = System.currentTimeMillis()
        )
        settingsDao.insert(setting)
    }
    
    /**
     * 获取字符串值
     */
    suspend fun getString(key: String, defaultValue: String = ""): String {
        return settingsDao.getSettingValue(key) ?: defaultValue
    }
    
    /**
     * 获取字符串值（Flow）
     */
    fun getStringFlow(key: String, defaultValue: String = ""): Flow<String> {
        return settingsDao.getSettingFlow(key).map { it?.settingValue ?: defaultValue }
    }
    
    /**
     * 设置布尔值
     */
    suspend fun setBoolean(key: String, value: Boolean) {
        setString(key, value.toString())
    }
    
    /**
     * 获取布尔值
     */
    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return getString(key, defaultValue.toString()).toBooleanStrictOrNull() ?: defaultValue
    }
    
    /**
     * 获取布尔值（Flow）
     */
    fun getBooleanFlow(key: String, defaultValue: Boolean = false): Flow<Boolean> {
        return getStringFlow(key, defaultValue.toString()).map { 
            it.toBooleanStrictOrNull() ?: defaultValue 
        }
    }
    
    /**
     * 设置整数值
     */
    suspend fun setInt(key: String, value: Int) {
        setString(key, value.toString())
    }
    
    /**
     * 获取整数值
     */
    suspend fun getInt(key: String, defaultValue: Int = 0): Int {
        return getString(key, defaultValue.toString()).toIntOrNull() ?: defaultValue
    }
    
    /**
     * 获取整数值（Flow）
     */
    fun getIntFlow(key: String, defaultValue: Int = 0): Flow<Int> {
        return getStringFlow(key, defaultValue.toString()).map { 
            it.toIntOrNull() ?: defaultValue 
        }
    }
    
    /**
     * 设置长整数值
     */
    suspend fun setLong(key: String, value: Long) {
        setString(key, value.toString())
    }
    
    /**
     * 获取长整数值
     */
    suspend fun getLong(key: String, defaultValue: Long = 0L): Long {
        return getString(key, defaultValue.toString()).toLongOrNull() ?: defaultValue
    }
    
    /**
     * 设置浮点数值
     */
    suspend fun setFloat(key: String, value: Float) {
        setString(key, value.toString())
    }
    
    /**
     * 获取浮点数值
     */
    suspend fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return getString(key, defaultValue.toString()).toFloatOrNull() ?: defaultValue
    }
    
    /**
     * 设置 JSON 对象
     */
    suspend inline fun <reified T> setJson(key: String, value: T) {
        val jsonString = json.encodeToString(value)
        setString(key, jsonString)
    }
    
    /**
     * 获取 JSON 对象
     */
    suspend inline fun <reified T> getJson(key: String, defaultValue: T): T {
        val jsonString = getString(key)
        return if (jsonString.isNotEmpty()) {
            try {
                json.decodeFromString<T>(jsonString)
            } catch (e: Exception) {
                defaultValue
            }
        } else {
            defaultValue
        }
    }
    
    /**
     * 获取 JSON 对象（Flow）
     */
    inline fun <reified T> getJsonFlow(key: String, defaultValue: T): Flow<T> {
        return getStringFlow(key).map { jsonString ->
            if (jsonString.isNotEmpty()) {
                try {
                    json.decodeFromString<T>(jsonString)
                } catch (e: Exception) {
                    defaultValue
                }
            } else {
                defaultValue
            }
        }
    }
    
    /**
     * 删除设置
     */
    suspend fun remove(key: String) {
        settingsDao.deleteSetting(key)
    }
    
    /**
     * 批量删除设置
     */
    suspend fun remove(keys: List<String>) {
        settingsDao.deleteSettings(keys)
    }
    
    /**
     * 检查设置是否存在
     */
    suspend fun contains(key: String): Boolean {
        return settingsDao.exists(key)
    }
    
    /**
     * 获取所有设置
     */
    suspend fun getAllSettings(): List<UserSettings> {
        return settingsDao.getAllSettings()
    }
    
    /**
     * 清空所有设置
     */
    suspend fun clearAll() {
        settingsDao.deleteAll()
    }
    
    // ==================== 主题管理 ====================
    
    /**
     * 主题设置数据类
     */
    @kotlinx.serialization.Serializable
    data class ThemeSettings(
        val isDarkMode: Boolean = false,
        val isHighContrast: Boolean = false,
        val useDynamicColors: Boolean = true,
        val fontSize: Float = 1.0f
    )
    
    /**
     * 设置主题配置
     */
    suspend fun setThemeSettings(settings: ThemeSettings) {
        setJson(SettingsKeys.THEME_SETTINGS, settings)
    }
    
    /**
     * 获取主题配置
     */
    suspend fun getThemeSettings(): ThemeSettings {
        return getJson(SettingsKeys.THEME_SETTINGS, ThemeSettings())
    }
    
    /**
     * 获取主题配置（Flow）
     */
    fun getThemeSettingsFlow(): Flow<ThemeSettings> {
        return getJsonFlow(SettingsKeys.THEME_SETTINGS, ThemeSettings())
    }
    
    /**
     * 设置深色模式
     */
    suspend fun setDarkMode(enabled: Boolean) {
        val current = getThemeSettings()
        setThemeSettings(current.copy(isDarkMode = enabled))
    }
    
    /**
     * 获取深色模式状态
     */
    suspend fun isDarkMode(): Boolean {
        return getThemeSettings().isDarkMode
    }
    
    /**
     * 获取深色模式状态（Flow）
     */
    fun isDarkModeFlow(): Flow<Boolean> {
        return getThemeSettingsFlow().map { it.isDarkMode }
    }
    
    /**
     * 设置高对比度模式
     */
    suspend fun setHighContrast(enabled: Boolean) {
        val current = getThemeSettings()
        setThemeSettings(current.copy(isHighContrast = enabled))
    }
    
    /**
     * 获取高对比度模式状态
     */
    suspend fun isHighContrast(): Boolean {
        return getThemeSettings().isHighContrast
    }
    
    // ==================== 语言设置管理 ====================
    
    /**
     * 支持的语言枚举
     */
    enum class Language(val code: String, val displayName: String) {
        CHINESE("zh", "中文"),
        ENGLISH("en", "English"),
        SYSTEM("system", "跟随系统")
    }
    
    /**
     * 设置语言
     */
    suspend fun setLanguage(language: Language) {
        setString(SettingsKeys.LANGUAGE_SETTING, language.code)
    }
    
    /**
     * 获取当前语言
     */
    suspend fun getLanguage(): Language {
        val code = getString(SettingsKeys.LANGUAGE_SETTING, Language.SYSTEM.code)
        return Language.values().find { it.code == code } ?: Language.SYSTEM
    }
    
    /**
     * 获取当前语言（Flow）
     */
    fun getLanguageFlow(): Flow<Language> {
        return getStringFlow(SettingsKeys.LANGUAGE_SETTING, Language.SYSTEM.code).map { code ->
            Language.values().find { it.code == code } ?: Language.SYSTEM
        }
    }
    
    // ==================== 性能模式管理 ====================
    
    /**
     * 性能模式枚举
     */
    enum class PerformanceMode(val displayName: String, val intervalMs: Long) {
        POWER_SAVING("节能模式", 30000L),      // 30秒
        STANDARD("标准模式", 5000L),           // 5秒
        HIGH_PRECISION("高精度模式", 1000L)     // 1秒
    }
    
    /**
     * 性能模式设置数据类
     */
    @kotlinx.serialization.Serializable
    data class PerformanceModeSettings(
        val mode: String = PerformanceMode.STANDARD.name,
        val autoSwitchEnabled: Boolean = true,
        val batteryThreshold: Int = 20 // 电量低于20%时自动切换到节能模式
    )
    
    /**
     * 设置性能模式配置
     */
    suspend fun setPerformanceModeSettings(settings: PerformanceModeSettings) {
        setJson(SettingsKeys.PERFORMANCE_MODE_SETTINGS, settings)
    }
    
    /**
     * 获取性能模式配置
     */
    suspend fun getPerformanceModeSettings(): PerformanceModeSettings {
        return getJson(SettingsKeys.PERFORMANCE_MODE_SETTINGS, PerformanceModeSettings())
    }
    
    /**
     * 获取性能模式配置（Flow）
     */
    fun getPerformanceModeSettingsFlow(): Flow<PerformanceModeSettings> {
        return getJsonFlow(SettingsKeys.PERFORMANCE_MODE_SETTINGS, PerformanceModeSettings())
    }
    
    /**
     * 设置性能模式
     */
    suspend fun setPerformanceMode(mode: PerformanceMode) {
        val current = getPerformanceModeSettings()
        setPerformanceModeSettings(current.copy(mode = mode.name))
    }
    
    /**
     * 获取当前性能模式
     */
    suspend fun getPerformanceMode(): PerformanceMode {
        val settings = getPerformanceModeSettings()
        return try {
            PerformanceMode.valueOf(settings.mode)
        } catch (e: IllegalArgumentException) {
            PerformanceMode.STANDARD
        }
    }
    
    // ==================== 提醒配置管理 ====================
    
    /**
     * 全局提醒设置数据类
     */
    @kotlinx.serialization.Serializable
    data class GlobalReminderSettings(
        val enabled: Boolean = true,
        val dailyLimitMinutes: Int = 480, // 8小时
        val dailyLimitEnabled: Boolean = true,
        val dailyLimitHours: Int = 8, // 8小时
        val warningPercentages: List<Int> = listOf(80, 90, 100),
        val soundEnabled: Boolean = true,
        val vibrationEnabled: Boolean = true,
        val restReminderEnabled: Boolean = true,
        val restIntervalMinutes: Int = 60 // 1小时
    )
    
    /**
     * 应用提醒设置数据类
     */
    @kotlinx.serialization.Serializable
    data class AppReminderSettings(
        val packageName: String,
        val enabled: Boolean = false,
        val dailyLimitMinutes: Int = 60, // 1小时
        val warningPercentages: List<Int> = listOf(80, 90, 100),
        val soundEnabled: Boolean = true,
        val vibrationEnabled: Boolean = true
    )
    
    /**
     * 免打扰设置数据类
     */
    @kotlinx.serialization.Serializable
    data class DoNotDisturbSettings(
        val enabled: Boolean = false,
        val startHour: Int = 22, // 晚上10点
        val startMinute: Int = 0,
        val endHour: Int = 8,   // 早上8点
        val endMinute: Int = 0,
        val enabledDays: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7), // 周一到周日
        val whitelistApps: Set<String> = emptySet()
    )
    
    /**
     * 设置全局提醒配置
     */
    suspend fun setGlobalReminderSettings(settings: GlobalReminderSettings) {
        setJson(SettingsKeys.GLOBAL_REMINDER_SETTINGS, settings)
    }
    
    /**
     * 获取全局提醒配置
     */
    suspend fun getGlobalReminderSettings(): GlobalReminderSettings {
        return getJson(SettingsKeys.GLOBAL_REMINDER_SETTINGS, GlobalReminderSettings())
    }
    
    /**
     * 获取全局提醒配置（Flow）
     */
    fun getGlobalReminderSettingsFlow(): Flow<GlobalReminderSettings> {
        return getJsonFlow(SettingsKeys.GLOBAL_REMINDER_SETTINGS, GlobalReminderSettings())
    }
    
    /**
     * 设置应用提醒配置
     */
    suspend fun setAppReminderSettings(packageName: String, settings: AppReminderSettings) {
        val key = SettingsKeys.appReminderKey(packageName)
        setJson(key, settings)
    }
    
    /**
     * 获取应用提醒配置
     */
    suspend fun getAppReminderSettings(packageName: String): AppReminderSettings {
        val key = SettingsKeys.appReminderKey(packageName)
        return getJson(key, AppReminderSettings(packageName = packageName))
    }
    
    /**
     * 获取所有应用提醒配置
     */
    suspend fun getAllAppReminderSettings(): List<AppReminderSettings> {
        val allSettings = getAllSettings()
        return allSettings.filter { it.settingKey.startsWith(SettingsKeys.APP_REMINDER_PREFIX) }
            .mapNotNull { setting ->
                try {
                    json.decodeFromString<AppReminderSettings>(setting.settingValue)
                } catch (e: Exception) {
                    null
                }
            }
    }
    
    // ==================== 应用黑名单管理 ====================
    
    /**
     * 设置应用黑名单
     */
    suspend fun setAppBlacklist(packageNames: Set<String>) {
        setJson(SettingsKeys.APP_BLACKLIST, packageNames)
    }
    
    /**
     * 获取应用黑名单
     */
    suspend fun getAppBlacklist(): Set<String> {
        return getJson(SettingsKeys.APP_BLACKLIST, emptySet<String>())
    }
    
    /**
     * 获取应用黑名单（Flow）
     */
    fun getAppBlacklistFlow(): Flow<Set<String>> {
        return getJsonFlow(SettingsKeys.APP_BLACKLIST, emptySet<String>())
    }
    
    /**
     * 添加应用到黑名单
     */
    suspend fun addToBlacklist(packageName: String) {
        val current = getAppBlacklist()
        setAppBlacklist(current + packageName)
    }
    
    /**
     * 从黑名单移除应用
     */
    suspend fun removeFromBlacklist(packageName: String) {
        val current = getAppBlacklist()
        setAppBlacklist(current - packageName)
    }
    
    /**
     * 检查应用是否在黑名单中
     */
    suspend fun isInBlacklist(packageName: String): Boolean {
        return getAppBlacklist().contains(packageName)
    }
    
    // ==================== 其他设置 ====================
    
    /**
     * 设置数据保留天数
     */
    suspend fun setDataRetentionDays(days: Int) {
        setInt(SettingsKeys.DATA_RETENTION_DAYS, days)
    }
    
    /**
     * 获取数据保留天数
     */
    suspend fun getDataRetentionDays(): Int {
        return getInt(SettingsKeys.DATA_RETENTION_DAYS, 30)
    }
    
    /**
     * 设置引导完成状态
     */
    suspend fun setOnboardingCompleted(completed: Boolean) {
        setBoolean(SettingsKeys.ONBOARDING_COMPLETED, completed)
    }
    
    /**
     * 获取引导完成状态
     */
    suspend fun isOnboardingCompleted(): Boolean {
        return getBoolean(SettingsKeys.ONBOARDING_COMPLETED, false)
    }
    
    /**
     * 设置权限授予状态
     */
    suspend fun setPermissionsGranted(granted: Boolean) {
        setBoolean(SettingsKeys.PERMISSIONS_GRANTED, granted)
    }
    
    /**
     * 获取权限授予状态
     */
    suspend fun arePermissionsGranted(): Boolean {
        return getBoolean(SettingsKeys.PERMISSIONS_GRANTED, false)
    }
    
    /**
     * 获取权限授予状态（Flow）
     */
    fun arePermissionsGrantedFlow(): Flow<Boolean> {
        return getBooleanFlow(SettingsKeys.PERMISSIONS_GRANTED, false)
    }
}
