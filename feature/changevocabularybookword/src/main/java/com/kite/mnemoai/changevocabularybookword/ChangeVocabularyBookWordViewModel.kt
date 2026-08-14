package com.kite.mnemoai.changevocabularybookword.ui.changevocabularybookword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kite.mnemoai.model.word.WordItem
import com.kite.mnemoai.model.repository.GroupRepository
import com.kite.mnemoai.model.repository.WordRepository
import com.kite.mnemoai.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeVocabularyBookWordViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val groupRepository: GroupRepository,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private val _uiStatus: MutableLiveData<ChangeVocabularyBookWordUIState> = MutableLiveData()
    val uiStatus: LiveData<ChangeVocabularyBookWordUIState> get() = _uiStatus

    private var groupId: Long = -1
    private var operationType = ChangeVocabularyBookWordUIState.ChangeType.ADD
    private var searchText: String = ""
    private var optionalWords = mutableListOf<WordItem>()
    private val alterWords = mutableListOf<VocabularyBookChangedWord>()

    init {
        groupId = savedStateHandle.get<Long>("group_id") ?: -1
        performOptionWordsSearch()
    }

    fun setOperationType(type: ChangeVocabularyBookWordUIState.ChangeType) {
        this.operationType = type
        this.optionalWords.clear()
        updateUIStatus()
        performOptionWordsSearch()
    }

    fun setSearchText(searchText: String) {
        this.searchText = searchText
        performOptionWordsSearch()
    }

    fun addAlterWords(wordItem: WordItem) {
        val changedWord = VocabularyBookChangedWord(
            wordItem.id,
            wordItem.word,
            wordItem.phonetic,
            wordItem.translation,
            wordItem.reviewState,
            this.operationType
        )
        alterWords.add(changedWord)
        optionalWords.remove(wordItem)
        updateUIStatus()
    }

    fun removeAlterWords(vocabularyBookChangedWord: VocabularyBookChangedWord) {
        val changeWord = WordItem(
            vocabularyBookChangedWord.id,
            vocabularyBookChangedWord.word,
            vocabularyBookChangedWord.phonetic ?: "",
            vocabularyBookChangedWord.translation ?: "",
            vocabularyBookChangedWord.reviewState
        )
        alterWords.remove(vocabularyBookChangedWord)
        optionalWords.add(changeWord)
        updateUIStatus()
    }

    private fun performOptionWordsSearch() {
        viewModelScope.launch {
            val result = when (operationType) {
                ChangeVocabularyBookWordUIState.ChangeType.ADD ->
                    wordRepository.addOptionWordsSearch(groupId, searchText)
                ChangeVocabularyBookWordUIState.ChangeType.REMOVE ->
                    wordRepository.removeOptionWordsSearch(groupId, searchText)
            }
            if (result is Result.Success) {
                val idsToExclude = alterWords.map { it.id }.toSet()
                optionalWords = result.data.filter { it.id !in idsToExclude }.toMutableList()
                updateUIStatus()
            }
        }
    }

    fun applyAlterWords() {
        viewModelScope.launch {
            val addIds = alterWords.filter { it.operation == ChangeVocabularyBookWordUIState.ChangeType.ADD }.map { it.id }
            val removeIds = alterWords.filter { it.operation == ChangeVocabularyBookWordUIState.ChangeType.REMOVE }.map { it.id }
            groupRepository.addAlterWords(groupId, addIds)
            groupRepository.removeAlterWords(groupId, removeIds)
        }
    }

    private fun updateUIStatus() {
        _uiStatus.value = ChangeVocabularyBookWordUIState(groupId, operationType, searchText, optionalWords, alterWords)
    }
}
