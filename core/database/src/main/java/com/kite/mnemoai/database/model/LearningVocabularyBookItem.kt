package com.kite.mnemoai.database.model

import androidx.room.ColumnInfo

data class LearningVocabularyBookItem(
    val id: Long,
    val name: String,
    @ColumnInfo(name = "description") val desc: String,
    @ColumnInfo(name = "total_words") val totalWords: Int,
    @ColumnInfo(name = "learning_words") val learningWords: Int,
    @ColumnInfo(name = "reviewing_words") val reviewingWords: Int,
    @ColumnInfo(name = "mastered_words") val masteredWords: Int
) {
    val masteredProgress: Float
        get() = if (totalWords == 0) 1f else masteredWords / totalWords.toFloat()

    val learningProgress: Float
        get() = if (totalWords == 0) 0f else learningWords / totalWords.toFloat()
}
