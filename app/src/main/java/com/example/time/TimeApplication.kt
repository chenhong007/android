package com.example.time

import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Time Application
 * Hilt 入口点
 */
@HiltAndroidApp
class TimeApplication : Application() {
    
    companion object {
        private const val TAG = "TimeApplication"
        
        /**
         * 获取24小时标准时间格式的时间戳
         */
        private fun getFormattedTime(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
            return sdf.format(Date())
        }
    }
    
    private lateinit var appName: String
    private lateinit var packageName: String
    
    override fun onCreate() {
        super.onCreate()
        
        // 获取应用信息
        appName = getString(applicationInfo.labelRes)
        packageName = getPackageName()
        
        // 注册应用前台/后台状态监听
        setupAppLifecycleObserver()
        
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "✅ 应用启动完成")
        Log.d(TAG, "   应用名称: $appName")
        Log.d(TAG, "   包名: $packageName")
        Log.d(TAG, "   启动时间: ${getFormattedTime()}")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
    
    /**
     * 设置应用生命周期监听器
     * 监听应用进入前台和后台
     */
    private fun setupAppLifecycleObserver() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                val currentTime = getFormattedTime()
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        // 应用进入前台
                        Log.d(TAG, "╔════════════════════════════════════════╗")
                        Log.d(TAG, "║  🟢 应用切换到【前台】FOREGROUND      ║")
                        Log.d(TAG, "║  应用: $appName")
                        Log.d(TAG, "║  包名: $packageName")
                        Log.d(TAG, "║  时间: $currentTime")
                        Log.d(TAG, "╚════════════════════════════════════════╝")
                    }
                    Lifecycle.Event.ON_STOP -> {
                        // 应用进入后台
                        Log.d(TAG, "╔════════════════════════════════════════╗")
                        Log.d(TAG, "║  🔴 应用切换到【后台】BACKGROUND      ║")
                        Log.d(TAG, "║  应用: $appName")
                        Log.d(TAG, "║  包名: $packageName")
                        Log.d(TAG, "║  时间: $currentTime")
                        Log.d(TAG, "╚════════════════════════════════════════╝")
                    }
                    Lifecycle.Event.ON_RESUME -> {
                        // 应用已恢复
                        Log.d(TAG, "╔════════════════════════════════════════╗")
                        Log.d(TAG, "║  📱 应用已恢复 (ON_RESUME)            ║")
                        Log.d(TAG, "║  应用: $appName")
                        Log.d(TAG, "║  包名: $packageName")
                        Log.d(TAG, "║  时间: $currentTime")
                        Log.d(TAG, "╚════════════════════════════════════════╝")
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        // 应用已暂停
                        Log.d(TAG, "╔════════════════════════════════════════╗")
                        Log.d(TAG, "║  ⏸️ 应用已暂停 (ON_PAUSE)             ║")
                        Log.d(TAG, "║  应用: $appName")
                        Log.d(TAG, "║  包名: $packageName")
                        Log.d(TAG, "║  时间: $currentTime")
                        Log.d(TAG, "╚════════════════════════════════════════╝")
                    }
                    Lifecycle.Event.ON_CREATE -> {
                        // 应用进程创建
                        Log.d(TAG, "╔════════════════════════════════════════╗")
                        Log.d(TAG, "║  🆕 应用进程创建 (ON_CREATE)          ║")
                        Log.d(TAG, "║  应用: $appName")
                        Log.d(TAG, "║  包名: $packageName")
                        Log.d(TAG, "║  时间: $currentTime")
                        Log.d(TAG, "╚════════════════════════════════════════╝")
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        // 应用进程销毁
                        Log.d(TAG, "╔════════════════════════════════════════╗")
                        Log.d(TAG, "║  💀 应用进程销毁 (ON_DESTROY)         ║")
                        Log.d(TAG, "║  应用: $appName")
                        Log.d(TAG, "║  包名: $packageName")
                        Log.d(TAG, "║  时间: $currentTime")
                        Log.d(TAG, "╚════════════════════════════════════════╝")
                    }
                    else -> {
                        // 其他生命周期事件
                        Log.d(TAG, "╔════════════════════════════════════════╗")
                        Log.d(TAG, "║  🔄 生命周期事件: $event")
                        Log.d(TAG, "║  应用: $appName")
                        Log.d(TAG, "║  包名: $packageName")
                        Log.d(TAG, "║  时间: $currentTime")
                        Log.d(TAG, "╚════════════════════════════════════════╝")
                    }
                }
            }
        })
        
        Log.i(TAG, "✓ 应用生命周期监听器已注册 - 时间: ${getFormattedTime()}")
    }
}
