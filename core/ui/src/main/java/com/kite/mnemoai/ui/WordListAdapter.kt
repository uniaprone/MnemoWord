package com.kite.mnemoai.ui

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.kite.mnemoai.model.word.WordItem
import com.kite.mnemoai.ui.databinding.ItemWordListBinding

class WordListAdapter(private val listener: (WordItem) -> Unit) :
    ListAdapter<WordItem, WordListAdapter.ViewHolder>(WordItemDiffCallback) {
    companion object{
        val WordItemDiffCallback = object : DiffUtil.ItemCallback<WordItem>() {
            override fun areItemsTheSame(
                p0: WordItem,
                p1: WordItem
            ): Boolean {
                return p0.id == p1.id
            }

            override fun areContentsTheSame(
                p0: WordItem,
                p1: WordItem
            ): Boolean {
                return p0 == p1
            }
        }
    }

    class ViewHolder(private val binding: ItemWordListBinding, listener: (WordItem) -> Unit) :
        RecyclerView.ViewHolder(
            binding.getRoot()
        ) {
        private lateinit var word: WordItem

        init {
            binding.wordListItemCardView.setOnClickListener { listener.invoke(word) }
        }

        fun bind(word: WordItem) {
            this.word = word
            binding.wordTextView.text = word.word
            binding.phoneticTextView.text = word.phonetic
            binding.meaningTextView.text = word.translation
            val color: Int = when (word.reviewState) {
                0 -> MaterialColors.getColor(binding.statusView, R.attr.wordAwaitingLearning)
                1 -> MaterialColors.getColor(binding.statusView, R.attr.wordReviewing)
                2 -> MaterialColors.getColor(binding.statusView, R.attr.wordMastered)
                else -> MaterialColors.getColor(binding.statusView, R.attr.wordAwaitingLearning)
            }
            binding.statusView.setBackgroundTintList(
                ColorStateList.valueOf(color)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemWordListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
