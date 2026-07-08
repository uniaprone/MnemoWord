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
import androidx.core.view.updateLayoutParams
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.kite.mnemoai.R
import com.kite.mnemoai.databinding.ViewSegmentedControlBinding

class SegmentedControl @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {
    val Float.dp: Float
        get() = this * Resources.getSystem().displayMetrics.density
    private val binding = ViewSegmentedControlBinding.inflate(LayoutInflater.from(context), this)
    private var selectedIndex = 0
    private var listener: ((Int) -> Unit)? = null

    init {
        binding.thumb.background = createBackground()
    }

    private fun createBackground(): Drawable = MaterialShapeDrawable().apply {
            fillColor =
                ColorStateList.valueOf(
                    MaterialColors.getColor(
                        context,
                        com.google.android.material.R.attr.colorSurfaceContainer,
                        Color.LTGRAY
                    )
                )

            shapeAppearanceModel =
                ShapeAppearanceModel.builder()
                    .setAllCornerSizes(24f.dp)
                    .build()
    }

    fun setOnSelectionChangedListener(listener: (Int) -> Unit) {
        this.listener = listener
    }

    fun setItems(selectedIndex: Int, lastSelectedIndex: Int = 0, vararg titles: String){
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

                cornerRadius = 0

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LayoutParams.MATCH_PARENT,
                    1f
                )

                setOnClickListener {
                    setSelectedIndex(index, lastSelectedIndex)
                }
            }
            binding.container.addView(button)
        }

        this.selectedIndex = selectedIndex

        post {
            if(lastSelectedIndex == selectedIndex){
                refreshButtons()
                updateThumb(false, 0)
            }else{
                refreshButtons()
                updateThumb(true, lastSelectedIndex)
            }
        }

    }

    private fun updateThumb(animate: Boolean, lastSelectedIndex: Int) {
        val width = width / binding.container.childCount

        binding.thumb.updateLayoutParams<LayoutParams> {
            this.width = width
        }

        val start = lastSelectedIndex * width.toFloat()
        val end = selectedIndex * width.toFloat()

        binding.thumb.translationX = start

        if (animate) {
            binding.thumb.animate()
                .translationX(end)
                .setDuration(180)
                .start()
        } else {
            binding.thumb.translationX = end
        }
    }

    private fun refreshButtons() {
        for (i in 0 until binding.container.childCount) {
            val button = binding.container.getChildAt(i) as MaterialButton
            if (i == selectedIndex) {
                button.setTextColor(Color.BLACK)
            } else {
                button.setTextColor(Color.GRAY)
            }
        }
    }

    fun setSelectedIndex(index: Int, lastSelectedIndex: Int) {

        if (selectedIndex == index)
            return

        selectedIndex = index

        updateThumb(true, lastSelectedIndex)

        refreshButtons()

        listener?.invoke(index)
    }


}