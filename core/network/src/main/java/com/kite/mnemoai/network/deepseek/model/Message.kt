package com.kite.mnemoai.network.deepseek.model

import com.google.gson.annotations.SerializedName

data class Message(
    val content: String,
    val role: Role,
    val name: String? = null
)

enum class Role {
    @SerializedName("system")
    SYSTEM,
    @SerializedName("user")
    USER,
    @SerializedName("assistant")
    ASSISTANT
}
