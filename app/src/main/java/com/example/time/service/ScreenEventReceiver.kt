package com.example.time.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.example.time.data.dao.ScreenEventDao
import com.example.time.data.model.ScreenEvent
import com.example.time.data.model.ScreenEventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen Event Receiver - 屏幕事件接收器
 * 监听屏幕开关和用户解锁事件
 */
class ScreenEventReceiver(
    private val screenEventDao: ScreenEventDao
) : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "ScreenEventReceiver"
        
        private fun getFormattedTime(timestamp: Long = System.currentTimeMillis()): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onReceive(context: Context, intent: Intent) {
        val currentTime = System.currentTimeMillis()
        val formattedTime = getFormattedTime(currentTime)
        
        val eventType = when (intent.action) {
            Intent.ACTION_SCREEN_ON -> ScreenEventType.SCREEN_ON
            Intent.ACTION_SCREEN_OFF -> ScreenEventType.SCREEN_OFF
            Intent.ACTION_USER_PRESENT -> ScreenEventType.USER_PRESENT
            else -> {
                Log.w(TAG, "⚠️ 未知的Intent Action: ${intent.action}")
                return
            }
        }
        
        // ✅ 增强日志：显示清晰的屏幕事件接收标识
        Log.d(TAG, "═══════════════════════════════════════════════════════")
        when (eventType) {
            ScreenEventType.SCREEN_ON -> {
                Log.d(TAG, "🔆 屏幕点亮事件 (SCREEN_ON)")
            }
            ScreenEventType.SCREEN_OFF -> {
                Log.d(TAG, "🌙 屏幕熄灭事件 (SCREEN_OFF)")
            }
            ScreenEventType.USER_PRESENT -> {
                Log.d(TAG, "🔓 用户解锁事件 (USER_PRESENT)")
            }
        }
        Log.d(TAG, "   事件类型: $eventType")
        Log.d(TAG, "   时间戳: $currentTime")
        Log.d(TAG, "   时间: $formattedTime")
        Log.d(TAG, "═══════════════════════════════════════════════════════")
        
        // 记录事件
        recordScreenEvent(eventType, currentTime)
    }
    
    /**
     * 记录屏幕事件
     */
    private fun recordScreenEvent(eventType: ScreenEventType, timestamp: Long) {
        scope.launch {
            try {
                val event = ScreenEvent(
                    eventType = eventType,
                    timestamp = timestamp
                )
                
                Log.d(TAG, "💾 准备写入数据库...")
                val insertedId = screenEventDao.insert(event)
                
                Log.d(TAG, "✅ 数据库写入成功")
                Log.d(TAG, "   记录ID: $insertedId")
                Log.d(TAG, "   事件类型: $eventType")
                Log.d(TAG, "   时间戳: $timestamp")
                Log.d(TAG, "───────────────────────────────────────────────────────")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 数据库写入失败", e)
                Log.e(TAG, "   事件类型: $eventType")
                Log.e(TAG, "   时间戳: $timestamp")
                Log.e(TAG, "   错误信息: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    companion object {
        /**
         * 创建 Intent Filter
         */
        fun createIntentFilter(): IntentFilter {
            return IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
            }
        }
    }
}

