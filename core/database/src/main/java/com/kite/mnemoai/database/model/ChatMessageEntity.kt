package com.kite.mnemoai.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kite.mnemoai.model.chat.ChatMessage
import com.kite.mnemoai.model.chat.ChatRole

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = "word_id")
    val wordId: Long,
    val role: String,
    val content: String,
    val timestamp: Long
)

fun ChatMessageEntity.asExternalModel() = ChatMessage(
    id = id,
    wordId = wordId,
    role = ChatRole.fromStorage(role),
    content = content,
    timestamp = timestamp
)
