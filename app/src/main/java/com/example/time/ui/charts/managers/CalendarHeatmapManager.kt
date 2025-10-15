package com.example.time.ui.charts.managers

import android.content.Context
import android.graphics.*
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.time.ui.charts.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * 日历热力图管理器
 * 负责创建和配置日历热力图组件
 */
class CalendarHeatmapManager(
    private val context: Context
) {
    
    companion object {
        const val DEFAULT_CELL_SIZE = 40
        const val CELL_SPACING = 2
        const val WEEK_DAYS = 7
        const val MAX_WEEKS = 6
    }

    /**
     * 创建日历热力图
     */
    suspend fun createCalendarHeatmap(
        data: Map<LocalDate, UsageIntensity>,
        config: HeatmapConfig = HeatmapConfig(),
        targetDate: LocalDate = LocalDate.now()
    ): CustomCalendarView = withContext(Dispatchers.Default) {
        val calendarView = CustomCalendarView(context)
        
        // 设置数据
        calendarView.setData(data, config)
        
        // 设置目标月份
        calendarView.setTargetDate(targetDate)
        
        // 配置外观
        calendarView.setupAppearance(config)
        
        // 设置交互
        calendarView.setupInteraction()
        
        return@withContext calendarView
    }

    /**
     * 生成颜色刻度
     */
    fun generateColorScale(
        minValue: Float,
        maxValue: Float,
        colorScheme: ColorScheme = ColorScheme.BRAND,
        scaleType: ColorScale = ColorScale.GRADIENT
    ): List<Int> {
        return when (scaleType) {
            ColorScale.GRADIENT -> generateGradientColors(minValue, maxValue, colorScheme)
            ColorScale.DISCRETE -> generateDiscreteColors(minValue, maxValue, colorScheme)
            ColorScale.BINARY -> generateBinaryColors(minValue, maxValue, colorScheme)
        }
    }

    /**
     * 生成渐变色
     */
    private fun generateGradientColors(
        minValue: Float,
        maxValue: Float,
        colorScheme: ColorScheme
    ): List<Int> {
        val baseColors = getBaseColors(colorScheme)
        val colors = mutableListOf<Int>()
        
        if (maxValue <= minValue) {
            return listOf(baseColors.first())
        }
        
        val steps = 10
        for (i in 0..steps) {
            val ratio = i.toFloat() / steps
            val color = interpolateColor(baseColors.first(), baseColors.last(), ratio)
            colors.add(color)
        }
        
        return colors
    }

    /**
     * 生成离散色
     */
    private fun generateDiscreteColors(
        minValue: Float,
        maxValue: Float,
        colorScheme: ColorScheme
    ): List<Int> {
        val baseColors = getBaseColors(colorScheme)
        val colors = mutableListOf<Int>()
        
        if (maxValue <= minValue) {
            return listOf(baseColors.first())
        }
        
        val range = maxValue - minValue
        val step = range / baseColors.size
        
        for (i in baseColors.indices) {
            colors.add(baseColors[i])
        }
        
        return colors
    }

    /**
     * 生成二值色
     */
    private fun generateBinaryColors(
        minValue: Float,
        maxValue: Float,
        colorScheme: ColorScheme
    ): List<Int> {
        val baseColors = getBaseColors(colorScheme)
        return listOf(
            Color.parseColor("#f3f4f6"), // 浅色 - 低值
            baseColors.first()          // 主色 - 高值
        )
    }

    /**
     * 获取基础颜色
     */
    private fun getBaseColors(colorScheme: ColorScheme): List<Int> {
        return when (colorScheme) {
            ColorScheme.BRAND -> listOf(
                Color.parseColor("#a5b4fc"), // 浅色
                Color.parseColor("#667eea")  // 深色
            )
            ColorScheme.SUCCESS -> listOf(
                Color.parseColor("#a7f3d0"),
                Color.parseColor("#10b981")
            )
            ColorScheme.WARNING -> listOf(
                Color.parseColor("#fde68a"),
                Color.parseColor("#f59e0b")
            )
            ColorScheme.ERROR -> listOf(
                Color.parseColor("#fecaca"),
                Color.parseColor("#ef4444")
            )
            ColorScheme.MONOCHROME -> listOf(
                Color.parseColor("#d1d5db"),
                Color.parseColor("#6b7280")
            )
            ColorScheme.VIBRANT -> listOf(
                Color.parseColor("#fbbf24"),
                Color.parseColor("#f59e0b")
            )
            ColorScheme.PASTEL -> listOf(
                Color.parseColor("#e0e7ff"),
                Color.parseColor("#a5b4fc")
            )
        }
    }

    /**
     * 颜色插值
     */
    private fun interpolateColor(startColor: Int, endColor: Int, ratio: Float): Int {
        val startA = Color.alpha(startColor)
        val startR = Color.red(startColor)
        val startG = Color.green(startColor)
        val startB = Color.blue(startColor)
        
        val endA = Color.alpha(endColor)
        val endR = Color.red(endColor)
        val endG = Color.green(endColor)
        val endB = Color.blue(endColor)
        
        val a = (startA + (endA - startA) * ratio).toInt()
        val r = (startR + (endR - startR) * ratio).toInt()
        val g = (startG + (endG - startG) * ratio).toInt()
        val b = (startB + (endB - startB) * ratio).toInt()
        
        return Color.argb(a, r, g, b)
    }

    /**
     * 获取值对应的颜色索引
     */
    fun getColorIndex(value: Float, minValue: Float, maxValue: Float, colorCount: Int): Int {
        if (maxValue <= minValue) return 0
        
        val normalizedValue = (value - minValue) / (maxValue - minValue)
        val index = (normalizedValue * (colorCount - 1)).toInt()
        
        return index.coerceIn(0, colorCount - 1)
    }
}

/**
 * 自定义日历视图
 */
class CustomCalendarView(context: Context) : ViewGroup(context) {
    
    private var data: Map<LocalDate, UsageIntensity> = emptyMap()
    private var config: HeatmapConfig = HeatmapConfig()
    private var targetDate: LocalDate = LocalDate.now()
    private var colorScale: List<Int> = emptyList()
    
    private val cellViews = mutableListOf<CalendarCellView>()
    private val monthLabel: TextView
    private val weekDayLabels = mutableListOf<TextView>()
    
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy年MM月")
    private val dayFormatter = DateTimeFormatter.ofPattern("d")
    
    init {
        // 创建月份标签
        monthLabel = TextView(context).apply {
            textSize = 16f
            setTextColor(Color.parseColor("#374151"))
            textAlignment = TEXT_ALIGNMENT_CENTER
        }
        addView(monthLabel)
        
        // 创建星期标签
        val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")
        for (day in weekDays) {
            val label = TextView(context).apply {
                text = day
                textSize = 12f
                setTextColor(Color.parseColor("#6b7280"))
                textAlignment = TEXT_ALIGNMENT_CENTER
            }
            weekDayLabels.add(label)
            addView(label)
        }
    }

    /**
     * 设置数据
     */
    fun setData(data: Map<LocalDate, UsageIntensity>, config: HeatmapConfig) {
        this.data = data
        this.config = config
        
        // 计算颜色刻度
        val values = data.values.map { it.intensityLevel }
        val minValue = values.minOrNull() ?: 0f
        val maxValue = values.maxOrNull() ?: 1f
        
        colorScale = CalendarHeatmapManager(context).generateColorScale(
            minValue = minValue,
            maxValue = maxValue,
            colorScheme = ColorScheme.BRAND,
            scaleType = config.colorScale
        )
        
        // 重新创建单元格视图
        recreateCellViews()
        
        // 更新显示
        updateDisplay()
    }

    /**
     * 设置目标日期
     */
    fun setTargetDate(date: LocalDate) {
        this.targetDate = date
        monthLabel.text = date.format(monthFormatter)
        updateDisplay()
    }

    /**
     * 配置外观
     */
    fun setupAppearance(config: HeatmapConfig) {
        this.config = config
        updateDisplay()
    }

    /**
     * 设置交互
     */
    fun setupInteraction() {
        // 设置单元格点击监听器
        for (cellView in cellViews) {
            cellView.setOnClickListener {
                val date = cellView.date
                date?.let { onDateSelected(it) }
            }
        }
    }

    /**
     * 日期选择回调
     */
    private fun onDateSelected(date: LocalDate) {
        // 可以在这里添加日期选择的处理逻辑
        // 例如：显示详细信息、触发事件等
    }

    /**
     * 重新创建单元格视图
     */
    private fun recreateCellViews() {
        // 清除现有单元格
        for (cellView in cellViews) {
            removeView(cellView)
        }
        cellViews.clear()
        
        // 创建新的单元格
        val yearMonth = YearMonth.of(targetDate.year, targetDate.month)
        val firstDay = yearMonth.atDay(1)
        val lastDay = yearMonth.atEndOfMonth()
        
        var currentDate = firstDay
        while (!currentDate.isAfter(lastDay)) {
            val cellView = CalendarCellView(context)
            cellView.date = currentDate
            cellViews.add(cellView)
            addView(cellView)
            
            currentDate = currentDate.plusDays(1)
        }
    }

    /**
     * 更新显示
     */
    private fun updateDisplay() {
        // 更新单元格颜色
        for (cellView in cellViews) {
            val date = cellView.date
            if (date != null) {
                val intensity = data[date]
                if (intensity != null && colorScale.isNotEmpty()) {
                    val colorIndex = CalendarHeatmapManager(context).getColorIndex(
                        intensity.intensityLevel,
                        0f,
                        1f,
                        colorScale.size
                    )
                    cellView.setBackgroundColor(colorScale[colorIndex])
                    cellView.setTextColor(getContrastColor(colorScale[colorIndex]))
                } else {
                    cellView.setBackgroundColor(Color.parseColor("#f3f4f6"))
                    cellView.setTextColor(Color.parseColor("#6b7280"))
                }
                
                // 标记今天
                if (date == LocalDate.now() && config.showTodayIndicator) {
                    cellView.setTodayIndicator(true)
                }
            }
        }
        
        // 请求重新布局
        requestLayout()
        invalidate()
    }

    /**
     * 获取对比色
     */
    private fun getContrastColor(color: Int): Int {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return if (darkness < 0.5) Color.BLACK else Color.WHITE
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = r - l
        val height = b - t
        
        val cellSize = config.cellSize
        val spacing = CalendarHeatmapManager.CELL_SPACING
        
        // 布局月份标签
        val monthLabelHeight = 60
        monthLabel.layout(0, 0, width, monthLabelHeight)
        
        // 布局星期标签
        val weekDayHeight = 30
        for (i in weekDayLabels.indices) {
            val x = i * (cellSize + spacing)
            val right = x + cellSize
            val bottom = monthLabelHeight + weekDayHeight
            weekDayLabels[i].layout(x, monthLabelHeight, right, bottom)
        }
        
        // 布局单元格
        val startY = monthLabelHeight + weekDayHeight + spacing
        val yearMonth = YearMonth.of(targetDate.year, targetDate.month)
        val firstDay = yearMonth.atDay(1)
        val startDayOfWeek = firstDay.dayOfWeek.value % 7 // 转换为0-6，周日为0
        
        for (i in cellViews.indices) {
            val week = i / CalendarHeatmapManager.WEEK_DAYS
            val dayOfWeek = i % CalendarHeatmapManager.WEEK_DAYS
            val dayOfMonth = i - startDayOfWeek + 1
            
            val x = dayOfWeek * (cellSize + spacing)
            val y = startY + week * (cellSize + spacing)
            val right = x + cellSize
            val bottom = y + cellSize
            
            cellViews[i].layout(x, y, right, bottom)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val cellSize = config.cellSize
        val spacing = CalendarHeatmapManager.CELL_SPACING
        
        val calendarHeight = 60 + 30 + (CalendarHeatmapManager.MAX_WEEKS * (cellSize + spacing))
        val calendarWidth = CalendarHeatmapManager.WEEK_DAYS * (cellSize + spacing)
        
        setMeasuredDimension(calendarWidth, calendarHeight)
    }
}

/**
 * 日历单元格视图
 */
class CalendarCellView(context: Context) : View(context) {
    
    var date: LocalDate? = null
    private var isToday: Boolean = false
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    init {
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 14f
        textPaint.color = Color.parseColor("#374151")
    }

    fun setTodayIndicator(today: Boolean) {
        isToday = today
        invalidate()
    }
    
    fun setTextColor(color: Int) {
        textPaint.color = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val date = this.date ?: return
        
        // 绘制日期数字
        val dayText = date.dayOfMonth.toString()
        val x = width / 2f
        val y = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(dayText, x, y, textPaint)
        
        // 如果是今天，绘制边框
        if (isToday) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = Color.parseColor("#667eea")
            val margin = 4f
            canvas.drawRect(margin, margin, width - margin, height - margin, paint)
        }
    }
}