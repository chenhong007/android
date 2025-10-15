package com.example.time.ui.charts.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.sqrt

/**
 * 图表相关扩展函数
 */

/**
 * 计算两点之间的距离
 */
fun Offset.distanceTo(other: Offset): Float {
    val dx = x - other.x
    val dy = y - other.y
    return sqrt(dx * dx + dy * dy)
}

/**
 * 获取颜色的亮度
 */
fun Color.luminance(): Float {
    return this.luminance()
}

/**
 * 时间范围数据类扩展
 */
data class TimeRangeExtension(
    val start: Long,
    val end: Long
)
