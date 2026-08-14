package com.kite.mnemoai.model.group

data class AllVocabularyBookItem(
    val id: Long,
    val name: String,
    val totalWords: Int,
    val masteredWords: Int
) {
    val masteredProgress: Int
        get() = if (totalWords == 0) 0 else (masteredWords * 100) / totalWords
}

data class LearningVocabularyBookItem(
    val id: Long,
    val name: String,
    val desc: String,
    val totalWords: Int,
    val learningWords: Int,
    val reviewingWords: Int,
    val masteredWords: Int
) {
    val masteredProgress: Float
        get() = if (totalWords == 0) 1f else masteredWords / totalWords.toFloat()

    val learningProgress: Float
        get() = if (totalWords == 0) 0f else learningWords / totalWords.toFloat()
}
