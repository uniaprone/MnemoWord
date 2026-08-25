package com.kite.mnemoai.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.kite.mnemoai.model.word.ExampleSentence
import com.kite.mnemoai.ui.R
import com.kite.mnemoai.ui.TextHighlighter
import com.kite.mnemoai.ui.databinding.ItemExampleSentenceBinding
import com.kite.mnemoai.ui.databinding.ViewExampleSentenceBinding

class ExampleSentenceView(context: Context, attrs: AttributeSet
) : MaterialCardView(context, attrs) {
    private val binding = ViewExampleSentenceBinding.inflate(
        LayoutInflater.from(context), this, true
    )
    private var highlightTextColor: Int = 0
    private var highlightTextRelativeSize: Float = 1.1f
    private var highlightTextStyle: Int = 0
    init {
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.ExampleSentenceView,
            0, 0)
            .apply {
            try{
                highlightTextColor = getColor(R.styleable.ExampleSentenceView_highlightTextColor,
                    MaterialColors.getColor(binding.root, androidx.appcompat.R.attr.colorPrimary))
                highlightTextRelativeSize = getFloat(R.styleable.ExampleSentenceView_highlightTextRelativeSize, 1.1f)
                highlightTextStyle = getInt(R.styleable.ExampleSentenceView_highlightTextStyle, 0)
            }finally {
                recycle()
            }
        }
    }


    fun setData(exampleSentences: List<ExampleSentence>, highlightTexts: List<String>){
        binding.exampleSentenceContent.removeAllViews()
        exampleSentences.forEach {
            val itemBinding = ItemExampleSentenceBinding.inflate(LayoutInflater.from(context), binding.exampleSentenceContent, false)
            val spannableString = TextHighlighter.highlightWordWithForms(
                it.sentence,
                highlightTexts,
                highlightTextColor,
                highlightTextStyle,
                highlightTextRelativeSize
            )

            itemBinding.exampleSentence.text = spannableString
            itemBinding.exampleSentenceTranslation.text = it.translation
            binding.exampleSentenceContent.addView(itemBinding.root)
        }
    }
}