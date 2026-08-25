package com.kite.mnemoai.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kite.mnemoai.database.model.ReviewWordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewWordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReviewWord(reviewWordEntity: ReviewWordEntity)

    @Update
    fun updateReviewWord(reviewWordEntity: ReviewWordEntity)

    @Query("SELECT * FROM word_review WHERE id = :id")
    fun getReviewWordEntityLiveData(id: Long): Flow<ReviewWordEntity?>?

    @Query("SELECT * FROM word_review WHERE id = :id")
    suspend fun getReviewWordEntityById(id: Long): ReviewWordEntity?
}
