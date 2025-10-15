package com.example.time.ui.charts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.ui.charts.models.AppSession
import com.example.time.ui.charts.models.TimelineConfig
import com.example.time.ui.shapes.TriangleShape
import com.example.time.ui.charts.utils.luminance
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import kotlin.math.max

/**
 * 时间线Compose组件
 * 显示24小时内的应用使用时间线
 */
@Composable
fun TimelineCompose(
    sessions: List<AppSession>,
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
    config: TimelineConfig = TimelineConfig(),
    onSessionClick: (AppSession) -> Unit = {}
) {
    var scrollOffset by remember { mutableStateOf(0f) }
    var zoomLevel by remember { mutableStateOf(1f) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // 日期和时间显示
        DateAndTimeHeader(
            selectedDate = selectedDate,
            sessions = sessions
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 时间轴主体
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(config.sessionHeight.dp + 40.dp)
        ) {
            // 时间刻度背景
            TimelineBackground(
                config = config,
                zoomLevel = zoomLevel
            )
            
            // 会话块
            SessionsTimeline(
                sessions = sessions,
                config = config,
                zoomLevel = zoomLevel,
                scrollOffset = scrollOffset,
                onSessionClick = onSessionClick
            )
            
            // 当前时间指示器
            CurrentTimeIndicator(
                config = config,
                zoomLevel = zoomLevel,
                scrollOffset = scrollOffset,
                selectedDate = selectedDate
            )
        }
        
        // 时间标签
        TimeLabels(
            config = config,
            zoomLevel = zoomLevel,
            scrollOffset = scrollOffset,
            onScrollChange = { newOffset ->
                scrollOffset = newOffset
            }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 图例
        TimelineLegend(sessions = sessions)
    }
}

/**
 * 日期和时间头部
 */
@Composable
private fun DateAndTimeHeader(
    selectedDate: LocalDate,
    sessions: List<AppSession>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        
        val totalDuration = sessions.sumOf { it.duration }
        Text(
            text = "总使用时长: ${formatDuration(totalDuration)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 时间轴背景
 */
@Composable
private fun TimelineBackground(
    config: TimelineConfig,
    zoomLevel: Float
) {
    val totalWidth = (24 * 60 * config.pixelPerMinute * zoomLevel).dp
    val hourWidth = (60 * config.pixelPerMinute * zoomLevel).dp
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(config.sessionHeight.dp + 20.dp)
    ) {
        // 使用Canvas绘制时间网格
        Box(
            modifier = Modifier
                .width(totalWidth)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // 小时分割线
            for (hour in 0..23) {
                Box(
                    modifier = Modifier
                        .offset(x = (hour * hourWidth.value).dp)
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
            }
            
            // 半小时虚线
            for (hour in 0..23) {
                Box(
                    modifier = Modifier
                        .offset(x = ((hour + 0.5f) * hourWidth.value).dp)
                        .width(0.5.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                )
            }
        }
    }
}

/**
 * 会话时间线
 */
@Composable
private fun SessionsTimeline(
    sessions: List<AppSession>,
    config: TimelineConfig,
    zoomLevel: Float,
    scrollOffset: Float,
    onSessionClick: (AppSession) -> Unit
) {
    val totalWidth = (24 * 60 * config.pixelPerMinute * zoomLevel).dp
    val pixelPerMinute = config.pixelPerMinute * zoomLevel
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(config.sessionHeight.dp + 20.dp)
    ) {
        // 使用水平滚动容器
        val scrollState = rememberScrollState()
        
        LaunchedEffect(scrollOffset) {
            scrollState.scrollTo(scrollOffset.toInt())
        }
        
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .width(totalWidth)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            sessions.forEach { session ->
                SessionBlock(
                    session = session,
                    pixelPerMinute = pixelPerMinute,
                    onClick = { onSessionClick(session) }
                )
            }
        }
    }
}

/**
 * 会话块
 */
@Composable
private fun SessionBlock(
    session: AppSession,
    pixelPerMinute: Float,
    onClick: () -> Unit
) {
    val startMinutes = getMinutesFromMidnight(session.startTime)
    val durationMinutes = session.duration / (60 * 1000) // 转换为分钟
    val width = (durationMinutes * pixelPerMinute).dp
    val offset = (startMinutes * pixelPerMinute).dp
    
    Box(
        modifier = Modifier
            .offset(x = offset)
            .width(width)
            .height(20.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
            .background(
                color = Color(session.color).copy(alpha = 0.8f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            )
            .border(
                width = 1.dp,
                color = Color(session.color).copy(alpha = 0.9f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // 显示应用名称（如果空间足够）
        if (width > 40.dp) {
            Text(
                text = session.appName.take(3),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 8.sp,
                color = if (Color(session.color).luminance() < 0.5f) {
                    Color.White
                } else {
                    Color.Black
                },
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 当前时间指示器
 */
@Composable
private fun CurrentTimeIndicator(
    config: TimelineConfig,
    zoomLevel: Float,
    scrollOffset: Float,
    selectedDate: LocalDate
) {
    val now = LocalDateTime.now()
    val isToday = selectedDate == now.toLocalDate()
    
    if (!isToday || !config.showNowIndicator) {
        return
    }
    
    val nowMinutes = now.hour * 60 + now.minute
    val pixelPerMinute = config.pixelPerMinute * zoomLevel
    val position = (nowMinutes * pixelPerMinute).dp
    
    Box(
        modifier = Modifier
            .offset(x = position)
            .width(2.dp)
            .height(config.sessionHeight.dp + 20.dp)
            .background(Color.Red)
    ) {
        // 顶部三角形
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(8.dp)
                .background(Color.Red)
                .clip(com.example.time.ui.shapes.TriangleShape())
        )
    }
}

/**
 * 时间标签
 */
@Composable
private fun TimeLabels(
    config: TimelineConfig,
    zoomLevel: Float,
    scrollOffset: Float,
    onScrollChange: (Float) -> Unit
) {
    val totalWidth = (24 * 60 * config.pixelPerMinute * zoomLevel).dp
    val hourWidth = (60 * config.pixelPerMinute * zoomLevel).dp
    val scrollState = rememberScrollState()
    
    LaunchedEffect(scrollOffset) {
        scrollState.scrollTo(scrollOffset.toInt())
    }
    
    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .width(totalWidth)
            .height(20.dp),
        horizontalArrangement = Arrangement.spacedBy(hourWidth - 60.dp) // 调整间距
    ) {
        for (hour in 0..23) {
            Text(
                text = String.format("%02d:00", hour),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 时间线图例
 */
@Composable
private fun TimelineLegend(sessions: List<AppSession>) {
    val uniqueApps = sessions
        .groupBy { it.packageName }
        .map { (_, appSessions) ->
            appSessions.first() // 取第一个会话作为代表
        }
        .take(10) // 限制显示数量
    
    if (uniqueApps.isEmpty()) {
        return
    }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "应用图例",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // 使用网格布局显示图例
        val chunkedApps = uniqueApps.chunked(3) // 每行3个
        
        chunkedApps.forEach { rowApps ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowApps.forEach { session ->
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                                .background(Color(session.color))
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = session.appName.take(6),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
                
                // 填充剩余空间
                if (rowApps.size < 3) {
                    repeat(3 - rowApps.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * 从时间戳获取分钟数（从午夜开始）
 */
private fun getMinutesFromMidnight(timestamp: Long): Int {
    val dateTime = LocalDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(timestamp),
        ZoneId.systemDefault()
    )
    return dateTime.hour * 60 + dateTime.minute
}

/**
 * 格式化时长
 */
private fun formatDuration(millis: Long): String {
    val minutes = millis / (60 * 1000)
    return when {
        minutes < 60 -> "${minutes}分钟"
        minutes < 1440 -> "${minutes / 60}小时${minutes % 60}分钟"
        else -> "${minutes / 1440}天${(minutes % 1440) / 60}小时"
    }
}