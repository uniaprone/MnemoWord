package com.kite.mnemoai.model.statistic

data class DailyStatistic(
    val reviewCount: Int,
    val learnedCount: Int,
    val reviewedCount: Int
)

data class StudyStatistic(
    val date: String,
    val reviewedCount: Int,
    val newLearnedCount: Int,
    val totalLearningTime: Long
)
