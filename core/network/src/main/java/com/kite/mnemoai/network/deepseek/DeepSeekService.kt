package com.kite.mnemoai.network.deepseek

import com.kite.mnemoai.network.deepseek.model.DeepseekModelsResponse
import com.kite.mnemoai.network.deepseek.model.DeepseekRequestBody
import com.kite.mnemoai.network.deepseek.model.DeepseekResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface DeepseekService {
    @POST("chat/completions")
    fun chat(
        @Body body: DeepseekRequestBody?,
        @Header("Authorization") authorization: String?
    ): Call<DeepseekResponseBody?>?

    @GET("models")
    fun getModelList(
        @Header("Authorization") authorization: String?
    ): Call<DeepseekModelsResponse>
}
