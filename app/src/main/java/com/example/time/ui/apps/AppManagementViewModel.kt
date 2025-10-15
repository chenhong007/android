package com.example.time.ui.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.time.data.repository.SettingsRepository
import com.example.time.data.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * 应用管理 ViewModel
 * 管理应用列表、搜索、类别过滤、时间限制配置、黑名单状态
 */
@HiltViewModel
class AppManagementViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usageRepository: UsageRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AppManagementState())
    val uiState: StateFlow<AppManagementState> = _uiState.asStateFlow()
    
    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    
    init {
        loadInstalledApps()
        observeBlacklist()
    }
    
    /**
     * 加载已安装应用列表
     */
    private fun loadInstalledApps() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val packageManager = context.packageManager
                val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                
                // 过滤掉系统应用（可选）
                val userApps = installedApps.filter { appInfo ->
                    (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                    (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                }
                
                // 获取今日使用数据
                val today = System.currentTimeMillis()
                val startOfDay = today - (today % (24 * 60 * 60 * 1000))
                
                val appInfoList = userApps.map { appInfo ->
                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    val packageName = appInfo.packageName
                    val category = getAppCategory(appInfo)
                    
                    // 获取今日使用时长
                    val todayUsage = try {
                        usageRepository.getUsageInRange(startOfDay, today).filter { it.packageName == packageName }
                            .sumOf { it.duration }
                    } catch (e: Exception) {
                        0L
                    }
                    
                    // 获取时间限制设置
                    val reminderSettings = settingsRepository.getAppReminderSettings(packageName)
                    val timeLimit = if (reminderSettings.enabled) {
                        reminderSettings.dailyLimitMinutes
                    } else {
                        0
                    }
                    
                    AppInfo(
                        packageName = packageName,
                        appName = appName,
                        category = category,
                        todayUsage = todayUsage,
                        timeLimit = timeLimit,
                        isBlacklisted = false // 将在 observeBlacklist 中更新
                    )
                }.sortedBy { it.appName.lowercase(Locale.getDefault()) }
                
                _allApps.value = appInfoList
                updateFilteredApps()
                
                _uiState.value = _uiState.value.copy(isLoading = false)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "加载应用列表失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 观察黑名单变化
     */
    private fun observeBlacklist() {
        viewModelScope.launch {
            settingsRepository.getAppBlacklistFlow().collect { blacklist ->
                val updatedApps = _allApps.value.map { app ->
                    app.copy(isBlacklisted = blacklist.contains(app.packageName))
                }
                _allApps.value = updatedApps
                updateFilteredApps()
            }
        }
    }
    
    /**
     * 更新搜索查询
     */
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        updateFilteredApps()
    }
    
    /**
     * 选择类别
     */
    fun selectCategory(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        updateFilteredApps()
    }
    
    /**
     * 选择排序方式
     */
    fun selectSort(sortType: SortType) {
        _uiState.value = _uiState.value.copy(selectedSort = sortType)
        updateFilteredApps()
    }
    
    /**
     * 更新过滤后的应用列表
     */
    private fun updateFilteredApps() {
        val currentState = _uiState.value
        val query = currentState.searchQuery.lowercase(Locale.getDefault())
        val category = currentState.selectedCategory
        val sortType = currentState.selectedSort
        
        var filteredApps = _allApps.value.filter { app ->
            // 搜索过滤
            val matchesSearch = query.isEmpty() || 
                app.appName.lowercase(Locale.getDefault()).contains(query) ||
                app.packageName.lowercase(Locale.getDefault()).contains(query)
            
            // 类别过滤
            val matchesCategory = category == null || app.category == category
            
            matchesSearch && matchesCategory
        }
        
        // 排序
        filteredApps = when (sortType) {
            SortType.USAGE_TIME -> filteredApps.sortedByDescending { it.todayUsage }
            SortType.NAME -> filteredApps.sortedBy { it.appName }
            else -> filteredApps
        }
        
        // 计算统计数据
        val now = System.currentTimeMillis()
        val todayStart = now - (now % (24 * 60 * 60 * 1000))
        val weekAgo = now - (7 * 24 * 60 * 60 * 1000)
        
        val todayUsedCount = _allApps.value.count { it.todayUsage > 0 }
        val longUnusedCount = _allApps.value.count { it.todayUsage == 0L }
        
        _uiState.value = currentState.copy(
            filteredApps = filteredApps,
            installedAppsCount = _allApps.value.size,
            todayUsedCount = todayUsedCount,
            longUnusedCount = longUnusedCount
        )
    }
    
    /**
     * 显示时间限制对话框
     */
    fun showTimeLimitDialog(appInfo: AppInfo) {
        _uiState.value = _uiState.value.copy(
            showTimeLimitDialog = true,
            selectedApp = appInfo,
            currentTimeLimit = appInfo.timeLimit
        )
    }
    
    /**
     * 隐藏时间限制对话框
     */
    fun hideTimeLimitDialog() {
        _uiState.value = _uiState.value.copy(
            showTimeLimitDialog = false,
            selectedApp = null,
            currentTimeLimit = 0
        )
    }
    
    /**
     * 设置时间限制
     */
    fun setTimeLimit(packageName: String, limitMinutes: Int) {
        viewModelScope.launch {
            try {
                val currentSettings = settingsRepository.getAppReminderSettings(packageName)
                val newSettings = currentSettings.copy(
                    enabled = limitMinutes > 0,
                    dailyLimitMinutes = limitMinutes
                )
                settingsRepository.setAppReminderSettings(packageName, newSettings)
                
                // 更新本地应用信息
                val updatedApps = _allApps.value.map { app ->
                    if (app.packageName == packageName) {
                        app.copy(timeLimit = limitMinutes)
                    } else {
                        app
                    }
                }
                _allApps.value = updatedApps
                updateFilteredApps()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "设置时间限制失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 切换黑名单状态
     */
    fun toggleBlacklist(packageName: String, isBlacklisted: Boolean) {
        viewModelScope.launch {
            try {
                if (isBlacklisted) {
                    settingsRepository.addToBlacklist(packageName)
                } else {
                    settingsRepository.removeFromBlacklist(packageName)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "更新黑名单失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 显示应用详情
     */
    fun showAppDetail(app: AppInfo) {
        // 当前暂不实现详情页，可以在后续版本添加
        _uiState.value = _uiState.value.copy(
            selectedApp = app,
            showTimeLimitDialog = false
        )
    }
    
    /**
     * 删除应用（卸载）
     */
    fun deleteApp(app: AppInfo) {
        viewModelScope.launch {
            try {
                // Android 无法通过程序直接卸载应用，只能打开系统卸载页面
                // 这里暂时只是从黑名单中移除
                if (app.isBlacklisted) {
                    toggleBlacklist(app.packageName, false)
                }
                
                _uiState.value = _uiState.value.copy(
                    error = "应用卸载需要用户手动操作"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "操作失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 刷新应用列表
     */
    fun refreshApps() {
        loadInstalledApps()
    }
    
    /**
     * 获取应用类别
     */
    private fun getAppCategory(appInfo: ApplicationInfo): String {
        return when (appInfo.category) {
            ApplicationInfo.CATEGORY_GAME -> "游戏"
            ApplicationInfo.CATEGORY_SOCIAL -> "社交"
            ApplicationInfo.CATEGORY_NEWS -> "新闻"
            ApplicationInfo.CATEGORY_MAPS -> "地图"
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> "效率"
            else -> {
                // 根据包名推测类别
                val packageName = appInfo.packageName.lowercase(Locale.getDefault())
                when {
                    packageName.contains("game") -> "游戏"
                    packageName.contains("social") || 
                    packageName.contains("chat") || 
                    packageName.contains("message") -> "社交"
                    packageName.contains("video") || 
                    packageName.contains("music") || 
                    packageName.contains("media") -> "娱乐"
                    packageName.contains("news") -> "新闻"
                    packageName.contains("tool") || 
                    packageName.contains("util") -> "工具"
                    packageName.contains("office") || 
                    packageName.contains("work") || 
                    packageName.contains("productivity") -> "效率"
                    else -> "其他"
                }
            }
        }
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * 应用管理页面状态
 */
data class AppManagementState(
    val apps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val selectedSort: SortType = SortType.NAME,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showTimeLimitDialog: Boolean = false,
    val selectedApp: AppInfo? = null,
    val currentTimeLimit: Int = 0,
    // 统计数据
    val installedAppsCount: Int = 0,
    val todayUsedCount: Int = 0,
    val longUnusedCount: Int = 0
)
