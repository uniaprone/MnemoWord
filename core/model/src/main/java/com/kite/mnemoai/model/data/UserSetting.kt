package com.kite.mnemoai.model.data

data class UserSetting(
    val newLearningWordCount: Int,
    val lightDarkModel: ThemeType,
    val apiKey: String
)
