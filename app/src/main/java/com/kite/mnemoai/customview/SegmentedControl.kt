package com.kite.mnemoai.customview

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.contains
import androidx.core.view.marginEnd
import androidx.core.view.marginLeft
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import androidx.transition.TransitionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.kite.mnemoai.R
import com.kite.mnemoai.databinding.ViewSegmentedControlBinding
import com.kite.mnemoai.utils.dpToPx

class SegmentedControl @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {
    val Float.dp: Float
        get() = this * Resources.getSystem().displayMetrics.density
    private val binding = ViewSegmentedControlBinding.inflate(LayoutInflater.from(context), this, true)
    private var selectedIndex = -1
    private var listener: ((Int) -> Unit)? = null

    init {
        binding.thumb.background = createBackground()
    }

    private fun createBackground(): Drawable = MaterialShapeDrawable().apply {
            fillColor =
                ColorStateList.valueOf(
                    MaterialColors.getColor(
                        context,
                        R.attr.colorPrimary,
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

    fun setItems(selectedIndex: Int = -1, vararg titles: String){
        this.selectedIndex = selectedIndex
        binding.container.removeAllViews()
        titles.forEachIndexed { index, title ->
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
        refreshButtons()
        val actualHeight = binding.container.height // 或者 binding.container.measuredHeight

        if (actualHeight > 0) {
            binding.thumb.updateLayoutParams<FrameLayout.LayoutParams> {
                height = actualHeight
            }
            // 如果不在动画中，需手动触发布局
            binding.thumb.requestLayout()
        }

        post {
            if (selectedIndex != -1){

                updateThumb(false)
            }
        }
        requestLayout()
    }

    private fun updateThumb(animate: Boolean) {
        var startMargin = context.dpToPx(4)
        for (i in 0 until selectedIndex) {
            startMargin += binding.container.getChildAt(i).width +
                    binding.container.getChildAt(selectedIndex).marginStart +
                    binding.container.getChildAt(selectedIndex).marginEnd
        }
        val thumbWidth = binding.container.getChildAt(selectedIndex).width +
                binding.container.getChildAt(selectedIndex).marginStart +
                binding.container.getChildAt(selectedIndex).marginEnd

        if (animate) {
            TransitionManager.endTransitions(binding.root)
            val transition = ChangeBounds().apply {
                duration = 300
                addListener(object: Transition.TransitionListener {
                    override fun onTransitionStart(p0: Transition) {
                        refreshButtons()
                    }
                    override fun onTransitionEnd(p0: Transition) {
                        if(selectedIndex == -1) return
                         listener?.invoke(selectedIndex)
                    }
                    override fun onTransitionCancel(p0: Transition) {}
                    override fun onTransitionPause(p0: Transition) {}
                    override fun onTransitionResume(p0: Transition) {}

                })
            }
            TransitionManager.beginDelayedTransition(binding.root, transition)
        }

        binding.thumb.updateLayoutParams<FrameLayout.LayoutParams> {
            width = thumbWidth
            height = binding.container.height
            marginStart = startMargin
            topMargin = context.dpToPx(4)
        }

        if (!animate) {
            binding.thumb.requestLayout()
            if(selectedIndex == -1) return
            listener?.invoke(selectedIndex)
        }
    }

    private fun refreshButtons() {
        for (i in 0 until binding.container.childCount) {
            val button = binding.container.getChildAt(i) as MaterialButton
            if (i == selectedIndex) {
                button.setTextColor(MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorOnPrimary))
            } else {
                button.setTextColor(MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorOnSurface))
            }
        }
    }

    fun setSelectedIndex(index: Int) {
        selectedIndex = index
        if(selectedIndex == -1) return

        updateThumb(true)
    }

}