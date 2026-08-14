package com.kite.mnemoai.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _uiState: MutableLiveData<MainUIState> = MutableLiveData()
    val uiState: LiveData<MainUIState> get() = _uiState
    private var title: String = ""
    private var isShowNavIcon = false

    fun settitle(title: String) {
        this.title = title
        updateUIState()
    }

    fun setShowNavIcon(isShow: Boolean) {
        this.isShowNavIcon = isShow
        updateUIState()
    }

    private fun updateUIState() {
        _uiState.value = MainUIState(title, isShowNavIcon)
    }
}
