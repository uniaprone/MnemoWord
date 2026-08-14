package com.kite.mnemoai.network.deepseek

import com.kite.mnemoai.model.request.WordExtractRequest
import com.kite.mnemoai.network.NetworkResult
import com.kite.mnemoai.network.WordExtractDataSource
import com.kite.mnemoai.network.deepseek.model.DeepseekRequestBody
import javax.inject.Inject

class WordExtractDeepSeekDataSource @Inject constructor(
    val wordExtractDeepSeekService: WordExtractDeepSeekService,
): WordExtractDataSource {
    override fun generateWordExtract(apiKey: String, wordExtractRequest: WordExtractRequest): NetworkResult<String> {
        val deepseekRequestBody = DeepseekRequestBody(wordExtractRequest.word, wordExtractRequest.isEnableThinking)
        val deepseekResponseBody = wordExtractDeepSeekService.generateWordExtract(deepseekRequestBody,
            "Bearer $apiKey"
        )?.execute()
        deepseekResponseBody?.let {
            if(it.isSuccessful && it.body() != null){
                val deepseekResponseBody = it.body()
                val wordExtractString: String? =
                    deepseekResponseBody?.choices?.get(0)?.message?.content
                return NetworkResult.Success(wordExtractString?:"")
            }
        }
        return NetworkResult.Error(IllegalArgumentException("请检查网络或密钥是否有效!"))
    }
}