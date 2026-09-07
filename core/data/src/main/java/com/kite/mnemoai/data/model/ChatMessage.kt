package com.kite.mnemoai.data.mapper

import com.kite.mnemoai.database.model.ChatMessageEntity
import com.kite.mnemoai.model.chat.ChatMessage

fun ChatMessage.asEntity() = ChatMessageEntity(
    id = id,
    wordId = wordId,
    role = role,
    content = content,
    timestamp = timestamp
)