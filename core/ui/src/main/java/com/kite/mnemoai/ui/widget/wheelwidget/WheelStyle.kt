package com.kite.mnemoai.ui.widget.wheelwidget

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.view.View
import com.kite.mnemoai.ui.R
import kotlin.math.exp

class WheelStyle(
    val view: View
) {
    val context: Context get() = view.context
    val paddingStart: Int get() = view.paddingStart
    val paddingEnd: Int get() = view.paddingEnd
    val paddingTop: Int get() = view.paddingTop
    val paddingBottom: Int get() =  view.paddingBottom
    var itemSpacing = 10f
    var textPaint = Paint(ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }
    var visibleItemCount = 5
    
    val lineHeight: Float get() = textPaint.fontSpacing + itemSpacing

    val selectionCenterY: Float get() = paddingTop + visibleItemCount * lineHeight / 2f

    init {
        textPaint.textSize = context.resources.getDimension(R.dimen.text_size_title_large)
    }

    fun measureItemWidth(text: String) = textPaint.measureText(text) + paddingStart + paddingEnd

    fun selectedIndex(offsetY: Float, itemCount: Int): Int =
        ((offsetY + selectionCenterY - paddingTop) / lineHeight)
            .toInt().coerceIn(0, (itemCount - 1).coerceAtLeast(0))

    fun snapOffset(index: Int): Float =
        (index + 0.5f) * lineHeight - (selectionCenterY - paddingTop)

    // 缩放配置：越靠近中线越大
    var minScale = 0.6f          // 最远可见项的缩放（渐近线，不会低于它）

    /** 距离中线 [distance] 行 → 缩放比例（0 行 = 1.0）。指数衰减，平滑无断崖。 */
    fun scaleForDistance(distance: Float): Float =
        (1f - (1f - minScale) * (1f - exp(-distance))).coerceIn(minScale, 1f)
}
