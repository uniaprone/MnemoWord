package com.kite.mnemoai.vocabularybook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kite.mnemoai.model.group.AllVocabularyBookItem
import com.kite.mnemoai.vocabularybook.databinding.ItemVocabularyBookAllBinding

class AllVocabularyBookAdapter(val listener: (View, Long) -> Unit): ListAdapter<AllVocabularyBookItem, AllVocabularyBookAdapter.ViewHolder>(
    AllVocabularyBookItemDiffCallback
) {
    override fun onCreateViewHolder(
        p0: ViewGroup,
        p1: Int
    ): ViewHolder {
        val binding = ItemVocabularyBookAllBinding.inflate(LayoutInflater.from(p0.context), p0, false)
        return ViewHolder(binding, listener)
    }

    override fun onBindViewHolder(
        p0: ViewHolder,
        p1: Int
    ) {
        p0.bind(getItem(p1))
    }

    class ViewHolder(val binding: ItemVocabularyBookAllBinding, val listener: (View, Long) -> Unit): RecyclerView.ViewHolder(binding.root){
        private var item: AllVocabularyBookItem? = null
        init {
            binding.itemVocabularyBookAll.setOnClickListener { v ->
                item?.let { listener(v, it.id) }
            }
        }
        fun bind(item: AllVocabularyBookItem){
            this.item = item
            binding.groupNameTV.text = item.name
            binding.totalWordsTV.text = binding.itemVocabularyBookAll.context.resources.getString(R.string.bracket_number, item.totalWords)
        }
    }
}