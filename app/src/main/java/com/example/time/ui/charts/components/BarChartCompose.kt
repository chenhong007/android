package com.example.time.ui.charts.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import android.graphics.Color
import android.content.Context
import com.example.time.ui.charts.models.*

/**
 * MPAndroidChart条形图Compose组件
 * 在Compose中包装原生AndroidView以使用MPAndroidChart
 */
@Composable
fun BarChartCompose(
    data: BarChartData,
    modifier: Modifier = Modifier,
    isHorizontal: Boolean = false,
    showValues: Boolean = true,
    enableTouch: Boolean = true,
    onChartClick: (ChartEntry) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            if (isHorizontal) {
                createHorizontalBarChart(ctx, data, showValues, enableTouch, onChartClick)
            } else {
                createVerticalBarChart(ctx, data, showValues, enableTouch, onChartClick)
            }
        },
        update = { chart ->
            updateBarChart(chart, data, showValues, enableTouch, onChartClick)
        }
    )
}

/**
 * 创建垂直条形图
 */
private fun createVerticalBarChart(
    context: Context,
    data: BarChartData,
    showValues: Boolean,
    enableTouch: Boolean,
    onChartClick: (ChartEntry) -> Unit
): BarChart {
    val chart = BarChart(context).apply {
        // 基本配置
        description.isEnabled = false
        setTouchEnabled(enableTouch)
        isDragEnabled = enableTouch
        setScaleEnabled(enableTouch)
        setPinchZoom(false)
        
        // 图例配置
        legend.isEnabled = true
        legend.textSize = 12f
        legend.textColor = Color.parseColor("#374151")
        legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
        
        // X轴配置
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setDrawAxisLine(true)
            granularity = 1f
            textSize = 10f
            textColor = Color.parseColor("#6b7280")
            valueFormatter = IndexAxisValueFormatter(data.entries.map { it.label })
            labelRotationAngle = -45f
        }
        
        // Y轴配置
        axisLeft.apply {
            setDrawGridLines(true)
            gridColor = Color.parseColor("#e5e7eb")
            setDrawAxisLine(true)
            axisLineColor = Color.parseColor("#d1d5db")
            textSize = 10f
            textColor = Color.parseColor("#6b7280")
            valueFormatter = DurationValueFormatter()
        }
        
        axisRight.isEnabled = false
        
        // 动画
        animateY(1000)
        
        // 设置数据
        setData(createBarData(data, showValues))
        
        // 设置点击监听器
        if (enableTouch) {
            setOnChartValueSelectedListener(object : com.github.mikephil.charting.listener.OnChartValueSelectedListener {
                override fun onValueSelected(entry: com.github.mikephil.charting.data.Entry?, highlight: com.github.mikephil.charting.highlight.Highlight?) {
                    entry?.let {
                        val index = it.x.toInt()
                        if (index >= 0 && index < data.entries.size) {
                            onChartClick(data.entries[index])
                        }
                    }
                }
                
                override fun onNothingSelected() {
                    // 不处理
                }
            })
        }
        
        invalidate()
    }
    
    return chart
}

/**
 * 创建水平条形图
 */
private fun createHorizontalBarChart(
    context: Context,
    data: BarChartData,
    showValues: Boolean,
    enableTouch: Boolean,
    onChartClick: (ChartEntry) -> Unit
): HorizontalBarChart {
    val chart = HorizontalBarChart(context).apply {
        // 基本配置
        description.isEnabled = false
        setTouchEnabled(enableTouch)
        isDragEnabled = enableTouch
        setScaleEnabled(enableTouch)
        setPinchZoom(false)
        
        // 图例配置
        legend.isEnabled = true
        legend.textSize = 12f
        legend.textColor = Color.parseColor("#374151")
        legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
        
        // X轴配置（在水平图中是垂直的）
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setDrawAxisLine(true)
            granularity = 1f
            textSize = 10f
            textColor = Color.parseColor("#6b7280")
            valueFormatter = IndexAxisValueFormatter(data.entries.map { it.label })
        }
        
        // Y轴配置（在水平图中是水平的）
        axisLeft.apply {
            setDrawGridLines(true)
            gridColor = Color.parseColor("#e5e7eb")
            setDrawAxisLine(true)
            axisLineColor = Color.parseColor("#d1d5db")
            textSize = 10f
            textColor = Color.parseColor("#6b7280")
            valueFormatter = DurationValueFormatter()
        }
        
        axisRight.isEnabled = false
        
        // 动画
        animateX(1000)
        
        // 设置数据
        setData(createHorizontalBarData(data, showValues))
        
        // 设置点击监听器
        if (enableTouch) {
            setOnChartValueSelectedListener(object : com.github.mikephil.charting.listener.OnChartValueSelectedListener {
                override fun onValueSelected(entry: com.github.mikephil.charting.data.Entry?, highlight: com.github.mikephil.charting.highlight.Highlight?) {
                    entry?.let {
                        val index = it.y.toInt()
                        if (index >= 0 && index < data.entries.size) {
                            onChartClick(data.entries[index])
                        }
                    }
                }
                
                override fun onNothingSelected() {
                    // 不处理
                }
            })
        }
        
        invalidate()
    }
    
    return chart
}

/**
 * 更新条形图数据
 */
private fun updateBarChart(
    chart: BarChart,
    data: BarChartData,
    showValues: Boolean,
    enableTouch: Boolean,
    onChartClick: (ChartEntry) -> Unit
) {
    chart.apply {
        // 更新数据
        val barData = if (this is HorizontalBarChart) {
            createHorizontalBarData(data, showValues)
        } else {
            createBarData(data, showValues)
        }
        
        setData(barData)
        
        // 更新X轴标签
        xAxis.valueFormatter = IndexAxisValueFormatter(data.entries.map { it.label })
        
        // 重新设置点击监听器
        if (enableTouch) {
            setOnChartValueSelectedListener(object : com.github.mikephil.charting.listener.OnChartValueSelectedListener {
                override fun onValueSelected(entry: com.github.mikephil.charting.data.Entry?, highlight: com.github.mikephil.charting.highlight.Highlight?) {
                    entry?.let {
                        val index = if (this@apply is HorizontalBarChart) it.y.toInt() else it.x.toInt()
                        if (index >= 0 && index < data.entries.size) {
                            onChartClick(data.entries[index])
                        }
                    }
                }
                
                override fun onNothingSelected() {
                    // 不处理
                }
            })
        }
        
        invalidate()
    }
}

/**
 * 创建条形图数据
 */
private fun createBarData(data: BarChartData, showValues: Boolean): BarData {
    val entries = data.entries.mapIndexed { index, entry ->
        BarEntry(index.toFloat(), entry.value)
    }
    
    val dataSet = BarDataSet(entries, data.title).apply {
        // 设置颜色
        colors = data.entries.map { entry ->
            if (entry.color != 0) {
                entry.color
            } else {
                ColorTemplate.MATERIAL_COLORS[0]
            }
        }
        
        // 设置值显示
        setDrawValues(showValues)
        valueTextSize = 10f
        valueTextColor = Color.parseColor("#374151")
        valueFormatter = DurationValueFormatter()
        
        // 设置高亮
        highLightColor = Color.parseColor("#3b82f6")
        highLightAlpha = 200
    }
    
    return BarData(dataSet).apply {
        barWidth = 0.8f
        setValueTextSize(10f)
    }
}

/**
 * 创建水平条形图数据
 */
private fun createHorizontalBarData(data: BarChartData, showValues: Boolean): BarData {
    val entries = data.entries.mapIndexed { index, entry ->
        BarEntry(index.toFloat(), entry.value, entry.label)
    }
    
    val dataSet = BarDataSet(entries, data.title).apply {
        // 设置颜色
        colors = data.entries.map { entry ->
            if (entry.color != 0) {
                entry.color
            } else {
                ColorTemplate.MATERIAL_COLORS[0]
            }
        }
        
        // 设置值显示
        setDrawValues(showValues)
        valueTextSize = 10f
        valueTextColor = Color.parseColor("#374151")
        valueFormatter = DurationValueFormatter()
        
        // 设置高亮
        highLightColor = Color.parseColor("#3b82f6")
        highLightAlpha = 200
    }
    
    return BarData(dataSet).apply {
        barWidth = 0.8f
        setValueTextSize(10f)
    }
}

/**
 * 时长格式化器
 * 将分钟数格式化为易读的字符串
 */
private class DurationValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        val minutes = value.toInt()
        return when {
            minutes < 60 -> "${minutes}m"
            minutes < 1440 -> "${minutes / 60}h${minutes % 60}m"
            else -> "${minutes / 1440}d${(minutes % 1440) / 60}h"
        }
    }
}