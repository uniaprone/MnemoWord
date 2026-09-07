package com.kite.mnemoai.network.deepseek.model

import com.google.gson.annotations.SerializedName

enum class ReasoningEffort {
    @SerializedName("low")
    LOW,
    @SerializedName("high")
    HIGH,
    @SerializedName("max")
    MAX
}
