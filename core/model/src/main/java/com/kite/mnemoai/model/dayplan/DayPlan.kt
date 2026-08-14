package com.kite.mnemoai.model.dayplan

data class DayPlan(
    val date: String,
    val learnGoal: Long
)

data class DayPlanWord(
    val wordId: Long,
    val date: String,
    val type: Int,
    val status: Int,
    val blurCount: Int,
    val forgetCount: Int,
    val learningTime: Long,
    val completeTime: String?
)

data class ReviewWord(
    val wordId: Long,
    val reviewState: Int,
    val reviewCount: Int,
    val nextReviewTime: String?
)
