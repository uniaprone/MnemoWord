package com.kite.mnemoai.model.repository

import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.word.WordDetail

interface AiMnemonicRepository {
    suspend fun apiKeyTest(apiKey: String): Result<String>

    suspend fun generateWordExtract(wordDetails: List<WordDetail>): Result<String>

    suspend fun chat(wordDetail: WordDetail, content: String): Result<String>

    suspend fun getModelList(): Result<List<String>>

    suspend fun getVoice(word: String, id: Int): Result<ByteArray>
}
