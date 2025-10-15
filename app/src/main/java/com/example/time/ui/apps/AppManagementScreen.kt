package com.example.time.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.time.ui.components.*
import com.example.time.ui.theme.*

/**
 * 应用管理页面
 * 严格按照 UI/应用管理页面.html 实现
 */
@Composable
fun AppManagementScreen(
    viewModel: AppManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = BackgroundGradientBrush)
    ) {
        // 顶部固定区域（头部卡片）
        HeaderCard(
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = { query -> viewModel.updateSearchQuery(query) },
            selectedSort = uiState.selectedSort,
            onSortSelected = { sort -> viewModel.selectSort(sort) }
        )
        
        // 应用列表
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.ExtraLarge),
            verticalArrangement = Arrangement.spacedBy(Spacing.ExtraLarge)
        ) {
            // 统计概览
            item {
                AppOverviewCard(
                    installedApps = uiState.installedAppsCount,
                    todayUsed = uiState.todayUsedCount,
                    longUnused = uiState.longUnusedCount
                )
            }
            
            // 应用列表
            item {
                AppListCard(
                    apps = uiState.filteredApps,
                    isLoading = uiState.isLoading,
                    onAppClick = { app -> viewModel.showAppDetail(app) },
                    onTimeLimitClick = { app -> viewModel.showTimeLimitDialog(app) },
                    onDeleteClick = { app -> viewModel.deleteApp(app) }
                )
            }
            
            // 底部间距
            item {
                Spacer(modifier = Modifier.height(ComponentSize.BottomNavHeight))
            }
        }
    }
}

/**
 * 头部卡片 - 包含搜索和排序
 */
@Composable
private fun HeaderCard(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedSort: SortType,
    onSortSelected: (SortType) -> Unit
) {
    GlassmorphismCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.ExtraLarge, vertical = Spacing.Large),
        cornerRadius = CornerRadius.ExtraLarge
    ) {
        Column(
            modifier = Modifier.padding(Spacing.ExtraLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "应用管理",
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
                ) {
                    IconButton(
                        onClick = { /* 搜索 */ },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = BackgroundLightSecondary,
                                shape = RoundedCornerShape(CornerRadius.Medium)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索",
                            tint = TextSecondary,
                            modifier = Modifier.size(IconSize.Small)
                        )
                    }
                    
                    IconButton(
                        onClick = { /* 筛选 */ },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = BackgroundLightSecondary,
                                shape = RoundedCornerShape(CornerRadius.Medium)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "筛选",
                            tint = TextSecondary,
                            modifier = Modifier.size(IconSize.Small)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.ExtraLarge))
            
            // 排序选项
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                item {
                    FilterButton(
                        text = "使用时长",
                        selected = selectedSort == SortType.USAGE_TIME,
                        onClick = { onSortSelected(SortType.USAGE_TIME) }
                    )
                }
                item {
                    FilterButton(
                        text = "使用频次",
                        selected = selectedSort == SortType.FREQUENCY,
                        onClick = { onSortSelected(SortType.FREQUENCY) }
                    )
                }
                item {
                    FilterButton(
                        text = "应用大小",
                        selected = selectedSort == SortType.SIZE,
                        onClick = { onSortSelected(SortType.SIZE) }
                    )
                }
                item {
                    FilterButton(
                        text = "安装时间",
                        selected = selectedSort == SortType.INSTALL_TIME,
                        onClick = { onSortSelected(SortType.INSTALL_TIME) }
                    )
                }
            }
        }
    }
}

/**
 * 应用概览卡片
 */
@Composable
private fun AppOverviewCard(
    installedApps: Int,
    todayUsed: Int,
    longUnused: Int
) {
    GlassmorphismCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = CornerRadius.Large
    ) {
        Column(
            modifier = Modifier.padding(Spacing.ExtraLarge)
        ) {
            Text(
                text = "应用概览",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(Spacing.Large))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 已安装应用
                OverviewItem(
                    icon = Icons.Default.Smartphone,
                    value = installedApps.toString(),
                    label = "已安装应用",
                    gradient = Brush.linearGradient(
                        listOf(Color(0xFF3b82f6), Color(0xFF2563eb))
                    )
                )
                
                // 今日使用
                OverviewItem(
                    icon = Icons.Default.CheckCircle,
                    value = todayUsed.toString(),
                    label = "今日使用",
                    gradient = Brush.linearGradient(
                        listOf(Color(0xFF10b981), Color(0xFF059669))
                    )
                )
                
                // 长期未用
                OverviewItem(
                    icon = Icons.Default.Schedule,
                    value = longUnused.toString(),
                    label = "长期未用",
                    gradient = Brush.linearGradient(
                        listOf(Color(0xFFef4444), Color(0xFFdc2626))
                    )
                )
            }
        }
    }
}

/**
 * 概览项
 */
@Composable
private fun OverviewItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    gradient: Brush
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(Spacing.Large)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    brush = gradient,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(IconSize.Large)
            )
        }
        
        Spacer(modifier = Modifier.height(Spacing.Medium))
        
        Text(
            text = value,
            style = MaterialTheme.typography.displayMedium,
            color = Color(0xFF3b82f6),
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

/**
 * 应用列表卡片
 */
@Composable
private fun AppListCard(
    apps: List<AppInfo>,
    isLoading: Boolean,
    onAppClick: (AppInfo) -> Unit,
    onTimeLimitClick: (AppInfo) -> Unit,
    onDeleteClick: (AppInfo) -> Unit
) {
    GlassmorphismCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = CornerRadius.Large
    ) {
        Column(
            modifier = Modifier.padding(Spacing.ExtraLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "应用列表",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                
                Button(
                    onClick = { /* 批量管理 */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(horizontal = Spacing.Large, vertical = Spacing.Small),
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFFa855f7), Color(0xFF9333ea))
                            ),
                            shape = RoundedCornerShape(CornerRadius.Medium)
                        )
                ) {
                    Text(
                        text = "批量管理",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.ExtraLarge))
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandBlue)
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
                ) {
                    apps.take(5).forEach { app ->
                        AppItemRow(
                            app = app,
                            onAppClick = { onAppClick(app) },
                            onTimeLimitClick = { onTimeLimitClick(app) },
                            onDeleteClick = { onDeleteClick(app) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 应用项行
 */
@Composable
private fun AppItemRow(
    app: AppInfo,
    onAppClick: () -> Unit,
    onTimeLimitClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.Medium),
        color = Color.White.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.Large),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 应用图标
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = BrandGradientBrush,
                        shape = RoundedCornerShape(CornerRadius.Medium)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(Spacing.Large))
            
            // 应用信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = "今日使用 ${formatDuration(app.todayUsage)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                
                Text(
                    text = "${app.category}${if (app.timeLimit > 0) " • 限制 ${app.timeLimit}分钟" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
                
                // 进度条（如果有时间限制）
                if (app.timeLimit > 0 && app.todayUsage > 0) {
                    val progress = (app.todayUsage / (app.timeLimit * 60 * 1000f)).coerceIn(0f, 1f)
                    Spacer(modifier = Modifier.height(Spacing.Small))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(ComponentSize.ProgressBarHeight)
                            .clip(RoundedCornerShape(CornerRadius.Small)),
                        color = when {
                            progress >= 0.8f -> ErrorRed
                            else -> BrandBlue
                        },
                        trackColor = NeutralGray.copy(alpha = 0.2f)
                    )
                }
            }
            
            // 操作按钮
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                IconButton(
                    onClick = onTimeLimitClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(CornerRadius.Medium)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "设置",
                        tint = Color(0xFF3b82f6),
                        modifier = Modifier.size(IconSize.Small)
                    )
                }
                
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(CornerRadius.Medium)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = ErrorRed,
                        modifier = Modifier.size(IconSize.Small)
                    )
                }
            }
        }
    }
}

/**
 * 格式化时长
 */
private fun formatDuration(millis: Long): String {
    val hours = millis / (1000 * 60 * 60)
    val minutes = (millis / (1000 * 60)) % 60
    return when {
        hours > 0 -> "${hours}小时${minutes}分钟"
        minutes > 0 -> "${minutes}分钟"
        else -> "少于1分钟"
    }
}
