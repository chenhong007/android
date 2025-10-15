package com.example.time.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.time.ui.theme.*

/**
 * Brand Gradient Box - 品牌渐变容器
 * 用于创建带渐变背景的容器
 */
@Composable
fun BrandGradientBox(
    modifier: Modifier = Modifier,
    brush: Brush = BrandGradientBrush,
    shape: Shape = RoundedCornerShape(CornerRadius.Large),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(brush = brush, shape = shape)
            .clip(shape)
    ) {
        content()
    }
}

/**
 * Brand Button - 品牌按钮
 * 三种样式：主要按钮、次要按钮、深色按钮
 */
@Composable
fun BrandButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: ButtonType = ButtonType.Primary,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "button_scale"
    )
    
    val (background, contentColor) = when (type) {
        ButtonType.Primary -> BrandGradientBrush to Color.White
        ButtonType.Secondary -> Brush.linearGradient(listOf(Color.White, Color.White)) to TextSecondary
        ButtonType.Dark -> DarkGradientBrush to Color.White
    }
    
    Surface(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .then(
                if (type == ButtonType.Secondary) {
                    Modifier.border(
                        width = 1.dp,
                        color = NeutralGray.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(CornerRadius.Small)
                    )
                } else Modifier
            ),
        enabled = enabled,
        shape = RoundedCornerShape(CornerRadius.Small),
        color = Color.Transparent,
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier
                .background(brush = background)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

enum class ButtonType {
    Primary,
    Secondary,
    Dark
}

/**
 * Glass Card - 毛玻璃卡片
 * 实现半透明背景效果的卡片组件（完全匹配HTML原型）
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(CornerRadius.Large),
    backgroundColor: Color = CardBackground,
    borderColor: Color = Color.White.copy(alpha = 0.2f),
    elevation: Dp = Elevation.Medium,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = shape
                ),
            color = Color.Transparent
        ) {
            content()
        }
    }
}

/**
 * Glassmorphism Card - 玻璃拟态卡片
 * 完全按照 UI 设计规范实现的玻璃拟态效果
 */
@Composable
fun GlassmorphismCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = CornerRadius.Large,
    elevation: Dp = Elevation.Medium,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(cornerRadius)
                )
        ) {
            content()
        }
    }
}

/**
 * Gradient Card - 渐变卡片
 * 用于创建带渐变背景的卡片
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradient: Brush,
    cornerRadius: Dp = CornerRadius.Large,
    elevation: Dp = Elevation.Medium,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = gradient)
        ) {
            content()
        }
    }
}

/**
 * Gradient Icon Container - 渐变图标容器
 * 用于创建带渐变背景的圆形图标容器
 */
@Composable
fun GradientIconContainer(
    modifier: Modifier = Modifier,
    size: Dp = ComponentSize.AppIconMedium,
    brush: Brush = BrandGradientBrush,
    shape: Shape = RoundedCornerShape(12.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .background(brush = brush, shape = shape),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Toggle Switch - 开关组件
 * 实现品牌风格的开关按钮
 */
@Composable
fun BrandToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) 22f else 0f,
        animationSpec = tween(300),
        label = "thumb_offset"
    )
    
    val backgroundColor = if (checked) {
        SuccessGradientBrush
    } else {
        Brush.linearGradient(listOf(NeutralGray.copy(alpha = 0.3f), NeutralGray.copy(alpha = 0.3f)))
    }
    
    Box(
        modifier = modifier
            .size(width = ComponentSize.ToggleWidth, height = ComponentSize.ToggleHeight)
            .clip(CircleShape)
            .background(brush = backgroundColor)
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset.dp)
                .size(24.dp)
                .background(Color.White, CircleShape)
        )
    }
}

/**
 * Progress Bar - 进度条组件
 * 实现品牌风格的进度条
 */
@Composable
fun BrandProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    brush: Brush = SuccessGradientBrush,
    backgroundColor: Color = NeutralGray.copy(alpha = 0.2f),
    height: Dp = ComponentSize.ProgressBarHeight
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(CornerRadius.Small)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(
                    brush = brush,
                    shape = RoundedCornerShape(CornerRadius.Small)
                )
                .matchParentSize()
        )
    }
}

/**
 * Section Title - 章节标题
 * 用于各个页面的章节标题
 */
@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineLarge
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = TextPrimary,
        fontWeight = FontWeight.SemiBold
    )
}

/**
 * Filter Button - 筛选按钮
 * 用于统计和历史页面的时间范围筛选
 */
@Composable
fun FilterButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "filter_button_scale"
    )
    
    val (background, textColor) = if (selected) {
        DarkGradientBrush to Color.White
    } else {
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.8f), Color.White.copy(alpha = 0.8f))) to TextSecondary
    }
    
    Surface(
        onClick = onClick,
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(CornerRadius.Full),
        color = Color.Transparent,
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier
                .background(brush = background)
                .border(
                    width = 1.dp,
                    color = if (selected) Color.Transparent else NeutralGray.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(CornerRadius.Full)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

