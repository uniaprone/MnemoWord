package com.kite.mnemoai.model.word

data class WordItem(
    val id: Long,
    val word: String,
    val phonetic: String,
    val translation: String,
    val reviewState: Int
)
