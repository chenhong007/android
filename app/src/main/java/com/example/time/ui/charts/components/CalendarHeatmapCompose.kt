package com.example.time.ui.charts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.ui.charts.models.HeatmapData
import com.example.time.ui.charts.models.ColorScale
import com.example.time.ui.charts.utils.luminance
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.format.TextStyle
import java.util.Locale

/**
 * 日历热力图Compose组件
 */
@Composable
fun CalendarHeatmapCompose(
    data: HeatmapData,
    modifier: Modifier = Modifier,
    colorScale: ColorScale = ColorScale.GRADIENT,
    onDateClick: (LocalDate) -> Unit = {}
) {
    val calendarData = remember(data) {
        generateCalendarData(data)
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // 月份标签
        MonthLabelsRow(data)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 热力图网格
        LazyVerticalGrid(
            columns = GridCells.Fixed(7), // 7天一周
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp, max = 300.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(calendarData.days) { dayData ->
                DayCell(
                    dayData = dayData,
                    colorScale = colorScale,
                    minValue = data.minValue,
                    maxValue = data.maxValue,
                    onClick = { date ->
                        if (date != null) {
                            onDateClick(date)
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 颜色图例
        ColorLegend(
            minValue = data.minValue,
            maxValue = data.maxValue,
            colorScale = colorScale
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // 星期标签
        WeekdayLabelsRow()
    }
}

/**
 * 生成日历数据
 */
private fun generateCalendarData(data: HeatmapData): CalendarData {
    val days = mutableListOf<DayData>()
    
    // 找到开始日期对应的星期几
    val startDayOfWeek = data.startDate.dayOfWeek.value % 7 // 转换为0-6，周日为0
    
    // 添加空白天数以对齐星期
    repeat(startDayOfWeek) {
        days.add(DayData(null, null, 0f))
    }
    
    // 添加实际的天数
    var currentDate = data.startDate
    while (!currentDate.isAfter(data.endDate)) {
        val value = data.dateValues[currentDate] ?: 0f
        days.add(DayData(currentDate, currentDate.dayOfMonth, value))
        currentDate = currentDate.plusDays(1)
    }
    
    return CalendarData(days)
}

/**
 * 月份标签行
 */
@Composable
private fun MonthLabelsRow(data: HeatmapData) {
    val monthFormatter = DateTimeFormatter.ofPattern("MMM")
    val months = mutableListOf<Pair<LocalDate, String>>()
    
    var currentDate = data.startDate
    while (!currentDate.isAfter(data.endDate)) {
        if (currentDate.dayOfMonth == 1 || currentDate == data.startDate) {
            months.add(currentDate to currentDate.format(monthFormatter))
        }
        currentDate = currentDate.plusDays(1)
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        months.forEach { (date, monthName) ->
            val startDayOfWeek = data.startDate.dayOfWeek.value % 7
            val daysFromStart = ChronoUnit.DAYS.between(data.startDate, date)
            val weekOffset = ((daysFromStart + startDayOfWeek).toInt() / 7)
            
            Text(
                text = monthName,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 星期标签行
 */
@Composable
private fun WeekdayLabelsRow() {
    val weekdays = listOf("日", "一", "二", "三", "四", "五", "六")
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weekdays.forEach { weekday ->
            Text(
                text = weekday,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 日期单元格
 */
@Composable
private fun DayCell(
    dayData: DayData,
    colorScale: ColorScale,
    minValue: Float,
    maxValue: Float,
    onClick: (LocalDate?) -> Unit
) {
    val backgroundColor = when (colorScale) {
        ColorScale.GRADIENT -> getGradientColor(dayData.value, minValue, maxValue)
        ColorScale.DISCRETE -> getDiscreteColor(dayData.value, minValue, maxValue)
        ColorScale.BINARY -> getBinaryColor(dayData.value)
    }
    
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(backgroundColor)
            .border(
                width = if (dayData.date != null) 0.5.dp else 0.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(2.dp)
            )
            .clickable(enabled = dayData.date != null) {
                onClick(dayData.date)
            },
        contentAlignment = Alignment.Center
    ) {
        if (dayData.date != null && dayData.dayNumber != null) {
            Text(
                text = dayData.dayNumber.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 8.sp,
                color = if (backgroundColor.luminance() < 0.5f) {
                    Color.White
                } else {
                    Color.Black
                },
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 获取渐变颜色
 */
private fun getGradientColor(value: Float, minValue: Float, maxValue: Float): Color {
    if (maxValue == minValue) {
        return Color(0xFFebedf0) // 灰色
    }
    
    val normalizedValue = (value - minValue) / (maxValue - minValue)
    val intensity = normalizedValue.coerceIn(0f, 1f)
    
    // 从浅绿色到深绿色的渐变
    val startColor = Color(0xFFebedf0) // 浅灰色
    val endColor = Color(0xFF216e39) // 深绿色
    
    return Color(
        red = startColor.red + (endColor.red - startColor.red) * intensity,
        green = startColor.green + (endColor.green - startColor.green) * intensity,
        blue = startColor.blue + (endColor.blue - startColor.blue) * intensity,
        alpha = startColor.alpha + (endColor.alpha - startColor.alpha) * intensity
    )
}

/**
 * 获取离散颜色
 */
private fun getDiscreteColor(value: Float, minValue: Float, maxValue: Float): Color {
    if (maxValue == minValue) {
        return Color(0xFFebedf0)
    }
    
    val normalizedValue = (value - minValue) / (maxValue - minValue)
    
    return when {
        normalizedValue < 0.2f -> Color(0xFFebedf0) // 浅灰色
        normalizedValue < 0.4f -> Color(0xFFc6e48b) // 浅绿色
        normalizedValue < 0.6f -> Color(0xFF7bc96f) // 中绿色
        normalizedValue < 0.8f -> Color(0xFF239a3b) // 深绿色
        else -> Color(0xFF196127) // 最深绿色
    }
}

/**
 * 获取二元颜色
 */
private fun getBinaryColor(value: Float): Color {
    return if (value > 0) {
        Color(0xFF40c463) // 绿色
    } else {
        Color(0xFFebedf0) // 灰色
    }
}

/**
 * 颜色图例
 */
@Composable
private fun ColorLegend(
    minValue: Float,
    maxValue: Float,
    colorScale: ColorScale
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "少",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        // 颜色渐变条
        when (colorScale) {
            ColorScale.GRADIENT -> {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(8.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFebedf0),
                                    Color(0xFFc6e48b),
                                    Color(0xFF7bc96f),
                                    Color(0xFF239a3b),
                                    Color(0xFF196127)
                                )
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
            ColorScale.DISCRETE -> {
                Row {
                    val colors = listOf(
                        Color(0xFFebedf0),
                        Color(0xFFc6e48b),
                        Color(0xFF7bc96f),
                        Color(0xFF239a3b),
                        Color(0xFF196127)
                    )
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(color)
                        )
                    }
                }
            }
            ColorScale.BINARY -> {
                Row {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFebedf0))
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF40c463))
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = "多",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 日历数据类
 */
private data class CalendarData(
    val days: List<DayData>
)

/**
 * 日期数据类
 */
private data class DayData(
    val date: LocalDate?,
    val dayNumber: Int?,
    val value: Float
)