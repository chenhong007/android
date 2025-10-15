package com.example.time.ui.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.time.data.model.AppUsageSummary
import com.example.time.data.repository.UsageRepository
import com.example.time.data.repository.ScreenEventRepository
import com.example.time.debug.DiagnosticHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Home View Model - 首页视图模型
 * 管理首页的数据和业务逻辑
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usageRepository: UsageRepository,
    private val screenEventRepository: ScreenEventRepository
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadTodayData()
    }
    
    /**
     * 加载今日数据
     */
    fun loadTodayData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // ✅ 新增：运行快速诊断
                val diagnostic = DiagnosticHelper.quickDiagnostic(context, usageRepository)
                Log.d("HomeViewModel", "🔍 快速诊断结果:")
                Log.d("HomeViewModel", "  权限: ${if (diagnostic.hasPermission) "✅" else "❌"}")
                Log.d("HomeViewModel", "  服务: ${if (diagnostic.isServiceRunning) "✅" else "❌"}")
                Log.d("HomeViewModel", "  总记录: ${diagnostic.totalRecordCount} 条")
                Log.d("HomeViewModel", "  今日记录: ${diagnostic.todayRecordCount} 条")
                
                val (startTime, endTime) = getTodayTimeRange()
                val (yesterdayStart, yesterdayEnd) = getYesterdayTimeRange()
                
                Log.d("HomeViewModel", "加载今日数据: ${Date(startTime)} 到 ${Date(endTime)}")
                
                // ✅ 性能优化：只查询一次总时长，然后复用
                // SQL: SELECT SUM(duration) FROM usage_tracking WHERE timestamp >= startTime
                val totalDuration = usageRepository.getTotalDuration(startTime, endTime)
                Log.d("HomeViewModel", "📊 今日总使用时长: ${totalDuration}ms (${totalDuration/1000/60}分钟)")
                
                // ✅ 如果数据为0，显示诊断信息
                if (totalDuration == 0L && diagnostic.getErrorMessage() != null) {
                    Log.w("HomeViewModel", "⚠️ 数据为0，可能的问题: ${diagnostic.getErrorMessage()}")
                }
                
                // ✅ 复用 totalDuration，避免重复 SUM 查询
                // SQL: SELECT packageName, SUM(duration), COUNT(*) FROM usage_tracking GROUP BY packageName
                val topApps = usageRepository.getTopApps(startTime, endTime, limit = 5, totalDuration = totalDuration)
                Log.d("HomeViewModel", "📊 Top 5应用数量: ${topApps.size}")
                topApps.forEachIndexed { index, app ->
                    Log.d("HomeViewModel", "  ${index+1}. ${app.appName}: ${app.totalDuration/1000/60}分钟, 打开次数: ${app.sessionCount}")
                }
                
                // ✅ 复用 totalDuration，避免再次查询
                val allApps = usageRepository.getAppUsageSummary(startTime, endTime, totalDuration = totalDuration)
                Log.d("HomeViewModel", "📊 所有应用数量: ${allApps.size}")
                
                // 获取真实的解锁次数（从 ScreenEventRepository）
                Log.d("HomeViewModel", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d("HomeViewModel", "🔓 查询解锁次数...")
                val unlockCount = screenEventRepository.getUnlockCount(startTime, endTime)
                Log.d("HomeViewModel", "   解锁次数: $unlockCount 次")
                if (unlockCount == 0) {
                    Log.w("HomeViewModel", "   ⚠️ 解锁次数为0，可能原因：")
                    Log.w("HomeViewModel", "      1. screen_events表中没有USER_PRESENT事件")
                    Log.w("HomeViewModel", "      2. ScreenEventReceiver未正常工作")
                    Log.w("HomeViewModel", "      3. 时间范围内确实没有解锁")
                }
                Log.d("HomeViewModel", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                
                // 获取屏幕开启时长
                Log.d("HomeViewModel", "🔆 查询屏幕开启时长...")
                val screenOnDuration = screenEventRepository.calculateScreenOnDuration(startTime, endTime)
                Log.d("HomeViewModel", "   屏幕时长: ${screenOnDuration}ms (${screenOnDuration/1000/60}分钟)")
                if (screenOnDuration == 0L) {
                    Log.w("HomeViewModel", "   ⚠️ 屏幕时长为0，可能原因：")
                    Log.w("HomeViewModel", "      1. screen_events表中没有SCREEN_ON/OFF事件")
                    Log.w("HomeViewModel", "      2. ScreenEventReceiver未正常工作")
                    Log.w("HomeViewModel", "      3. 时间范围内屏幕未打开")
                }
                Log.d("HomeViewModel", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                
                // 计算与昨天的对比（使用时长）
                val yesterdayDuration = usageRepository.getTotalDuration(yesterdayStart, yesterdayEnd)
                val durationDiff = totalDuration - yesterdayDuration
                val isDecrease = durationDiff < 0
                
                // 计算与昨天的对比（解锁次数）
                val yesterdayUnlockCount = screenEventRepository.getUnlockCount(yesterdayStart, yesterdayEnd)
                val unlockCountDiff = unlockCount - yesterdayUnlockCount
                
                // 计算与昨天的对比（屏幕时长）
                val yesterdayScreenOnDuration = screenEventRepository.calculateScreenOnDuration(yesterdayStart, yesterdayEnd)
                val screenOnDurationDiff = screenOnDuration - yesterdayScreenOnDuration
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    greeting = getGreeting(),
                    totalDuration = totalDuration,
                    unlockCount = unlockCount,
                    screenOnDuration = screenOnDuration,
                    topApps = topApps,
                    yesterdayComparison = formatDuration(Math.abs(durationDiff)),
                    isDecrease = isDecrease,
                    unlockCountDiff = unlockCountDiff,
                    screenOnDurationDiff = screenOnDurationDiff,
                    diagnosticResult = diagnostic
                )
                
                Log.d("HomeViewModel", "数据加载完成")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "加载数据失败", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载数据失败"
                )
            }
        }
    }
    
    /**
     * 运行完整诊断（输出到Logcat）
     */
    fun runFullDiagnostic() {
        viewModelScope.launch {
            DiagnosticHelper.runFullDiagnostic(context, usageRepository)
        }
    }
    
    /**
     * 刷新数据
     */
    fun refresh() {
        loadTodayData()
    }
    
    /**
     * 获取今日时间范围
     */
    private fun getTodayTimeRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endTime = calendar.timeInMillis
        
        return Pair(startTime, endTime)
    }
    
    /**
     * 获取昨日时间范围
     */
    private fun getYesterdayTimeRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endTime = calendar.timeInMillis
        
        return Pair(startTime, endTime)
    }
    
    /**
     * 获取问候语
     */
    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..5 -> "凌晨好"
            in 6..11 -> "早上好"
            in 12..13 -> "中午好"
            in 14..17 -> "下午好"
            in 18..23 -> "晚上好"
            else -> "你好"
        }
    }
    
    /**
     * 格式化时长
     */
    private fun formatDuration(millis: Long): String {
        val hours = millis / (1000 * 60 * 60)
        val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
        
        return if (hours > 0) {
            "${hours}小时 ${minutes}分"
        } else {
            "${minutes}分钟"
        }
    }
}

/**
 * Home UI State - 首页UI状态
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val greeting: String = "你好",
    val totalDuration: Long = 0,
    val unlockCount: Int = 0,
    val screenOnDuration: Long = 0,
    val topApps: List<AppUsageSummary> = emptyList(),
    val yesterdayComparison: String = "0分钟",
    val isDecrease: Boolean = false,
    val unlockCountDiff: Int = 0,
    val screenOnDurationDiff: Long = 0,
    val diagnosticResult: com.example.time.debug.DiagnosticResult? = null
) {
    /**
     * 计算解锁次数进度
     * 基于合理的日均目标（100次）来计算进度
     */
    fun getUnlockProgress(): Float {
        val targetUnlockCount = 100 // 合理的日均解锁目标
        return (unlockCount.toFloat() / targetUnlockCount).coerceIn(0f, 1f)
    }
    
    /**
     * 计算屏幕时长进度
     * 基于合理的日均目标（8小时）来计算进度
     */
    fun getScreenOnProgress(): Float {
        val targetScreenTime = 8 * 60 * 60 * 1000L // 8小时（毫秒）
        return (screenOnDuration.toFloat() / targetScreenTime).coerceIn(0f, 1f)
    }
}

