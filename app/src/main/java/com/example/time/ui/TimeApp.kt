package com.example.time.ui

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.time.ui.apps.AppManagementScreen
import com.example.time.ui.apps.AppManagementViewModel
import com.example.time.ui.history.HistoryScreen
import com.example.time.ui.history.HistoryViewModel
import com.example.time.ui.home.HomeScreen
import com.example.time.ui.home.HomeViewModel
import com.example.time.ui.navigation.TimeBottomNavigation
import com.example.time.ui.onboarding.OnboardingScreen
import com.example.time.ui.permissions.PermissionsScreen
import com.example.time.ui.reminders.RemindersScreen
import com.example.time.ui.reminders.RemindersViewModel
import com.example.time.ui.statistics.StatisticsScreen
import com.example.time.ui.statistics.StatisticsViewModel
import com.example.time.ui.theme.TimeTheme

/**
 * Time App - 应用主入口
 * 管理导航和主题
 */
@Composable
fun TimeApp(
    onStartDataCollection: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }
    
    // 检查是否完成引导和权限授予
    var hasCompletedOnboarding by remember {
        mutableStateOf(sharedPreferences.getBoolean("completed_onboarding", false))
    }
    var hasPermissions by remember {
        mutableStateOf(checkAllPermissions(context))
    }
    
    TimeTheme {
        when {
            // 未完成引导
            !hasCompletedOnboarding -> {
                OnboardingScreen(
                    onComplete = {
                        sharedPreferences.edit().putBoolean("completed_onboarding", true).apply()
                        hasCompletedOnboarding = true
                    }
                )
            }
            // 未授予权限
            !hasPermissions -> {
                PermissionsScreen(
                    onPermissionsGranted = {
                        hasPermissions = true
                        // 启动数据收集服务
                        onStartDataCollection()
                    }
                )
            }
            // 主应用界面
            else -> {
                MainApp()
            }
        }
    }
}

/**
 * Main App - 主应用界面
 */
@Composable
private fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    
    Scaffold(
        bottomBar = {
            TimeBottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        // 避免重复导航
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        TimeNavHost(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

/**
 * 检查所有必要权限
 */
private fun checkAllPermissions(context: Context): Boolean {
    return checkUsageStatsPermission(context) && checkNotificationPermission(context)
}

/**
 * 检查通知访问权限
 */
private fun checkNotificationPermission(context: Context): Boolean {
    val enabledListeners = android.provider.Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    )
    return enabledListeners?.contains(context.packageName) == true
}

/**
 * 检查使用统计权限
 */
private fun checkUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

/**
 * Time Nav Host - 导航主机
 */
@Composable
private fun TimeNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        // 首页
        composable("home") {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(viewModel = viewModel)
        }
        
        // 统计页面
        composable("statistics") {
            val viewModel: StatisticsViewModel = hiltViewModel()
            StatisticsScreen(viewModel = viewModel)
        }
        
        // 应用管理页面
        composable("apps") {
            val viewModel: AppManagementViewModel = hiltViewModel()
            AppManagementScreen(viewModel = viewModel)
        }
        
        // 提醒设置页面
        composable("reminders") {
            val viewModel: RemindersViewModel = hiltViewModel()
            RemindersScreen(viewModel = viewModel)
        }
        
        // 历史数据页面
        composable("history") {
            val viewModel: HistoryViewModel = hiltViewModel()
            HistoryScreen(viewModel = viewModel)
        }
    }
}

/**
 * Placeholder Screen - 占位符屏幕
 * 用于尚未实现的页面
 */
@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "即将推出",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

