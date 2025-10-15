package com.example.time.ui.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Compose 扩展函数
 */

/**
 * 获取颜色的亮度值
 */
fun Color.luminance(): Float {
    return this.luminance()
}
