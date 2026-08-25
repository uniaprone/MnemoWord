package com.kite.mnemoai.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.card.MaterialCardView
import com.kite.mnemoai.model.word.WordForm
import com.kite.mnemoai.model.word.WordFormType
import com.kite.mnemoai.ui.databinding.ItemMorphologyBinding
import com.kite.mnemoai.ui.databinding.ViewMorphologyBinding

class MorphologyView(context: Context, attrs: AttributeSet): MaterialCardView(context, attrs) {
    private val binding = ViewMorphologyBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    fun setData(wordForms: List<WordForm>){
        binding.morphologyContent.removeAllViews()
        wordForms.forEach { wordForm ->
            val itemBinding = ItemMorphologyBinding.inflate(LayoutInflater.from(context), binding.morphologyContent, false)
            WordFormType.fromCode(wordForm.typeCode)?.let { wordFormType ->
                itemBinding.morphologyName.text = wordFormType.displayName
                itemBinding.morphologyWord.text = wordForm.form
                binding.morphologyContent.addView(itemBinding.root)
            }
        }
    }
}