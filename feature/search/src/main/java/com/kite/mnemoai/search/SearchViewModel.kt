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
