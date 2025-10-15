package com.example.time.ui.statistics.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.time.ui.charts.managers.BarChartManager
import com.example.time.ui.charts.managers.CalendarHeatmapManager
import com.example.time.ui.charts.managers.TimelineChartManager
import com.example.time.ui.charts.models.*
import com.example.time.ui.components.LoadingIndicator
import com.example.time.ui.components.ErrorMessage
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun BarChartComponent(
    chartData: BarChartData?,
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier,
    config: ChartConfig = ChartConfig(),
    barChartConfig: BarChartConfig = BarChartConfig(),
    onChartClick: ((AppUsageData) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                LoadingIndicator()
            }

            error != null -> {
                ErrorMessage(
                    message = error,
                    onRetry = { /* 重试逻辑 */ }
                )
            }

            chartData != null && chartData.entries.isNotEmpty() -> {
                AndroidView(
                    factory = { ctx ->
                        val chart = HorizontalBarChart(ctx)

                        scope.launch {
                            try {
                                val manager = BarChartManager(ctx)
                                val configuredChart = manager.createComparisonBarChart(
                                    baselineData = chartData.entries.map { entry ->
                                        AppUsageData(
                                            packageName = "",
                                            appName = entry.label,
                                            totalUsageTime = entry.value.toLong(),
                                            usageCount = 0,
                                            category = AppCategory.OTHER,
                                            color = entry.color
                                        )
                                    },
                                    comparisonData = emptyList(),
                                    config = config.copy(
                                        textColor = onSurfaceColor,
                                        backgroundColor = surfaceColor
                                    ),
                                    barChartConfig = barChartConfig
                                )

                                // 应用配置到原始图表
                                chart.data = configuredChart.data
                                chart.xAxis.apply {
                                    configuredChart.xAxis.let { sourceAxis ->
                                        this.position = sourceAxis.position
                                        this.setDrawGridLines(config.showGrid)
                                        this.granularity = sourceAxis.granularity
                                        this.valueFormatter = sourceAxis.valueFormatter
                                        this.textColor = onSurfaceColor
                                    }
                                }
                                chart.axisLeft.apply {
                                    configuredChart.axisLeft.let { sourceAxis ->
                                        this.setDrawGridLines(config.showGrid)
                                        this.granularity = sourceAxis.granularity
                                        this.textColor = onSurfaceColor
                                    }
                                }
                                chart.axisRight.isEnabled = false
                                chart.legend.isEnabled = false
                                chart.description.isEnabled = false
                                chart.setTouchEnabled(true)
                                chart.isDragEnabled = true
                                chart.setScaleEnabled(false)
                                chart.setPinchZoom(false)
                                chart.animateY(1000)
                                chart.invalidate()

                                // 点击事件
                                onChartClick?.let { clickHandler ->
                                    chart.setOnChartValueSelectedListener(
                                        object : com.github.mikephil.charting.listener.OnChartValueSelectedListener {
                                            override fun onValueSelected(
                                                e: com.github.mikephil.charting.data.Entry?,
                                                h: com.github.mikephil.charting.highlight.Highlight?
                                            ) {
                                                e?.let { entry ->
                                                    val index = entry.x.toInt()
                                                    if (index < chartData.entries.size) {
                                                        clickHandler(
                                                            AppUsageData(
                                                                packageName = "",
                                                                appName = chartData.entries[index].label,
                                                                totalUsageTime = chartData.entries[index].value.toLong(),
                                                                usageCount = 0,
                                                                category = AppCategory.OTHER
                                                            )
                                                        )
                                                    }
                                                }
                                            }

                                            override fun onNothingSelected() {}
                                        }
                                    )
                                }
                            } catch (e: Exception) {
                                // 处理图表创建错误
                                e.printStackTrace()
                            }
                        }

                        chart
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                Text(
                    text = "暂无数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CalendarHeatmapComponent(
    heatmapData: HeatmapData?,
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier,
    config: ChartConfig = ChartConfig(),
    onDateSelected: ((LocalDate) -> Unit)? = null
) {
    val context = LocalContext.current
    val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> LoadingIndicator()
            error != null -> ErrorMessage(message = error, onRetry = { /* 重试逻辑 */ })
            heatmapData != null && heatmapData.dateValues.isNotEmpty() -> {
                AndroidView(
                    factory = { ctx ->
                        val view = com.example.time.ui.charts.managers.CustomCalendarView(ctx)
                        val heatmapConfig = HeatmapConfig()
                        val dataMap = heatmapData.dateValues.mapValues { UsageIntensity(0L, 0, 0, it.value) }
                        view.setData(dataMap, heatmapConfig)
                        view.setTargetDate(heatmapData.startDate)
                        view.setupAppearance(heatmapConfig)
                        view.setupInteraction()
                        view
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Text(
                    text = "暂无数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TimelineChartComponent(
    timelineData: List<AppSession>?,
    selectedDate: LocalDate,
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier,
    config: ChartConfig = ChartConfig(),
    onSessionClick: ((AppSession) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> LoadingIndicator()
            error != null -> ErrorMessage(message = error, onRetry = { /* 重试逻辑 */ })
            timelineData != null && timelineData.isNotEmpty() -> {
                AndroidView(
                    factory = { ctx ->
                        // 使用自定义时间线视图而非MPAndroidChart
                        val timelineView = com.example.time.ui.charts.managers.CustomTimelineView(ctx)
                        val timelineConfig = TimelineConfig(
                            pixelPerMinute = 2f,
                            sessionHeight = 24,
                            hourLabelHeight = 30,
                            enableZoom = true,
                            minZoom = 0.5f,
                            maxZoom = 3.0f,
                            showHourLabels = true,
                            showNowIndicator = true,
                            enablePan = true
                        )

                        // 异步配置数据
                        scope.launch {
                            try {
                                timelineView.setSessions(timelineData, timelineConfig)
                                timelineView.setTargetDate(selectedDate)
                                timelineView.setupAppearance(timelineConfig)
                                timelineView.setupInteraction()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        timelineView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Text(
                    text = "暂无数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ChartTypeSwitcher(
    selectedType: ChartType,
    onTypeSelected: (ChartType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChartType.values().forEach { type ->
            val text = when (type) {
                ChartType.BAR_CHART -> "应用排行"
                ChartType.CALENDAR_HEATMAP -> "热力图"
                ChartType.TIMELINE -> "时间线"
            }
            ChartTypeChip(
                text = text,
                selected = selectedType == type,
                onClick = { onTypeSelected(type) }
            )
        }
    }
}

@Composable
private fun ChartTypeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val textColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier
            .padding(4.dp)
            .height(32.dp),
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = textColor
            )
        }
    }
}
