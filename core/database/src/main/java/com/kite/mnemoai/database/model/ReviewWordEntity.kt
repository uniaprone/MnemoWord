package com.kite.mnemoai.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kite.mnemoai.model.dayplan.ReviewWord

@Entity(tableName = "word_review")
class ReviewWordEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    var wordId: Long = 0,
    @ColumnInfo(name = "review_status")
    var reviewState: Int = 0, // 0-未复习 1-复习中 2-完成
    @ColumnInfo(name = "review_count")
    var reviewCount: Int = 0,
    @ColumnInfo(name = "next_review_time")
    var nextReviewTime: String? = null
)

fun ReviewWordEntity.asExternalModel() = ReviewWord(
    wordId = wordId,
    reviewState = reviewState,
    reviewCount = reviewCount,
    nextReviewTime = nextReviewTime
)
