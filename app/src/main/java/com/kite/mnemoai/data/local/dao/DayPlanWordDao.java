package com.kite.mnemoai.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kite.mnemoai.data.local.DTO.DailyStatistic;
import com.kite.mnemoai.data.local.DTO.StudyStatistic;
import com.kite.mnemoai.data.local.entity.DayPlanWordEntity;

import java.util.List;

@Dao
public interface DayPlanWordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void InsertDayPlanWord(List<DayPlanWordEntity> dayPlanWordEntities);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void InsertDayPlanWord(DayPlanWordEntity dayPlanWordEntity);

    @Query("SELECT * FROM day_plan_word WHERE date = :date")
    LiveData<List<DayPlanWordEntity>> queryAllDayPlanWordsLiveDataByDate(String date);

    @Query("SELECT * FROM day_plan_word WHERE date = :date AND status = 0")
    LiveData<List<DayPlanWordEntity>> queryUnfinishedDayPlanWordsLiveDataByDate(String date);

    @Query("SELECT EXISTS (SELECT 1 FROM day_plan_word WHERE date = :date AND status = 0)")
    LiveData<Integer> hasUnfinishedDayPlanWord(String date);

    @Query("SELECT EXISTS (SELECT 1 FROM day_plan_word WHERE date = :date)")
    LiveData<Integer> hasDayPlanWord(String date);

    @Query("SELECT * FROM day_plan_word WHERE date = :date")
    List<DayPlanWordEntity> queryDayPlanWordsByDate(String date);

    @Query("SELECT * FROM day_plan_word WHERE word_id = :wordId AND date = :date")
    DayPlanWordEntity queryDayPlanWordBywordIdAndDate(long wordId, String date);

    @Query("SELECT COUNT(word_id) FROM day_plan_word WHERE date = :date")
    LiveData<Integer> getAllPlanCountByDate(String date);

    @Query("SELECT COUNT(word_id) FROM day_plan_word " +
            "WHERE status = 1 AND date = :date")
    LiveData<Integer> getFinishedPlanCountByDate(String date);

    @Query("DELETE FROM day_plan_word WHERE word_id IN (" +
            "SELECT word_id FROM day_plan_word " +
            "WHERE date = :date " +
            "AND type = 0 " +
            "AND status = 0 " +
            "ORDER BY RANDOM() " +
            "LIMIT :count" +
            ")")
    void deleteRandomNewLearningWord(String date, int count);

    @Query("SELECT COUNT(DISTINCT CASE WHEN type = 1 THEN word_id END) AS review_count, " +
            "COUNT(DISTINCT CASE WHEN type = 0 AND status = 1 THEN word_id END) AS learned_count, " +
            "COUNT(DISTINCT CASE WHEN type = 1 AND status = 1 THEN word_id END) AS reviewed_count " +
            "FROM day_plan_word AS dpw " +
            "WHERE date = :date")
    LiveData<DailyStatistic> queryDailyStatisticByDate(String date);

    @Query("SELECT date," +
            "COUNT(CASE WHEN type = 0 AND status = 1 THEN 1 END) AS new_learned_count," +
            "COUNT(CASE WHEN type = 1 AND status = 1 THEN 1 END) AS reviewed_count, " +
            "SUM(COALESCE(learning_time, 0)) AS total_learning_time " +
            "FROM day_plan_word " +
            "GROUP BY date " +
            "ORDER BY date")
    List<StudyStatistic> getStudyStatistic();
}
