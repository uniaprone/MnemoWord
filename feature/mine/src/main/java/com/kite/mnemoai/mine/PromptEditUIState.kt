package com.kite.mnemoai.mine

data class PromptEditUIState(
    val wordExtractSystemPrompt: String,
    val wordExtractUserPrompt: String,
    val chatSystemPrompt: String,
    val chatUserPrompt: String
)
