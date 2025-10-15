package com.example.time.ui.charts.managers

import android.content.Context
import android.graphics.Color
import com.example.time.ui.charts.models.*
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * 条形图管理器
 * 负责创建和配置各种类型的条形图
 */
class BarChartManager(
    private val context: Context,
    private val performanceConfig: PerformanceConfig = PerformanceConfig()
) {
    
    companion object {
        private const val DEFAULT_ANIMATION_DURATION = 1000
        private const val MAX_VISIBLE_BARS = 10
        private const val MIN_BAR_WIDTH = 0.3f
        private const val MAX_BAR_WIDTH = 0.9f
    }

    /**
     * 创建水平条形图 - 用于应用使用排行
     */
    suspend fun createHorizontalBarChart(
        data: List<AppUsageData>,
        config: ChartConfig = ChartConfig(),
        barChartConfig: BarChartConfig = BarChartConfig()
    ): HorizontalBarChart = withContext(Dispatchers.Default) {
        val chart = HorizontalBarChart(context)
        
        // 数据采样处理
        val processedData = sampleDataIfNeeded(data, config)
        
        // 创建条形图数据
        val barData = createBarData(processedData, config, barChartConfig)
        
        // 配置图表外观
        setupChartAppearance(chart, config)
        
        // 设置坐标轴
        setupAxes(chart, processedData, config)
        
        // 设置数据
        chart.data = barData
        
        // 配置交互
        setupInteraction(chart, config)
        
        // 设置动画
        if (config.animationDuration > 0) {
            chart.animateY(config.animationDuration)
        }
        
        // 刷新图表
        chart.invalidate()
        
        return@withContext chart
    }

    /**
     * 创建垂直条形图 - 用于时间趋势
     */
    suspend fun createVerticalBarChart(
        data: List<TimeSeriesData>,
        config: ChartConfig = ChartConfig(),
        barChartConfig: BarChartConfig = BarChartConfig()
    ): BarChart = withContext(Dispatchers.Default) {
        val chart = BarChart(context)
        
        // 数据采样处理
        val processedData = sampleTimeSeriesDataIfNeeded(data, config)
        
        // 创建条形图数据
        val barData = createTimeSeriesBarData(processedData, config, barChartConfig)
        
        // 配置图表外观
        setupChartAppearance(chart, config)
        
        // 设置坐标轴
        setupTimeSeriesAxes(chart, processedData, config)
        
        // 设置数据
        chart.data = barData
        
        // 配置交互
        setupInteraction(chart, config)
        
        // 设置动画
        if (config.animationDuration > 0) {
            chart.animateY(config.animationDuration)
        }
        
        // 刷新图表
        chart.invalidate()
        
        return@withContext chart
    }

    /**
     * 创建对比条形图
     */
    suspend fun createComparisonBarChart(
        baselineData: List<AppUsageData>,
        comparisonData: List<AppUsageData>,
        config: ChartConfig = ChartConfig(),
        barChartConfig: BarChartConfig = BarChartConfig()
    ): BarChart = withContext(Dispatchers.Default) {
        val chart = BarChart(context)
        
        // 数据采样处理
        val processedBaseline = sampleDataIfNeeded(baselineData, config)
        val processedComparison = sampleDataIfNeeded(comparisonData, config)
        
        // 创建对比条形图数据
        val barData = createComparisonBarData(
            processedBaseline, 
            processedComparison, 
            config, 
            barChartConfig
        )
        
        // 配置图表外观
        setupChartAppearance(chart, config)
        
        // 设置坐标轴
        setupComparisonAxes(chart, processedBaseline, config)
        
        // 设置数据
        chart.data = barData
        
        // 配置交互
        setupInteraction(chart, config)
        
        // 设置动画
        if (config.animationDuration > 0) {
            chart.animateY(config.animationDuration)
        }
        
        // 刷新图表
        chart.invalidate()
        
        return@withContext chart
    }

    /**
     * 配置图表外观
     */
    private fun setupChartAppearance(chart: com.github.mikephil.charting.charts.BarChart, config: ChartConfig) {
        // 背景设置
        chart.setBackgroundColor(config.backgroundColor)
        chart.setDrawGridBackground(config.showGrid)
        chart.setGridBackgroundColor(config.gridColor)
        
        // 边框和描述
        chart.setDrawBorders(false)
        chart.description.isEnabled = false
        
        // 图例
        chart.legend.isEnabled = config.showLegend
        if (config.showLegend) {
            chart.legend.textColor = config.textColor
            chart.legend.textSize = 12f
        }
        
        // 触摸交互
        chart.setTouchEnabled(config.enableInteraction)
        chart.isDragEnabled = config.enableInteraction && config.enableScroll
        chart.setScaleEnabled(config.enableInteraction && config.enableZoom)
        chart.setPinchZoom(config.enableInteraction && config.enableZoom)
        
        // 性能优化
        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)
        chart.setMaxVisibleValueCount(60)
        chart.setNoDataText("暂无数据")
        chart.setNoDataTextColor(config.textColor)
    }

    /**
     * 设置坐标轴 - 应用排行
     */
    private fun setupAxes(chart: HorizontalBarChart, data: List<AppUsageData>, config: ChartConfig) {
        // X轴配置
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(config.showGrid)
        xAxis.gridColor = config.gridColor
        xAxis.textColor = config.textColor
        xAxis.textSize = 10f
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        
        // 设置X轴标签
        val labels = data.map { it.appName }.take(config.maxVisibleDataPoints)
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        
        // Y轴配置
        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(config.showGrid)
        leftAxis.gridColor = config.gridColor
        leftAxis.textColor = config.textColor
        leftAxis.textSize = 10f
        leftAxis.axisMinimum = 0f
        
        // 格式化Y轴标签为时间
        leftAxis.valueFormatter = TimeFormatter()
        
        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false
    }

    /**
     * 设置坐标轴 - 时间趋势
     */
    private fun setupTimeSeriesAxes(chart: BarChart, data: List<TimeSeriesData>, config: ChartConfig) {
        // X轴配置
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(config.showGrid)
        xAxis.gridColor = config.gridColor
        xAxis.textColor = config.textColor
        xAxis.textSize = 10f
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        
        // 设置X轴标签为时间
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val labels = data.map { timeFormatter.format(Date(it.timestamp)) }
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        
        // Y轴配置
        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(config.showGrid)
        leftAxis.gridColor = config.gridColor
        leftAxis.textColor = config.textColor
        leftAxis.textSize = 10f
        leftAxis.axisMinimum = 0f
        
        // 格式化Y轴标签为时间
        leftAxis.valueFormatter = TimeFormatter()
        
        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false
    }

    /**
     * 设置对比图坐标轴
     */
    private fun setupComparisonAxes(chart: BarChart, data: List<AppUsageData>, config: ChartConfig) {
        // X轴配置
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(config.showGrid)
        xAxis.gridColor = config.gridColor
        xAxis.textColor = config.textColor
        xAxis.textSize = 10f
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        
        // 设置X轴标签
        val labels = data.map { it.appName }.take(config.maxVisibleDataPoints)
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        
        // Y轴配置
        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(config.showGrid)
        leftAxis.gridColor = config.gridColor
        leftAxis.textColor = config.textColor
        leftAxis.textSize = 10f
        leftAxis.axisMinimum = 0f
        
        // 格式化Y轴标签为时间
        leftAxis.valueFormatter = TimeFormatter()
        
        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false
    }

    /**
     * 配置交互
     */
    private fun setupInteraction(chart: com.github.mikephil.charting.charts.BarChart, config: ChartConfig) {
        if (!config.enableInteraction) return
        
        // 设置高亮颜色
        chart.setHighlightPerDragEnabled(true)
        chart.setHighlightPerTapEnabled(true)
        
        // 设置选择监听器
        chart.setOnChartValueSelectedListener(object : com.github.mikephil.charting.listener.OnChartValueSelectedListener {
            override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: com.github.mikephil.charting.highlight.Highlight?) {
                // 处理值选择事件
                e?.let { entry ->
                    // 可以在这里添加自定义逻辑
                }
            }
            
            override fun onNothingSelected() {
                // 处理取消选择事件
            }
        })
    }

    /**
     * 创建条形图数据 - 应用使用排行
     */
    private fun createBarData(
        data: List<AppUsageData>,
        config: ChartConfig,
        barChartConfig: BarChartConfig
    ): BarData {
        // 转换数据为BarEntry
        val entries = data.mapIndexed { index, appUsage ->
            BarEntry(index.toFloat(), appUsage.totalUsageTime.toFloat(), appUsage)
        }
        
        // 创建数据集
        val dataSet = BarDataSet(entries, "应用使用时长")
        
        // 设置颜色
        val colors = if (config.colorScheme == ColorScheme.VIBRANT) {
            ColorTemplate.MATERIAL_COLORS.toList()
        } else {
            generateColors(config.colorScheme, data.size)
        }
        dataSet.colors = colors
        
        // 配置数据集
        dataSet.valueTextSize = barChartConfig.valueTextSize
        dataSet.valueTextColor = config.textColor
        dataSet.valueFormatter = TimeValueFormatter()
        
        // 创建BarData
        val barData = BarData(dataSet)
        barData.barWidth = barChartConfig.barWidth
        
        return barData
    }

    /**
     * 创建时间序列条形图数据
     */
    private fun createTimeSeriesBarData(
        data: List<TimeSeriesData>,
        config: ChartConfig,
        barChartConfig: BarChartConfig
    ): BarData {
        // 转换数据为BarEntry
        val entries = data.mapIndexed { index, timeData ->
            BarEntry(index.toFloat(), timeData.value, timeData)
        }
        
        // 创建数据集
        val dataSet = BarDataSet(entries, "时间趋势")
        
        // 设置颜色
        val colors = generateColors(config.colorScheme, 1)
        dataSet.color = colors.first()
        
        // 配置数据集
        dataSet.valueTextSize = barChartConfig.valueTextSize
        dataSet.valueTextColor = config.textColor
        
        // 创建BarData
        val barData = BarData(dataSet)
        barData.barWidth = barChartConfig.barWidth
        
        return barData
    }

    /**
     * 创建对比条形图数据
     */
    private fun createComparisonBarData(
        baselineData: List<AppUsageData>,
        comparisonData: List<AppUsageData>,
        config: ChartConfig,
        barChartConfig: BarChartConfig
    ): BarData {
        // 对齐数据（确保相同的应用）
        val alignedBaseline = baselineData.map { baseline ->
            val comparison = comparisonData.find { it.packageName == baseline.packageName }
            baseline to (comparison ?: baseline.copy(totalUsageTime = 0))
        }
        
        // 创建基准数据集
        val baselineEntries = alignedBaseline.mapIndexed { index, (baseline, _) ->
            BarEntry(index.toFloat(), baseline.totalUsageTime.toFloat(), baseline)
        }
        val baselineDataSet = BarDataSet(baselineEntries, "基准期")
        baselineDataSet.color = Color.parseColor("#667eea")
        
        // 创建对比数据集
        val comparisonEntries = alignedBaseline.mapIndexed { index, (_, comparison) ->
            BarEntry(index.toFloat(), comparison.totalUsageTime.toFloat(), comparison)
        }
        val comparisonDataSet = BarDataSet(comparisonEntries, "对比期")
        comparisonDataSet.color = Color.parseColor("#10b981")
        
        // 创建BarData
        val barData = BarData(baselineDataSet, comparisonDataSet)
        barData.barWidth = barChartConfig.barWidth
        
        // 设置组间距
        val groupCount = alignedBaseline.size
        val groupSpace = barChartConfig.groupSpacing
        val barSpace = 0.05f
        barData.groupBars(0f, groupSpace, barSpace)
        
        return barData
    }

    /**
     * 数据采样处理
     */
    private fun sampleDataIfNeeded(data: List<AppUsageData>, config: ChartConfig): List<AppUsageData> {
        if (!performanceConfig.enableSampling || data.size <= performanceConfig.maxDataPoints) {
            return data
        }
        
        // 智能采样 - 保持重要数据点
        return smartSample(data, performanceConfig.maxDataPoints)
    }

    /**
     * 时间序列数据采样
     */
    private fun sampleTimeSeriesDataIfNeeded(data: List<TimeSeriesData>, config: ChartConfig): List<TimeSeriesData> {
        if (!performanceConfig.enableSampling || data.size <= performanceConfig.maxDataPoints) {
            return data
        }
        
        // 均匀采样
        return uniformSample(data, performanceConfig.maxDataPoints)
    }

    /**
     * 智能采样算法
     */
    private fun smartSample(data: List<AppUsageData>, maxPoints: Int): List<AppUsageData> {
        // 按使用时长排序，取前N个
        return data.sortedByDescending { it.totalUsageTime }.take(maxPoints)
    }

    /**
     * 均匀采样算法
     */
    private fun uniformSample(data: List<TimeSeriesData>, maxPoints: Int): List<TimeSeriesData> {
        val step = data.size.toFloat() / maxPoints
        return (0 until maxPoints).map { i ->
            val index = (i * step).toInt().coerceAtMost(data.size - 1)
            data[index]
        }
    }

    /**
     * 生成颜色方案
     */
    private fun generateColors(colorScheme: ColorScheme, count: Int): List<Int> {
        return when (colorScheme) {
            ColorScheme.BRAND -> listOf(Color.parseColor("#667eea"))
            ColorScheme.SUCCESS -> listOf(Color.parseColor("#10b981"))
            ColorScheme.WARNING -> listOf(Color.parseColor("#f59e0b"))
            ColorScheme.ERROR -> listOf(Color.parseColor("#ef4444"))
            ColorScheme.MONOCHROME -> listOf(Color.parseColor("#6b7280"))
            ColorScheme.VIBRANT -> ColorTemplate.MATERIAL_COLORS.toList()
            ColorScheme.PASTEL -> listOf(Color.parseColor("#a5b4fc"))
        }
    }

    /**
     * 时间格式化器
     */
    private class TimeFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val hours = (value / (1000 * 60 * 60)).toInt()
            val minutes = ((value % (1000 * 60 * 60)) / (1000 * 60)).toInt()
            return when {
                hours > 0 -> "${hours}h${minutes}m"
                minutes > 0 -> "${minutes}m"
                else -> "${(value / 1000).toInt()}s"
            }
        }
    }

    /**
     * 时间值格式化器
     */
    private class TimeValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val hours = (value / (1000 * 60 * 60)).toInt()
            val minutes = ((value % (1000 * 60 * 60)) / (1000 * 60)).toInt()
            return when {
                hours > 0 -> "${hours}h"
                minutes > 0 -> "${minutes}m"
                else -> "${(value / 1000).toInt()}s"
            }
        }
    }
}