package com.kite.mnemoai.ui.worddetail

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.ViewModelInitializer
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kite.mnemoai.MainApplication
import com.kite.mnemoai.data.local.UserSetting
import com.kite.mnemoai.data.local.entity.WordExtractEntity
import com.kite.mnemoai.data.local.entity.WordMeaningEntity
import com.kite.mnemoai.data.model.WordDetailInfo
import com.kite.mnemoai.data.model.WordTranslation
import com.kite.mnemoai.data.network.WordExtractRequest
import com.kite.mnemoai.data.repository.AiMnemonicRepository
import com.kite.mnemoai.data.repository.UserSettingRepository
import com.kite.mnemoai.data.repository.WordRepository
import com.kite.mnemoai.ui.model.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.function.Consumer
import javax.inject.Inject

@HiltViewModel
class WordDetailViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val userSettingRepository: UserSettingRepository,
    private val aiMnemonicRepository: AiMnemonicRepository,
    savedStateHandle: SavedStateHandle?
) : ViewModel() {
    @JvmField
    val uiState: MediatorLiveData<WordDetailUIState?> = MediatorLiveData<WordDetailUIState?>()
    private var apiKey: String? = null
    private var wordDetailInfo: WordDetailInfo? = null
    private var aiMnemonicLoadingState: LoadingState<String?>? = null
    private fun updateUIState() {
        uiState.value = WordDetailUIState(wordDetailInfo, aiMnemonicLoadingState, apiKey)
    }

    fun fetchWordExtract() {
        this.aiMnemonicLoadingState = LoadingState.Loading
        updateUIState()
        viewModelScope.launch {
            wordDetailInfo?.let {
                aiMnemonicLoadingState = aiMnemonicRepository.generateWordExtract(it, WordExtractRequest(it.wordEntity.word + "", false))
                updateUIState()
            }
        }
    }

    init {
        var wordId: Long = 1
        if (savedStateHandle != null && savedStateHandle.contains("word_id")) {
            wordId = savedStateHandle.get<Long?>("word_id")!!
        }
        uiState.addSource<WordDetailInfo?>(
            wordRepository.getWordDetailInfoLiveDataById(wordId),
            Observer { wordDetailInfo: WordDetailInfo? ->
                val phonetic = "\\" + wordDetailInfo!!.wordEntity.phonetic + "\\"
                wordDetailInfo.wordEntity.phonetic = phonetic

                wordDetailInfo.wordTranslation.forEach(Consumer { wordTranslation: WordTranslation? ->
                    wordTranslation!!.wordMeanings.forEach(
                        Consumer { wordMeaningEntity: WordMeaningEntity? ->
                            wordMeaningEntity!!.meaning = wordMeaningEntity.meaning + ";"
                        })
                }
                )
                wordDetailInfo.wordTranslation.sort()

                this.wordDetailInfo = wordDetailInfo
                updateUIState()
            })

        uiState.addSource<UserSetting?>(
            userSettingRepository.userSettingLiveData,
            Observer { userSetting: UserSetting? ->
                this.apiKey = userSetting!!.apiKey
                updateUIState()
            })
    }

}
