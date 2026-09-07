package com.kite.mnemoai.ui.widget.wheelwidget

import android.graphics.Canvas
import android.view.View.MeasureSpec
import kotlin.math.abs
import androidx.core.graphics.withScale

class WheelRender {
    fun minScroll(wheelStyle: WheelStyle, adapter: WheelAdapter?): Float {
        val itemCount = adapter?.itemCount ?: 0
        return if(itemCount == 0) 0f else wheelStyle.snapOffset(0)
    }
    fun maxScroll(wheelStyle: WheelStyle, adapter: WheelAdapter?): Float {
        val itemCount = adapter?.itemCount ?: 0
        return if(itemCount == 0) 0f else wheelStyle.snapOffset(itemCount - 1)
    }

    fun onDraw(
        canvas: Canvas,
        wheelStyle: WheelStyle,
        wheelState: WheelState,
        wheelAdapter: WheelAdapter?
    ) {
        val adapter = wheelAdapter ?: return
        val itemCount = adapter.itemCount
        if (itemCount == 0) return

        val lineHeight = wheelStyle.lineHeight
        val visibleItemCount = wheelStyle.visibleItemCount

        val firstIndex = (wheelState.offsetY / lineHeight).toInt()
        val lastIndex = (wheelState.offsetY / lineHeight + visibleItemCount).toInt()
        val first = firstIndex.coerceIn(0, itemCount - 1)
        val last = lastIndex.coerceIn(0, itemCount - 1)

        for (i in first..last) {
            val top = wheelStyle.paddingTop + i * lineHeight - wheelState.offsetY
            val centerY = top + lineHeight / 2f
            val baseline = centerY - (wheelStyle.textPaint.ascent() + wheelStyle.textPaint.descent()) / 2f
            val text = adapter.getItemText(i)

            val scale = wheelStyle.scaleForDistance(abs(centerY - wheelStyle.selectionCenterY) / lineHeight)
            val oldAlpha = wheelStyle.textPaint.alpha
            wheelStyle.textPaint.alpha = (255 * scale).toInt()

            canvas.withScale(
                scale,
                scale,
                wheelStyle.paddingStart + wheelStyle.textPaint.measureText(text) / 2f,
                centerY
            ) {
                // 以文字中心为锚点缩放：变大变小都保持居中
                drawText(text, wheelStyle.paddingStart.toFloat(), baseline, wheelStyle.textPaint)
            }

            wheelStyle.textPaint.alpha = oldAlpha
        }

    }

    fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        wheelStyle: WheelStyle,
        adapter: WheelAdapter?
    ): Pair<Int, Int> {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val contentWidth = measureAdapterMaxWidth(wheelStyle, adapter).toInt()
        val width = when (widthMode) {
            MeasureSpec.AT_MOST -> minOf(contentWidth, widthSize)
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.UNSPECIFIED -> minOf(contentWidth, widthSize)
            else -> minOf(contentWidth, widthSize)
        }

        val contentHeight = (wheelStyle.paddingTop + wheelStyle.visibleItemCount * wheelStyle.lineHeight + wheelStyle.paddingBottom).toInt()
        val height = when (heightMode) {
            MeasureSpec.AT_MOST -> minOf(contentHeight, heightSize)
            MeasureSpec.UNSPECIFIED -> contentHeight
            MeasureSpec.EXACTLY -> heightSize
            else -> heightSize
        }
        return width to height
    }

    private fun measureAdapterMaxWidth(wheelStyle: WheelStyle, adapter: WheelAdapter?): Float {
        val adapter = adapter ?: return 0f
        var maxWidth = 0f
        for (i in 0 until adapter.itemCount) {
            val itemWidth = wheelStyle.measureItemWidth(adapter.getItemText(i))
            if (itemWidth > maxWidth) {
                maxWidth = itemWidth
            }
        }
        return maxWidth
    }
}
