package com.kite.mnemoai.network.voice

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface YouDaoVoiceService {
    @GET("/dictvoice")
    fun getVoice(@Query("audio") word: String, @Query("type") id: Int): Call<okhttp3.ResponseBody>
}