package com.kite.mnemoai.vocabularybook

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kite.domain.SetDailyReciteWordUseCase
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.group.AllVocabularyBookItem
import com.kite.mnemoai.model.group.Group
import com.kite.mnemoai.model.group.LearningVocabularyBookItem
import com.kite.mnemoai.model.repository.GroupRepository
import com.kite.mnemoai.model.repository.StatisticsRepository
import com.kite.mnemoai.model.repository.UserSettingRepository
import com.kite.mnemoai.model.statistic.DailyStatistic
import com.kite.mnemoai.shared_ui.VocabularySelectDialogFragment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.function.Consumer
import javax.inject.Inject

@HiltViewModel
class VocabularyViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val statisticsRepository: StatisticsRepository,
    private val userSettingRepository: UserSettingRepository,
    private val setDailyReciteWordUseCase: SetDailyReciteWordUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<VocabularyUIState>()
    val uiState: LiveData<VocabularyUIState> get() = _uiState

    private var learningVocabularyBookItems: List<LearningVocabularyBookItem>? = null
    private var allVocabularyBookItems: List<AllVocabularyBookItem>? = null
    private var dailyStatistic: DailyStatistic? = null
    private var newLearningWordCount: Int = 0

    init {
        viewModelScope.launch {
            groupRepository.observeLearningVocabularyBookItems().collect { result ->
                if (result is Result.Success) {
                    learningVocabularyBookItems = result.data
                    updateUIState()
                }
            }
        }
        viewModelScope.launch {
            groupRepository.observeAllVocabularyBookItems().collect { result ->
                if (result is Result.Success) {
                    allVocabularyBookItems = result.data
                    updateUIState()
                }
            }
        }
        viewModelScope.launch {
            userSettingRepository.userSetting.collect { userSetting ->
                newLearningWordCount = userSetting.newLearningWordCount
                updateUIState()
            }
        }
        viewModelScope.launch {
            statisticsRepository.observeDailyStatisticByDate(LocalDate.now().toString()).collect { result ->
                if (result is Result.Success) {
                    dailyStatistic = result.data
                    updateUIState()
                }
            }
        }
    }

    private fun updateUIState() {
        _uiState.value = VocabularyUIState(
            learningVocabularyBookItems,
            allVocabularyBookItems,
            newLearningWordCount,
            dailyStatistic
        )
    }

    fun setNewLearningWordCount(newLearningWordCount: Int) {
        viewModelScope.launch {
            userSettingRepository.setNewLearningWordCount(newLearningWordCount)
            setDailyReciteWordUseCase()
        }
    }

    fun addLearningGroups(ids: List<Long>) {
        viewModelScope.launch {
            groupRepository.addLearningGroups(ids)
        }
    }

    fun getVocabularySelectedInfo(consumer: Consumer<List<VocabularySelectDialogFragment.VocabularySelectInfo>>) {
        viewModelScope.launch {
            val result = groupRepository.getAllUnlearningGroups()
            if (result is Result.Success) {
                val infos = result.data.map { group ->
                    VocabularySelectDialogFragment.VocabularySelectInfo(
                        group.id,
                        group.name,
                        group.description
                    )
                }
                consumer.accept(infos)
            }
        }
    }

    fun addNewVocabularyBook(name: String, desc: String, timestamp: Long) {
        viewModelScope.launch {
            groupRepository.addNewOrModifyVocabularyBook(
                Group(
                    id = 0,
                    name = name,
                    description = desc,
                    createTime = timestamp,
                    isLearning = 0
                )
            )
        }
    }

    fun getNewLearningWordCount(): Int = newLearningWordCount
}
