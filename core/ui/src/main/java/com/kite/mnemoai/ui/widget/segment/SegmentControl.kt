package com.kite.mnemoai.ui.widget.segment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.updateLayoutParams
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.kite.mnemoai.ui.R
import com.kite.mnemoai.ui.databinding.ViewSegmentedControlBinding
import com.kite.mnemoai.ui.dpToPx
import kotlin.math.abs

open class SegmentControl @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr){
    val Float.dp: Float
        get() = this * Resources.getSystem().displayMetrics.density
    val binding = ViewSegmentedControlBinding.inflate(LayoutInflater.from(context), this, true)
    protected var segmentTitles: List<String> = mutableListOf()
    private var thumbBackground: Drawable
    private var segmentContainerBg: ColorStateList? = null
    protected var segments: MutableList<MaterialButton> = mutableListOf()
    private var listener: ((Int) -> Unit)? = null
    var currentIndex = -1
    private var thumbAnimator: ValueAnimator? = null

    // 手指滑动相关状态
    private val touchSlop: Int = (ViewConfiguration.get(context).scaledTouchSlop * 0).toInt()
    private var downX = 0f
    private var downY = 0f
    private var horizontalDragStarted = false

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

        segmentContainerBg?.let { binding.segmentContainerFL.backgroundTintList = it }
        binding.thumb.background = thumbBackground
        if(!segmentTitles.isEmpty()) init()
    }

    /**
     * 手指滑动手势：
     * - 只在「水平位移超过 touchSlop 且明显大于垂直位移」时才接管事件，
     *   垂直方向滑动交给父容器（如 NestedScrollView）正常滚动；
     * - 接管后子按钮会收到 ACTION_CANCEL，不会误触发点击；
     * - 抬手时按位移方向切换选中项：左滑下一个、右滑上一个。
     */
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                horizontalDragStarted = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (!horizontalDragStarted) {
                    val dx = event.x - downX
                    val dy = event.y - downY
                    if (abs(dx) > touchSlop) {
                        horizontalDragStarted = true
                        return true
                    }
                }
            }
            else -> {}
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                if (horizontalDragStarted && segmentTitles.isNotEmpty()) {
                    val dx = event.x - downX
                    val threshold = touchSlop * 1
                    if (dx < -threshold) {
                        setSelectedIndex((currentIndex - 1).coerceIn(0, segmentTitles.lastIndex))
                    } else if (dx > threshold) {
                        setSelectedIndex((currentIndex + 1).coerceIn(0, segmentTitles.lastIndex))
                    }
                }
                horizontalDragStarted = false
            }
            MotionEvent.ACTION_CANCEL -> horizontalDragStarted = false
            else -> {}
        }
        return true
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
                thumbAnimator?.let { it.removeAllListeners(); it.cancel() }
                val fromMargin = binding.thumb.marginStart.toFloat()
                val toMargin = startMargin.toFloat()
                val fromWidth = binding.thumb.width.toFloat()
                val toWidth = thumbWidth.toFloat()
                val animator = ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 300
                    addUpdateListener { anim ->
                        val t = anim.animatedValue as Float
                        binding.thumb.updateLayoutParams<LayoutParams> {
                            width = (fromWidth + (toWidth - fromWidth) * t).toInt()
                            height = binding.container.height
                            marginStart = (fromMargin + (toMargin - fromMargin) * t).toInt()
                            topMargin = context.dpToPx(4)
                        }
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            if (thumbAnimator === animation) {
                                thumbAnimator = null
                                listener?.invoke(index)
                            }
                        }
                    })
                    start()
                }
                thumbAnimator = animator
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
