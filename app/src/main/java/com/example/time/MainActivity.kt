package com.example.time

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.time.service.DataCollectionService
import com.example.time.ui.TimeApp
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity - 应用主Activity
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 使用兼容的方式启用边缘到边缘显示
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            TimeApp(
                onStartDataCollection = { startDataCollectionService() }
            )
        }
    }
    
    /**
     * 启动数据收集服务
     */
    private fun startDataCollectionService() {
        val intent = Intent(this, DataCollectionService::class.java)
        startForegroundService(intent)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 注意：通常不需要关闭数据库，让系统管理
        // TimeDatabase.closeDatabase()
    }
}