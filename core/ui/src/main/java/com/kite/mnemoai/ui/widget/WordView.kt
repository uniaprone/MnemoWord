package com.kite.mnemoai.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.textview.MaterialTextView
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.word.WordDetail
import com.kite.mnemoai.ui.R
import com.kite.mnemoai.ui.databinding.ItemWordTranslationBinding
import com.kite.mnemoai.ui.databinding.ViewWordBinding
import com.kite.mnemoai.ui.dpToPx

class WordView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): MaterialCardView(context, attrs, defStyleAttr) {
    private val binding = ViewWordBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    private val bannerView = BannerView(this.context)

    private var generateAIMnemonicListener: (() -> Unit)? = null
    private var showReciteDetailListener: (() -> Unit)? = null
    private var communicateWithAIListener: (() -> Unit)? = null
    private var speechListener:(() -> Unit)? = null

    fun setGenerateAIMnemonicListener(block: () -> Unit){
        generateAIMnemonicListener = block
        binding.generateAIMnemonicChip.setOnClickListener { generateAIMnemonicListener?.invoke() }
    }

    fun setShowReciteDetailListener(block: () -> Unit){
        showReciteDetailListener = block
        binding.showWordDefinitionLL.setOnClickListener { showReciteDetailListener?.invoke() }
    }

    fun setCommunicateWithAIListener(block: () -> Unit){
        communicateWithAIListener = block
        binding.communicateWithAIChip.setOnClickListener { communicateWithAIListener?.invoke() }
    }

    fun setSpeechListener(block: () -> Unit){
        speechListener = block
        binding.headerLayout.setOnClickListener { speechListener?.invoke() }
    }

    fun setReciteStyle(wordDetail: WordDetail){
        binding.wordTV.text = wordDetail.word.word
        binding.phoneticTV.text = wordDetail.word.phonetic
        binding.wordTranslationLL.visibility = GONE
        binding.showWordDefinitionLL.visibility = VISIBLE
    }

    fun setReciteDetailStyle(wordDetail: WordDetail){
        binding.wordTV.text = wordDetail.word.word
        binding.phoneticTV.text = wordDetail.word.phonetic

        binding.wordTranslationLL.removeAllViews()
        wordDetail.translations.forEach {
            val itemBinding = ItemWordTranslationBinding.inflate(
                LayoutInflater.from(context),
                binding.wordTranslationLL,
                false
            )

            itemBinding.wordPosTV.text = it.pos.pos
            it.meanings.forEach { (_, _, meaning) ->
                val wordMeaningTV = MaterialTextView(context)
                wordMeaningTV.text = meaning
                wordMeaningTV.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(com.kite.mnemoai.ui.R.dimen.text_size_body_large)
                )
                wordMeaningTV.setTextColor(
                    MaterialColors.getColor(
                        wordMeaningTV,
                        com.google.android.material.R.attr.colorOnSurface
                    )
                )
                wordMeaningTV.setPadding(
                    context.dpToPx(4),
                    0,
                    context.dpToPx(4),
                    0
                )
                itemBinding.wordMeaningsFlow.addView(wordMeaningTV)
            }
            binding.wordTranslationLL.addView(itemBinding.root)
        }

        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.headerContainerCL)
        constraintSet.clear(R.id.headerLayout, ConstraintSet.END)
        constraintSet.applyTo(binding.headerContainerCL)

        binding.headerLayout.gravity = Gravity.START
        binding.phoneticTV.gravity = Gravity.START

        binding.wordTranslationLL.visibility = VISIBLE
        binding.showWordDefinitionLL.visibility = GONE
    }

    fun showBanner(aiMnemonicLoadingState: Result<String>?) {
        when (aiMnemonicLoadingState) {
            null -> {
                (bannerView.parent as? ViewGroup)?.removeView(bannerView)
            }
            is Result.Loading -> {
                (bannerView.parent as? ViewGroup)?.removeView(bannerView)
                binding.root.addView(bannerView)
                bannerView.setLoading(resources.getString(R.string.ai_thinking))
            }

            is Result.Success<*> -> {
                (bannerView.parent as? ViewGroup)?.removeView(bannerView)
            }

            is Result.Error -> {
                (bannerView.parent as? ViewGroup)?.removeView(bannerView)
                aiMnemonicLoadingState.exception.message?.let {
                    bannerView.setLoading(it)
                    binding.root.addView(bannerView)
                }
            }
        }
    }
}