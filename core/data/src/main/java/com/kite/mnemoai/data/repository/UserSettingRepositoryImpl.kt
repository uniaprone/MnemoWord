package com.kite.mnemoai.data.repository

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.kite.mnemoai.common.Dispatcher
import com.kite.mnemoai.common.MaiDispatcher
import com.kite.mnemoai.database.dao.DayPlanWordDao
import com.kite.mnemoai.database.dao.WordDao
import com.kite.mnemoai.database.model.DayPlanWordEntity
import com.kite.mnemoai.datastore.UserSettingDataSource
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.data.ThemeType
import com.kite.mnemoai.model.data.UserSetting
import com.kite.mnemoai.model.repository.UserSettingRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class UserSettingRepositoryImpl @Inject constructor(
    private val userSettingDataSource: UserSettingDataSource,
    @Dispatcher(MaiDispatcher.IO) private val ioDispatcher: CoroutineDispatcher
) : UserSettingRepository {

    override val userSetting: Flow<UserSetting> = userSettingDataSource.userSetting

    override suspend fun setNewLearningWordCount(targetCount: Int) = withContext(ioDispatcher) {
        userSettingDataSource.setNewLearningWordCount(targetCount)
    }

    override suspend fun setLightDarkModel(model: ThemeType) = withContext(ioDispatcher) {
        userSettingDataSource.setLightDarkModel(model)
    }

    override suspend fun setApiKey(apiKey: String) = withContext(ioDispatcher) {
        userSettingDataSource.setApiKey(apiKey)
    }
}
