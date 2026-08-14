package com.kite.mnemoai.model.repository

import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.dayplan.DayPlan
import com.kite.mnemoai.model.statistic.DailyStatistic
import com.kite.mnemoai.model.statistic.StudyStatistic
import kotlinx.coroutines.flow.Flow

data class ReciteStatistics(
    val wordId: Long,
    var blurCount: Int = 0,
    var forgetCount: Int = 0,
    var learningTime: Long = 0,
    var startLearningTime: Long = 0
) {
    fun addLearningTime(time: Long) {
        this.learningTime += time
    }

    fun addBlurCount() {
        blurCount += 1
    }

    fun addForgetCount() {
        forgetCount += 1
    }
}

interface StatisticsRepository {
    fun observeDayPlanByDate(date: String): Flow<Result<DayPlan?>>

    fun observeAllPlanCountByDate(date: String): Flow<Result<Int>>

    fun observeFinishedPlanCountByDate(date: String): Flow<Result<Int>>

    fun observeDailyStatisticByDate(date: String): Flow<Result<DailyStatistic>>

    suspend fun rememberWord(reciteStatistics: ReciteStatistics)

    suspend fun getStudyStatistic(): Result<List<StudyStatistic>>
}
