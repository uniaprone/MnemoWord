package com.kite.mnemoai.data.repository

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.kite.mnemoai.common.Dispatcher
import com.kite.mnemoai.common.MaiDispatcher
import com.kite.mnemoai.data.model.asEntity
import com.kite.mnemoai.database.dao.WordExtractDao
import com.kite.mnemoai.database.model.WordExtract
import com.kite.mnemoai.database.model.WordExtractEntity
import com.kite.mnemoai.data.model.toAiChatRequest
import com.kite.mnemoai.data.model.toChatContextText
import com.kite.mnemoai.database.dao.ChatMessageDao
import com.kite.mnemoai.database.model.asExternalModel
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.chat.ChatMessage
import com.kite.mnemoai.model.chat.ChatRole
import com.kite.mnemoai.model.data.AiPromptType
import com.kite.mnemoai.model.data.AiPromptType.WORD_EXTRACT
import com.kite.mnemoai.model.data.UserSetting
import com.kite.mnemoai.model.repository.AiMnemonicRepository
import com.kite.mnemoai.model.repository.UserSettingRepository
import com.kite.mnemoai.model.word.WordDetail
import com.kite.mnemoai.network.AiChatRequest
import com.kite.mnemoai.network.AiServiceProvider
import com.kite.mnemoai.network.NetworkResult
import com.kite.mnemoai.network.VoiceDataSource
import com.kite.mnemoai.network.deepseek.model.ResponseFormat
import com.kite.mnemoai.network.deepseek.model.DeepseekModelsResponse
import com.kite.mnemoai.network.deepseek.model.DeepseekModelInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiMnemonicRepositoryImpl @Inject constructor(
    private val wordExtractDao: WordExtractDao,
    private val userSettingRepository: UserSettingRepository,
    private val aiServiceProvider: AiServiceProvider,
    private val voiceDataSource: VoiceDataSource,
    private val chatMessageDao: ChatMessageDao,
    @Dispatcher(MaiDispatcher.IO) private val ioDispatcher: CoroutineDispatcher
) : AiMnemonicRepository {
    private val gson = Gson()

    private sealed interface RoundResult {
        data class Processed(val successEntities: List<WordExtractEntity>, val failedWords: List<String>) : RoundResult
        data class RequestFailed(val error: Throwable) : RoundResult
    }

    override suspend fun apiKeyTest(apiKey: String): Result<String> =
        withContext(ioDispatcher) {
            try {
                val userSetting = userSettingRepository.userSetting.first()
                val userMessage = ChatMessage(
                    0, 0, ChatRole.USER,
                    wordExtractUserContent(userSetting, listOf("apple").toString()),
                    0
                )
                val request = userSetting.toAiChatRequest(
                    systemPrompt = userSetting.aiPrompts[WORD_EXTRACT]?.systemPrompt.orEmpty(),
                    userMessage = listOf(userMessage),
                    apiKey = apiKey
                )
                val networkResult = aiServiceProvider.getNetworkDataSourceImp()
                    .getChatResult(request)
                when (networkResult) {
                    is NetworkResult.Success -> Result.Success("测试成功，API Key可用")
                    is NetworkResult.Error -> Result.Error(networkResult.exception)
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun generateWordExtract(wordDetails: List<WordDetail>): Result<String> =
        withContext(ioDispatcher) {
            try {
                if (wordDetails.isEmpty()) return@withContext Result.Error(IllegalArgumentException("单词列表为空"))
                val userSetting = userSettingRepository.userSetting.first()
                val total = wordDetails.size
                val maxRetries = 3
                var retriesUsed = 0
                var pending = wordDetails
                val successEntities = mutableListOf<WordExtractEntity>()
                var lastError: Throwable? = null
                val systemPrompt = userSetting.aiPrompts[WORD_EXTRACT]?.systemPrompt.orEmpty()

                fun runRound(words: List<WordDetail>) {
                    val userMessage = ChatMessage(
                        0, 0, ChatRole.USER,
                        wordExtractUserContent(
                            userSetting,
                            words.map { it.word.word }.toList().toString()
                        ),
                        0
                    )
                    val roundRequest = userSetting.toAiChatRequest(
                        systemPrompt = systemPrompt,
                        userMessage = listOf(userMessage),
                    )
                    when (val result = requestRound(roundRequest, words)) {
                        is RoundResult.Processed -> {
                            successEntities += result.successEntities
                            pending = words.filter { it.word.word in result.failedWords }
                        }
                        is RoundResult.RequestFailed -> lastError = result.error
                    }
                }

                runRound(pending)
                while (retriesUsed < maxRetries && pending.isNotEmpty()) {
                    retriesUsed++
                    runRound(pending)
                }

                if (successEntities.isNotEmpty()) {
                    successEntities.forEach { wordExtractDao.insertWordExtractEntity(it) }
                    Result.Success(
                        "生成完成：共 $total 个，成功 ${successEntities.size} 个，" +
                            "失败 ${total - successEntities.size} 个"
                    )
                } else {
                    Result.Error(lastError ?: IllegalArgumentException("生成失败：失败 $total 个"))
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun chat(wordDetail: WordDetail, content: String): Result<String> =
        withContext(ioDispatcher){
            val currentMessage = ChatMessage(
                0, wordDetail.word.id,
                ChatRole.USER, content, System.currentTimeMillis()
            )
            chatMessageDao.insertChatMessage(currentMessage.asEntity())

            val userSetting = userSettingRepository.userSetting.first()
            val historyChatMessages = chatMessageDao.getChatMessagesByWordId(wordDetail.word.id).first().map { it.asExternalModel() }
            val contextText = wordDetail.toChatContextText()
            val systemPrompt = buildString {
                append(userSetting.aiPrompts[AiPromptType.CHAT]?.systemPrompt.orEmpty())
                if (contextText.isNotBlank()) {
                    append("\n\n当前单词信息：\n")
                    append(contextText)
                }
            }
            val networkResult = aiServiceProvider.getNetworkDataSourceImp().getChatResult(
                request = userSetting.toAiChatRequest(
                    systemPrompt = systemPrompt,
                    userMessage = historyChatMessages,
                    responseFormat = ResponseFormat.TEXT
                )
            )
            when (networkResult) {
                is NetworkResult.Error -> Result.Error(networkResult.exception)
                is NetworkResult.Success -> {
                    val chatMessage = ChatMessage(
                        wordId = wordDetail.word.id,
                        role = ChatRole.ASSISTANT,
                        content = networkResult.data,
                        timestamp = System.currentTimeMillis()
                    )
                    chatMessageDao.insertChatMessage(chatMessage.asEntity())
                    Result.Success("")
                }
            }
        }

    override suspend fun getModelList(): Result<List<String>> = withContext(ioDispatcher) {
        val userSetting = userSettingRepository.userSetting.first()
        val netWordResult = aiServiceProvider.getNetworkDataSourceImp()
            .getModelList(userSetting.deepseekSettings.apiKey)
        when (netWordResult) {
            is NetworkResult.Success -> {
                val models = netWordResult.data?.modelInfos?.map { it.id }?.toList()
                if (models.isNullOrEmpty()) {
                    Result.Error(IllegalStateException("模型列表获取失败"))
                } else {
                    Result.Success(models)
                }
            }
            is NetworkResult.Error -> Result.Error(netWordResult.exception)
        }
    }

    override suspend fun getVoice(word: String, id: Int) = withContext(ioDispatcher){
        val networkResult = voiceDataSource.getVoice(word, id)
        when(networkResult){
            is NetworkResult.Success -> Result.Success(networkResult.data)
            is NetworkResult.Error -> Result.Error(networkResult.exception)
        }
    }

    private fun wordExtractUserContent(userSetting: UserSetting, wordsJson: String): String {
        val requirement = userSetting.aiPrompts[WORD_EXTRACT]?.userPrompt?.takeIf { it.isNotBlank() }
        return if (requirement == null) {
            wordsJson
        } else {
            "$wordsJson\n用户要求：$requirement"
        }
    }

    private fun requestRound(
        request: AiChatRequest,
        words: List<WordDetail>
    ): RoundResult {
        val networkResult = aiServiceProvider.getNetworkDataSourceImp()
            .getChatResult(request)
        return when (networkResult) {
            is NetworkResult.Error -> RoundResult.RequestFailed(networkResult.exception)
            is NetworkResult.Success -> {
                try {
                    val extracts = stringToWordExtractList(networkResult.data)
                    val extractMap = extracts.associateBy { it.word }
                    val successEntities = mutableListOf<WordExtractEntity>()
                    val failedWords = mutableListOf<String>()
                    words.forEach { detail ->
                        val extract = extractMap[detail.word.word]
                        if (extract != null) {
                            successEntities.add(WordExtractEntity(detail.word.id, extract))
                        } else {
                            failedWords.add(detail.word.word)
                        }
                    }
                    RoundResult.Processed(successEntities, failedWords)
                } catch (e: JsonSyntaxException) {
                    RoundResult.RequestFailed(IllegalArgumentException("接收数据格式错误！"))
                }
            }
        }
    }

    private fun stringToWordExtractList(extractJson: String): List<WordExtract> {
        val listType = object : TypeToken<List<WordExtract?>?>() {}.type
        return gson.fromJson(extractJson, listType)
    }
}
