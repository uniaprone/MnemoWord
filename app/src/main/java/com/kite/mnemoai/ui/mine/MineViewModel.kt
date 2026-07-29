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
import kotlinx.coroutines.launch

class MineViewModel(
    app: Application,
    private val wordRepository: WordRepository,
    private val userSettingRepository: UserSettingRepository,
    private val aiMnemonicRepository: AiMnemonicRepository
) : ViewModel() {
    private val _uiStatus = MediatorLiveData<MineUIState?>()
    private val mineBaseItems: MutableList<MineBaseItem?> = ArrayList<MineBaseItem?>()
    private var apiTestState: LoadingState<String?>? = null

    private fun convertLightDarkModelToOption(lightDarkModel: Int): Int {
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

    private fun getThemeLastSelectedIndex(title: String?, lastSelectedIndex: Int): Int {
        for (mineBaseItem in mineBaseItems) {
            if (mineBaseItem is MineSelectorItem) {
                if (mineBaseItem.title == title) {
                    return mineBaseItem.selected
                }
            }
        }
        return lastSelectedIndex
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
        _uiStatus.value = MineUIState(mineBaseItems, apiTestState)
    }

    val uIStatus: LiveData<MineUIState?>
        get() = _uiStatus

    private fun saveLightDarkModel(index: Int) {
        userSettingRepository.setLightDarkModel(index)
    }

    init {
        _uiStatus.addSource<UserSetting?>(
            userSettingRepository.userSettingLiveData,
            Observer { userSetting: UserSetting? ->
                val newList: MutableList<MineBaseItem?> = ArrayList<MineBaseItem?>()
                val themeSetting = MineSelectorItem(
                    app.resources.getString(R.string.light_dark_model),
                    listOf<String?>(
                        app.getResources().getString(R.string.fallow_system),
                        app.getResources().getString(R.string.light_model),
                        app.getResources().getString(R.string.dark_model)
                    ),
                    convertLightDarkModelToOption(userSetting!!.lightDarkModel),
                    getThemeLastSelectedIndex(
                        app.getResources().getString(R.string.light_dark_model),
                        convertLightDarkModelToOption(userSetting.lightDarkModel)
                    ),
                    OnOptionSelectedListener { index: Int ->
                        val lightDarkModel = userSetting.lightDarkModel
                        if (index == 0) {
                            if (lightDarkModel == -1) return@OnOptionSelectedListener
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                            saveLightDarkModel(-1)
                        } else if (index == 1) {
                            if (lightDarkModel == 1) return@OnOptionSelectedListener
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            saveLightDarkModel(1)
                        } else {
                            if (lightDarkModel == 2) return@OnOptionSelectedListener
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            saveLightDarkModel(2)
                        }
                    })
                newList.add(0, themeSetting)

                val apiKeySetting = MineTextItem(
                    app.getResources().getString(R.string.setting_title_api_key),
                    userSetting.apiKey
                )
                newList.add(1, apiKeySetting)

                mineBaseItems.clear()
                mineBaseItems.addAll(newList)
                updateUIStatus()
            })
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[AndroidViewModelFactory.APPLICATION_KEY] as MainApplication
                MineViewModel(app, app.wordRepository, app.userSettingRepository, app.aiMnemonicRepository)
            }
        }

    }
}
