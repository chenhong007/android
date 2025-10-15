package com.example.time.ui.charts.utils

import androidx.compose.foundation.gestures.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 图表交互管理器
 * 处理图表的触摸、缩放、拖拽等交互行为
 */
class ChartInteractionManager {
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val gestureEvents = MutableSharedFlow<GestureEvent>()
    private val interactionStates = ConcurrentHashMap<String, InteractionState>()
    
    // 事件流
    val tapEvents = MutableSharedFlow<TapEvent>()
    val longPressEvents = MutableSharedFlow<LongPressEvent>()
    val zoomEvents = MutableSharedFlow<ZoomEvent>()
    val scrollEvents = MutableSharedFlow<ScrollEvent>()
    val doubleTapEvents = MutableSharedFlow<DoubleTapEvent>()
    
    init {
        // 启动事件处理协程
        scope.launch {
            gestureEvents.collect { event ->
                when (event) {
                    is GestureEvent.Tap -> handleTap(event)
                    is GestureEvent.LongPress -> handleLongPress(event)
                    is GestureEvent.Zoom -> handleZoom(event)
                    is GestureEvent.Scroll -> handleScroll(event)
                    is GestureEvent.DoubleTap -> handleDoubleTap(event)
                }
            }
        }
    }
    
    /**
     * 处理点击事件
     */
    private suspend fun handleTap(event: GestureEvent.Tap) {
        val tapEvent = TapEvent(
            position = event.position,
            chartId = event.chartId,
            timestamp = System.currentTimeMillis()
        )
        tapEvents.emit(tapEvent)
    }
    
    /**
     * 处理长按事件
     */
    private suspend fun handleLongPress(event: GestureEvent.LongPress) {
        val longPressEvent = LongPressEvent(
            position = event.position,
            chartId = event.chartId,
            timestamp = System.currentTimeMillis()
        )
        longPressEvents.emit(longPressEvent)
    }
    
    /**
     * 处理缩放事件
     */
    private suspend fun handleZoom(event: GestureEvent.Zoom) {
        val zoomEvent = ZoomEvent(
            scaleFactor = event.scaleFactor,
            focalPoint = event.focalPoint,
            chartId = event.chartId,
            timestamp = System.currentTimeMillis()
        )
        zoomEvents.emit(zoomEvent)
    }
    
    /**
     * 处理滚动事件
     */
    private suspend fun handleScroll(event: GestureEvent.Scroll) {
        val scrollEvent = ScrollEvent(
            delta = event.delta,
            position = event.position,
            chartId = event.chartId,
            timestamp = System.currentTimeMillis()
        )
        scrollEvents.emit(scrollEvent)
    }
    
    /**
     * 处理双击事件
     */
    private suspend fun handleDoubleTap(event: GestureEvent.DoubleTap) {
        val doubleTapEvent = DoubleTapEvent(
            position = event.position,
            chartId = event.chartId,
            timestamp = System.currentTimeMillis()
        )
        doubleTapEvents.emit(doubleTapEvent)
    }
    
    /**
     * 处理触摸事件
     */
    fun handlePointerEvent(
        pointerInputChange: PointerInputChange,
        chartId: String
    ) {
        scope.launch {
            val position = pointerInputChange.position
            val interactionState = getOrCreateInteractionState(chartId)
            
            when (pointerInputChange.type) {
                PointerType.Touch -> handleTouchEvent(pointerInputChange, interactionState, chartId)
                PointerType.Mouse -> handleMouseEvent(pointerInputChange, interactionState, chartId)
                else -> handleGenericEvent(pointerInputChange, interactionState, chartId)
            }
        }
    }
    
    /**
     * 处理触摸事件
     */
    private suspend fun handleTouchEvent(
        change: PointerInputChange,
        state: InteractionState,
        chartId: String
    ) {
        val position = change.position
        val timeDelta = System.currentTimeMillis() - state.lastTouchTime
        
        when {
            // 双击检测
            timeDelta < DOUBLE_TAP_TIMEOUT && position.distanceTo(state.lastTouchPosition) < DOUBLE_TAP_DISTANCE -> {
                gestureEvents.emit(GestureEvent.DoubleTap(position, chartId))
                state.resetTouchState()
            }
            // 长按检测
            timeDelta > LONG_PRESS_TIMEOUT && !state.isLongPressHandled -> {
                gestureEvents.emit(GestureEvent.LongPress(position, chartId))
                state.isLongPressHandled = true
            }
            // 点击检测
            timeDelta < TAP_TIMEOUT && !state.hasMoved -> {
                gestureEvents.emit(GestureEvent.Tap(position, chartId))
            }
            // 移动检测
            state.hasMoved -> {
                val delta = position - state.lastTouchPosition
                gestureEvents.emit(GestureEvent.Scroll(delta, position, chartId))
            }
        }
        
        state.updateTouchState(position)
    }
    
    /**
     * 处理鼠标事件
     */
    private suspend fun handleMouseEvent(
        change: PointerInputChange,
        state: InteractionState,
        chartId: String
    ) {
        val position = change.position
        
        when {
            change.pressed -> {
                if (!state.isDragging) {
                    state.startDrag(position)
                } else {
                    val delta = position - state.dragStartPosition
                    gestureEvents.emit(GestureEvent.Scroll(delta, position, chartId))
                }
            }
            else -> {
                if (state.isDragging) {
                    state.endDrag()
                }
            }
        }
    }
    
    /**
     * 处理通用事件
     */
    private suspend fun handleGenericEvent(
        change: PointerInputChange,
        state: InteractionState,
        chartId: String
    ) {
        // 默认处理为滚动事件
        val delta = change.position - state.lastTouchPosition
        gestureEvents.emit(GestureEvent.Scroll(delta, change.position, chartId))
        state.updateTouchState(change.position)
    }
    
    /**
     * 处理缩放手势
     */
    suspend fun handleTransformGesture(
        centroid: Offset,
        pan: Offset,
        zoom: Float,
        rotation: Float,
        chartId: String
    ) {
        if (zoom != 1f) {
            gestureEvents.emit(GestureEvent.Zoom(zoom, centroid, chartId))
        }
        
        if (pan != Offset.Zero) {
            gestureEvents.emit(GestureEvent.Scroll(pan, centroid, chartId))
        }
    }
    
    /**
     * 获取或创建交互状态
     */
    private fun getOrCreateInteractionState(chartId: String): InteractionState {
        return interactionStates.getOrPut(chartId) { InteractionState() }
    }
    
    /**
     * 清理指定图表的交互状态
     */
    fun clearInteractionState(chartId: String) {
        interactionStates.remove(chartId)
    }
    
    /**
     * 清理所有交互状态
     */
    fun clearAllInteractionStates() {
        interactionStates.clear()
    }
    
    /**
     * 销毁管理器
     */
    fun destroy() {
        scope.cancel()
        clearAllInteractionStates()
    }
    
    companion object {
        private const val TAP_TIMEOUT = 200L // 点击超时时间
        private const val LONG_PRESS_TIMEOUT = 500L // 长按超时时间
        private const val DOUBLE_TAP_TIMEOUT = 300L // 双击超时时间
        private const val DOUBLE_TAP_DISTANCE = 50f // 双击距离阈值
    }
}

/**
 * 交互状态
 */
data class InteractionState(
    var lastTouchPosition: Offset = Offset.Zero,
    var lastTouchTime: Long = 0L,
    var hasMoved: Boolean = false,
    var isLongPressHandled: Boolean = false,
    var isDragging: Boolean = false,
    var dragStartPosition: Offset = Offset.Zero
) {
    fun updateTouchState(position: Offset) {
        val distance = position.distanceTo(lastTouchPosition)
        if (distance > 5f) {
            hasMoved = true
        }
        lastTouchPosition = position
        lastTouchTime = System.currentTimeMillis()
    }
    
    fun resetTouchState() {
        lastTouchPosition = Offset.Zero
        lastTouchTime = 0L
        hasMoved = false
        isLongPressHandled = false
    }
    
    fun startDrag(position: Offset) {
        isDragging = true
        dragStartPosition = position
        lastTouchPosition = position
    }
    
    fun endDrag() {
        isDragging = false
        dragStartPosition = Offset.Zero
    }
}

/**
 * 手势事件
 */
sealed class GestureEvent {
    data class Tap(val position: Offset, val chartId: String) : GestureEvent()
    data class LongPress(val position: Offset, val chartId: String) : GestureEvent()
    data class Zoom(val scaleFactor: Float, val focalPoint: Offset, val chartId: String) : GestureEvent()
    data class Scroll(val delta: Offset, val position: Offset, val chartId: String) : GestureEvent()
    data class DoubleTap(val position: Offset, val chartId: String) : GestureEvent()
}

/**
 * 点击事件
 */
data class TapEvent(
    val position: Offset,
    val chartId: String,
    val timestamp: Long
)

/**
 * 长按事件
 */
data class LongPressEvent(
    val position: Offset,
    val chartId: String,
    val timestamp: Long
)

/**
 * 缩放事件
 */
data class ZoomEvent(
    val scaleFactor: Float,
    val focalPoint: Offset,
    val chartId: String,
    val timestamp: Long
)

/**
 * 滚动事件
 */
data class ScrollEvent(
    val delta: Offset,
    val position: Offset,
    val chartId: String,
    val timestamp: Long
)

/**
 * 双击事件
 */
data class DoubleTapEvent(
    val position: Offset,
    val chartId: String,
    val timestamp: Long
)

/**
 * 图表工具提示管理器
 */
class ChartTooltipManager {
    
    private val activeTooltips = ConcurrentHashMap<String, TooltipData>()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // 工具提示事件流
    val tooltipEvents = MutableSharedFlow<TooltipEvent>()
    
    /**
     * 显示工具提示
     */
    fun showTooltip(
        chartId: String,
        position: Offset,
        content: String,
        duration: Long = 3000L
    ) {
        val tooltipData = TooltipData(
            position = position,
            content = content,
            timestamp = System.currentTimeMillis(),
            duration = duration
        )
        
        activeTooltips[chartId] = tooltipData
        
        scope.launch {
            tooltipEvents.emit(TooltipEvent.Show(chartId, tooltipData))
        }
        
        // 自动隐藏
        scope.launch {
            delay(duration)
            hideTooltip(chartId)
        }
    }
    
    /**
     * 隐藏工具提示
     */
    fun hideTooltip(chartId: String) {
        activeTooltips.remove(chartId)
        
        scope.launch {
            tooltipEvents.emit(TooltipEvent.Hide(chartId))
        }
    }
    
    /**
     * 更新工具提示位置
     */
    fun updateTooltipPosition(chartId: String, newPosition: Offset) {
        activeTooltips[chartId]?.let { tooltipData ->
            val updatedData = tooltipData.copy(position = newPosition)
            activeTooltips[chartId] = updatedData
            
            scope.launch {
                tooltipEvents.emit(TooltipEvent.Update(chartId, updatedData))
            }
        }
    }
    
    /**
     * 获取活动的工具提示
     */
    fun getActiveTooltip(chartId: String): TooltipData? {
        return activeTooltips[chartId]
    }
    
    /**
     * 获取所有活动的工具提示
     */
    fun getAllActiveTooltips(): Map<String, TooltipData> {
        return activeTooltips.toMap()
    }
    
    /**
     * 清理过期工具提示
     */
    fun cleanupExpiredTooltips() {
        val currentTime = System.currentTimeMillis()
        val expiredChartIds = activeTooltips.entries
            .filter { entry ->
                currentTime - entry.value.timestamp > entry.value.duration
            }
            .map { it.key }
        
        expiredChartIds.forEach { chartId ->
            hideTooltip(chartId)
        }
    }
    
    /**
     * 销毁管理器
     */
    fun destroy() {
        scope.cancel()
        activeTooltips.clear()
    }
}

/**
 * 工具提示数据
 */
data class TooltipData(
    val position: Offset,
    val content: String,
    val timestamp: Long,
    val duration: Long
)

/**
 * 工具提示事件
 */
sealed class TooltipEvent {
    data class Show(val chartId: String, val tooltipData: TooltipData) : TooltipEvent()
    data class Hide(val chartId: String) : TooltipEvent()
    data class Update(val chartId: String, val tooltipData: TooltipData) : TooltipEvent()
}