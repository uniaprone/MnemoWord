package com.kite.mnemoai.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.ViewModelInitializer
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

class MainViewModel: ViewModel() {
    private val _uiState: MutableLiveData<MainUIState> = MutableLiveData<MainUIState>()
    val uiState: LiveData<MainUIState> get() = _uiState
    private var title: String = "1"
    private var isShowNavIcon = false

    fun settitle(title: String){
        this.title = title
        updateUIState()
    }

    fun setShowNavIcon(isShow: Boolean){
        this.isShowNavIcon = isShow
        updateUIState()
    }
    fun updateUIState(){
        _uiState.value = MainUIState(title, isShowNavIcon)
    }


}