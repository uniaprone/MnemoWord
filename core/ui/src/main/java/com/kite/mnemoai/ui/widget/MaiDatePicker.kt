package com.kite.mnemoai.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.NumberPicker
import android.widget.OverScroller
import androidx.core.view.GestureDetectorCompat
import com.kite.mnemoai.ui.R
import com.kite.mnemoai.ui.dpToPxFloat
import java.time.LocalDate
import java.time.Year
import java.util.Calendar
import kotlin.math.max

class MaiDatePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val visibleItemCount = 5
    private val calendar = Calendar.getInstance()
    private val defaultYear = calendar.get(Calendar.YEAR)
    private val defaultMonth = calendar.get(Calendar.MONTH)
    private val defaultDay = calendar.get(Calendar.DATE)

    private val mainStringPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
    }
    private var textPaddingStart = context.dpToPxFloat(8f)
    private var textPaddingEnd = context.dpToPxFloat(8f)
    private var textPaddingTop = context.dpToPxFloat(8f)
    private var textPaddingBottom = context.dpToPxFloat(8f)
    private val textPaint = Paint(ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    private val maiDateItem = MaiDateItem(
        defaultYear, defaultMonth, defaultDay,
        (1970..defaultYear).toList(), (1..12).toList(),
        (1..getMaxDaysInMonth(defaultYear, defaultMonth)).toList()
    )
    private var gestureDetector: GestureDetector
    private val scroller = OverScroller(context)
    private var scrollOffsetY = 0f
    private val maxScrollY: Float
        get() = ((maiDateItem.years.size - visibleItemCount) * textPaint.fontSpacing)
            .coerceAtLeast(0f)


    init {
        textPaint.textSize = resources.getDimension(R.dimen.text_size_body_medium)
        gestureDetector =
            GestureDetector(this.context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    return true
                }

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    fling(-velocityY.toInt())
                    return true
                }

                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    scrollOffsetY += distanceY
                    scrollOffsetY = scrollOffsetY.coerceIn(0f, maxScrollY)
                    invalidate()
                    return true
                }
            })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    override fun computeScroll() {
        super.computeScroll()
        if (scroller.computeScrollOffset()) {
            scrollOffsetY = scroller.currY.toFloat()
            invalidate()
        }
    }

    fun fling(velocityY: Int) {
        scroller.forceFinished(true)
        // 滚动范围：0 到 (总条目数 - 可见数) * 条目高度
        scroller.fling(
            0, scrollOffsetY.toInt(),
            0, velocityY,
            0, 0,
            0, maxScrollY.toInt()
        )
        postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            maiDateItem.years.forEachIndexed { index, year ->
                val baseline = -textPaint.ascent() + index * (textPaint.fontSpacing + textPaddingTop)
                when(index){
                    0 ->
                        drawText(
                            year.toString(),
                            0f + paddingLeft + textPaddingStart,
                            baseline - scrollOffsetY + textPaddingTop,
                            textPaint
                        )
                    maiDateItem.years.size - 1 ->
                        drawText(
                            year.toString(),
                            0f + paddingLeft + textPaddingStart,
                            baseline - scrollOffsetY + textPaddingTop,
                            textPaint
                        )
                }

            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val contentWidth = maiDateItem.years.maxOf { textPaint.measureText(it.toString()) }
            .toInt() + paddingLeft + paddingRight
        val width = when (widthMode) {
            MeasureSpec.AT_MOST -> minOf(contentWidth, widthSize)
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.UNSPECIFIED -> minOf(contentWidth, widthSize)
            else -> minOf(contentWidth, widthSize)
        }

        val contentHeight = (5 * textPaint.fontSpacing + paddingTop + paddingBottom).toInt()
        val height = when (heightMode) {
            MeasureSpec.AT_MOST -> minOf(contentHeight, heightSize)
            MeasureSpec.UNSPECIFIED -> contentHeight
            MeasureSpec.EXACTLY -> heightSize
            else -> heightSize
        }

        setMeasuredDimension(width, height)
    }

    private data class MaiDateItem(
        var selectedYear: Int,
        var selectedMonth: Int,
        var selectedDay: Int,
        val years: List<Int>,
        val months: List<Int> = (1..12).toList(),
        val days: List<Int>
    )

    private fun getMaxDaysInMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1) // 将日历设置为该年月的第一天
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH) // 获取该月的最大天数
    }
}