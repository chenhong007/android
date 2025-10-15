package com.example.time.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Brand Primary Colors - 品牌主色调
val BrandBlue = Color(0xFF667eea)
val BrandPurple = Color(0xFF764ba2)

// Dark Theme Colors - 深色主题
val DarkSlate = Color(0xFF1e293b)
val DarkSlateLight = Color(0xFF334155)

// Success Colors - 成功色
val SuccessGreen = Color(0xFF10b981)
val SuccessGreenLight = Color(0xFF34d399)
val SuccessLight = Color(0xFF34d399)

// Auxiliary Colors - 辅助色彩
val WarningOrange = Color(0xFFf59e0b)
val WarningLight = Color(0xFFfbbf24)
val ErrorRed = Color(0xFFef4444)
val ErrorLight = Color(0xFFf87171)
val NeutralGray = Color(0xFF6b7280)

// Background Colors - 背景色彩
val BackgroundLight = Color(0xFFf8fafc)
val BackgroundLightSecondary = Color(0xFFf1f5f9)
val CardBackground = Color(0xFFFFFFFF).copy(alpha = 0.9f)

// Text Colors - 文本颜色
val TextPrimary = Color(0xFF1d1d1f)
val TextSecondary = Color(0xFF6b7280)
val TextTertiary = Color(0xFF9ca3af)

// App Specific Colors - 应用特定颜色
val YouTubeRed = Color(0xFFef4444)
val YouTubeRedDark = Color(0xFFdc2626)
val WeChatGreen = Color(0xFF10b981)
val WeChatGreenDark = Color(0xFF059669)
val ChromeBlue = Color(0xFF3b82f6)
val ChromeBlueDark = Color(0xFF2563eb)
val InstagramPurple = Color(0xFFa855f7)
val InstagramPurpleDark = Color(0xFF9333ea)

// Gradient Data Class - 渐变色数据类
data class GradientColors(
    val start: Color,
    val end: Color,
    val angle: Float = 135f
)

// Brand Gradients - 品牌渐变
val BrandGradientColors = GradientColors(
    start = BrandBlue,
    end = BrandPurple
)

val DarkGradientColors = GradientColors(
    start = DarkSlate,
    end = DarkSlateLight
)

val SuccessGradientColors = GradientColors(
    start = SuccessGreen,
    end = SuccessGreenLight
)

val BackgroundGradientColors = GradientColors(
    start = BackgroundLight,
    end = BackgroundLightSecondary
)

val YouTubeGradientColors = GradientColors(
    start = YouTubeRed,
    end = YouTubeRedDark
)

val WeChatGradientColors = GradientColors(
    start = WeChatGreen,
    end = WeChatGreenDark
)

val ChromeGradientColors = GradientColors(
    start = ChromeBlue,
    end = ChromeBlueDark
)

val InstagramGradientColors = GradientColors(
    start = InstagramPurple,
    end = InstagramPurpleDark
)

// Gradient Brushes - 渐变 Brush 对象（135度角）
val BrandGradientBrush = Brush.linearGradient(
    0.0f to BrandBlue,
    1.0f to BrandPurple
)

val DarkGradientBrush = Brush.linearGradient(
    0.0f to DarkSlate,
    1.0f to DarkSlateLight
)

val SuccessGradientBrush = Brush.linearGradient(
    0.0f to SuccessGreen,
    1.0f to SuccessGreenLight
)

val BackgroundGradientBrush = Brush.linearGradient(
    0.0f to BackgroundLight,
    1.0f to BackgroundLightSecondary
)

val YouTubeGradientBrush = Brush.linearGradient(
    0.0f to YouTubeRed,
    1.0f to YouTubeRedDark
)

val WeChatGradientBrush = Brush.linearGradient(
    0.0f to WeChatGreen,
    1.0f to WeChatGreenDark
)

val ChromeGradientBrush = Brush.linearGradient(
    0.0f to ChromeBlue,
    1.0f to ChromeBlueDark
)

val InstagramGradientBrush = Brush.linearGradient(
    0.0f to InstagramPurple,
    1.0f to InstagramPurpleDark
)

// Additional gradient colors for blue-purple, green-teal combinations
val BluePurpleGradientBrush = Brush.linearGradient(
    0.0f to Color(0xFF3b82f6),
    1.0f to Color(0xFF9333ea)
)

val GreenTealGradientBrush = Brush.linearGradient(
    0.0f to Color(0xFF10b981),
    1.0f to Color(0xFF14b8a6)
)

// Warning and Neutral gradients
val WarningGradientBrush = Brush.linearGradient(
    0.0f to WarningOrange,
    1.0f to WarningLight
)

val NeutralGradientBrush = Brush.linearGradient(
    0.0f to NeutralGray,
    1.0f to Color(0xFF9ca3af)
)