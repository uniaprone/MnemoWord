package com.kite.mnemoai.database.dao;

import kotlinx.coroutines.flow.Flow;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kite.mnemoai.database.model.DayPlanEntity;


@Dao
public interface DayPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDayPlanEntity(DayPlanEntity dayPlanEntity);
    @Query("SELECT * FROM day_plan WHERE date = :date")
    Flow<DayPlanEntity> getDayPlanEntityLiveData(String date);
}
