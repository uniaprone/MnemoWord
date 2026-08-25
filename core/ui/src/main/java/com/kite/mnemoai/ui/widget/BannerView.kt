package com.kite.mnemoai.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.google.android.material.color.MaterialColors
import com.google.android.material.textview.MaterialTextView
import com.kite.mnemoai.ui.R

class BannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {
    private var bannerTV: MaterialTextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_banner, this, true)
        bannerTV = findViewById(R.id.bannerTV)
        setBackgroundColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimaryContainer))
    }

    fun setBanner(message: String){
        bannerTV.text = message
    }
}