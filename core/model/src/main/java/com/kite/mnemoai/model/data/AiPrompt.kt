package com.kite.mnemoai.model.data

enum class AiPromptType {
    WORD_EXTRACT, CHAT
}

data class AiPromptSet(
    val systemPrompt: String,
    val userPrompt: String
)
