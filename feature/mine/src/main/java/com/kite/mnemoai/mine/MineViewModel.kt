package com.kite.mnemoai.mine

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.data.ThemeType
import com.kite.mnemoai.model.request.WordExtractRequest
import com.kite.mnemoai.model.repository.AiMnemonicRepository
import com.kite.mnemoai.model.repository.UserSettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MineViewModel @Inject constructor(
    private val userSettingRepository: UserSettingRepository,
    private val aiMnemonicRepository: AiMnemonicRepository
) : ViewModel() {
    private val _uiStatus = MutableLiveData<MineUIState?>()
    val uIStatus: LiveData<MineUIState?> get() = _uiStatus
    private val _apiTestResult = MutableSharedFlow<Result<String>>()
    val apiTestResult: SharedFlow<Result<String>> = _apiTestResult.asSharedFlow()
    private var dayNightMode: ThemeType = ThemeType.FOLLOW_SYS
    private var apiKey: String? = null

    init {
        viewModelScope.launch {
            userSettingRepository.userSetting.collect { userSetting ->
                dayNightMode = userSetting.lightDarkModel
                apiKey = userSetting.apiKey
                updateUIStatus()
            }
        }
    }

    fun apiKeyTest(apiKey: String) {
        viewModelScope.launch {
            _apiTestResult.emit(Result.Loading)
            val result = aiMnemonicRepository.apiKeyTest(apiKey, WordExtractRequest("apple", false))
            if (result is Result.Success) {
                saveApiKey(apiKey)
            }
            _apiTestResult.emit(result)
        }
    }

    fun saveApiKey(apiKey: String?) {
        viewModelScope.launch {
            userSettingRepository.setApiKey(apiKey ?: "")
        }
    }

    fun saveLightDarkModel(lightDarkModel: ThemeType, setTheme: () -> Unit) {
        viewModelScope.launch {
            userSettingRepository.setLightDarkModel(lightDarkModel)
            setTheme.invoke()
        }
    }

    private fun updateUIStatus() {
        _uiStatus.value = MineUIState(dayNightMode, apiKey)
    }

}
