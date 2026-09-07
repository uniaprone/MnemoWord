package com.kite.mnemoai.network.deepseek.model

import com.google.gson.annotations.SerializedName

data class DeepseekModelInfo(
    val id: String,
    @SerializedName("object")
    val type: String,
    @SerializedName("owned_by")
    val ownedBy: String,
)