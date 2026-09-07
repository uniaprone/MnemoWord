package com.kite.mnemoai.worddetail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.recite.PronounceType
import com.kite.mnemoai.model.repository.AiMnemonicRepository
import com.kite.mnemoai.model.repository.UserSettingRepository
import com.kite.mnemoai.model.repository.WordRepository
import com.kite.mnemoai.model.word.WordDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _aiMnemonicLoadingState = MutableStateFlow<Result<String>?>(null)
    val aiMnemonicLoadingState: StateFlow<Result<String>?> = _aiMnemonicLoadingState.asStateFlow()

    private var apiKey: String? = null
    private var pronounceType: PronounceType = PronounceType.USA
    private var wordDetail: WordDetail? = null
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
                apiKey = userSetting.deepseekSettings.apiKey
                pronounceType = userSetting.pronounceType
                updateUIState()
            }
        }
    }

    fun getWordVoiceUri(): String {
        val word = wordDetail?.word?.word ?: return ""
        val type = when (pronounceType) {
            PronounceType.USA -> 2
            PronounceType.UK -> 1
        }
        return "https://dict.youdao.com/dictvoice?audio=${Uri.encode(word)}&type=$type"
    }

    fun fetchWordExtract() {
        if (wordDetail == null) return
        _aiMnemonicLoadingState.value = Result.Loading
        viewModelScope.launch {
            val detail = wordDetail
            if (detail != null) {
                _aiMnemonicLoadingState.value =
                    aiMnemonicRepository.generateWordExtract(listOf(detail))
                updateUIState()
            }
        }
    }

    private fun updateUIState() {
        revision++
        _uiState.value = WordDetailUIState(wordDetail, apiKey, revision)
    }
}
