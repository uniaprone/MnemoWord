package com.kite.mnemoai.data.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    fun createOkHttpClient(): OkHttpClient{
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    fun createRetrofit(url: String): Retrofit{
        return Retrofit.Builder()
            .baseUrl(url)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val deepseekRetrofit: Retrofit = createRetrofit("https://api.deepseek.com/")

    val openAiRetrofit: Retrofit = createRetrofit("https://api.openai.com/")
}