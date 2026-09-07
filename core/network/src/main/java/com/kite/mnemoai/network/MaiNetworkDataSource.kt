package com.kite.mnemoai.network

import com.kite.mnemoai.network.deepseek.model.DeepseekModelsResponse

interface MaiNetworkDataSource {
    fun getChatResult(request: AiChatRequest): NetworkResult<String>

    fun getModelList(apiKey: String): NetworkResult<DeepseekModelsResponse?>
}
