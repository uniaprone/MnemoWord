package com.kite.mnemoai.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kite.mnemoai.database.model.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(chatMessageEntity: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessages(chatMessageEntities: List<ChatMessageEntity>)

    @Query("SELECT * FROM chat_messages WHERE word_id = :wordId")
    fun getChatMessagesByWordId(wordId: Long): Flow<List<ChatMessageEntity>>
}