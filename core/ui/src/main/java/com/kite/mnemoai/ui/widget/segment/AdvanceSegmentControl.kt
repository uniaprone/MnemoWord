package com.kite.mnemoai.ui.widget.segment

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import com.kite.mnemoai.ui.R

/**
 * 在最后一个 segment 上挂自定义点击行为（如弹出对话框）。
 * “自定义”项通过 segmentTitle 属性写入，与其他 segment 一起由基类构建，
 * 这里只负责特判最后一项的点击。
 */
class AdvanceSegmentControl @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
): SegmentControl(context, attrs, defStyleAttr) {

    private var customSegmentClickListener: (() -> Unit)? = null

    fun setCustomSegmentClickListener(listener: (() -> Unit)?) {
        customSegmentClickListener = listener
    }

    override fun onSegmentClick(index: Int) {
        if (index == segmentTitles.lastIndex && index == currentIndex) {

            customSegmentClickListener?.invoke()
        } else {
            super.onSegmentClick(index)
        }
    }

    fun setCustomTitle(title: CharSequence){
        if(segments.isNotEmpty()){
            segments[segments.size - 1].text = title
            segments[segments.size - 1].setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.text_size_body_small))
        }
        invalidate()
        requestLayout()
    }
}