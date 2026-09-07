package com.kite.mnemoai.data.repository

import com.kite.mnemoai.common.Dispatcher
import com.kite.mnemoai.common.MaiDispatcher
import com.kite.mnemoai.data.model.asEntity
import com.kite.mnemoai.data.model.asExternalModel
import com.kite.mnemoai.database.dao.DayPlanDao
import com.kite.mnemoai.database.dao.DayPlanWordDao
import com.kite.mnemoai.database.dao.ReviewWordDao
import com.kite.mnemoai.database.model.asExternalModel
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.dayplan.DayPlan
import com.kite.mnemoai.model.dayplan.DayPlanWord
import com.kite.mnemoai.model.dayplan.ReviewWord
import com.kite.mnemoai.model.repository.ReciteStatistics
import com.kite.mnemoai.model.repository.StatisticsRepository
import com.kite.mnemoai.model.statistic.DailyStatistic
import com.kite.mnemoai.model.statistic.StudyStatistic
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map

@Singleton
class StatisticsRepositoryImpl @Inject constructor(
    private val dayPlanDao: DayPlanDao,
    private val dayPlanWordDao: DayPlanWordDao,
    private val reviewWordDao: ReviewWordDao,
    @Dispatcher(MaiDispatcher.IO) private val ioDispatcher: CoroutineDispatcher
) : StatisticsRepository {
    override suspend fun getDayPlanWordsByDate(date: String): List<DayPlanWord> = withContext(ioDispatcher) {
        dayPlanWordDao.queryDayPlanWordsByDate(date).map { it.asExternalModel() }
    }

    override suspend fun addNewLearningDayPlanWords(dayPlanWords: List<DayPlanWord>) = withContext(ioDispatcher){
        dayPlanWordDao.insertDayPlanWord(dayPlanWords.map { it.asEntity() })
    }

    override suspend fun deleteExceedDayPlanWords(removeCount: Int) = withContext(ioDispatcher) {
        dayPlanWordDao.deleteRandomNewLearningWord(LocalDate.now().toString(), removeCount)
    }


    override fun observeDayPlanByDate(date: String): Flow<Result<DayPlan?>> =
        dayPlanDao.getDayPlanEntityLiveData(date)
            .map { entity -> entity?.asExternalModel() }
            .map { Result.Success(it) as Result<DayPlan?> }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(e)) }

    override fun observeAllPlanCountByDate(date: String): Flow<Result<Int>> =
        dayPlanWordDao.getAllPlanCountByDate(date)
            .map { count -> count ?: 0 }
            .map { Result.Success(it) as Result<Int> }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(e)) }

    override fun observeFinishedPlanCountByDate(date: String): Flow<Result<Int>> =
        dayPlanWordDao.getFinishedPlanCountByDate(date)
            .map { count -> count ?: 0 }
            .map { Result.Success(it) as Result<Int> }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(e)) }

    override fun observeDailyStatisticByDate(date: String): Flow<Result<DailyStatistic>> =
        dayPlanWordDao.queryDailyStatisticByDate(date)
            .map { it.asExternalModel() }
            .map { Result.Success(it) as Result<DailyStatistic> }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(e)) }

    override suspend fun getReviewWordById(wordId: Long): ReviewWord? =
        withContext(ioDispatcher) {
            reviewWordDao.getReviewWordEntityById(wordId)?.asExternalModel()
        }

    override suspend fun saveWordReciteStatistic(reciteStatistics: ReciteStatistics) {
        withContext(ioDispatcher){
            val wordId = reciteStatistics.wordId
            val date = LocalDate.now().toString()
            val dateTime = LocalDateTime.now().toString()

            dayPlanWordDao.queryDayPlanWordBywordIdAndDate(wordId, date)?.let { entity ->
                entity.status = 1
                entity.blurCount = reciteStatistics.blurCount
                entity.forgetCount = reciteStatistics.forgetCount
                entity.learningTime = reciteStatistics.learningTime
                entity.completeTime = dateTime
                dayPlanWordDao.insertDayPlanWord(entity)
            }
        }
    }

    override suspend fun saveReviewWord(reviewWord: ReviewWord) {
        withContext(ioDispatcher){
            reviewWordDao.insertReviewWord(reviewWord.asEntity())
        }
    }

    override suspend fun getStudyStatistic(): Result<List<StudyStatistic>> =
        withContext(ioDispatcher) {
            try {
                Result.Success(dayPlanWordDao.studyStatistic.map { it.asExternalModel() })
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override fun queryStudyStatisticByDateInterval(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<StudyStatistic>> =
        dayPlanWordDao.queryStudyStatisticByDateInterval(startDate.toString(), endDate.toString())
            .map { list ->
                val byDate = list.map { it.asExternalModel() }.associateBy { it.date }
                (0L..ChronoUnit.DAYS.between(startDate, endDate)).map { offset ->
                    val date = startDate.plusDays(offset).toString()
                    byDate[date] ?: StudyStatistic(date, 0, 0, 0)
                }
            }
}
