package com.kite.mnemoai.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.textview.MaterialTextView
import com.kite.mnemoai.ui.R

class StatisticDataHView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {
    private var imageView: ImageView
    private var descriptTextView: MaterialTextView
    private var dataTextView: MaterialTextView
    private var image: Int
    private var descript: String
    private var data: String

    init {
        LayoutInflater.from(context).inflate(R.layout.view_statistic_data_h, this, true)
        imageView = findViewById(R.id.statisticDataImage)
        descriptTextView = findViewById(R.id.statisticDataDescript)
        dataTextView = findViewById(R.id.statisticDataData)

        context.theme.obtainStyledAttributes(
            attrs, R.styleable.StatisticDataHView,
            0, 0).apply {
                try{
                    image = getResourceId(R.styleable.StatisticDataHView_statisticDataHImage, 0)
                    descript = getString(R.styleable.StatisticDataHView_statisticDataHDescript)?:""
                    data = getString(R.styleable.StatisticDataHView_statisticDataHData)?:""
                }finally {
                    recycle()
                }
        }

        imageView.setImageResource(image)
        descriptTextView.text = descript
        dataTextView.text = data
    }

    fun setData(data: String){
        dataTextView.text = data
    }

    fun setImage(image: Int){
        imageView.setImageResource(image)
    }

    fun setDescript(descript: String){
        descriptTextView.text = descript
    }
}