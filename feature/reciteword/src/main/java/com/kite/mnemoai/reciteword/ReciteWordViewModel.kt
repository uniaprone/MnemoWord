package com.kite.mnemoai.reciteword

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kite.domain.RememberWordUseCase
import com.kite.domain.SetDailyReciteWordUseCase
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.repository.AiMnemonicRepository
import com.kite.mnemoai.model.repository.ReciteStatistics
import com.kite.mnemoai.model.repository.StatisticsRepository
import com.kite.mnemoai.model.repository.UserSettingRepository
import com.kite.mnemoai.model.repository.WordRepository
import com.kite.mnemoai.model.request.WordExtractRequest
import com.kite.mnemoai.model.word.WordDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Random
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@HiltViewModel
class ReciteWordViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val statisticsRepository: StatisticsRepository,
    private val userSettingRepository: UserSettingRepository,
    private val aiMnemonicRepository: AiMnemonicRepository,
    private val rememberWordUseCase: RememberWordUseCase,
    private val setDailyReciteWordUseCase: SetDailyReciteWordUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<ReciteWordUIState?>(null)
    val uiState: StateFlow<ReciteWordUIState?> = _uiState.asStateFlow()
    private var reciteStage: ReciteStage? = null
    private var reciteWordDetailInfoItemUIState: MutableList<WordDetail>? = null
    private var reciteStatistics: MutableList<ReciteStatistics> = mutableListOf()
    private var totalProgress = 0
    private var currentProgress = 0
    private val _showNext = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val showNext: SharedFlow<Unit> = _showNext.asSharedFlow()
    private var isShowTranslation = false
    private var isShowDetail = false
    private var aiMnemonicLoadingState: Result<String>? = null
    private var revision = 0L
    private val random = Random()
    private val date: LocalDate = LocalDate.now()
    private var apiKey: String? = null

    init {
        viewModelScope.launch {
            wordRepository.observeDailyReciteStatus().collect { result ->
                if (result is Result.Success) {
                    reciteStage = when (result.data) {
                        0 -> ReciteStage.NO_VOCABULARY
                        1 -> ReciteStage.FINISH
                        2 -> ReciteStage.IN_PROGRESS
                        else -> null
                    }
                    updateUIStatus()
                }
            }
        }
        viewModelScope.launch {
            userSettingRepository.userSetting.collect { result ->
                apiKey = result.apiKey
                updateUIStatus()
            }
        }
        viewModelScope.launch {
            wordRepository.observeUnfinishedWordDetailInfos().collect { result ->
                if (result is Result.Success) {
                    val formatted = result.data.map { detail -> formatWordDetail(detail) }
                    updateOrderData(formatted.toMutableList())
                    updateUIStatus()
                }
            }
        }
        viewModelScope.launch {
            statisticsRepository.observeAllPlanCountByDate(date.toString()).collect { result ->
                if (result is Result.Success) {
                    totalProgress = result.data
                    updateUIStatus()
                }
            }
        }
        viewModelScope.launch {
            statisticsRepository.observeFinishedPlanCountByDate(date.toString()).collect { result ->
                if (result is Result.Success) {
                    currentProgress = result.data
                    updateUIStatus()
                }
            }
        }
    }

    private fun formatWordDetail(detail: WordDetail): WordDetail {
        val updatedWord = detail.word.copy(phonetic = "\\${detail.word.phonetic}\\")
        val sortedTranslations = detail.translations.sorted()
        val updatedTranslations = sortedTranslations.map { translation ->
            translation.copy(
                meanings = translation.meanings.map { meaning ->
                    meaning.copy(meaning = "${meaning.meaning};")
                }
            )
        }
        return detail.copy(word = updatedWord, translations = updatedTranslations)
    }

    private fun updateOrderData(newPlans: MutableList<WordDetail>) {
        if (this.reciteWordDetailInfoItemUIState == null) {
            this.reciteWordDetailInfoItemUIState = newPlans
        } else {
            val newPlanMap = newPlans.associateBy { it.word.id }

            // 1. 旧列表中存在、新列表中不存在 → 移除
            val iterator = this.reciteWordDetailInfoItemUIState!!.iterator()
            while (iterator.hasNext()) {
                val wordDetailInfo = iterator.next()
                if (newPlanMap[wordDetailInfo.word.id] == null) {
                    iterator.remove()
                    _showNext.tryEmit(Unit)
                    resetShowState()
                }
            }

            val oldWordDetailInfoMap =
                this.reciteWordDetailInfoItemUIState!!.associateBy { it.word.id }

            // 2. 新列表中存在、旧列表中不存在 → 插入；存在 → 更新字段
            for (newPlan in newPlans) {
                val wordId = newPlan.word.id
                val oldStatus = oldWordDetailInfoMap[wordId]
                if (oldStatus == null) {
                    val orderSize = this.reciteWordDetailInfoItemUIState!!.size
                    if (orderSize == 0) {
                        this.reciteWordDetailInfoItemUIState!!.add(newPlan)
                    } else {
                        val insertIndex = random.nextInt(orderSize) + 1
                        this.reciteWordDetailInfoItemUIState!!.add(insertIndex, newPlan)
                    }
                    _showNext.tryEmit(Unit)
                    resetShowState()
                } else {
                    val index = this.reciteWordDetailInfoItemUIState!!.indexOf(oldStatus)
                    if (index >= 0) {
                        this.reciteWordDetailInfoItemUIState!![index] = oldStatus.copy(
                            word = newPlan.word,
                            extract = newPlan.extract,
                            dayPlanWords = newPlan.dayPlanWords
                        )
                    }
                }
            }
        }
    }

    fun setDailyDayPlanWordEntities() {
        viewModelScope.launch {
            setDailyReciteWordUseCase()
        }
    }

    private fun updateUIStatus() {
        revision++
        _uiState.value = ReciteWordUIState(
            reciteStage,
            reciteWordDetailInfoItemUIState,
            totalProgress,
            currentProgress,
            isShowTranslation,
            isShowDetail,
            aiMnemonicLoadingState,
            revision
        )
    }

    fun showAll() {
        this.isShowTranslation = true
        this.isShowDetail = true
        updateUIStatus()
    }

    fun resetShowState() {
        this.isShowTranslation = false
        this.isShowDetail = false
        this.aiMnemonicLoadingState = null
        updateUIStatus()
    }

    fun fetchWordExtract() {
        if (this.reciteWordDetailInfoItemUIState.isNullOrEmpty()) return
        val currentWord = this.reciteWordDetailInfoItemUIState!![0]
        aiMnemonicLoadingState = Result.Loading
        updateUIStatus()
        viewModelScope.launch {
            aiMnemonicLoadingState = aiMnemonicRepository.generateWordExtract(
                currentWord,
                WordExtractRequest(currentWord.word.word, false)
            )
            updateUIStatus()
        }
    }

    fun rememberWord() {
        if (this.reciteWordDetailInfoItemUIState.isNullOrEmpty()) return
        val remembered = this.reciteWordDetailInfoItemUIState!![0]
        resetShowState()
        this.reciteWordDetailInfoItemUIState!!.remove(remembered)
        ensureReciteStatistics(remembered.word.id)
        val rememberedReciteStatistics = reciteStatistics.first { rs -> rs.wordId == remembered.word.id }
        updateReciteStatistics(remembered.word.id, 2)
        viewModelScope.launch {
            rememberWordUseCase(rememberedReciteStatistics)
        }
        _showNext.tryEmit(Unit)
        updateUIStatus()
    }

    fun blurWord() {
        if (this.reciteWordDetailInfoItemUIState.isNullOrEmpty()) return
        val blured = this.reciteWordDetailInfoItemUIState!![0]
        resetShowState()

        this.reciteWordDetailInfoItemUIState!!.remove(blured)
        val size = this.reciteWordDetailInfoItemUIState!!.size

        if (size == 0) {
            this.reciteWordDetailInfoItemUIState!!.add(blured)
            updateUIStatus()
            return
        }

        var minPos = (0.6 * size).toInt()
        var maxPos = size
        minPos = max(0, minPos)
        maxPos = min(size, maxPos)
        if (minPos > maxPos) minPos = maxPos

        val position = minPos + random.nextInt(maxPos - minPos + 1)
        this.reciteWordDetailInfoItemUIState!!.add(position, blured)
        _showNext.tryEmit(Unit)
        ensureReciteStatistics(blured.word.id)
        updateReciteStatistics(blured.word.id, 1)
        updateUIStatus()
    }

    fun forgetWord() {
        if (this.reciteWordDetailInfoItemUIState.isNullOrEmpty()) return
        val forgot = this.reciteWordDetailInfoItemUIState!![0]
        resetShowState()

        this.reciteWordDetailInfoItemUIState!!.remove(forgot)
        val size = this.reciteWordDetailInfoItemUIState!!.size

        if (size == 0) {
            this.reciteWordDetailInfoItemUIState!!.add(forgot)
            updateUIStatus()
            return
        }

        var minPos = (0.3 * size).toInt()
        var maxPos = (0.6 * size).toInt()
        minPos = max(0, minPos)
        maxPos = min(size, maxPos)
        if (minPos > maxPos) minPos = maxPos

        val position = minPos + random.nextInt(maxPos - minPos + 1)
        this.reciteWordDetailInfoItemUIState!!.add(position, forgot)
        _showNext.tryEmit(Unit)
        ensureReciteStatistics(forgot.word.id)
        updateReciteStatistics(forgot.word.id, 0)
        updateUIStatus()
    }

    /**
     * 确保指定单词的学习统计项已建立。
     * 修复竞态：进程重建后 Room 数据流晚于 onStart 到达时，
     * startReciteStatistics 会因列表为空提前返回，导致 reciteStatistics 缺失当前词统计项，
     * 进而使 rememberWord 的 first{} 抛 NoSuchElementException、blur/forget 的计数丢失。
     */
    private fun ensureReciteStatistics(wordId: Long) {
        if (reciteStatistics.none { it.wordId == wordId }) {
            reciteStatistics.add(
                ReciteStatistics(
                    wordId = wordId,
                    startLearningTime = SystemClock.elapsedRealtime()
                )
            )
        }
    }

    fun startReciteStatistics() {
        if (this.reciteWordDetailInfoItemUIState.isNullOrEmpty()) return
        val currentWordId = this.reciteWordDetailInfoItemUIState!![0].word.id
        val existing = reciteStatistics.firstOrNull { rs -> rs.wordId == currentWordId }
        if (existing != null) {
            existing.startLearningTime = SystemClock.elapsedRealtime()
        } else {
            reciteStatistics.add(
                ReciteStatistics(
                    wordId = currentWordId,
                    startLearningTime = SystemClock.elapsedRealtime()
                )
            )
        }
    }

    fun updateReciteStatistics(wordId: Long, type: Int) {
        for (currentReciteStatistics in this.reciteStatistics) {
            if (currentReciteStatistics.wordId == wordId) {
                when (type) {
                    0 -> {
                        currentReciteStatistics.addBlurCount()
                        currentReciteStatistics.addLearningTime(SystemClock.elapsedRealtime() - currentReciteStatistics.startLearningTime)
                    }
                    1 -> {
                        currentReciteStatistics.addForgetCount()
                        currentReciteStatistics.addLearningTime(SystemClock.elapsedRealtime() - currentReciteStatistics.startLearningTime)
                    }
                    2, 3 -> {
                        currentReciteStatistics.addLearningTime(SystemClock.elapsedRealtime() - currentReciteStatistics.startLearningTime)
                    }
                }
            }
        }
    }

    fun stopReciteStatistics() {
        if (this.reciteWordDetailInfoItemUIState.isNullOrEmpty()) return
        val currentWordId = this.reciteWordDetailInfoItemUIState!![0].word.id
        updateReciteStatistics(currentWordId, 3)
    }
}
