package com.kite.mnemoai.ui.widget.wheelwidget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller
import kotlin.math.abs

class WheelView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
): View(context, attr, defStyleAttr){
    private var state: WheelState = WheelState(selected = 0, offsetY = 0f)

    var adapter: WheelAdapter? = null
        set(value) {
            val hadData = field != null
            val oldSelected = state.selected
            field = value
            // 数据变了：滚动位置收进新范围（瞬移修正，不播动画）
            val clamped = state.offsetY.coerceIn(
                wheelRender.minScroll(wheelStyle, value),
                wheelRender.maxScroll(wheelStyle, value)
            )
            val newSelected = wheelStyle.selectedIndex(clamped, value?.itemCount ?: 0)
            state = state.copy(offsetY = clamped, selected = newSelected)
            // 首次设置不触发；之后数据替换且选中值变化才触发
            if (hadData && newSelected != oldSelected) onSelectedIndexChanged?.invoke(newSelected)
            requestLayout()
            invalidate()
        }

    val wheelStyle = WheelStyle(this)

    private var wheelRender: WheelRender = WheelRender()
    private var wheelControl: WheelControl
    private val gestureDetector: GestureDetector

    val selectedIndex: Int get() = state.selected

    var onSelectedIndexChanged: ((index: Int) -> Unit)? = null

    init {
        wheelControl = WheelControl(
            scroller = OverScroller(context),
            onOffsetChanged = { newOffset -> setOffset(newOffset) },
            onScrollEnd = { onScrollEnd() }
        )
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                setOffset(state.offsetY + distanceY)
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                wheelControl.fling(
                    state.offsetY, velocityY,
                    wheelRender.minScroll(wheelStyle, adapter).toInt(),
                    wheelRender.maxScroll(wheelStyle, adapter).toInt())
                return true
            }
        })
    }

    fun setCurrentItem(index: Int, smooth: Boolean = false) {
        val maxIndex = (adapter?.itemCount ?: 1) - 1
        val clampedIndex = index.coerceIn(0, maxIndex.coerceAtLeast(0))
        val minScroll = wheelRender.minScroll(wheelStyle, adapter)
        val maxScroll = wheelRender.maxScroll(wheelStyle, adapter)
        val target = wheelStyle.snapOffset(clampedIndex).coerceIn(minScroll, maxScroll)
        wheelControl.abortScroll()
        if (smooth) {
            if (abs(state.offsetY - target) > 0.5f) {
                wheelControl.smoothScrollTo(state.offsetY, target)
                postInvalidateOnAnimation()
            }
        } else {
            setOffset(target)
        }
    }

    private fun setOffset(newOffset: Float) {
        val clamped = newOffset.coerceIn(
            wheelRender.minScroll(wheelStyle, adapter),
            wheelRender.maxScroll(wheelStyle, adapter)
        )
        val newSelected = wheelStyle.selectedIndex(clamped, adapter?.itemCount ?: 0)
        val changed = newSelected != state.selected
        state = state.copy(offsetY = clamped, selected = newSelected)
        invalidate()
        if (changed) onSelectedIndexChanged?.invoke(newSelected)
    }

    private fun onScrollEnd() {
        val minScroll = wheelRender.minScroll(wheelStyle, adapter)
        val maxScroll = wheelRender.maxScroll(wheelStyle, adapter)
        val target = wheelStyle.snapOffset(state.selected).coerceIn(minScroll, maxScroll)
        if (abs(state.offsetY - target) > 0.5f) {
            wheelControl.smoothScrollTo(state.offsetY, target)
            postInvalidateOnAnimation()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        wheelRender.onDraw(canvas, wheelStyle, state, adapter)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val (width, height) = wheelRender.onMeasure(widthMeasureSpec, heightMeasureSpec, wheelStyle, adapter)
        setMeasuredDimension(width, height)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> wheelControl.abortScroll()
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                if (!wheelControl.isAnimating) onScrollEnd()
        }

        postInvalidateOnAnimation()
        return true
    }

    override fun computeScroll() {
        super.computeScroll()
        wheelControl.computeScroll()
    }
}
