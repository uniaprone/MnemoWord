package com.kite.mnemoai.database.model

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
}
