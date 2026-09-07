package com.kite.mnemoai.network

import com.kite.mnemoai.model.data.DeepseekModelType

interface MaiNetWordDataSource {
    fun getWordExtracts(
        apiKey: String,
        words: List<String>,
        systemPrompt: String,
        enableThinking: Boolean,
        modelType: DeepseekModelType
    ): NetworkResult<String>
}
