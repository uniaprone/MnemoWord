package com.kite.mnemoai.network

import com.kite.mnemoai.network.deepseek.model.Message
import com.kite.mnemoai.network.deepseek.model.ResponseFormat

data class AiChatRequest(
    val aiServiceType: AiServiceType,
    val apiKey: String,
    val messages: List<Message>,
    val enableThinking: Boolean,
    val modelType: String,
    val responseFormat: ResponseFormat
)
