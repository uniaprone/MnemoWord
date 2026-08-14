package com.kite.mnemoai.model.word

data class Word(
    val id: Long,
    val word: String,
    val phonetic: String,
    val definition: String,
    val translation: String,
    val pos: String,
    val collins: Int?,
    val oxford: Int?,
    val tag: String,
    val bnc: Int?,
    val frq: Int?,
    val exchange: String,
    val detail: String,
    val audio: String
)
