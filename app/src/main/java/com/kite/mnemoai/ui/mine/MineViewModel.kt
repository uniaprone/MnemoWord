package com.kite.mnemoai.ui.mine

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kite.mnemoai.MainApplication
import com.kite.mnemoai.R
import com.kite.mnemoai.data.local.UserSetting
import com.kite.mnemoai.data.network.WordExtractRequest
import com.kite.mnemoai.data.repository.AiMnemonicRepository
import com.kite.mnemoai.data.repository.UserSettingRepository
import com.kite.mnemoai.data.repository.WordRepository
import com.kite.mnemoai.ui.mine.model.MineBaseItem
import com.kite.mnemoai.ui.mine.model.MineSelectorItem
import com.kite.mnemoai.ui.mine.model.MineSelectorItem.OnOptionSelectedListener
import com.kite.mnemoai.ui.mine.model.MineTextItem
import com.kite.mnemoai.ui.model.LoadingState
import com.kite.mnemoai.ui.mine.model.ThemeType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MineViewModel @Inject constructor(
    private val userSettingRepository: UserSettingRepository,
    private val aiMnemonicRepository: AiMnemonicRepository
) : ViewModel() {
    private val _uiStatus = MediatorLiveData<MineUIState?>()
    private var dayNightMode: Int = -1
    private var apiKey: String? = null
    private var apiTestState: LoadingState<String?>? = null

    init {
        _uiStatus.addSource(
            userSettingRepository.userSettingLiveData,
            Observer { userSetting: UserSetting ->
                dayNightMode = userSetting.lightDarkModel
                apiKey = userSetting.apiKey
                updateUIStatus()
            })
    }

    fun apiKeyTest(apiKey: String) {
        apiTestState = LoadingState.Loading
        updateUIStatus()
        viewModelScope.launch {
            apiTestState = aiMnemonicRepository.apiKeyTest(apiKey, WordExtractRequest("apple", false))
            if (apiTestState is LoadingState.Success<*>) {
                saveApiKey(apiKey)
            }
            updateUIStatus()
        }
    }

    fun saveApiKey(apiKey: String?) {
        userSettingRepository.setApiKey(apiKey)
    }

    private fun updateUIStatus() {
        _uiStatus.value = MineUIState(dayNightMode, apiKey, apiTestState)
    }

    val uIStatus: LiveData<MineUIState?>
        get() = _uiStatus

    fun saveLightDarkModel(lightDarkModel: Int) {
        dayNightMode = convertLightDarkModelToOption(lightDarkModel)
        userSettingRepository.setLightDarkModel(lightDarkModel)
        updateUIStatus()
    }

    fun convertLightDarkModelToOption(lightDarkModel: Int): Int {
        return when (lightDarkModel) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> {
                0
            }
            AppCompatDelegate.MODE_NIGHT_NO -> {
                1
            }
            else -> {
                2
            }
        }
    }
}
