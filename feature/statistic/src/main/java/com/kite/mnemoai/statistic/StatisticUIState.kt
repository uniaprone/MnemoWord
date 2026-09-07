package com.kite.mnemoai.statistic

import com.kite.mnemoai.model.statistic.DateInterval
import com.kite.mnemoai.model.statistic.DateIntervalType
import com.kite.mnemoai.model.statistic.StudyStatistic

data class StatisticUIState(
    val studyStatistics: List<StudyStatistic>?,
    val dateIntervalType: DateIntervalType = DateIntervalType.IN_THE_LAST_SEVEN_DAY,
    val dateInterval: DateInterval,
    val revision: Long
)