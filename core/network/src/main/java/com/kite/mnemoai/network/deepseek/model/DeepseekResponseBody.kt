package com.kite.mnemoai.network.deepseek.model

data class DeepseekResponseBody(
    val choices: List<Choice>? = null
)

data class Choice(
    val message: ResponseMessage? = null
)

data class ResponseMessage(
    val content: String? = null
)
