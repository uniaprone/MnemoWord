package com.kite.mnemoai.data.repository

import android.util.Log
import com.google.gson.JsonSyntaxException
import com.kite.mnemoai.data.local.Converters
import com.kite.mnemoai.data.local.dao.WordExtractDao
import com.kite.mnemoai.data.local.entity.WordExtractEntity
import com.kite.mnemoai.data.model.WordDetailInfo
import com.kite.mnemoai.data.network.AiServiceProvider
import com.kite.mnemoai.data.network.NetworkResult
import com.kite.mnemoai.data.network.WordExtractRequest
import com.kite.mnemoai.di.Dispatcher
import com.kite.mnemoai.di.MaiDispatcher
import com.kite.mnemoai.ui.model.LoadingState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiMnemonicRepository @Inject constructor(
    private val wordExtractDao: WordExtractDao,
    private val userSettingRepository: UserSettingRepository,
    private val aiServiceProvider: AiServiceProvider,
    @param:Dispatcher(MaiDispatcher.IO) private val iODispatcher: CoroutineDispatcher
) {
    suspend fun apiKeyTest(apiKey: String, wordExtractRequest: WordExtractRequest): LoadingState<String>{
        return withContext(iODispatcher){
            val networkResult: NetworkResult<String> = aiServiceProvider.getWordExtractDataSourceImp().generateWordExtract(apiKey, wordExtractRequest)
            when (networkResult) {
                is NetworkResult.Success -> {
                    LoadingState.Success("测试成功，API Key可用")
                }

                is NetworkResult.Error -> {
                    LoadingState.Error(networkResult.exception)
                }
            }
        }
    }

    suspend fun generateWordExtract(wordDetailInfo: WordDetailInfo, wordExtractRequest: WordExtractRequest): LoadingState<String>{
        return withContext(iODispatcher){
            val userSetting = userSettingRepository.getUserSettingSync()
            val networkResult: NetworkResult<String> = aiServiceProvider.getWordExtractDataSourceImp().generateWordExtract(userSetting.apiKey, wordExtractRequest)
            when (networkResult) {
                is NetworkResult.Success -> {
                    Log.d("接收的json数据", networkResult.data + "")
                    try {
                        val wordExtract = Converters.stringToWordExtract(networkResult.data)
                        val wordExtractEntity =
                            WordExtractEntity(wordDetailInfo.wordEntity.id, wordExtract)
                        wordExtractDao.insertWordExtractEntity(wordExtractEntity)
                        LoadingState.Success("测试成功，API Key可用")
                    }catch (e: JsonSyntaxException){
                        LoadingState.Error(IllegalArgumentException("接收数据格式错误！"))
                    }
                }

                is NetworkResult.Error -> {
                    LoadingState.Error(networkResult.exception)
                }
            }
        }
    }
}