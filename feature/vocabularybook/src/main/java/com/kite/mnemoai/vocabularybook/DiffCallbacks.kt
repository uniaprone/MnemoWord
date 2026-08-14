package com.kite.mnemoai.vocabularybook

import androidx.recyclerview.widget.DiffUtil
import com.kite.mnemoai.model.group.AllVocabularyBookItem
import com.kite.mnemoai.model.group.LearningVocabularyBookItem

val AllVocabularyBookItemDiffCallback = object : DiffUtil.ItemCallback<AllVocabularyBookItem>() {
    override fun areItemsTheSame(
        p0: AllVocabularyBookItem,
        p1: AllVocabularyBookItem
    ): Boolean {
        return p0.id == p1.id
    }

    override fun areContentsTheSame(
        p0: AllVocabularyBookItem,
        p1: AllVocabularyBookItem
    ): Boolean {
        return p0 == p1
    }
}

val LearningVocabularyBookItemDiffCallback = object : DiffUtil.ItemCallback<LearningVocabularyBookItem>() {
    override fun areItemsTheSame(
        p0: LearningVocabularyBookItem,
        p1: LearningVocabularyBookItem
    ): Boolean {
        return p0.id == p1.id
    }

    override fun areContentsTheSame(
        p0: LearningVocabularyBookItem,
        p1: LearningVocabularyBookItem
    ): Boolean {
        return p0 == p1
    }
}
