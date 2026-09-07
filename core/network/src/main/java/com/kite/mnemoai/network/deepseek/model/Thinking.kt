package com.kite.mnemoai.network.deepseek.model

import com.google.gson.annotations.SerializedName

data class Thinking(
    val type: Type
)

enum class Type {
    @SerializedName("enabled")
    ENABLED,
    @SerializedName("disabled")
    DISABLED
}
