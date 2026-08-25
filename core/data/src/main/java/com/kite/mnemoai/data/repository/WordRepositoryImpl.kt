package com.kite.mnemoai.data.repository

import com.kite.mnemoai.common.Dispatcher
import com.kite.mnemoai.common.MaiDispatcher
import com.kite.mnemoai.data.mapper.asExternalModel
import com.kite.mnemoai.database.dao.DayPlanWordDao
import com.kite.mnemoai.database.dao.GroupDao
import com.kite.mnemoai.database.dao.WordDao
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.repository.WordRepository
import com.kite.mnemoai.model.word.WordDetail
import com.kite.mnemoai.model.word.WordItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class WordRepositoryImpl @Inject constructor(
    private val wordDao: WordDao,
    private val groupDao: GroupDao,
    private val dayPlanWordDao: DayPlanWordDao,
    @Dispatcher(MaiDispatcher.IO) private val ioDispatcher: CoroutineDispatcher
) : WordRepository {
    override suspend fun addTodayNewLearningWordEntities(
        count: Int,
        date: String,
        excludeWordIds: List<Long>
    ): List<Long> = withContext(ioDispatcher){
        wordDao.addTodayNewLearningWordEntities(count, date, excludeWordIds)
    }

    override suspend fun selectTodayReviewingWordEntities(date: String): List<Long> =
        withContext(ioDispatcher) {
            wordDao.selectTodayReviewingWordEntities(date)
        }

    override fun observeWordListByGroupId(groupId: Long): Flow<Result<List<WordItem>>> =
        wordDao.getWordListItemLiveDataByGroupId(groupId)
            .map { Result.Success(it.asExternalModel()) as Result<List<WordItem>> }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(e)) }

    override fun observeDailyReciteStatus(): Flow<Result<Int>> = flow<Result<Int>> {
        val date = java.time.LocalDate.now().toString()
        groupDao.hasLearningGroup()
            .flatMapLatest { hasLearningGroup ->
                if (hasLearningGroup == 0) {
                    flow { emit(0) }
                } else {
                    dayPlanWordDao.hasDayPlanWord(date)
                        .flatMapLatest { hasDayPlanWord ->
                            if (hasDayPlanWord == 0) {
                                flow { emit(1) }
                            } else {
                                dayPlanWordDao.hasUnfinishedDayPlanWord(date)
                                    .map { hasUnfinished ->
                                        if (hasUnfinished == 0) 1 else 2
                                    }
                            }
                        }
                }
            }
            .collect { status ->
                emit(Result.Success(status))
            }
    }.onStart { emit(Result.Loading) }
        .catch { e -> emit(Result.Error(e)) }

    override fun observeUnfinishedWordDetailInfos(): Flow<Result<List<WordDetail>>> {
        val date = java.time.LocalDate.now().toString()
        return wordDao.getUnfinishWordDetailInfoLiveData(date)
            .map { entities ->
                entities.map { entity ->
                    entity.asExternalModel().let { detail ->
                        detail.copy(
                            dayPlanWords = detail.dayPlanWords.filter { it.date != date },
                            forms = detail.forms.filter { it.typeCode != "0" &&  it.typeCode != "1" }
                        )
                    }
                }
            }
            .map { Result.Success(it) as Result<List<WordDetail>> }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(e)) }
    }

    override fun observeWordDetailById(id: Long): Flow<Result<WordDetail?>> =
        wordDao.getWordDetailInfoLiveDataById(id)
            .map { entity ->
                entity.asExternalModel().let {
                detail -> detail.copy(
                    forms = detail.forms.filter { it.typeCode != "0" &&  it.typeCode != "1" }
                    )
                }
            }
            .map { Result.Success(it) as Result<WordDetail?> }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(e)) }

    override suspend fun searchWord(word: String): Result<List<WordItem>> =
        withContext(ioDispatcher) {
            try {
                val result = wordDao.performSearch(word)
                Result.Success(result.asExternalModel())
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun addOptionWordsSearch(groupId: Long, word: String): Result<List<WordItem>> =
        withContext(ioDispatcher) {
            try {
                val result = wordDao.performAddOptionSearch(groupId, word)
                Result.Success(result.asExternalModel())
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun removeOptionWordsSearch(groupId: Long, word: String): Result<List<WordItem>> =
        withContext(ioDispatcher) {
            try {
                val result = wordDao.performRemoveOptionSearch(groupId, word)
                Result.Success(result.asExternalModel())
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
}
