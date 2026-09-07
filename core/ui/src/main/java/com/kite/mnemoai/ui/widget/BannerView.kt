package com.kite.mnemoai.ui.widget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.google.android.material.color.MaterialColors
import com.google.android.material.textview.MaterialTextView
import com.kite.mnemoai.ui.R

class BannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {
    private var bannerTV: MaterialTextView
    private var hideRunnable: Runnable? = null

    private val loadingBgColor = MaterialColors.getColor(
        this,
        com.google.android.material.R.attr.colorPrimaryContainer
    )

    private val loadingTextColor = MaterialColors.getColor(
        this,
        com.google.android.material.R.attr.colorOnPrimaryContainer
    )

    private val successBgColor = MaterialColors.getColor(
        this,
        com.google.android.material.R.attr.colorPrimaryContainer
    )

    private val successTextColor = MaterialColors.getColor(
        this,
        com.google.android.material.R.attr.colorOnPrimaryContainer
    )

    private val failureBgColor = MaterialColors.getColor(
        this,
        com.google.android.material.R.attr.colorErrorContainer
    )

    private val failureTextColor = MaterialColors.getColor(
        this,
        com.google.android.material.R.attr.colorOnErrorContainer
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.view_banner, this, true)
        bannerTV = findViewById(R.id.bannerTV)
        background = ContextCompat.getDrawable(context, R.drawable.bg_banner)
        backgroundTintList = ColorStateList.valueOf(loadingBgColor)
    }

    fun setLoading(message: String?){
        hideRunnable?.let { removeCallbacks(it) }
        bannerTV.text = message
        bannerTV.setTextColor(loadingTextColor)
        backgroundTintList = ColorStateList.valueOf(loadingBgColor)
        visibility = VISIBLE
    }

    fun setSuccess(message: String?, delay: Long = 1000){
        hideRunnable?.let { removeCallbacks(it) }
        bannerTV.text = message
        bannerTV.setTextColor(successTextColor)
        backgroundTintList = ColorStateList.valueOf(successBgColor)
        visibility = VISIBLE
        hideRunnable = Runnable{
            hideBanner()
        }
        postDelayed(hideRunnable, delay)
    }

    fun setFailure(message: String?, delay: Long = 3000){
        hideRunnable?.let { removeCallbacks(it) }
        bannerTV.text = message
        bannerTV.setTextColor(failureTextColor)
        backgroundTintList = ColorStateList.valueOf(failureBgColor)
        visibility = VISIBLE
        hideRunnable = Runnable{
            hideBanner()
        }
        postDelayed(hideRunnable, delay)
    }

    fun hideBanner(){
        visibility = GONE
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        hideRunnable?.let { removeCallbacks(it) }
        hideRunnable = null
    }
}