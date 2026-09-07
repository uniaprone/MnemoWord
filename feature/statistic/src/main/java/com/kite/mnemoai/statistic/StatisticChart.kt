package com.kite.mnemoai.statistic

import android.graphics.Typeface
import android.view.View
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
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
import com.kite.mnemoai.ui.R
import com.kite.mnemoai.model.statistic.StudyStatistic
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

class StatisticChart(val chart: CombinedChart, studyStatistics: List<StudyStatistic>) {
    private val dates: MutableList<String> = mutableListOf()
    private val learnedEntryGroup: MutableList<BarEntry> = mutableListOf()
    private val reviewedEntryGroup: MutableList<BarEntry> = mutableListOf()
    private val lineEntries: MutableList<Entry> = mutableListOf()

    private val surfaceColor = MaterialColors.getColor(chart, R.attr.colorOnSurface)
    private val axisTextColor: Int = MaterialColors.getColor(chart, R.attr.colorOnSurfaceVariant)
    private val axisGridColor: Int = MaterialColors.getColor(chart, R.attr.colorSurfaceContainer)

    private val learnedTimeColor = MaterialColors.getColor(chart, R.attr.colorTertiaryContainer)
    private val reviewedColor = MaterialColors.getColor(chart, R.attr.colorSecondaryContainer)
    private val learnedColor = MaterialColors.getColor(chart, R.attr.colorPrimary)

    private val groupSpace = 0.2f
    private val barSpace = 0.08f
    private val barWidth = 0.32f

    init{
        var maxLearnCount: Int = 0
        var maxLearnTime: Long = 0
        studyStatistics.forEachIndexed { index, statistic ->
            dates.add(statistic.date)

            learnedEntryGroup.add(BarEntry(index.toFloat(), statistic.newLearnedCount.toFloat()))
            reviewedEntryGroup.add(BarEntry(index.toFloat(), statistic.reviewedCount.toFloat()))

            if( statistic.newLearnedCount > maxLearnCount){
                maxLearnCount = statistic.newLearnedCount
            }else if(statistic.reviewedCount > maxLearnCount){
                maxLearnCount = statistic.reviewedCount
            }

            lineEntries.add(Entry(index + 0.5f, statistic.totalLearningTime.toFloat()))
            if(statistic.totalLearningTime > maxLearnTime){
                maxLearnTime = statistic.totalLearningTime
            }
        }
        //学习数量数据
        val learnedSet = BarDataSet(learnedEntryGroup, "新学").apply {
            color = learnedColor
        }
        val reviewedSet = BarDataSet(reviewedEntryGroup, "复习").apply {
            color = reviewedColor
        }

        val barData = BarData(learnedSet, reviewedSet)
        barData.barWidth = barWidth
        barData.groupBars(0f, groupSpace, barSpace)
        //学习时间数据
        val lineDataSet = LineDataSet(lineEntries, "").apply {
            color = learnedTimeColor
            fillColor = learnedTimeColor
            fillAlpha = 85
            label = "学习时长"
            axisDependency = YAxis.AxisDependency.RIGHT
            lineWidth = 2f
            circleRadius = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER;
            isHighlightEnabled = false
            setDrawCircles(false)
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
            setDrawGridLines(false)
            setCenterAxisLabels(true)
            setAxisMinimum(0f)
            setAxisMaximum((learnedEntryGroup.size).toFloat())
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
            setDrawAxisLine(false)
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
            setDrawAxisLine(false)
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
        val legends: List<LegendEntry> = listOf(
            LegendEntry(
                "学习时长", Legend.LegendForm.LINE,
                15f, 0f,
                null, learnedTimeColor),
            LegendEntry(
                "新学", Legend.LegendForm.SQUARE,
                15f, 0f,
                null, learnedColor),
            LegendEntry(
                "复习", Legend.LegendForm.SQUARE,
                15f, 0f,
                null, reviewedColor)
        )
        chart.legend.apply {
            setCustom(legends)
            textColor = surfaceColor
            xEntrySpace = 10f
            setTextSize(15f)
            yOffset = 10f
            form = Legend.LegendForm.SQUARE
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        }

        //设置视图
        chart.apply {
            animateY(1000, Easing.EaseInQuad)
            setDrawGridBackground(false)
            setBorderWidth(1f)
//            extraBottomOffset = 10f
            isScaleYEnabled = false
            isScaleXEnabled = true
            description = null
            setVisibleXRangeMaximum(10f)
            setVisibleXRangeMinimum(7f)
            setData(combinedData)
            setDrawOrder(
                arrayOf(
                    CombinedChart.DrawOrder.BAR,  // 先画柱状图
                    CombinedChart.DrawOrder.LINE // 再画折线图（如果有的话）
                )
            )
            chart.invalidate()
        }
    }
}
