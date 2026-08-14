package com.kite.mnemoai.model.repository

import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.request.WordExtractRequest
import com.kite.mnemoai.model.word.WordDetail

interface AiMnemonicRepository {
    suspend fun apiKeyTest(apiKey: String, request: WordExtractRequest): Result<String>

    suspend fun generateWordExtract(wordDetail: WordDetail, request: WordExtractRequest): Result<String>
}
