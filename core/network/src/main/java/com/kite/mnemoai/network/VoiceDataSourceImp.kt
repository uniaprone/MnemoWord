package com.kite.mnemoai.network

import com.kite.mnemoai.network.voice.YouDaoVoiceService

class VoiceDataSourceImp(
    private val service: YouDaoVoiceService
): VoiceDataSource {
    override suspend fun getVoice(word: String, id: Int): NetworkResult<ByteArray> {
        val response = service.getVoice(word, id).execute()
        val body = response.body()
        return if (response.isSuccessful && body != null) {
            NetworkResult.Success(body.bytes())
        }else{
            NetworkResult.Error(Exception("有道网络声源获取失败"))
        }
    }
}