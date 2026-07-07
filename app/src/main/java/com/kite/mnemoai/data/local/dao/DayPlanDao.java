package com.kite.mnemoai.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kite.mnemoai.data.local.entity.DayPlanEntity;

@Dao
public interface DayPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDayPlanEntity(DayPlanEntity dayPlanEntity);
    @Query("SELECT * FROM day_plan WHERE date = :date")
    LiveData<DayPlanEntity> getDayPlanEntityLiveData(String date);
}
