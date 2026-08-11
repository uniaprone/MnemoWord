package com.kite.mnemoai.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.ViewModelInitializer
import com.kite.mnemoai.MainApplication
import com.kite.mnemoai.data.model.WordListItem
import com.kite.mnemoai.data.repository.IRepositoryCallback
import com.kite.mnemoai.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(val wordRepository: WordRepository): ViewModel() {
    fun performSearch(searchText: String, callback: IRepositoryCallback<List<WordListItem>>){
        wordRepository.performSearch(searchText, callback)
    }
}