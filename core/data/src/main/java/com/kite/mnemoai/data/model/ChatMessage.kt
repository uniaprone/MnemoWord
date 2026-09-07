package com.kite.mnemoai.data.model

import com.kite.mnemoai.database.model.ChatMessageEntity
import com.kite.mnemoai.model.chat.ChatMessage
import com.kite.mnemoai.model.chat.ChatRole
import com.kite.mnemoai.network.deepseek.model.Message
import com.kite.mnemoai.network.deepseek.model.Role

fun ChatMessage.asEntity() = ChatMessageEntity(
    id = id?:0,
    wordId = wordId,
    role = role.storageValue,
    content = content,
    timestamp = timestamp
)

fun ChatMessage.asNetworkEntity() = Message(
    content = content,
    role = role.toNetworkRole()
)

fun ChatRole.toNetworkRole(): Role = when (this) {
    ChatRole.SYSTEM -> Role.SYSTEM
    ChatRole.USER -> Role.USER
    ChatRole.ASSISTANT -> Role.ASSISTANT
}
