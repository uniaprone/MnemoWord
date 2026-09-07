package com.kite.mnemoai.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kite.mnemoai.database.model.DailyStatistic
import com.kite.mnemoai.database.model.DayPlanWordEntity
import com.kite.mnemoai.database.model.StudyStatistic
import com.kite.mnemoai.model.statistic.DateInterval
import kotlinx.coroutines.flow.Flow

@Dao
interface DayPlanWordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDayPlanWord(dayPlanWordEntities:List<DayPlanWordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDayPlanWord(dayPlanWordEntity: DayPlanWordEntity)

    @Query("SELECT EXISTS (SELECT 1 FROM day_plan_word WHERE date = :date AND status = 0)")
    fun hasUnfinishedDayPlanWord(date: String): Flow<Int>

    @Query("SELECT EXISTS (SELECT 1 FROM day_plan_word WHERE date = :date)")
    fun hasDayPlanWord(date: String): Flow<Int>

    @Query("SELECT * FROM day_plan_word WHERE date = :date")
    suspend fun queryDayPlanWordsByDate(date: String): List<DayPlanWordEntity>

    @Query("SELECT * FROM day_plan_word WHERE word_id = :wordId AND date = :date")
    fun queryDayPlanWordBywordIdAndDate(wordId: Long, date: String): DayPlanWordEntity

    @Query("SELECT COUNT(word_id) FROM day_plan_word WHERE date = :date")
    fun getAllPlanCountByDate(date: String): Flow<Int>

    @Query(
        "SELECT COUNT(word_id) FROM day_plan_word " +
                "WHERE status = 1 AND date = :date"
    )
    fun getFinishedPlanCountByDate(date: String): Flow<Int>

    @Query(
        ("DELETE FROM day_plan_word WHERE word_id IN (" +
                "SELECT word_id FROM day_plan_word " +
                "WHERE date = :date " +
                "AND type = 0 " +
                "AND status = 0 " +
                "ORDER BY RANDOM() " +
                "LIMIT :count" +
                ")")
    )
    fun deleteRandomNewLearningWord(date: String, count: Int)

    @Query(
        ("SELECT COUNT(DISTINCT CASE WHEN type = 1 THEN word_id END) AS review_count, " +
                "COUNT(DISTINCT CASE WHEN type = 0 AND status = 1 THEN word_id END) AS learned_count, " +
                "COUNT(DISTINCT CASE WHEN type = 1 AND status = 1 THEN word_id END) AS reviewed_count " +
                "FROM day_plan_word AS dpw " +
                "WHERE date = :date")
    )
    fun queryDailyStatisticByDate(date: String): Flow<DailyStatistic>

    @get:Query(
        ("SELECT date," +
                "COUNT(CASE WHEN type = 0 AND status = 1 THEN 1 END) AS new_learned_count," +
                "COUNT(CASE WHEN type = 1 AND status = 1 THEN 1 END) AS reviewed_count, " +
                "SUM(COALESCE(learning_time, 0)) AS total_learning_time " +
                "FROM day_plan_word " +
                "GROUP BY date " +
                "ORDER BY date")
    )
    val studyStatistic: MutableList<StudyStatistic>

    @Query(
        "SELECT date," +
                "COUNT(CASE WHEN type = 0 AND status = 1 THEN 1 END) AS new_learned_count," +
                "COUNT(CASE WHEN type = 1 AND status = 1 THEN 1 END) AS reviewed_count, " +
                "SUM(COALESCE(learning_time, 0)) AS total_learning_time " +
                "FROM day_plan_word " +
                "WHERE date >= :startDate AND date <= :endDate " +
                "GROUP BY date " +
                "ORDER BY date"
    )
    fun queryStudyStatisticByDateInterval(startDate: String, endDate: String): Flow<List<StudyStatistic>>
}
