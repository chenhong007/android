package com.example.time.ui.charts.managers

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.example.time.ui.charts.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

/**
 * 时间线图表管理器
 * 负责创建和配置24小时时间线视图
 */
class TimelineChartManager(
    private val context: Context
) {
    
    companion object {
        private const val DEFAULT_PIXEL_PER_MINUTE = 2f
        private const val DEFAULT_SESSION_HEIGHT = 24
        private const val DEFAULT_HOUR_LABEL_HEIGHT = 30
        private const val MIN_ZOOM = 0.5f
        private const val MAX_ZOOM = 3.0f
        private const val HOURS_PER_DAY = 24
        private const val MINUTES_PER_HOUR = 60
        private const val MINUTES_PER_DAY = 1440
    }

    /**
     * 创建24小时时间线
     */
    suspend fun create24HourTimeline(
        sessions: List<AppSession>,
        config: TimelineConfig = TimelineConfig(),
        targetDate: LocalDate = LocalDate.now()
    ): CustomTimelineView = withContext(Dispatchers.Default) {
        val timelineView = CustomTimelineView(context)
        
        // 过滤指定日期的会话
        val filteredSessions = filterSessionsByDate(sessions, targetDate)
        
        // 设置数据
        timelineView.setSessions(filteredSessions, config)
        
        // 设置目标日期
        timelineView.setTargetDate(targetDate)
        
        // 配置外观
        timelineView.setupAppearance(config)
        
        // 设置交互
        timelineView.setupInteraction()
        
        return@withContext timelineView
    }

    /**
     * 渲染会话块
     */
    fun renderSessionBlocks(
        sessions: List<AppSession>,
        pixelPerMinute: Float = DEFAULT_PIXEL_PER_MINUTE,
        config: TimelineConfig = TimelineConfig()
    ): List<SessionBlock> {
        return sessions.map { session ->
            val startMinutes = getMinutesFromMidnight(session.startTime)
            val endMinutes = getMinutesFromMidnight(session.endTime)
            val durationMinutes = endMinutes - startMinutes
            
            SessionBlock(
                session = session,
                startX = startMinutes * pixelPerMinute,
                width = durationMinutes * pixelPerMinute,
                height = config.sessionHeight.toFloat(),
                color = session.color
            )
        }
    }

    /**
     * 按日期过滤会话
     */
    private fun filterSessionsByDate(
        sessions: List<AppSession>,
        targetDate: LocalDate
    ): List<AppSession> {
        val startOfDay = targetDate.atStartOfDay()
        val endOfDay = targetDate.plusDays(1).atStartOfDay()
        
        val startMillis = startOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        return sessions.filter { session ->
            session.startTime >= startMillis && session.startTime < endMillis
        }
    }

    /**
     * 从午夜开始的分钟数
     */
    private fun getMinutesFromMidnight(timestamp: Long): Int {
        val dateTime = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        )
        return dateTime.hour * MINUTES_PER_HOUR + dateTime.minute
    }

    /**
     * 设置时间轴标记
     */
    fun setupTimeAxis(
        timelineView: CustomTimelineView,
        config: TimelineConfig = TimelineConfig()
    ) {
        timelineView.setupTimeAxis(config)
    }

    /**
     * 设置缩放和平移
     */
    fun setupZoomAndPan(
        timelineView: CustomTimelineView,
        config: TimelineConfig = TimelineConfig()
    ) {
        timelineView.setupZoomAndPan(config)
    }
}

/**
 * 会话块数据类
 */
data class SessionBlock(
    val session: AppSession,
    val startX: Float,
    val width: Float,
    val height: Float,
    val color: Int
)

/**
 * 自定义时间线视图
 */
class CustomTimelineView(context: Context) : ViewGroup(context) {
    
    private var sessions: List<AppSession> = emptyList()
    private var config: TimelineConfig = TimelineConfig()
    private var targetDate: LocalDate = LocalDate.now()
    private var sessionBlocks: List<SessionBlock> = emptyList()
    
    private val timeAxisView: TimeAxisView
    private val sessionContainer: SessionContainerView
    private val nowIndicator: NowIndicatorView
    
    private var currentZoom = 1.0f
    private var currentScrollX = 0f
    private var isDragging = false
    private var lastTouchX = 0f
    
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    init {
        // 创建时间轴视图
        timeAxisView = TimeAxisView(context)
        addView(timeAxisView)
        
        // 创建会话容器视图
        sessionContainer = SessionContainerView(context)
        addView(sessionContainer)
        
        // 创建当前时间指示器
        nowIndicator = NowIndicatorView(context)
        addView(nowIndicator)
    }

    /**
     * 设置会话数据
     */
    fun setSessions(sessions: List<AppSession>, config: TimelineConfig) {
        this.sessions = sessions
        this.config = config
        
        // 渲染会话块
        sessionBlocks = TimelineChartManager(context).renderSessionBlocks(
            sessions, config.pixelPerMinute, config
        )
        
        // 更新子视图
        sessionContainer.setSessionBlocks(sessionBlocks)
        timeAxisView.setPixelPerMinute(config.pixelPerMinute)
        
        // 更新当前时间指示器
        updateNowIndicator()
        
        requestLayout()
        invalidate()
    }

    /**
     * 设置目标日期
     */
    fun setTargetDate(date: LocalDate) {
        this.targetDate = date
        updateNowIndicator()
        invalidate()
    }

    /**
     * 配置外观
     */
    fun setupAppearance(config: TimelineConfig) {
        this.config = config
        
        // 重新渲染会话块
        sessionBlocks = TimelineChartManager(context).renderSessionBlocks(
            sessions, config.pixelPerMinute, config
        )
        
        sessionContainer.setSessionBlocks(sessionBlocks)
        timeAxisView.setPixelPerMinute(config.pixelPerMinute)
        
        requestLayout()
        invalidate()
    }

    /**
     * 设置交互
     */
    fun setupInteraction() {
        // 设置触摸监听器
        setOnTouchListener { _, event ->
            handleTouchEvent(event)
            true
        }
    }

    /**
     * 设置时间轴
     */
    fun setupTimeAxis(config: TimelineConfig) {
        timeAxisView.setPixelPerMinute(config.pixelPerMinute)
        timeAxisView.setShowHourLabels(config.showHourLabels)
        requestLayout()
        invalidate()
    }

    /**
     * 设置缩放和平移
     */
    fun setupZoomAndPan(config: TimelineConfig) {
        this.config = config
        // 缩放和平移逻辑在handleTouchEvent中处理
    }

    /**
     * 处理触摸事件
     */
    private fun handleTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDragging = true
                lastTouchX = event.x
            }
            
            MotionEvent.ACTION_MOVE -> {
                if (isDragging && config.enablePan) {
                    val deltaX = event.x - lastTouchX
                    currentScrollX += deltaX
                    
                    // 限制滚动范围
                    val maxScroll = getMaxScrollX()
                    currentScrollX = currentScrollX.coerceIn(-maxScroll, 0f)
                    
                    // 应用滚动
                    applyScroll()
                    
                    lastTouchX = event.x
                }
            }
            
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
            }
        }
        
        return true
    }

    /**
     * 应用滚动
     */
    private fun applyScroll() {
        sessionContainer.translationX = currentScrollX
        timeAxisView.translationX = currentScrollX
        nowIndicator.translationX = getNowPosition() + currentScrollX
    }

    /**
     * 获取最大滚动距离
     */
    private fun getMaxScrollX(): Float {
        val totalWidth = 24 * 60 * config.pixelPerMinute * currentZoom
        return max(0f, totalWidth - width)
    }

    /**
     * 获取当前时间位置
     */
    private fun getNowPosition(): Float {
        val now = LocalDateTime.now()
        val minutesFromMidnight = now.hour * 60 + now.minute
        return minutesFromMidnight * config.pixelPerMinute * currentZoom
    }

    /**
     * 更新当前时间指示器
     */
    private fun updateNowIndicator() {
        val now = LocalDateTime.now()
        val isToday = targetDate == now.toLocalDate()
        
        nowIndicator.visibility = if (isToday && config.showNowIndicator) View.VISIBLE else View.GONE
        
        if (isToday) {
            nowIndicator.setPosition(getNowPosition())
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = r - l
        val height = b - t
        
        val timeAxisHeight = config.hourLabelHeight
        val sessionHeight = config.sessionHeight
        
        // 布局时间轴
        timeAxisView.layout(0, 0, width, timeAxisHeight)
        
        // 布局会话容器
        sessionContainer.layout(0, timeAxisHeight, width, timeAxisHeight + sessionHeight)
        
        // 布局当前时间指示器
        val nowPosition = getNowPosition()
        nowIndicator.layout(
            nowPosition.toInt() - 2,
            timeAxisHeight,
            nowPosition.toInt() + 2,
            timeAxisHeight + sessionHeight
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val timeAxisHeight = config.hourLabelHeight
        val sessionHeight = config.sessionHeight
        val totalHeight = timeAxisHeight + sessionHeight
        
        val totalWidth = (24 * 60 * config.pixelPerMinute * currentZoom).toInt()
        
        setMeasuredDimension(totalWidth, totalHeight)
        
        // 测量子视图
        timeAxisView.measure(
            MeasureSpec.makeMeasureSpec(totalWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(timeAxisHeight, MeasureSpec.EXACTLY)
        )
        
        sessionContainer.measure(
            MeasureSpec.makeMeasureSpec(totalWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(sessionHeight, MeasureSpec.EXACTLY)
        )
        
        nowIndicator.measure(
            MeasureSpec.makeMeasureSpec(4, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(sessionHeight, MeasureSpec.EXACTLY)
        )
    }
}

/**
 * 时间轴视图
 */
class TimeAxisView(context: Context) : View(context) {
    
    private var pixelPerMinute = 2f
    private var showHourLabels = true
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    init {
        paint.textSize = 12f
        paint.color = Color.parseColor("#6b7280")
        paint.textAlign = Paint.Align.CENTER
    }

    fun setPixelPerMinute(pixelPerMinute: Float) {
        this.pixelPerMinute = pixelPerMinute
        invalidate()
    }

    fun setShowHourLabels(showHourLabels: Boolean) {
        this.showHourLabels = showHourLabels
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (!showHourLabels) return
        
        // 绘制小时标签
        for (hour in 0..23) {
            val x = hour * 60 * pixelPerMinute
            val label = String.format("%02d:00", hour)
            canvas.drawText(label, x, height - 5f, paint)
            
            // 绘制小时刻度线
            paint.strokeWidth = 1f
            canvas.drawLine(x, 0f, x, height * 0.3f, paint)
        }
    }
}

/**
 * 会话容器视图
 */
class SessionContainerView(context: Context) : View(context) {
    
    private var sessionBlocks: List<SessionBlock> = emptyList()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    fun setSessionBlocks(sessionBlocks: List<SessionBlock>) {
        this.sessionBlocks = sessionBlocks
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // 绘制会话块
        for (block in sessionBlocks) {
            paint.color = block.color
            paint.alpha = 200 // 设置透明度
            
            val rect = RectF(
                block.startX,
                (height - block.height) / 2,
                block.startX + block.width,
                (height + block.height) / 2
            )
            
            // 绘制圆角矩形
            canvas.drawRoundRect(rect, 4f, 4f, paint)
            
            // 绘制边框
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            paint.color = Color.parseColor("#374151")
            paint.alpha = 255
            canvas.drawRoundRect(rect, 4f, 4f, paint)
            
            paint.style = Paint.Style.FILL
        }
    }
}

/**
 * 当前时间指示器视图
 */
class NowIndicatorView(context: Context) : View(context) {
    
    private var position = 0f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    init {
        paint.color = Color.RED
        paint.strokeWidth = 2f
    }

    fun setPosition(position: Float) {
        this.position = position
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // 绘制红色竖线
        canvas.drawLine(width / 2f, 0f, width / 2f, height.toFloat(), paint)
        
        // 绘制顶部三角形
        val path = Path()
        path.moveTo(width / 2f, 0f)
        path.lineTo(0f, height / 3f)
        path.lineTo(width.toFloat(), height / 3f)
        path.close()
        
        paint.style = Paint.Style.FILL
        canvas.drawPath(path, paint)
    }
}