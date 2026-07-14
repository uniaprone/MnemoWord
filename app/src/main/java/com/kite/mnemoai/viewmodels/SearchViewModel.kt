package com.kite.mnemoai.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.ViewModelInitializer
import com.kite.mnemoai.MainApplication
import com.kite.mnemoai.data.model.WordListItem
import com.kite.mnemoai.data.repository.IRepositoryCallback
import com.kite.mnemoai.data.repository.WordRepository

class SearchViewModel(val wordRepository: WordRepository): ViewModel() {
    companion object{
        val initializer = ViewModelInitializer(SearchViewModel::class.java){
            val app = this[APPLICATION_KEY] as MainApplication
            SearchViewModel(app.wordRepository)
        }
    }

    fun performSearch(searchText: String, callback: IRepositoryCallback<List<WordListItem>>){
        wordRepository.performSearch(searchText, callback)
    }
}