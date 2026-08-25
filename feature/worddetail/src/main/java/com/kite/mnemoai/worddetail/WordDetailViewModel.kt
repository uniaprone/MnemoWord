package com.kite.mnemoai.worddetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.request.WordExtractRequest
import com.kite.mnemoai.model.repository.AiMnemonicRepository
import com.kite.mnemoai.model.repository.UserSettingRepository
import com.kite.mnemoai.model.repository.WordRepository
import com.kite.mnemoai.model.word.WordDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WordDetailViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val userSettingRepository: UserSettingRepository,
    private val aiMnemonicRepository: AiMnemonicRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<WordDetailUIState?>(null)
    val uiState: StateFlow<WordDetailUIState?> get() = _uiState

    private var apiKey: String? = null
    private var wordDetail: WordDetail? = null
    private var aiMnemonicLoadingState: Result<String>? = null
    private var revision = 0L
    init {
        var wordId: Long = 1
        if (savedStateHandle.contains("word_id")) {
            wordId = savedStateHandle["word_id"] ?: 1
        }

        viewModelScope.launch {
            wordRepository.observeWordDetailById(wordId).collect { result ->
                if (result is Result.Success) {
                    val detail = result.data 
                    wordDetail = detail?.copy(
                        word = detail.word.copy(phonetic = "/${detail.word.phonetic}/")
                    )
                    updateUIState()
                }
            }
        }

        viewModelScope.launch {
            userSettingRepository.userSetting.collect { userSetting ->
                apiKey = userSetting.apiKey
                updateUIState()
            }
        }
    }

    fun fetchWordExtract() {
        aiMnemonicLoadingState = Result.Loading
        updateUIState()
        viewModelScope.launch {
            wordDetail?.let { detail ->
                aiMnemonicLoadingState = aiMnemonicRepository.generateWordExtract(
                    detail,
                    WordExtractRequest(detail.word.word, false)
                )
                updateUIState()
            }
        }
    }

    private fun updateUIState() {
        revision++
        _uiState.value = WordDetailUIState(wordDetail, aiMnemonicLoadingState, apiKey, revision)
    }
}
