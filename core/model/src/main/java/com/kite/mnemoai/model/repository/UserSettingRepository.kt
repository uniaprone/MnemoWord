package com.kite.mnemoai.model.repository

import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.data.ThemeType
import com.kite.mnemoai.model.data.UserSetting
import kotlinx.coroutines.flow.Flow

interface UserSettingRepository {
    val userSetting: Flow<UserSetting>

    suspend fun setNewLearningWordCount(targetCount: Int)

    suspend fun setLightDarkModel(model: ThemeType)

    suspend fun setApiKey(apiKey: String)
}
