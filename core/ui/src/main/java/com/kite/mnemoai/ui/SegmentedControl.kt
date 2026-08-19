package com.kite.mnemoai.ui

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
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.kite.mnemoai.ui.databinding.ViewSegmentedControlBinding
import com.kite.mnemoai.ui.dpToPx

class SegmentedControl @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {
    val Float.dp: Float
        get() = this * Resources.getSystem().displayMetrics.density
    private val binding = ViewSegmentedControlBinding.inflate(LayoutInflater.from(context), this, true)
    private var segmentTitles: Array<String> = emptyArray()
    private var thumbBackground: Drawable
    private var listener: ((Int) -> Unit)? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SegmentedControl,
            0, 0
        ).apply {
            try{
                val titlesResId  = getResourceId(R.styleable.SegmentedControl_segmentTitle, -1)
                if(titlesResId  != -1){
                    segmentTitles = resources.getStringArray(titlesResId)
                }

                thumbBackground = ContextCompat.getDrawable(context,
                    getResourceId(R.styleable.SegmentedControl_thumbBackground, -1))?:createBackground()
            } finally {
              recycle()
            }
        }
        binding.thumb.background = thumbBackground
        init()
    }

    fun init(){
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
                }

                setOnClickListener {
                    setSelectedIndex(index)
                }
            }
            binding.container.addView(button)
        }
        post{}
    }

    fun setDefaultSelection(index:Int){
        refreshButtons(index)
        updateThumb(index, false)
    }

    fun setSelectedIndex(index: Int) {
        refreshButtons(index)
        updateThumb(index, true)
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
                val transition = ChangeBounds().apply {
                    duration = 300
                    addListener(object: Transition.TransitionListener {
                        override fun onTransitionStart(p0: Transition) {
                            refreshButtons(index)
                        }
                        override fun onTransitionEnd(p0: Transition) {
                            if (binding.root.isAttachedToWindow) {
                                TransitionManager.endTransitions(binding.root)
                            }
                            listener?.invoke(index)
                        }
                        override fun onTransitionCancel(p0: Transition) {}
                        override fun onTransitionPause(p0: Transition) {}
                        override fun onTransitionResume(p0: Transition) {}

                    })
                }
                TransitionManager.beginDelayedTransition(binding.root, transition)
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