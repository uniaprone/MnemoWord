package com.kite.mnemoai.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.card.MaterialCardView
import com.kite.mnemoai.ui.databinding.ViewExplainBinding

class ExplainView(context: Context, attrs: AttributeSet): MaterialCardView(context, attrs) {
    private val binding = ViewExplainBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    fun setData(explain: String){
        binding.explainContent.text = explain
    }
}