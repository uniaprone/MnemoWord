package com.kite.mnemoai.data.network.deepseek

import com.kite.mnemoai.data.network.deepseek.model.DeepseekRequestBody
import com.kite.mnemoai.data.network.deepseek.model.DeepseekResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface WordExtractDeepSeekService {
    @POST("chat/completions")
    fun generateWordExtract(
        @Body body: DeepseekRequestBody?,
        @Header("Authorization") authorization: String?
    ): Call<DeepseekResponseBody?>?
}