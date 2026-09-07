package com.kite.mnemoai.mine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kite.mnemoai.model.data.AiPromptSet
import com.kite.mnemoai.model.data.AiPromptType.CHAT
import com.kite.mnemoai.model.data.AiPromptType.WORD_EXTRACT
import com.kite.mnemoai.model.repository.UserSettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PromptEditViewModel @Inject constructor(
    private val userSettingRepository: UserSettingRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<PromptEditUIState?>(null)
    val uiState get() = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userSettingRepository.userSetting.collect { userSetting ->
                val wordExtract = userSetting.aiPrompts[WORD_EXTRACT]
                val chat = userSetting.aiPrompts[CHAT]
                _uiState.value = PromptEditUIState(
                    wordExtractSystemPrompt = wordExtract?.systemPrompt.orEmpty(),
                    wordExtractUserPrompt = wordExtract?.userPrompt.orEmpty(),
                    chatSystemPrompt = chat?.systemPrompt.orEmpty(),
                    chatUserPrompt = chat?.userPrompt.orEmpty()
                )
            }
        }
    }

    fun save(
        wordExtractSystem: String,
        wordExtractUser: String,
        chatSystem: String,
        chatUser: String
    ) {
        viewModelScope.launch {
            userSettingRepository.setAiPrompts(
                mapOf(
                    WORD_EXTRACT to AiPromptSet(wordExtractSystem, wordExtractUser),
                    CHAT to AiPromptSet(chatSystem, chatUser)
                )
            )
        }
    }
}
