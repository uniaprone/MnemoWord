package com.kite.mnemoai.data.repository

import com.kite.mnemoai.common.Dispatcher
import com.kite.mnemoai.common.MaiDispatcher
import com.kite.mnemoai.common.MemoryAlgorithm
import com.kite.mnemoai.data.mapper.asExternalModel
import com.kite.mnemoai.database.dao.DayPlanDao
import com.kite.mnemoai.database.dao.DayPlanWordDao
import com.kite.mnemoai.database.dao.ReviewWordDao
import com.kite.mnemoai.database.model.DayPlanWordEntity
import com.kite.mnemoai.database.model.ReviewWordEntity
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.dayplan.DayPlan
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepositoryImpl @Inject constructor(
    private val dayPlanDao: DayPlanDao,
    private val dayPlanWordDao: DayPlanWordDao,
    private val reviewWordDao: ReviewWordDao,
    @Dispatcher(MaiDispatcher.IO) private val ioDispatcher: CoroutineDispatcher
) : StatisticsRepository {

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

    override suspend fun rememberWord(reciteStatistics: ReciteStatistics) = withContext(ioDispatcher) {
        val wordId = reciteStatistics.wordId
        val dateTime = LocalDateTime.now().toString()
        val date = LocalDate.now()

        val dayPlanWordEntity = dayPlanWordDao.queryDayPlanWordBywordIdAndDate(wordId, date.toString())
        if (dayPlanWordEntity != null) {
            dayPlanWordEntity.status = 1
            dayPlanWordEntity.blurCount = reciteStatistics.blurCount
            dayPlanWordEntity.forgetCount = reciteStatistics.forgetCount
            dayPlanWordEntity.learningTime = reciteStatistics.learningTime
            dayPlanWordEntity.completeTime = dateTime
            dayPlanWordDao.InsertDayPlanWord(dayPlanWordEntity)

            val reviewWordEntity = reviewWordDao.getReviewWordEntityById(wordId)
            if (reviewWordEntity != null) {
                val reviewCount = reviewWordEntity.reviewCount + 1
                reviewWordEntity.reviewCount = reviewCount
                reviewWordEntity.nextReviewTime = MemoryAlgorithm.calculateNextReviewDate(
                    reviewCount,
                    reciteStatistics.blurCount,
                    reciteStatistics.forgetCount,
                    reciteStatistics.learningTime,
                    date
                ).toString()
                reviewWordDao.insertReviewWord(reviewWordEntity)
            } else {
                val newReviewWordEntity = ReviewWordEntity(
                    wordId, 1, 0,
                    MemoryAlgorithm.calculateNextReviewDate(
                        0,
                        reciteStatistics.blurCount,
                        reciteStatistics.forgetCount,
                        reciteStatistics.learningTime,
                        date
                    ).toString()
                )
                reviewWordDao.insertReviewWord(newReviewWordEntity)
            }
        }
    }

    override suspend fun getStudyStatistic(): Result<List<StudyStatistic>> =
        withContext(ioDispatcher) {
            try {
                Result.Success(dayPlanWordDao.getStudyStatistic().map { it.asExternalModel() })
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
}
