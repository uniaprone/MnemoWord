package com.kite.mnemoai.network

import com.kite.mnemoai.model.request.WordExtractRequest

interface WordExtractDataSource {
    fun generateWordExtract(apiKey: String, wordExtractRequest: WordExtractRequest): NetworkResult<String>
}