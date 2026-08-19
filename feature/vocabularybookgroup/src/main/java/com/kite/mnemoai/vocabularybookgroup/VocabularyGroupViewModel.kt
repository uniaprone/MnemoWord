package com.kite.mnemoai.vocabularybookgroup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.group.Group
import com.kite.mnemoai.model.repository.GroupRepository
import com.kite.mnemoai.model.repository.WordRepository
import com.kite.mnemoai.model.word.WordItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VocabularyGroupViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val groupRepository: GroupRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableLiveData<VocabularyGroupUIState>()
    val uiState: LiveData<VocabularyGroupUIState> get() = _uiState

    private var group: Group? = null
    private var words: List<WordItem>? = null

    init {
        var groupId: Long = 0
        if (savedStateHandle.contains("group_id") && savedStateHandle.get<Long?>("group_id") != null) {
            groupId = savedStateHandle.get<Long?>("group_id") ?: 0
        }

        viewModelScope.launch {
            groupRepository.observeGroupById(groupId).collect { result ->
                if (result is Result.Success) {
                    group = result.data
                    updateUIState()
                }
            }
        }
        viewModelScope.launch {
            wordRepository.observeWordListByGroupId(groupId).collect { result ->
                if (result is Result.Success) {
                    words = result.data.map { word ->
                        word.copy(
                            phonetic = "/${word.phonetic}/",
                            translation = word.translation.replace("\\n", " ")
                        )
                    }
                    updateUIState()
                }
            }
        }
    }

    private fun updateUIState() {
        val list = words.orEmpty()
        _uiState.value = VocabularyGroupUIState(
            group = group,
            words = words,
            allCount = list.size.toLong(),
            learningCount = list.count { it.reviewState == 0 }.toLong(),
            reviewingCount = list.count { it.reviewState == 1 }.toLong(),
            masteredCount = list.count { it.reviewState == 2 }.toLong(),
        )
    }

    fun getGroup(): Group? = group

    fun modifyVocabularyBook(name: String, desc: String) {
        val currentGroup = group ?: return
        viewModelScope.launch {
            groupRepository.modifyVocabularyBook(
                currentGroup.copy(name = name, description = desc)
            )
        }
    }

    fun setVocabularyBookLearningStatus(status: Int) {
        val currentGroup = group ?: return
        viewModelScope.launch {
            groupRepository.setVocabularyBookLearningStatus(currentGroup.id, status)
        }
    }

    fun deleteGroup() {
        val currentGroup = group ?: return
        viewModelScope.launch {
            groupRepository.deleteGroup(currentGroup.id)
        }
    }
}