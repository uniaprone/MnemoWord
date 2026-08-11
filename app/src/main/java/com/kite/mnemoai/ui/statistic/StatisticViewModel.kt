package com.kite.mnemoai.ui.statistic

import androidx.lifecycle.ViewModel
import com.kite.mnemoai.data.model.StudyStatistic
import com.kite.mnemoai.data.repository.IRepositoryCallback
import com.kite.mnemoai.data.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {
    fun getStudyStatistic(callback: IRepositoryCallback<MutableList<StudyStatistic?>?>?) {
        statisticsRepository.getStudyStatistic(callback)
    }
}
