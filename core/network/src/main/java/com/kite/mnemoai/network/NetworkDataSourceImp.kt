package com.kite.mnemoai.network

import com.kite.mnemoai.network.deepseek.DeepseekRequestFactory
import com.kite.mnemoai.network.deepseek.DeepseekService
import com.kite.mnemoai.network.deepseek.model.DeepseekModelsResponse
import javax.inject.Inject

class NetworkDataSourceImp @Inject constructor(
    private val deepseekService: DeepseekService,
    private val requestFactory: DeepseekRequestFactory,
) : MaiNetworkDataSource {
    override fun getChatResult(request: AiChatRequest): NetworkResult<String> {
        val body = requestFactory.create(
            request.messages,
            request.enableThinking,
            request.modelType,
            request.responseFormat
        )
        val response = deepseekService.chat(body, "Bearer ${request.apiKey}")?.execute()
        response?.let {
            if (it.isSuccessful && it.body() != null) {
                val content = it.body()?.choices?.getOrNull(0)?.message?.content
                return NetworkResult.Success(content ?: "")
            }
        }
        return NetworkResult.Error(IllegalArgumentException("请检查网络或密钥是否有效!"))
    }

    override fun getModelList(apiKey: String): NetworkResult<DeepseekModelsResponse?> {
        val response = deepseekService.getModelList("Bearer $apiKey").execute()
        if(response.isSuccessful && response.body() != null){
            return NetworkResult.Success(response.body())
        }
        return NetworkResult.Error(IllegalArgumentException("请检查网络或密钥是否有效!"))
    }

}
