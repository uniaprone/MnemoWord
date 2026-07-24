package com.kite.mnemoai.stateholder

import android.graphics.Typeface
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.color.MaterialColors
import com.kite.mnemoai.R
import com.kite.mnemoai.data.model.StudyStatistic
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

class StatisticChart(val chart: CombinedChart, val studyStatistics: List<StudyStatistic>) {
    private val dates: MutableList<String> = mutableListOf()
    private val stackedEntries: MutableList<BarEntry> = mutableListOf()
    private val lineEntries: MutableList<Entry> = mutableListOf()

    private val axisTextColor: Int = MaterialColors.getColor(chart, com.google.android.material.R.attr.colorOnSurfaceVariant)
    private val axisGridColor: Int = MaterialColors.getColor(chart, com.google.android.material.R.attr.colorSurfaceContainerLow)

    init{
        var maxLearnCount: Int = 0
        var maxLearnTime: Long = 0
        studyStatistics.forEachIndexed { index, statistic ->
            dates.add(statistic.date)

            stackedEntries.add(BarEntry(index + 0.5f, floatArrayOf(statistic.newLearnedCount.toFloat(), statistic.reviewedCount.toFloat())))
            if( statistic.newLearnedCount + statistic.reviewedCount > maxLearnCount){
                maxLearnCount = statistic.newLearnedCount + statistic.reviewedCount
            }
            
            lineEntries.add(Entry(index + 0.5f, statistic.totalLearningTime.toFloat()))
            if(statistic.totalLearningTime > maxLearnTime){
                maxLearnTime = statistic.totalLearningTime
            }
        }
        //学习数量数据
        val barDataSet = BarDataSet(stackedEntries, "").apply {
            setDrawValues(false)
            stackLabels = arrayOf<String>("新学", "复习")
            setColors(
                MaterialColors.getColor(chart, R.attr.colorPrimary),
                MaterialColors.getColor(chart, R.attr.colorSecondary)
            )
        }

        val barData = BarData(barDataSet).apply {
            barWidth = 0.6f
        }
        //学习时间数据
        val lineDataSet = LineDataSet(lineEntries, "").apply {
            label = "学习时长"
            axisDependency = YAxis.AxisDependency.RIGHT
            lineWidth = 2f
            circleRadius = 2f
            setDrawFilled(true)
            setDrawCircleHole(false)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val longValue = value.toLong()
                    val duration = longValue.milliseconds
                    return duration.toComponents { hours, minutes, seconds, _ ->
                        buildList {
                            if (hours > 0) add("${hours}小时")
                            if (minutes > 0) add("${minutes}分钟")
                            else{
                                if (seconds > 0 || isEmpty()) add("${seconds}秒")
                            }

                        }.joinToString("")
                    }
                }
            }
        }
        

        val lineData = LineData(lineDataSet)
        //合并数据
        val combinedData = CombinedData().apply {
            setData(lineData)
            setData(barData)
        }
        
        //设置x轴
        chart.xAxis.apply {
            textColor = axisTextColor
            gridColor = axisGridColor
            position = XAxis.XAxisPosition.BOTTOM
            setCenterAxisLabels(true)
            setAxisMinimum(0f)
            setAxisMaximum((stackedEntries.size + 7).toFloat())
            isGranularityEnabled = true
            setGranularity(1f)
            setLabelCount(10)
            labelRotationAngle = -45f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    if (dates.isEmpty()) {
                        return LocalDate.now().plusDays(value.toLong()).toString()
                    } else {
                        val localDate = LocalDate.parse(dates[0])
                        return localDate.plusDays(value.toLong()).toString()
                    }
                }
            }
        }
        
        //设置左y轴
        var axisLeftGranularity = 5f
        val axisLeftLabelCount = 10
        while (maxLearnCount > axisLeftGranularity * axisLeftLabelCount){
            axisLeftGranularity += 5f
        }
        val axisLeftMaxHeight = axisLeftGranularity * axisLeftLabelCount.toFloat()

        chart.axisLeft.apply {
            gridColor = axisGridColor
            setTextSize(15f)
            textColor = axisTextColor
            val typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            setTypeface(typeface)
            setLabelCount(11)
            setAxisMinimum(0f)
            setGranularity(axisLeftGranularity)
            setAxisMaximum(axisLeftMaxHeight)
            isGranularityEnabled = true
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }
        
        //设置右y轴
        var axisRightGranularity = 60000
        var axisRightLabelCount = 10
        var granularityStep = 60000 * 5
        while (maxLearnTime > axisRightGranularity * axisRightLabelCount){
            axisRightGranularity += granularityStep
        }
        val axisRightMaxHeight = axisRightGranularity * axisRightLabelCount

        chart.axisRight.apply {
            gridColor = axisGridColor
            setTextSize(15f)
            textColor = axisTextColor
            typeface = typeface
            setLabelCount(11)
            setAxisMinimum(0f)
            setGranularity(axisRightGranularity.toFloat())
            setAxisMaximum(axisRightMaxHeight.toFloat())
            isGranularityEnabled = true
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val longValue = value.toLong()
                    val duration = longValue.milliseconds
                    return duration.toComponents { hours, minutes, seconds, _ ->
                        buildList {
                            if (hours > 0) add("${hours}时")
                            if (minutes > 0 || isEmpty()) add("${minutes}分")
                        }.joinToString("")
                    }
                }
            }
        }
        
        //设置图注
        chart.legend.apply {
            textColor = MaterialColors.getColor(
                chart,
                android.R.attr.colorPrimary
            )
            xEntrySpace = 35f
            setTextSize(15f)
            formSize = 15f
            yOffset = 10f
            form = Legend.LegendForm.SQUARE
        }
        
        //设置视图
        chart.apply {
            setDrawGridBackground(false)
            setBorderWidth(1f)
            extraBottomOffset = 10f
            isScaleYEnabled = false
            isScaleXEnabled = false
            description = null
            setVisibleXRangeMaximum(10f)
            setVisibleXRangeMinimum(2f)
            setData(combinedData)
            setDrawOrder(
                arrayOf<DrawOrder>(
                    DrawOrder.BAR,  // 先画柱状图
                    DrawOrder.LINE // 再画折线图（如果有的话）
                )
            )
            invalidate()
        }
    }
}