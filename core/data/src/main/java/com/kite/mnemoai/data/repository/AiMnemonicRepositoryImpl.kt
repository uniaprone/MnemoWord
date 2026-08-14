package com.kite.mnemoai.data.repository

import com.google.gson.JsonSyntaxException
import com.kite.mnemoai.common.Dispatcher
import com.kite.mnemoai.common.MaiDispatcher
import com.kite.mnemoai.database.Converters
import com.kite.mnemoai.database.dao.WordExtractDao
import com.kite.mnemoai.database.model.WordExtractEntity
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.repository.AiMnemonicRepository
import com.kite.mnemoai.model.repository.UserSettingRepository
import com.kite.mnemoai.model.request.WordExtractRequest
import com.kite.mnemoai.model.word.WordDetail
import com.kite.mnemoai.network.AiServiceProvider
import com.kite.mnemoai.network.NetworkResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiMnemonicRepositoryImpl @Inject constructor(
    private val wordExtractDao: WordExtractDao,
    private val userSettingRepository: UserSettingRepository,
    private val aiServiceProvider: AiServiceProvider,
    @Dispatcher(MaiDispatcher.IO) private val ioDispatcher: CoroutineDispatcher
) : AiMnemonicRepository {

    override suspend fun apiKeyTest(apiKey: String, request: WordExtractRequest): Result<String> =
        withContext(ioDispatcher) {
            try {
                val networkResult = aiServiceProvider.getWordExtractDataSourceImp()
                    .generateWordExtract(apiKey, request)
                when (networkResult) {
                    is NetworkResult.Success -> Result.Success("测试成功，API Key可用")
                    is NetworkResult.Error -> Result.Error(networkResult.exception)
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun generateWordExtract(
        wordDetail: WordDetail,
        request: WordExtractRequest
    ): Result<String> = withContext(ioDispatcher) {
        try {
            val userSetting = userSettingRepository.getUserSettingSync()
            val networkResult = aiServiceProvider.getWordExtractDataSourceImp()
                .generateWordExtract(userSetting.apiKey, request)
            when (networkResult) {
                is NetworkResult.Success -> {
                    try {
                        val wordExtract = Converters.stringToWordExtract(networkResult.data)
                        val wordExtractEntity = WordExtractEntity(wordDetail.word.id, wordExtract)
                        wordExtractDao.insertWordExtractEntity(wordExtractEntity)
                        Result.Success("生成成功")
                    } catch (e: JsonSyntaxException) {
                        Result.Error(IllegalArgumentException("接收数据格式错误！"))
                    }
                }
                is NetworkResult.Error -> Result.Error(networkResult.exception)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
