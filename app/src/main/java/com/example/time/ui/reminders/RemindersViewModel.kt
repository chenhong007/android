package com.example.time.ui.reminders

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.time.data.repository.SettingsRepository
import com.example.time.data.repository.SettingsRepository.GlobalReminderSettings
import com.example.time.data.repository.SettingsRepository.AppReminderSettings
import com.example.time.data.repository.SettingsRepository.DoNotDisturbSettings
import com.example.time.ui.apps.AppInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 提醒设置 ViewModel
 * 管理全局限制状态、每应用限制状态、免打扰时段、白名单等
 */
@HiltViewModel
class RemindersViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RemindersState())
    val uiState: StateFlow<RemindersState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
        loadAvailableApps()
    }
    
    /**
     * 加载设置
     */
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                // 加载全局设置
                val globalSettings = settingsRepository.getGlobalReminderSettings()
                
                // 加载免打扰设置
                val dndSettings = settingsRepository.getJson(
                    "do_not_disturb_settings",
                    DoNotDisturbSettings()
                )
                
                // 加载应用特定提醒设置
                val appReminders = settingsRepository.getAllAppReminderSettings()
                
                _uiState.value = _uiState.value.copy(
                    globalSettings = globalSettings,
                    doNotDisturbSettings = dndSettings,
                    appReminders = appReminders,
                    isLoading = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "加载设置失败: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * 加载可用应用列表
     */
    private fun loadAvailableApps() {
        viewModelScope.launch {
            try {
                val packageManager = context.packageManager
                val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                
                val availableApps = installedApps
                    .filter { appInfo ->
                        // 过滤掉系统应用
                        (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 ||
                        (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                    }
                    .map { appInfo ->
                        AppInfo(
                            packageName = appInfo.packageName,
                            appName = packageManager.getApplicationLabel(appInfo).toString(),
                            category = "其他", // 简化处理
                            todayUsage = 0L,
                            timeLimit = 0,
                            isBlacklisted = false
                        )
                    }
                    .sortedBy { it.appName }
                
                _uiState.value = _uiState.value.copy(availableApps = availableApps)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "加载应用列表失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 更新全局设置
     */
    fun updateGlobalSettings(settings: GlobalReminderSettings) {
        viewModelScope.launch {
            try {
                settingsRepository.setGlobalReminderSettings(settings)
                _uiState.value = _uiState.value.copy(globalSettings = settings)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "保存全局设置失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 更新免打扰设置
     */
    fun updateDoNotDisturbSettings(settings: DoNotDisturbSettings) {
        viewModelScope.launch {
            try {
                settingsRepository.setJson("do_not_disturb_settings", settings)
                _uiState.value = _uiState.value.copy(doNotDisturbSettings = settings)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "保存免打扰设置失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 显示应用选择对话框
     */
    fun showAppSelectionDialog() {
        _uiState.value = _uiState.value.copy(showAppSelectionDialog = true)
    }
    
    /**
     * 隐藏应用选择对话框
     */
    fun hideAppSelectionDialog() {
        _uiState.value = _uiState.value.copy(showAppSelectionDialog = false)
    }
    
    /**
     * 添加应用提醒
     */
    fun addAppReminder(appInfo: AppInfo) {
        viewModelScope.launch {
            try {
                val newReminder = AppReminderSettings(
                    packageName = appInfo.packageName,
                    enabled = true,
                    dailyLimitMinutes = 60, // 默认1小时
                    warningPercentages = listOf(80, 90, 100),
                    soundEnabled = true,
                    vibrationEnabled = true
                )
                
                settingsRepository.setAppReminderSettings(appInfo.packageName, newReminder)
                
                val updatedReminders = _uiState.value.appReminders + newReminder
                _uiState.value = _uiState.value.copy(
                    appReminders = updatedReminders,
                    showAppSelectionDialog = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "添加应用提醒失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 移除应用提醒
     */
    fun removeAppReminder(packageName: String) {
        viewModelScope.launch {
            try {
                // 删除设置
                settingsRepository.remove("app_reminder_$packageName")
                
                val updatedReminders = _uiState.value.appReminders.filter { 
                    it.packageName != packageName 
                }
                _uiState.value = _uiState.value.copy(appReminders = updatedReminders)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "移除应用提醒失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 编辑应用提醒
     */
    fun editAppReminder(packageName: String) {
        val appReminder = _uiState.value.appReminders.find { it.packageName == packageName }
        if (appReminder != null) {
            _uiState.value = _uiState.value.copy(
                showAppReminderDialog = true,
                editingAppReminder = appReminder
            )
        }
    }
    
    /**
     * 隐藏应用提醒编辑对话框
     */
    fun hideAppReminderDialog() {
        _uiState.value = _uiState.value.copy(
            showAppReminderDialog = false,
            editingAppReminder = null
        )
    }
    
    /**
     * 保存应用提醒设置
     */
    fun saveAppReminder(reminder: AppReminderSettings) {
        viewModelScope.launch {
            try {
                settingsRepository.setAppReminderSettings(reminder.packageName, reminder)
                
                val updatedReminders = _uiState.value.appReminders.map { 
                    if (it.packageName == reminder.packageName) reminder else it
                }
                _uiState.value = _uiState.value.copy(
                    appReminders = updatedReminders,
                    showAppReminderDialog = false,
                    editingAppReminder = null
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "保存应用提醒失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 验证时间设置合法性
     */
    fun validateTimeSettings(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ): Boolean {
        // 简单验证：开始时间不能等于结束时间
        return !(startHour == endHour && startMinute == endMinute)
    }
    
    /**
     * 检查当前是否在免打扰时段
     */
    fun isInDoNotDisturbPeriod(): Boolean {
        val dndSettings = _uiState.value.doNotDisturbSettings
        if (!dndSettings.enabled) return false
        
        val now = java.util.Calendar.getInstance()
        val currentHour = now.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(java.util.Calendar.MINUTE)
        val currentDayOfWeek = now.get(java.util.Calendar.DAY_OF_WEEK)
        
        // 检查是否在启用的星期
        val adjustedDayOfWeek = if (currentDayOfWeek == 1) 7 else currentDayOfWeek - 1 // 调整为周一=1的格式
        if (!dndSettings.enabledDays.contains(adjustedDayOfWeek)) return false
        
        // 检查时间范围
        val currentTimeMinutes = currentHour * 60 + currentMinute
        val startTimeMinutes = dndSettings.startHour * 60 + dndSettings.startMinute
        val endTimeMinutes = dndSettings.endHour * 60 + dndSettings.endMinute
        
        return if (startTimeMinutes <= endTimeMinutes) {
            // 同一天内的时间段
            currentTimeMinutes in startTimeMinutes..endTimeMinutes
        } else {
            // 跨天的时间段
            currentTimeMinutes >= startTimeMinutes || currentTimeMinutes <= endTimeMinutes
        }
    }
    
    /**
     * 获取应用的提醒设置
     */
    fun getAppReminderSettings(packageName: String): AppReminderSettings? {
        return _uiState.value.appReminders.find { it.packageName == packageName }
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * 刷新设置
     */
    fun refreshSettings() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadSettings()
    }
}

/**
 * 提醒设置页面状态
 */
data class RemindersState(
    val globalSettings: GlobalReminderSettings = GlobalReminderSettings(),
    val doNotDisturbSettings: DoNotDisturbSettings = DoNotDisturbSettings(),
    val appReminders: List<AppReminderSettings> = emptyList(),
    val availableApps: List<AppInfo> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showAppSelectionDialog: Boolean = false,
    val showAppReminderDialog: Boolean = false,
    val editingAppReminder: AppReminderSettings? = null
)
