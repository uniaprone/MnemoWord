package com.kite.mnemoai.ui.changevocabularybookword

import androidx.recyclerview.widget.DiffUtil

data class VocabularyBookChangedWord(
    val id: Long,
    val word: String,
    val phonetic: String?,
    val translation: String?,
    val reviewState: Int,
    val operation: ChangeVocabularyBookWordUIState.ChangeType){

    companion object{
         val DIFF_CALLBACK: DiffUtil.ItemCallback<VocabularyBookChangedWord> = object: DiffUtil.ItemCallback<VocabularyBookChangedWord>() {
             override fun areItemsTheSame(
                 p0: VocabularyBookChangedWord,
                 p1: VocabularyBookChangedWord
             ): Boolean {
                 return p0.id == p1.id
             }

             override fun areContentsTheSame(
                 p0: VocabularyBookChangedWord,
                 p1: VocabularyBookChangedWord
             ): Boolean {
                 return p0 == p1
             }
         }
    }
}