package com.kite.mnemoai.data.repository

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.kite.mnemoai.common.Dispatcher
import com.kite.mnemoai.common.MaiDispatcher
import com.kite.mnemoai.database.dao.DayPlanWordDao
import com.kite.mnemoai.database.dao.WordDao
import com.kite.mnemoai.database.model.DayPlanWordEntity
import com.kite.mnemoai.model.Result
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
    @ApplicationContext private val context: Context,
    private val dayPlanWordDao: DayPlanWordDao,
    private val wordDao: WordDao,
    @Dispatcher(MaiDispatcher.IO) private val ioDispatcher: CoroutineDispatcher
) : UserSettingRepository {

    private val sp get() = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)

    private val _userSetting = MutableStateFlow(readFromSP())

    override fun observeUserSetting(): Flow<Result<UserSetting>> = _userSetting
        .map { Result.Success(it) as Result<UserSetting> }
        .onStart { emit(Result.Loading) }
        .catch { e -> emit(Result.Error(e)) }

    private fun readFromSP(): UserSetting {
        val newStudyCount = sp.getInt("study_word_count", 20)
        val lightDarkModel = sp.getInt("light_dark_model", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        val apiKey = sp.getString("api_key", "") ?: ""
        return UserSetting(newStudyCount, lightDarkModel, apiKey)
    }

    private fun saveToSP(userSetting: UserSetting) {
        sp.edit().apply {
            putInt("study_word_count", userSetting.newLearningWordCount)
            putInt("light_dark_model", userSetting.lightDarkModel)
            putString("api_key", userSetting.apiKey)
            apply()
        }
    }

    override suspend fun getUserSetting(): Result<UserSetting> = withContext(ioDispatcher) {
        Result.Success(readFromSP())
    }

    override suspend fun getUserSettingSync(): UserSetting = readFromSP()

    override suspend fun setNewLearningWordCount(targetCount: Int) = withContext(ioDispatcher) {
        val date = LocalDate.now().toString()
        val dayPlanWordEntities = dayPlanWordDao.queryDayPlanWordsByDate(date)
        val planCount = dayPlanWordEntities.count { it.type == 0 }
        val finishCount = dayPlanWordEntities.count { it.type == 0 && it.status == 1 }

        if (targetCount > planCount) {
            addNewLearningDayPlanWordEntities(targetCount - planCount)
        } else if (targetCount < planCount && targetCount > finishCount) {
            removeNewLearningDayPlanWordEntities(planCount - targetCount)
        }

        val newSetting = readFromSP().copy(newLearningWordCount = targetCount)
        saveToSP(newSetting)
        _userSetting.value = newSetting
    }

    override suspend fun setLightDarkModel(model: Int) = withContext(ioDispatcher) {
        val newSetting = readFromSP().copy(lightDarkModel = model)
        saveToSP(newSetting)
        _userSetting.value = newSetting
    }

    override suspend fun setApiKey(apiKey: String) = withContext(ioDispatcher) {
        val newSetting = readFromSP().copy(apiKey = apiKey)
        saveToSP(newSetting)
        _userSetting.value = newSetting
    }

    private fun addNewLearningDayPlanWordEntities(learningCount: Int) {
        val date = LocalDate.now().toString()
        val ids = dayPlanWordDao.getTodayNewLearningWordsId(date)
        val newLearningWordEntities = wordDao.addTodayNewLearningWordEntities(learningCount, date, ids)
        val allDayPlanWordEntities = newLearningWordEntities.map {
            DayPlanWordEntity(it.id, date, 0, 0, 0, 0, 0, null)
        }
        dayPlanWordDao.InsertDayPlanWord(allDayPlanWordEntities)
    }

    private fun removeNewLearningDayPlanWordEntities(removeCount: Int) {
        dayPlanWordDao.deleteRandomNewLearningWord(LocalDate.now().toString(), removeCount)
    }
}
