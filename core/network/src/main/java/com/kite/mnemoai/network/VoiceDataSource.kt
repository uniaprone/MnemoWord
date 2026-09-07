package com.kite.mnemoai.network

interface VoiceDataSource {
    suspend fun getVoice(word: String, id: Int): NetworkResult<ByteArray>
}