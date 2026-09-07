package com.kite.mnemoai.shared_ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.chat.ChatMessage
import com.kite.mnemoai.model.repository.AiMnemonicRepository
import com.kite.mnemoai.model.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wordRepository: WordRepository,
    private val aiMnemonicRepository: AiMnemonicRepository
) : ViewModel() {
    private val wordId: Long = checkNotNull(savedStateHandle[ARG_WORD_ID])

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _sendResult = MutableSharedFlow<Result<String>>()
    val sendResult: SharedFlow<Result<String>> = _sendResult.asSharedFlow()

    init {
        viewModelScope.launch {
            wordRepository.observeChatMessages(wordId).collect { chatMessages ->
                _messages.value = chatMessages
            }
        }
    }

    fun send(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val detailResult = wordRepository.observeWordDetailById(wordId)
                .first { it !is Result.Loading }
            if (detailResult is Result.Error) {
                _sendResult.emit(Result.Error(detailResult.exception))
                return@launch
            }
            val wordDetail = (detailResult as? Result.Success)?.data
            if (wordDetail == null) {
                _sendResult.emit(Result.Error(IllegalArgumentException("当前单词不存在")))
                return@launch
            }
            _sendResult.emit(aiMnemonicRepository.chat(wordDetail, content))
        }
    }

    companion object {
        const val ARG_WORD_ID = "word_id"
    }
}
