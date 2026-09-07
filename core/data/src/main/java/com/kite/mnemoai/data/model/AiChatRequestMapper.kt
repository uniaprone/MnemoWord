package com.kite.mnemoai.data.model

import com.kite.mnemoai.model.chat.ChatMessage
import com.kite.mnemoai.model.data.UserSetting
import com.kite.mnemoai.network.AiChatRequest
import com.kite.mnemoai.network.deepseek.model.Message
import com.kite.mnemoai.network.deepseek.model.ResponseFormat
import com.kite.mnemoai.network.deepseek.model.Role

fun UserSetting.toAiChatRequest(
    userMessage: List<ChatMessage>,
    systemPrompt: String,
    apiKey: String = deepseekSettings.apiKey,
    responseFormat: ResponseFormat = ResponseFormat.JSON_OBJECT
): AiChatRequest = AiChatRequest(
    aiServiceType = currentAiProvider.asNetWorkModel(),
    apiKey = apiKey,
    messages = buildList {
        add(Message(systemPrompt, Role.SYSTEM))
        addAll(userMessage.map { it.asNetworkEntity() })
    },
    enableThinking = deepseekSettings.enableThinking,
    modelType = deepseekSettings.modelType,
    responseFormat = responseFormat
)
