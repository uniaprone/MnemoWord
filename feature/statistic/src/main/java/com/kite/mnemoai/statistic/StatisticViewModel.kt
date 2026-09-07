package com.kite.mnemoai.statistic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kite.mnemoai.model.data.UserSetting
import com.kite.mnemoai.model.repository.StatisticsRepository
import com.kite.mnemoai.model.repository.UserSettingRepository
import com.kite.mnemoai.model.statistic.DateInterval
import com.kite.mnemoai.model.statistic.DateIntervalType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatisticViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val userSettingRepository: UserSettingRepository,
) : ViewModel() {
    private var revision = 0L
    private val _uiState = MutableStateFlow<StatisticUIState?>(null)
    val uiState get() = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userSettingRepository.userSetting
                .flatMapLatest { setting ->
                    val effectiveInterval = rollToToday(setting)
                    if (effectiveInterval != setting.statisticDateInterval) {
                        userSettingRepository.setDateInterval(
                            setting.statisticDateIntervalType,
                            effectiveInterval
                        )
                    }
                    statisticsRepository.queryStudyStatisticByDateInterval(
                        effectiveInterval.startDate,
                        effectiveInterval.endDate
                    ).map { stats ->
                        StatisticUIState(
                            studyStatistics = stats,
                            dateIntervalType = setting.statisticDateIntervalType,
                            dateInterval = effectiveInterval,
                            revision = ++revision
                        )
                    }
                }
                .collect { _uiState.value = it }
        }
    }

    fun onResume() {
        viewModelScope.launch {
            val setting = userSettingRepository.userSetting.first()
            val effectiveInterval = rollToToday(setting)
            if (effectiveInterval != setting.statisticDateInterval) {
                userSettingRepository.setDateInterval(
                    setting.statisticDateIntervalType,
                    effectiveInterval
                )
            }
        }
    }

    fun setStatisticDataInterval(type: DateIntervalType, dateInterval: DateInterval) {
        viewModelScope.launch {
            userSettingRepository.setDateInterval(type, dateInterval)
        }
    }

    private fun rollToToday(setting: UserSetting): DateInterval {
        val today = LocalDate.now()
        val interval = setting.statisticDateInterval
        if (setting.statisticDateIntervalType == DateIntervalType.CUSTOM || interval.endDate >= today) {
            return interval
        }
        return when (setting.statisticDateIntervalType) {
            DateIntervalType.IN_THE_LAST_SEVEN_DAY -> DateInterval(today.minusDays(6), today)
            DateIntervalType.IN_THE_LAST_MONTH -> DateInterval(today.minusMonths(1), today)
            DateIntervalType.IN_THE_LAST_THREE_MONTH -> DateInterval(today.minusMonths(3), today)
            DateIntervalType.CUSTOM -> interval
        }
    }
}
