package com.kite.mnemoai.datastore

import androidx.datastore.core.DataStore
import com.kite.mnemoai.model.data.ThemeType
import com.kite.mnemoai.model.data.UserSetting
import jakarta.inject.Inject
import kotlinx.coroutines.flow.map

class UserSettingDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>
){
    private val defaultNewLearningWordCount = 20
    val userSetting = userPreferences.data
        .map {
            UserSetting(
                newLearningWordCount = if (defaultNewLearningWordCount == 0) defaultNewLearningWordCount else it.newLearningWordCount,
                lightDarkModel = when(it.lightDarkMode){
                    null,ThemeTypeProto.UNRECOGNIZED, ThemeTypeProto.UNSPECIFIED, ThemeTypeProto.FOLLOW_SYS ->
                        ThemeType.FOLLOW_SYS
                    ThemeTypeProto.DAY -> ThemeType.DAY
                    ThemeTypeProto.NIGHT -> ThemeType.NIGHT
                },
                apiKey = it.apiKey
            )
        }

    suspend fun setNewLearningWordCount(targetCount: Int){
        userPreferences.updateData { preferences ->
            preferences.toBuilder().setNewLearningWordCount(targetCount).build()
        }
    }

    suspend fun setLightDarkModel(model: ThemeType){
        val protoType = when(model){
            ThemeType.FOLLOW_SYS -> ThemeTypeProto.FOLLOW_SYS
            ThemeType.DAY -> ThemeTypeProto.DAY
            ThemeType.NIGHT -> ThemeTypeProto.NIGHT
        }
        userPreferences.updateData { preferences ->
            preferences.toBuilder().setLightDarkMode(protoType).build()
        }
    }

    suspend fun setApiKey(apiKey: String){
        userPreferences.updateData { preferences ->
            preferences.toBuilder().setApiKey(apiKey).build()
        }
    }
}