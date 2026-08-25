package com.kite.mnemoai.model.repository

import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.dayplan.DayPlan
import com.kite.mnemoai.model.dayplan.DayPlanWord
import com.kite.mnemoai.model.dayplan.ReviewWord
import com.kite.mnemoai.model.statistic.DailyStatistic
import com.kite.mnemoai.model.statistic.StudyStatistic
import kotlinx.coroutines.flow.Flow
import java.sql.Date

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
    /**
     * 根据日期获取每日计划单词
     */
    suspend fun getDayPlanWordsByDate(date: String): List<DayPlanWord>
    suspend fun setReviewDayPlanWords(dayPlanWords: List<DayPlanWord>)
    suspend fun addNewLearningDayPlanWords(dayPlanWords: List<DayPlanWord>)
    suspend fun deleteExceedDayPlanWords(removeCount: Int)
    fun observeDayPlanByDate(date: String): Flow<Result<DayPlan?>>

    fun observeAllPlanCountByDate(date: String): Flow<Result<Int>>

    fun observeFinishedPlanCountByDate(date: String): Flow<Result<Int>>

    fun observeDailyStatisticByDate(date: String): Flow<Result<DailyStatistic>>

    /**
     * 根据id获取ReviewWord（无记录时返回 null）
     */
    suspend fun getReviewWordById(wordId: Long): ReviewWord?

    /**
     * 保存单词背诵后的统计上信息
     */
    suspend fun saveWordReciteStatistic(reciteStatistics: ReciteStatistics)

    /**
     * 保存复习单词
     */
    suspend fun saveReviewWord(reviewWord: ReviewWord)

    suspend fun getStudyStatistic(): Result<List<StudyStatistic>>
}
