package com.kite.mnemoai.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.kite.mnemoai.R
import com.kite.mnemoai.databinding.ItemAlterWordListBinding
import com.kite.mnemoai.model.VocabularyBookChangedWord
import com.kite.mnemoai.uistate.ChangeVocabularyBookWordUIState

class AlterWordListAdapter(val listener: (word: VocabularyBookChangedWord) -> Unit):
    ListAdapter<VocabularyBookChangedWord, AlterWordListAdapter.AlterWordViewHolder>(VocabularyBookChangedWord.DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        p0: ViewGroup,
        p1: Int
    ): AlterWordViewHolder {
        val binding = ItemAlterWordListBinding.inflate(LayoutInflater.from(p0.context), p0, false)
        return AlterWordViewHolder(binding, listener)
    }

    override fun onBindViewHolder(
        p0: AlterWordViewHolder,
        p1: Int
    ) {
        p0.bind(getItem(p1))
    }

    class AlterWordViewHolder(
        private val binding: ItemAlterWordListBinding,
        private var listener: (VocabularyBookChangedWord) -> Unit
    ) :RecyclerView.ViewHolder(binding.root) {
        var word: VocabularyBookChangedWord? = null
        init {
            binding.alterWordListItemCardView.setOnClickListener {
                word?.let { listener(it) }
            }
        }
        fun bind(word: VocabularyBookChangedWord){
            this.word = word;
            binding.wordTextView.text = word.word
            binding.phoneticTextView.text = word.phonetic
            binding.meaningTextView.text = word.translation;

            val color = when(word.reviewState){
                0 -> MaterialColors.getColor(binding.statusView, R.attr.wordAwaitingLearning)
                1 -> MaterialColors.getColor(binding.statusView, R.attr.wordReviewing)
                2 -> MaterialColors.getColor(binding.statusView, R.attr.wordMastered)
                else -> MaterialColors.getColor(binding.statusView, R.attr.wordAwaitingLearning)
            }

            binding.statusView.setBackgroundTintList(
                    ColorStateList.valueOf(color)
            );
            when(word.operation){
                ChangeVocabularyBookWordUIState.ChangeType.ADD ->
                    binding.alterWordListItemCardView.setBackgroundColor( MaterialColors.getColor(binding.alterWordListItemCardView, R.attr.colorPrimary))
                ChangeVocabularyBookWordUIState.ChangeType.REMOVE ->
                    binding.alterWordListItemCardView.setBackgroundColor( MaterialColors.getColor(binding.alterWordListItemCardView, R.attr.colorTertiary))
            }

        }
    }
}