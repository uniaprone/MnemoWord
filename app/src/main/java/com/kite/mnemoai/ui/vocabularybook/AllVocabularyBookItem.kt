package com.kite.mnemoai.ui.vocabularybook

import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo

data class AllVocabularyBookItem(
    val id: Long,
    val name: String,
    @ColumnInfo(name = "total_words")
    val totalWords: Int,
    @ColumnInfo(name = "mastered_words")
    val masteredWords: Int
) {
    val masteredProgress: Int
        get() = if (totalWords == 0) 0 else (masteredWords * 100) / totalWords

    companion object{
        val DIFF_CALLBACK = object :DiffUtil.ItemCallback<AllVocabularyBookItem>(){
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
    }
}