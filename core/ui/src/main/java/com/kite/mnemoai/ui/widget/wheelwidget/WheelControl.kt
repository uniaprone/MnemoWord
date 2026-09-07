package com.kite.mnemoai.ui.widget.wheelwidget

import android.widget.OverScroller
import kotlin.math.roundToInt

private const val SNAP_DURATION_MS = 300

class WheelControl(
    private val scroller: OverScroller,
    private val onOffsetChanged: (Float) -> Unit,
    private val onScrollEnd: () -> Unit
) {
    private var _isAnimating = false
    val isAnimating: Boolean get() = _isAnimating

    fun fling(startOffset: Float, velocityY: Float, minScroll: Int, maxScroll: Int) {
        _isAnimating = true
        scroller.fling(
            0, startOffset.toInt(),
            0, -velocityY.toInt(),
            0, 0,
            minScroll, maxScroll
        )
    }

    fun smoothScrollTo(startOffset: Float, targetOffset: Float, duration: Int = SNAP_DURATION_MS) {
        _isAnimating = true
        scroller.startScroll(
            0, startOffset.roundToInt(),
            0, (targetOffset - startOffset).roundToInt(),
            duration
        )
    }

    fun computeScroll(): Boolean {
        if (scroller.computeScrollOffset()) {
            onOffsetChanged(scroller.currY.toFloat())
            return true
        }
        if (_isAnimating) {
            _isAnimating = false
            onScrollEnd()
        }
        return false
    }

    fun abortScroll() {
        _isAnimating = false
        scroller.abortAnimation()
    }
}
