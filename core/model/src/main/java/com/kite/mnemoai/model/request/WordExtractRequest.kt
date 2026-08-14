package com.kite.mnemoai.model.request

data class WordExtractRequest(
    val word: String,
    val isEnableThinking: Boolean
)
