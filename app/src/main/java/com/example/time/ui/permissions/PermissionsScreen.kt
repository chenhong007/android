package com.example.time.ui.permissions

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.time.ui.components.*
import com.example.time.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Permissions Screen - 权限请求页面
 * 引导用户授予必要的权限
 */
@Composable
fun PermissionsScreen(
    onPermissionsGranted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var usageStatsGranted by remember { mutableStateOf(checkUsageStatsPermission(context)) }
    var notificationGranted by remember { mutableStateOf(checkNotificationPermission(context)) }
    
    // 检查所有权限是否已授予
    LaunchedEffect(usageStatsGranted, notificationGranted) {
        if (usageStatsGranted && notificationGranted) {
            onPermissionsGranted()
        }
    }
    
    // 当页面重新显示时（从设置返回），重新检查权限状态
    androidx.compose.runtime.DisposableEffect(Unit) {
        val listener = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                // 延迟检查以确保系统权限状态已同步
                scope.launch {
                    delay(1000)  // 增加延迟时间
                    usageStatsGranted = checkUsageStatsPermission(context)
                    notificationGranted = checkNotificationPermission(context)
                }
            }
        }
        
        val lifecycleOwner = context as? androidx.lifecycle.LifecycleOwner
        lifecycleOwner?.lifecycle?.addObserver(listener)
        
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(listener)
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = BackgroundGradientBrush)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.ExtraLarge),
            verticalArrangement = Arrangement.spacedBy(Spacing.Large)
        ) {
            // 顶部说明
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Logo
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                brush = BrandGradientBrush,
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "安全",
                            tint = Color.White,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(Spacing.ExtraLarge))
                    
                    Text(
                        text = "授予必要权限",
                        style = MaterialTheme.typography.displayMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(Spacing.Medium))
                    
                    Text(
                        text = "为了准确记录您的使用数据，我们需要以下权限。所有数据都会加密存储在本地，绝不上传。",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // 权限列表
            items(
                listOf(
                    PermissionItem(
                        icon = Icons.Default.PhoneAndroid,
                        title = "使用情况访问权限",
                        description = "允许应用查看您的应用使用统计数据，这是核心功能的基础。",
                        isGranted = usageStatsGranted,
                        onRequest = {
                            requestUsageStatsPermission(context)
                            // 不在这里检查，依赖onResume自动检查
                        }
                    ),
                    PermissionItem(
                        icon = Icons.Default.Notifications,
                        title = "通知访问权限",
                        description = "允许应用读取通知信息，用于分析通知交互模式。",
                        isGranted = notificationGranted,
                        onRequest = {
                            requestNotificationPermission(context)
                            // 不在这里检查，依赖onResume自动检查
                        }
                    )
                )
            ) { permission ->
                PermissionCard(permission = permission)
            }
            
            // 底部说明
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.Large),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "信息",
                            tint = BrandBlue,
                            modifier = Modifier.size(IconSize.Large)
                        )
                        
                        Spacer(modifier = Modifier.width(Spacing.Medium))
                        
                        Text(
                            text = "您可以随时在系统设置中撤销这些权限。应用将继续工作，但部分功能可能受限。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            // 底部间距
            item {
                Spacer(modifier = Modifier.height(Spacing.Large))
            }
        }
    }
}

/**
 * 权限卡片
 */
@Composable
private fun PermissionCard(
    permission: PermissionItem,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = if (permission.isGranted) 
            SuccessGreen.copy(alpha = 0.1f) 
        else 
            Color.White.copy(alpha = 0.95f)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.Large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GradientIconContainer(
                        size = ComponentSize.AppIconMedium,
                        brush = if (permission.isGranted) 
                            SuccessGradientBrush 
                        else 
                            BrandGradientBrush
                    ) {
                        Icon(
                            imageVector = permission.icon,
                            contentDescription = permission.title,
                            tint = Color.White,
                            modifier = Modifier.size(IconSize.Medium)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(Spacing.Medium))
                    
                    Text(
                        text = permission.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // 状态图标
                if (permission.isGranted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "已授予",
                        tint = SuccessGreen,
                        modifier = Modifier.size(IconSize.Medium)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.Medium))
            
            Text(
                text = permission.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            if (!permission.isGranted) {
                Spacer(modifier = Modifier.height(Spacing.Large))
                
                BrandButton(
                    onClick = permission.onRequest,
                    modifier = Modifier.fillMaxWidth(),
                    type = ButtonType.Primary
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "授予权限",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(Spacing.Small))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(IconSize.Small)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 权限项数据类
 */
data class PermissionItem(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val isGranted: Boolean,
    val onRequest: () -> Unit
)

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
 * 请求使用统计权限
 */
private fun requestUsageStatsPermission(context: Context) {
    try {
        // 尝试直接跳转到应用专属的使用权限设置页面
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.data = android.net.Uri.parse("package:${context.packageName}")
        context.startActivity(intent)
    } catch (e: Exception) {
        // 如果失败，使用通用设置页面
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        context.startActivity(intent)
    }
}

/**
 * 检查通知访问权限
 */
private fun checkNotificationPermission(context: Context): Boolean {
    val enabledListeners = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    )
    return enabledListeners?.contains(context.packageName) == true
}

/**
 * 请求通知访问权限
 */
private fun requestNotificationPermission(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    context.startActivity(intent)
}

