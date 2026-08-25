package com.kite.mnemoai.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.kite.mnemoai.model.word.Phrase
import com.kite.mnemoai.ui.R
import com.kite.mnemoai.ui.TextHighlighter
import com.kite.mnemoai.ui.databinding.ItemPhraseBinding
import com.kite.mnemoai.ui.databinding.ViewPhraseBinding

class PhraseView(context: Context, attrs: AttributeSet): MaterialCardView(context, attrs) {
    private val binding = ViewPhraseBinding.inflate(
        LayoutInflater.from(context), this, true
    )
    private var highlightTextRelativeSize = 1.1f
    private var highlightTextColor = 0
    private var highlightTextStyle = 1

    init {
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.PhraseView,
        0, 0
        ).apply {
            try {
                highlightTextRelativeSize = getFloat(R.styleable.PhraseView_highlightTextRelativeSize, 1.1f)
                highlightTextColor = getColor(R.styleable.PhraseView_highlightTextColor,
                    MaterialColors.getColor(binding.root, androidx.appcompat.R.attr.colorPrimary)
                )
                highlightTextStyle = getInt(R.styleable.PhraseView_highlightTextStyle, 1)
            }finally {
                recycle()
            }

        }
    }

    fun setData(phrases: List<Phrase>, highlightTexts: List<String>){
        binding.phraseContent.removeAllViews()
        phrases.forEach { phrase ->
            val itemPhraseBinding = ItemPhraseBinding.inflate(
                LayoutInflater.from(context),
                binding.phraseContent, false
            )
            val spannableString = TextHighlighter.highlightWordWithForms(
                phrase.phrase,
                highlightTexts,
                highlightTextColor,
                highlightTextStyle,
                highlightTextRelativeSize
            )
            itemPhraseBinding.phraseSentence.text = spannableString
            itemPhraseBinding.phraseTranslation.text = phrase.meaning
            binding.phraseContent.addView(itemPhraseBinding.root)
        }
    }
}