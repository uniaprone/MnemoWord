package com.kite.mnemoai.model.statistic

import java.time.LocalDate

data class DateInterval(
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    override fun toString(): String {
        return "$startDate 至 $endDate"
    }
}