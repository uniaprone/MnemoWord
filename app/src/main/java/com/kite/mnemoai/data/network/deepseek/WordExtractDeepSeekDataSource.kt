package com.kite.mnemoai.data.network.deepseek

import android.util.Log
import com.google.gson.JsonSyntaxException
import com.kite.mnemoai.data.local.Converters
import com.kite.mnemoai.data.model.WordExtract
import com.kite.mnemoai.data.network.NetworkResult
import com.kite.mnemoai.data.network.RetrofitClient
import com.kite.mnemoai.data.network.WordExtractDataSource
import com.kite.mnemoai.data.network.WordExtractRequest
import com.kite.mnemoai.data.network.deepseek.model.DeepseekRequestBody

class WordExtractDeepSeekDataSource(
    val wordExtractDeepSeekService: WordExtractDeepSeekService = RetrofitClient.deepseekRetrofit.create(
        WordExtractDeepSeekService::class.java
    ),
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