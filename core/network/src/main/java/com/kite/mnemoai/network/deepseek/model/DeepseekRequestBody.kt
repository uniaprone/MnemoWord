package com.kite.mnemoai.network.deepseek.model

import com.google.gson.annotations.SerializedName

class DeepseekRequestBody(
    private val messages: MutableList<Message?>?,
    private val model: String?,
    @field:SerializedName(
        "response_format"
    ) private val responseFormat: ResponseFormat?,
    private val thinking: Thinking?,
    @field:SerializedName(
        "reasoning_effort"
    ) private val reasoningEffort: ReasoningEffort?
)
