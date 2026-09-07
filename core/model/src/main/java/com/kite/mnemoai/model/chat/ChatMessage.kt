package com.kite.mnemoai.model.chat

data class ChatMessage(
    val id: Long? = 0,
    val wordId: Long,
    val role: ChatRole,
    val content: String,
    val timestamp: Long
)
