package com.kite.mnemoai.network.deepseek

import com.kite.mnemoai.network.deepseek.model.DeepseekRequestBody
import com.kite.mnemoai.network.deepseek.model.Message
import com.kite.mnemoai.network.deepseek.model.ReasoningEffort
import com.kite.mnemoai.network.deepseek.model.ResponseFormat
import com.kite.mnemoai.network.deepseek.model.ResponseFormatConfig
import com.kite.mnemoai.network.deepseek.model.Thinking
import com.kite.mnemoai.network.deepseek.model.Type
import javax.inject.Inject

class DeepseekRequestFactory @Inject constructor() {
    fun create(
        messages: List<Message>,
        enableThinking: Boolean,
        modelType: String,
        responseFormat: ResponseFormat
    ): DeepseekRequestBody =
        DeepseekRequestBody(
            messages = messages.toMutableList(),
            model = modelType,
            thinking = Thinking(if (enableThinking) Type.ENABLED else Type.DISABLED),
            reasoningEffort = ReasoningEffort.HIGH,
            responseFormat = ResponseFormatConfig(responseFormat)
        )
}
