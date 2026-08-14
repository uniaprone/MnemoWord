package com.kite.mnemoai.statistic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.repository.StatisticsRepository
import com.kite.mnemoai.model.statistic.StudyStatistic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.function.Consumer
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {
    fun getStudyStatistic(onResult: Consumer<List<StudyStatistic>>) {
        viewModelScope.launch {
            val result = statisticsRepository.getStudyStatistic()
            if (result is Result.Success) {
                onResult.accept(result.data)
            }
        }
    }
}
