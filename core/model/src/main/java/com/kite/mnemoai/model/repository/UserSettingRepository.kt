package com.kite.mnemoai.model.repository

import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.data.UserSetting
import kotlinx.coroutines.flow.Flow

interface UserSettingRepository {
    fun observeUserSetting(): Flow<Result<UserSetting>>

    suspend fun getUserSetting(): Result<UserSetting>

    suspend fun getUserSettingSync(): UserSetting

    suspend fun setNewLearningWordCount(targetCount: Int)

    suspend fun setLightDarkModel(model: Int)

    suspend fun setApiKey(apiKey: String)
}
