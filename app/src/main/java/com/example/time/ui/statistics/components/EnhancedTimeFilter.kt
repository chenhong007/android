package com.example.time.ui.statistics.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.time.ui.charts.models.DateRange
import com.example.time.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 增强型时间筛选器组件
 * 使用渐变背景和玻璃拟态效果
 */
@Composable
fun EnhancedTimeFilterSection(
    selectedFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit,
    customDateRange: DateRange?,
    onCustomDateRangeSelected: (LocalDate, LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // 快速筛选按钮组
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GradientFilterButton(
                text = "今天",
                icon = Icons.Default.Today,
                selected = selectedFilter == TimeFilter.TODAY,
                onClick = { onFilterSelected(TimeFilter.TODAY) },
                modifier = Modifier.weight(1f)
            )
            
            GradientFilterButton(
                text = "昨天",
                icon = Icons.Default.CalendarToday,
                selected = selectedFilter == TimeFilter.YESTERDAY,
                onClick = { onFilterSelected(TimeFilter.YESTERDAY) },
                modifier = Modifier.weight(1f)
            )
            
            GradientFilterButton(
                text = "本周",
                icon = Icons.Default.DateRange,
                selected = selectedFilter == TimeFilter.THIS_WEEK,
                onClick = { onFilterSelected(TimeFilter.THIS_WEEK) },
                modifier = Modifier.weight(1f)
            )
            
            GradientFilterButton(
                text = "本月",
                icon = Icons.Default.CalendarMonth,
                selected = selectedFilter == TimeFilter.THIS_MONTH,
                onClick = { onFilterSelected(TimeFilter.THIS_MONTH) },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 自定义筛选按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GradientFilterButton(
                text = "自定义",
                icon = Icons.Default.EditCalendar,
                selected = selectedFilter == TimeFilter.CUSTOM,
                onClick = { showDatePicker = true },
                gradient = if (selectedFilter == TimeFilter.CUSTOM) {
                    SuccessGradientBrush
                } else {
                    BackgroundGradientBrush
                },
                modifier = Modifier.weight(1f)
            )
            
            // 快捷日期选择器
            if (selectedFilter != TimeFilter.CUSTOM) {
                GlassmorphismIconButton(
                    icon = Icons.Default.ChevronLeft,
                    onClick = { 
                        // 切换到上一个时间段
                        when (selectedFilter) {
                            TimeFilter.TODAY -> onFilterSelected(TimeFilter.YESTERDAY)
                            TimeFilter.YESTERDAY -> {
                                val twoDaysAgo = LocalDate.now().minusDays(2)
                                onCustomDateRangeSelected(twoDaysAgo, twoDaysAgo)
                                onFilterSelected(TimeFilter.CUSTOM)
                            }
                            TimeFilter.THIS_WEEK -> onFilterSelected(TimeFilter.YESTERDAY)
                            TimeFilter.THIS_MONTH -> onFilterSelected(TimeFilter.THIS_WEEK)
                            else -> {}
                        }
                    }
                )
                
                GlassmorphismIconButton(
                    icon = Icons.Default.ChevronRight,
                    onClick = { 
                        // 切换到下一个时间段
                        when (selectedFilter) {
                            TimeFilter.YESTERDAY -> onFilterSelected(TimeFilter.TODAY)
                            TimeFilter.TODAY -> {
                                val tomorrow = LocalDate.now().plusDays(1)
                                onCustomDateRangeSelected(tomorrow, tomorrow)
                                onFilterSelected(TimeFilter.CUSTOM)
                            }
                            TimeFilter.THIS_WEEK -> onFilterSelected(TimeFilter.THIS_MONTH)
                            TimeFilter.THIS_MONTH -> {
                                val nextMonthStart = LocalDate.now().plusMonths(1).withDayOfMonth(1)
                                val nextMonthEnd = nextMonthStart.plusMonths(1).minusDays(1)
                                onCustomDateRangeSelected(nextMonthStart, nextMonthEnd)
                                onFilterSelected(TimeFilter.CUSTOM)
                            }
                            else -> {}
                        }
                    }
                )
            }
        }
        
        // 自定义日期范围显示
        if (selectedFilter == TimeFilter.CUSTOM && customDateRange != null) {
            Spacer(modifier = Modifier.height(8.dp))
            GlassmorphismDateRangeCard(
                startDate = customDateRange.startDate,
                endDate = customDateRange.endDate,
                onEditClick = { showDatePicker = true }
            )
        }
    }
    
    // 日期选择器对话框
    if (showDatePicker) {
        EnhancedDateRangePickerDialog(
            onDateRangeSelected = { startDate, endDate ->
                onCustomDateRangeSelected(startDate, endDate)
                onFilterSelected(TimeFilter.CUSTOM)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

/**
 * 渐变筛选按钮
 */
@Composable
private fun GradientFilterButton(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: Brush = BrandGradientBrush
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "button_scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (selected) 6.dp else 4.dp,
        animationSpec = tween(durationMillis = 200),
        label = "button_elevation"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (selected) gradient else BackgroundGradientBrush,
                    alpha = if (selected) 1f else 0.7f
                )
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) {
                        Color.White.copy(alpha = 0.6f)
                    } else {
                        Color.White.copy(alpha = 0.3f)
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .shadow(
                    elevation = elevation,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = Color.Black.copy(alpha = 0.2f)
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = if (selected) Color.White else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) Color.White else TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * 玻璃拟态图标按钮
 */
@Composable
private fun GlassmorphismIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "icon_button_scale"
    )
    
    Box(
        modifier = modifier
            .size(40.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = BackgroundGradientBrush,
                alpha = 0.8f
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * 玻璃拟态日期范围卡片
 */
@Composable
private fun GlassmorphismDateRangeCard(
    startDate: LocalDate,
    endDate: LocalDate,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = BackgroundGradientBrush,
                alpha = 0.8f
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            
            Text(
                text = "${startDate.format(DateTimeFormatter.ofPattern("MM月dd日"))} - ${endDate.format(DateTimeFormatter.ofPattern("MM月dd日"))}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
        
        IconButton(
            onClick = onEditClick,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "编辑日期",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * 增强型日期范围选择器对话框
 */
@Composable
private fun EnhancedDateRangePickerDialog(
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "选择日期范围",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "请选择开始和结束日期",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                // 快速选择选项
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GradientQuickSelectButton(
                        text = "最近7天",
                        onClick = {
                            val endDate = LocalDate.now()
                            val startDate = endDate.minusDays(6)
                            onDateRangeSelected(startDate, endDate)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    GradientQuickSelectButton(
                        text = "最近30天",
                        onClick = {
                            val endDate = LocalDate.now()
                            val startDate = endDate.minusDays(29)
                            onDateRangeSelected(startDate, endDate)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val endDate = LocalDate.now()
                    val startDate = endDate.minusDays(7)
                    onDateRangeSelected(startDate, endDate)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandBlue
                )
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * 快速选择渐变按钮
 */
@Composable
private fun GradientQuickSelectButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "quick_select_scale"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                brush = BluePurpleGradientBrush,
                alpha = 0.8f
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

/**
 * 时间筛选器枚举
 */
enum class TimeFilter {
    TODAY,
    YESTERDAY,
    THIS_WEEK,
    THIS_MONTH,
    CUSTOM
}

// DateRange 在 com.example.time.ui.charts.models.DateRange 中已定义

// 预览函数
@Preview(showBackground = true)
@Composable
private fun EnhancedTimeFilterSectionPreview() {
    TimeTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnhancedTimeFilterSection(
                selectedFilter = TimeFilter.TODAY,
                onFilterSelected = {},
                customDateRange = null,
                onCustomDateRangeSelected = { _, _ -> }
            )
            
            EnhancedTimeFilterSection(
                selectedFilter = TimeFilter.CUSTOM,
                onFilterSelected = {},
                customDateRange = DateRange(
                    startDate = LocalDate.now().minusDays(7),
                    endDate = LocalDate.now()
                ),
                onCustomDateRangeSelected = { _, _ -> }
            )
        }
    }
}