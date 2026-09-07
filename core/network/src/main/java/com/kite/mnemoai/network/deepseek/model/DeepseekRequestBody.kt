package com.kite.mnemoai.network.deepseek.model

import com.google.gson.annotations.SerializedName

data class DeepseekRequestBody(
    val messages: MutableList<Message>,
    val model: String,
    @field:SerializedName("thinking")
    val thinking: Thinking?,
    @field:SerializedName("reasoning_effort")
    val reasoningEffort: ReasoningEffort?,
    @field:SerializedName("response_format")
    val responseFormat: ResponseFormatConfig?,
)
