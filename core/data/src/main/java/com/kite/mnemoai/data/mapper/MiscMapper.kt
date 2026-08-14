package com.kite.mnemoai.data.mapper

import com.kite.mnemoai.model.data.UserSetting

fun UserSetting.asMap(): Map<String, Any> = mapOf(
    "study_word_count" to newLearningWordCount,
    "light_dark_model" to lightDarkModel,
    "api_key" to apiKey
)

fun Map<String, Any>.asUserSetting(): UserSetting = UserSetting(
    newLearningWordCount = this["study_word_count"] as? Int ?: 20,
    lightDarkModel = this["light_dark_model"] as? Int ?: 0,
    apiKey = this["api_key"] as? String ?: ""
)
