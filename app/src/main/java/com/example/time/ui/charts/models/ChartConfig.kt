package com.example.time.ui.charts.models

import android.graphics.Color

/**
 * 图表配置模型
 * 定义各种图表的配置参数
 */

// 基础图表配置
data class ChartConfig(
    val width: Int = 0,
    val height: Int = 0,
    val animationDuration: Int = 1000,
    val enableInteraction: Boolean = true,
    val colorScheme: ColorScheme = ColorScheme.BRAND,
    val showGrid: Boolean = true,
    val showLegend: Boolean = true,
    val showLabels: Boolean = true,
    val enableZoom: Boolean = true,
    val enableScroll: Boolean = true,
    val backgroundColor: Int = Color.WHITE,
    val textColor: Int = Color.BLACK,
    val gridColor: Int = Color.LTGRAY,
    val maxVisibleDataPoints: Int = 20
)

// 热力图配置
data class HeatmapConfig(
    val cellSize: Int = 40,
    val colorScale: ColorScale = ColorScale.GRADIENT,
    val showLabels: Boolean = true,
    val monthNavigation: Boolean = true,
    val showTodayIndicator: Boolean = true,
    val borderWidth: Int = 1,
    val borderColor: Int = Color.LTGRAY
)

// 时间线配置
data class TimelineConfig(
    val pixelPerMinute: Float = 2f,
    val sessionHeight: Int = 24,
    val hourLabelHeight: Int = 30,
    val enableZoom: Boolean = true,
    val minZoom: Float = 0.5f,
    val maxZoom: Float = 3.0f,
    val showHourLabels: Boolean = true,
    val showNowIndicator: Boolean = true,
    val enablePan: Boolean = true
)

// 条形图配置
data class BarChartConfig(
    val barWidth: Float = 0.8f,
    val groupSpacing: Float = 0.3f,
    val valueTextSize: Float = 12f,
    val labelTextSize: Float = 10f,
    val maxVisibleBars: Int = 10,
    val enableValueLabels: Boolean = true,
    val enableBarShadow: Boolean = false,
    val barShadowColor: Int = Color.parseColor("#20000000")
)

// 颜色方案
enum class ColorScheme {
    BRAND,        // 品牌色 - 蓝紫渐变
    SUCCESS,      // 成功色 - 绿色渐变
    WARNING,      // 警告色 - 橙色渐变
    ERROR,        // 错误色 - 红色渐变
    MONOCHROME,   // 单色 - 灰度
    VIBRANT,      // 鲜艳 - 多彩
    PASTEL        // 柔和 - 浅色
}

// 颜色刻度
enum class ColorScale {
    GRADIENT,     // 渐变色
    DISCRETE,     // 离散色
    BINARY        // 二值色
}

// 图表主题
data class ChartTheme(
    val primaryColor: Int,
    val secondaryColor: Int,
    val accentColor: Int,
    val backgroundColor: Int,
    val textColor: Int,
    val gridColor: Int,
    val legendTextColor: Int,
    val valueTextColor: Int
) {
    companion object {
        // 预设主题
        fun brandTheme(): ChartTheme = ChartTheme(
            primaryColor = Color.parseColor("#667eea"),
            secondaryColor = Color.parseColor("#764ba2"),
            accentColor = Color.parseColor("#10b981"),
            backgroundColor = Color.WHITE,
            textColor = Color.parseColor("#1f2937"),
            gridColor = Color.parseColor("#e5e7eb"),
            legendTextColor = Color.parseColor("#6b7280"),
            valueTextColor = Color.parseColor("#374151")
        )

        fun darkTheme(): ChartTheme = ChartTheme(
            primaryColor = Color.parseColor("#818cf8"),
            secondaryColor = Color.parseColor("#a78bfa"),
            accentColor = Color.parseColor("#34d399"),
            backgroundColor = Color.parseColor("#1f2937"),
            textColor = Color.WHITE,
            gridColor = Color.parseColor("#374151"),
            legendTextColor = Color.parseColor("#9ca3af"),
            valueTextColor = Color.parseColor("#d1d5db")
        )

        fun monochromeTheme(): ChartTheme = ChartTheme(
            primaryColor = Color.parseColor("#6b7280"),
            secondaryColor = Color.parseColor("#9ca3af"),
            accentColor = Color.parseColor("#374151"),
            backgroundColor = Color.WHITE,
            textColor = Color.parseColor("#1f2937"),
            gridColor = Color.parseColor("#d1d5db"),
            legendTextColor = Color.parseColor("#6b7280"),
            valueTextColor = Color.parseColor("#4b5563")
        )
    }
}

// 动画配置
data class AnimationConfig(
    val enableAnimation: Boolean = true,
    val animationDuration: Int = 1000,
    val animationEasing: AnimationEasing = AnimationEasing.EASE_IN_OUT,
    val animateX: Boolean = true,
    val animateY: Boolean = true,
    val staggerAnimation: Boolean = false
)

// 动画缓动函数
enum class AnimationEasing {
    LINEAR,
    EASE_IN,
    EASE_OUT,
    EASE_IN_OUT,
    BOUNCE,
    ELASTIC
}

// 交互配置
data class InteractionConfig(
    val enableTouch: Boolean = true,
    val enableHighlight: Boolean = true,
    val enableSelection: Boolean = true,
    val enableLongPress: Boolean = true,
    val highlightColor: Int = Color.YELLOW,
    val selectionColor: Int = Color.CYAN,
    val highlightAlpha: Int = 128,
    val selectionAlpha: Int = 150
)

// 性能配置
data class PerformanceConfig(
    val enableSampling: Boolean = true,
    val maxDataPoints: Int = 1000,
    val enableCaching: Boolean = true,
    val cacheExpiration: Long = 5 * 60 * 1000, // 5分钟
    val enableHardwareAcceleration: Boolean = true,
    val renderQuality: RenderQuality = RenderQuality.HIGH
)

// 渲染质量
enum class RenderQuality {
    LOW,      // 快速渲染，较低质量
    MEDIUM,   // 平衡质量和性能
    HIGH      // 高质量渲染，较慢速度
}

// 图表边距配置
data class ChartMargins(
    val left: Float = 10f,
    val top: Float = 10f,
    val right: Float = 10f,
    val bottom: Float = 10f
)

// 图例配置
data class LegendConfig(
    val enabled: Boolean = true,
    val position: LegendPosition = LegendPosition.BOTTOM,
    val orientation: LegendOrientation = LegendOrientation.HORIZONTAL,
    val textSize: Float = 12f,
    val textColor: Int = Color.BLACK,
    val backgroundColor: Int = Color.TRANSPARENT
)

// 图例位置
enum class LegendPosition {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT
}

// 图例方向
enum class LegendOrientation {
    HORIZONTAL,
    VERTICAL
}