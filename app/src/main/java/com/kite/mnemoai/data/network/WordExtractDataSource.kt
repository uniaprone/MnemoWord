package com.kite.mnemoai.data.network

import com.kite.mnemoai.data.model.WordExtract

interface WordExtractDataSource {
    fun generateWordExtract(apiKey: String, wordExtractRequest: WordExtractRequest): NetworkResult<String>
}