package com.example.time.ui.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.time.data.repository.SettingsRepository.AppReminderSettings
import com.example.time.ui.components.*
import com.example.time.ui.theme.*

/**
 * 提醒设置页面
 * 严格按照 UI/提醒设置页面.html 实现
 */
@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = BackgroundGradientBrush)
            .padding(horizontal = Spacing.ExtraLarge),
        verticalArrangement = Arrangement.spacedBy(Spacing.ExtraLarge)
    ) {
        // 顶部间距
        item {
            Spacer(modifier = Modifier.height(Spacing.Large))
        }
        
        // 头部标题
        item {
            Column {
                Text(
                    text = "提醒设置",
                    style = MaterialTheme.typography.displayLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(Spacing.Small))
                Text(
                    text = "管理您的使用时长提醒",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextSecondary
                )
            }
        }
        
        // 启用提醒总开关
        item {
            ReminderMasterSwitch(
                enabled = uiState.globalSettings.enabled,
                onEnabledChange = { 
                    viewModel.updateGlobalSettings(
                        uiState.globalSettings.copy(enabled = it)
                    )
                }
            )
        }
        
        // 每日使用时长提醒
        item {
            DailyLimitReminderCard(
                enabled = uiState.globalSettings.dailyLimitEnabled,
                hours = uiState.globalSettings.dailyLimitHours,
                minutes = uiState.globalSettings.dailyLimitMinutes,
                onEnabledChange = { 
                    viewModel.updateGlobalSettings(
                        uiState.globalSettings.copy(dailyLimitEnabled = it)
                    )
                },
                onHoursChange = { 
                    viewModel.updateGlobalSettings(
                        uiState.globalSettings.copy(dailyLimitHours = it)
                    )
                },
                onMinutesChange = { 
                    viewModel.updateGlobalSettings(
                        uiState.globalSettings.copy(dailyLimitMinutes = it)
                    )
                }
            )
        }
        
        // 单个应用提醒
        item {
            AppSpecificReminderCard(
                enabled = uiState.appReminders.isNotEmpty(),
                apps = uiState.appReminders.map { settings ->
                    AppReminderItem(
                        packageName = settings.packageName,
                        name = settings.packageName, // 简化处理，实际应该获取应用名称
                        icon = Icons.Default.Apps,
                        iconColor = BrandBlue,
                        backgroundColor = BackgroundLightSecondary,
                        limitHours = settings.dailyLimitMinutes / 60,
                        enabled = settings.enabled,
                        dailyLimitMinutes = settings.dailyLimitMinutes,
                        warningPercentages = settings.warningPercentages
                    )
                },
                onEnabledChange = { /* 开关由添加/删除应用控制 */ },
                onAddApp = { viewModel.showAppSelectionDialog() },
                onEditApp = { app -> viewModel.editAppReminder(app.packageName) }
            )
        }
        
        // 休息提醒
        item {
            RestReminderCard(
                enabled = uiState.globalSettings.restReminderEnabled,
                intervalMinutes = uiState.globalSettings.restIntervalMinutes,
                onEnabledChange = { 
                    viewModel.updateGlobalSettings(
                        uiState.globalSettings.copy(restReminderEnabled = it)
                    )
                },
                onIntervalChange = { 
                    viewModel.updateGlobalSettings(
                        uiState.globalSettings.copy(restIntervalMinutes = it)
                    )
                }
            )
        }
        
        // 免打扰时段
        item {
            DoNotDisturbCard(
                enabled = uiState.doNotDisturbSettings.enabled,
                startTime = "${uiState.doNotDisturbSettings.startHour}:${String.format("%02d", uiState.doNotDisturbSettings.startMinute)}",
                endTime = "${uiState.doNotDisturbSettings.endHour}:${String.format("%02d", uiState.doNotDisturbSettings.endMinute)}",
                onEnabledChange = { 
                    viewModel.updateDoNotDisturbSettings(
                        uiState.doNotDisturbSettings.copy(enabled = it)
                    )
                },
                onStartTimeClick = { /* TODO: 显示时间选择器 */ },
                onEndTimeClick = { /* TODO: 显示时间选择器 */ }
            )
        }
        
        // 底部间距
        item {
            Spacer(modifier = Modifier.height(ComponentSize.BottomNavHeight))
        }
    }
}

/**
 * 提醒总开关
 */
@Composable
private fun ReminderMasterSwitch(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit
) {
    GlassmorphismCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = CornerRadius.Large
    ) {
        Row(
            modifier = Modifier.padding(Spacing.ExtraLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFFa855f7), Color(0xFF9333ea))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(IconSize.Large)
                )
            }
            
            Spacer(modifier = Modifier.width(Spacing.Large))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "启用提醒",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "开启所有使用时长提醒",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            BrandToggleSwitch(
                checked = enabled,
                onCheckedChange = onEnabledChange
            )
        }
    }
}

/**
 * 每日使用时长提醒卡片
 */
@Composable
private fun DailyLimitReminderCard(
    enabled: Boolean,
    hours: Int,
    minutes: Int,
    onEnabledChange: (Boolean) -> Unit,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.Medium),
        color = Color.White.copy(alpha = 0.8f),
        shadowElevation = Elevation.Small
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFF3b82f6), Color(0xFF2563eb))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Smartphone,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(IconSize.Small)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(Spacing.Medium))
                    
                    Column {
                        Text(
                            text = "每日使用时长提醒",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "设置每日总使用时长阈值",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                
                BrandToggleSwitch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange
                )
            }
            
            if (enabled) {
                Spacer(modifier = Modifier.height(Spacing.Large))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.Large)
                ) {
                    // 提醒阈值
                    Column {
                        Text(
                            text = "提醒阈值",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(Spacing.Small))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
                        ) {
                            OutlinedTextField(
                                value = hours.toString(),
                                onValueChange = { /* onHoursChange */ },
                                modifier = Modifier.width(80.dp),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandBlue,
                                    unfocusedBorderColor = NeutralGray.copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(CornerRadius.Medium)
                            )
                            Text(text = "小时", color = TextSecondary)
                            
                            OutlinedTextField(
                                value = minutes.toString(),
                                onValueChange = { /* onMinutesChange */ },
                                modifier = Modifier.width(80.dp),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandBlue,
                                    unfocusedBorderColor = NeutralGray.copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(CornerRadius.Medium)
                            )
                            Text(text = "分钟", color = TextSecondary)
                        }
                    }
                    
                    // 快速设置
                    Column {
                        Text(
                            text = "快速设置",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(Spacing.Small))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
                        ) {
                            FilterButton(
                                text = "3小时",
                                selected = false,
                                onClick = { /* Set 3 hours */ }
                            )
                            FilterButton(
                                text = "5.5小时",
                                selected = true,
                                onClick = { /* Set 5.5 hours */ }
                            )
                            FilterButton(
                                text = "8小时",
                                selected = false,
                                onClick = { /* Set 8 hours */ }
                            )
                        }
                    }
                    
                    // 提醒预览
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(CornerRadius.Medium),
                        color = Color(0xFF1e293b)
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.Large),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Smartphone,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(IconSize.Small)
                            )
                            Spacer(modifier = Modifier.width(Spacing.Small))
                            Column {
                                Text(
                                    text = "提醒预览",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "您今天已使用手机${hours}小时${minutes}分钟，建议适当休息一下。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 单个应用提醒卡片
 */
@Composable
private fun AppSpecificReminderCard(
    enabled: Boolean,
    apps: List<AppReminderItem>,
    onEnabledChange: (Boolean) -> Unit,
    onAddApp: () -> Unit,
    onEditApp: (AppReminderItem) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.Medium),
        color = Color.White.copy(alpha = 0.8f),
        shadowElevation = Elevation.Small
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFF10b981), Color(0xFF059669))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(IconSize.Small)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(Spacing.Medium))
                    
                    Column {
                        Text(
                            text = "单个应用提醒",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "为特定应用设置使用时长提醒",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                
                BrandToggleSwitch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange
                )
            }
            
            if (enabled) {
                Spacer(modifier = Modifier.height(Spacing.Large))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
                ) {
                    apps.forEach { app ->
                        AppReminderRow(app = app, onEdit = { onEditApp(app) })
                    }
                    
                    // 添加应用按钮
                    OutlinedButton(
                        onClick = onAddApp,
                        modifier = Modifier.fillMaxWidth(),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = SolidColor(NeutralGray.copy(alpha = 0.3f)),
                            width = 2.dp
                        ),
                        shape = RoundedCornerShape(CornerRadius.Medium)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(IconSize.Small)
                        )
                        Spacer(modifier = Modifier.width(Spacing.Small))
                        Text("添加应用提醒")
                    }
                }
            }
        }
    }
}

/**
 * 应用提醒行
 */
@Composable
private fun AppReminderRow(
    app: AppReminderItem,
    onEdit: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.Medium),
        color = app.backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(Spacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = app.iconColor,
                        shape = RoundedCornerShape(CornerRadius.Small)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = app.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(Spacing.Medium))
            
            Text(
                text = app.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                OutlinedTextField(
                    value = app.limitHours.toString(),
                    onValueChange = { },
                    modifier = Modifier.width(64.dp),
                    textStyle = MaterialTheme.typography.bodySmall,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandBlue,
                        unfocusedBorderColor = NeutralGray.copy(alpha = 0.2f)
                    )
                )
                Text(
                    text = "小时",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

/**
 * 休息提醒卡片
 */
@Composable
private fun RestReminderCard(
    enabled: Boolean,
    intervalMinutes: Int,
    onEnabledChange: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.Medium),
        color = Color.White.copy(alpha = 0.8f),
        shadowElevation = Elevation.Small
    ) {
        Column(
            modifier = Modifier
                .padding(Spacing.Large)
                .then(if (!enabled) Modifier.alpha(0.5f) else Modifier)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFFf59e0b), Color(0xFFf97316))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalCafe,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(IconSize.Small)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(Spacing.Medium))
                    
                    Column {
                        Text(
                            text = "休息提醒",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "定时提醒您休息眼睛",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                
                BrandToggleSwitch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange
                )
            }
            
            if (enabled) {
                Spacer(modifier = Modifier.height(Spacing.Large))
                
                Column {
                    Text(
                        text = "提醒间隔",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(Spacing.Small))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
                    ) {
                        OutlinedTextField(
                            value = intervalMinutes.toString(),
                            onValueChange = { /* onIntervalChange */ },
                            modifier = Modifier.width(80.dp),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandBlue,
                                unfocusedBorderColor = NeutralGray.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(CornerRadius.Medium)
                        )
                        Text(text = "分钟", color = TextSecondary)
                    }
                    
                    Spacer(modifier = Modifier.height(Spacing.Medium))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
                    ) {
                        FilterButton(
                            text = "20分钟",
                            selected = false,
                            onClick = { /* Set 20 min */ }
                        )
                        FilterButton(
                            text = "30分钟",
                            selected = true,
                            onClick = { /* Set 30 min */ }
                        )
                        FilterButton(
                            text = "60分钟",
                            selected = false,
                            onClick = { /* Set 60 min */ }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 免打扰卡片
 */
@Composable
private fun DoNotDisturbCard(
    enabled: Boolean,
    startTime: String,
    endTime: String,
    onEnabledChange: (Boolean) -> Unit,
    onStartTimeClick: () -> Unit,
    onEndTimeClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.Medium),
        color = Color.White.copy(alpha = 0.8f),
        shadowElevation = Elevation.Small
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFF6366f1), Color(0xFF4f46e5))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NightsStay,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(IconSize.Small)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(Spacing.Medium))
                    
                    Column {
                        Text(
                            text = "免打扰时段",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "设置不接收提醒的时间段",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                
                BrandToggleSwitch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange
                )
            }
            
            if (enabled) {
                Spacer(modifier = Modifier.height(Spacing.Large))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Large)
                ) {
                    OutlinedButton(
                        onClick = onStartTimeClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(CornerRadius.Medium)
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "开始时间",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                            Text(
                                text = startTime,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    OutlinedButton(
                        onClick = onEndTimeClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(CornerRadius.Medium)
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "结束时间",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                            Text(
                                text = endTime,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// Data classes

/**
 * 应用提醒项数据类
 */
data class AppReminderItem(
    val packageName: String,
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconColor: Color,
    val backgroundColor: Color,
    val limitHours: Int,
    val enabled: Boolean = true,
    val dailyLimitMinutes: Int = 0,
    val warningPercentages: List<Int> = emptyList()
)
