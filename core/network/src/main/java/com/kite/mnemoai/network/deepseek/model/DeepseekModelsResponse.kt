package com.kite.mnemoai.network.deepseek.model

import com.google.gson.annotations.SerializedName

data class DeepseekModelsResponse(
    @SerializedName("object")
    val type: String,
    @SerializedName("data")
    val modelInfos: List<DeepseekModelInfo>
)