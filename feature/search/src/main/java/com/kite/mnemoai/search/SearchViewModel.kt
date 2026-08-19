package com.kite.mnemoai.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.repository.WordRepository
import com.kite.mnemoai.model.word.WordItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val words: List<WordItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // 新增：搜索输入流（Fragment 每次按键只更新它）
    private val _query = MutableStateFlow("")

    init {
        viewModelScope.launch {
            _query
                .debounce(300L)            // ① 停止输入 300ms 后才继续
                .distinctUntilChanged()    // ② 值没变不重复处理
                .collect { query -> performSearch(query) }
        }
    }

    fun onQueryChanged(newText: String) {
        _query.value = newText            // Fragment 调用这个，而不是 searchWord
    }

    private suspend fun performSearch(query: String) {
        if (query.isEmpty()) {           // ③ 最少输入 1 个字符
            _uiState.value = SearchUiState(words = emptyList())
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        when (val result = wordRepository.searchWord(query)) {
            is Result.Success -> _uiState.value =
                SearchUiState(words = result.data, isLoading = false)
            is Result.Error -> _uiState.value =
                SearchUiState(isLoading = false, error = result.exception.message ?: "Unknown error")
            Result.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
        }
    }
    fun searchWord(word: String) {
        if (word.isBlank()) {
            _uiState.value = SearchUiState(words = emptyList())
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = wordRepository.searchWord(word)) {
                is Result.Success -> {
                    _uiState.value = SearchUiState(
                        words = result.data,
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = SearchUiState(
                        isLoading = false,
                        error = result.exception.message ?: "Unknown error"
                    )
                }
                Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
}
