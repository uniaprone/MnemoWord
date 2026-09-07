package com.kite.mnemoai.network.deepseek.model

import com.google.gson.annotations.SerializedName

data class ResponseFormatConfig(
    val type: ResponseFormat
)

enum class ResponseFormat {
    @SerializedName("text")
    TEXT,
    @SerializedName("json_object")
    JSON_OBJECT
}
