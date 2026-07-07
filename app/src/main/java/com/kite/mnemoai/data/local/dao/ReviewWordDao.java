package com.kite.mnemoai.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.kite.mnemoai.data.local.entity.ReviewWordEntity;

@Dao
public interface ReviewWordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReviewWord(ReviewWordEntity reviewWordEntity);

    @Update
    void updateReviewWord(ReviewWordEntity reviewWordEntity);

    @Query("SELECT * FROM word_review WHERE id = :id")
    LiveData<ReviewWordEntity> getReviewWordEntityLiveData(long id);

    @Query("SELECT * FROM word_review WHERE id = :id")
    ReviewWordEntity getReviewWordEntityById(long id);
}
