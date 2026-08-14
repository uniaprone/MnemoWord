package com.kite.mnemoai.mine

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.request.WordExtractRequest
import com.kite.mnemoai.model.repository.AiMnemonicRepository
import com.kite.mnemoai.model.repository.UserSettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MineViewModel @Inject constructor(
    private val userSettingRepository: UserSettingRepository,
    private val aiMnemonicRepository: AiMnemonicRepository
) : ViewModel() {
    private val _uiStatus = MutableLiveData<MineUIState?>()
    val uIStatus: LiveData<MineUIState?> get() = _uiStatus

    private var dayNightMode: Int = -1
    private var apiKey: String? = null
    private var apiTestState: Result<String>? = null

    init {
        viewModelScope.launch {
            userSettingRepository.observeUserSetting().collect { result ->
                if (result is Result.Success) {
                    dayNightMode = result.data.lightDarkModel
                    apiKey = result.data.apiKey
                    updateUIStatus()
                }
            }
        }
    }

    fun apiKeyTest(apiKey: String) {
        apiTestState = Result.Loading
        updateUIStatus()
        viewModelScope.launch {
            apiTestState = aiMnemonicRepository.apiKeyTest(apiKey, WordExtractRequest("apple", false))
            if (apiTestState is Result.Success) {
                saveApiKey(apiKey)
            }
            updateUIStatus()
        }
    }

    fun saveApiKey(apiKey: String?) {
        viewModelScope.launch {
            userSettingRepository.setApiKey(apiKey ?: "")
        }
    }

    fun saveLightDarkModel(lightDarkModel: Int) {
        dayNightMode = convertLightDarkModelToOption(lightDarkModel)
        viewModelScope.launch {
            userSettingRepository.setLightDarkModel(lightDarkModel)
        }
        updateUIStatus()
    }

    private fun updateUIStatus() {
        _uiStatus.value = MineUIState(dayNightMode, apiKey, apiTestState)
    }

    fun convertLightDarkModelToOption(lightDarkModel: Int): Int {
        return when (lightDarkModel) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> 0
            AppCompatDelegate.MODE_NIGHT_NO -> 1
            else -> 2
        }
    }
}
