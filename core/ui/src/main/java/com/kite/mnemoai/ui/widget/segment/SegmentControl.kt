package com.kite.mnemoai.ui.widget.segment

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.updateLayoutParams
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.kite.mnemoai.ui.R
import com.kite.mnemoai.ui.databinding.ViewSegmentedControlBinding
import com.kite.mnemoai.ui.dpToPx

open class SegmentedControl @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {
    val Float.dp: Float
        get() = this * Resources.getSystem().displayMetrics.density
    val binding = ViewSegmentedControlBinding.inflate(LayoutInflater.from(context), this, true)
    protected var segmentTitles: List<String> = mutableListOf()
    private var thumbBackground: Drawable
    private var segmentContainerBg: ColorStateList? = null
    protected var segments: MutableList<MaterialButton> = mutableListOf()
    private var listener: ((Int) -> Unit)? = null
    var currentIndex = -1

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SegmentedControl,
            0, 0
        ).apply {
            try{
                thumbBackground = ContextCompat.getDrawable(context,
                    getResourceId(R.styleable.SegmentedControl_thumbBackground, -1))?:createBackground()
                segmentContainerBg = getColorStateList(R.styleable.SegmentedControl_segmentContainerBg)
                val titlesString  = getString(R.styleable.SegmentedControl_segmentTitle)?:""
                if(titlesString.isEmpty()) return@apply
                segmentTitles = titlesString.split("|")
            } finally {
              recycle()
            }
        }
        // 仅当使用方显式设置了 segmentContainerBg 时才覆盖布局里的默认 backgroundTint
        segmentContainerBg?.let { binding.segmentContainerFL.backgroundTintList = it }
        binding.thumb.background = thumbBackground
        if(!segmentTitles.isEmpty()) init()
    }

    private fun init(){
        segments.clear()
        binding.container.removeAllViews()
        segmentTitles.forEachIndexed { index, title ->
            val button: MaterialButton = MaterialButton(context).apply {
                text = title
                isCheckable = false
                backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
                strokeWidth = 0
                insetTop = 0
                insetBottom = 0

                minHeight = 0
                minimumHeight = 0
                minWidth = 0
                minimumWidth = 0

                setPadding(context.dpToPx(4), context.dpToPx(4), context.dpToPx(4), context.dpToPx(4))

                cornerRadius = 0

                rippleColor = null
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    setMargins(context.dpToPx(4), context.dpToPx(4), context.dpToPx(4), context.dpToPx(4))
                    weight = 1f
                }

                setOnClickListener {
                    onSegmentClick(index)
                }
            }
            segments.add(button)
            binding.container.addView(button)
        }
        invalidate()
        requestLayout()
    }

    fun setTitle(titles: List<String>){
        segmentTitles = titles
        init()
    }

    protected open fun onSegmentClick(index: Int) {
        setSelectedIndex(index)
    }

    fun setDefaultSelection(index:Int){
        currentIndex = index
        refreshButtons(index)
        updateThumb(index, false)
    }

    fun setSelectedIndex(index: Int) {
        if (index == currentIndex) return
        currentIndex = index
        refreshButtons(index)
        updateThumb(index, true)
        // 业务回调与动画解耦：选中即通知，不依赖 transition 生命周期
        listener?.invoke(index)
    }
    private fun createBackground(): Drawable = MaterialShapeDrawable().apply {
        fillColor =
            ColorStateList.valueOf(
                MaterialColors.getColor(
                    context,
                    android.R.attr.colorPrimary,
                    Color.BLACK
                )
            )

        shapeAppearanceModel =
            ShapeAppearanceModel.builder()
                .setAllCornerSizes(4f.dp)
                .build()
    }

    fun setOnSelectionChangedListener(listener: (Int) -> Unit) {
        this.listener = listener
    }


    private fun updateThumb(index: Int, animate: Boolean) {
        binding.container.post {
            var startMargin = context.dpToPx(4)
            for (i in 0 until index) {
                startMargin += binding.container.getChildAt(i).measuredWidth +
                        binding.container.getChildAt(i).marginStart +
                        binding.container.getChildAt(i).marginEnd
            }
            val thumbWidth = binding.container.getChildAt(index).width +
                    binding.container.getChildAt(index).marginStart +
                    binding.container.getChildAt(index).marginEnd
            if (animate) {
                // 只做视觉动画；不再挂 listener/endTransitions，
                // 避免连续点击时结束掉后发起的过渡导致 thumb 闪回
                TransitionManager.beginDelayedTransition(
                    binding.root,
                    ChangeBounds().apply { duration = 300 }
                )
                binding.thumb.updateLayoutParams<LayoutParams> {
                    width = thumbWidth
                    height = binding.container.height
                    marginStart = startMargin
                    topMargin = context.dpToPx(4)
                }
            }else{
                binding.thumb.updateLayoutParams<LayoutParams> {
                    width = thumbWidth
                    height = binding.container.height
                    marginStart = startMargin
                    topMargin = context.dpToPx(4)
                }
            }
        }
    }

    private fun refreshButtons(index: Int) {
        for (i in 0 until binding.container.childCount) {
            val button = binding.container.getChildAt(i) as MaterialButton
            if (i == index) {
                button.setTextColor(MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorOnPrimary))
            } else {
                button.setTextColor(MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorOnSurface))
            }
        }
    }
}