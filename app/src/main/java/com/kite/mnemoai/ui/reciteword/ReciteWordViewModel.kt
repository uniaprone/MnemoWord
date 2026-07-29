package com.kite.mnemoai.ui.reciteword

import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kite.mnemoai.MainApplication
import com.kite.mnemoai.data.local.UserSetting
import com.kite.mnemoai.data.local.entity.WordMeaningEntity
import com.kite.mnemoai.data.model.WordDetailInfo
import com.kite.mnemoai.data.model.WordTranslation
import com.kite.mnemoai.data.network.WordExtractRequest
import com.kite.mnemoai.data.repository.AiMnemonicRepository
import com.kite.mnemoai.data.repository.GroupRepository
import com.kite.mnemoai.data.repository.IRepositoryCallback
import com.kite.mnemoai.data.repository.StatisticsRepository
import com.kite.mnemoai.data.repository.UserSettingRepository
import com.kite.mnemoai.data.repository.WordRepository
import com.kite.mnemoai.ui.model.LoadingState
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Collections
import java.util.Random
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors
import kotlin.math.max
import kotlin.math.min

class ReciteWordViewModel(
    private val wordRepository: WordRepository,
    private val groupRepository: GroupRepository?,
    private val statisticsRepository: StatisticsRepository,
    private val userSettingRepository: UserSettingRepository,
    private val aiMnemonicRepository: AiMnemonicRepository,
    savedStateHandle: SavedStateHandle?
) : ViewModel() {
    private val _uiState = MediatorLiveData<ReciteWordUIState?>()
    private var reciteStage: ReciteStage? = null
    private var reciteWordDetailInfoItemUIState: MutableList<WordDetailInfo>? = null
    val reciteStatistics: MutableList<ReciteStatistics> = ArrayList<ReciteStatistics>()
    private var totalProgress = 0
    private var currentProgress = 0
    @JvmField
    var isShowNext: Boolean = true
    private var isShowTranslation = false
    private var isShowDetail = false
    private var aiMnemonicLoadingState: LoadingState<String?>? = null
    private val random = Random()
    private val date: LocalDate = LocalDate.now()
    private var apiKey: String? = null
    private var hasSetDailyPlanWord = false

    private fun updateOrderData(newPlans: MutableList<WordDetailInfo>) {
        if (this.reciteWordDetailInfoItemUIState == null) {
            // 首次初始化：直接创建新的状态列表
            this.reciteWordDetailInfoItemUIState = newPlans
        } else {
            val newPlanMap = newPlans.stream()
                .collect(
                    Collectors.toMap(
                        Function { plan: WordDetailInfo? -> plan!!.wordEntity.getId() },
                        Function.identity<WordDetailInfo?>()
                    ) { _: WordDetailInfo?, replacement: WordDetailInfo? -> replacement })

            // 1. 新列表中不存在，旧列表中存在
            val iterator = this.reciteWordDetailInfoItemUIState!!.iterator()
            while (iterator.hasNext()) {
                val wordDetailInfo = iterator.next()
                val wordId = wordDetailInfo.wordEntity.getId()
                if (newPlanMap.get(wordId) == null) {
                    iterator.remove()
                    isShowNext = true
                }
            }

            val oldWordDetailInfoMap =
                this.reciteWordDetailInfoItemUIState!!.stream()
                    .collect(
                        Collectors.toMap(
                            Function { status: WordDetailInfo? -> status!!.wordEntity.getId() },
                            Function.identity<WordDetailInfo?>()
                        )
                    )

            // 2. 新列表中存在，旧列表中不存在
            for (newPlan in newPlans) {
                val worldId = newPlan.wordEntity.getId()
                val oldStatus = oldWordDetailInfoMap.get(worldId)
                if (oldStatus == null) {
                    val orderSize = this.reciteWordDetailInfoItemUIState!!.size
                    if (orderSize == 0) {
                        this.reciteWordDetailInfoItemUIState!!.add(newPlan)
                    } else {
                        val insertIndex = random.nextInt(orderSize) + 1
                        this.reciteWordDetailInfoItemUIState!!.add(insertIndex, newPlan)
                    }
                    isShowNext = true
                } else {
                    //赋新值
                    oldStatus.wordEntity = newPlan.wordEntity
                    oldStatus.wordExtractEntity = newPlan.wordExtractEntity
                    oldStatus.dayPlanWordEntities = newPlan.dayPlanWordEntities
                }
            }
        }
    }

    fun setDailyDayPlanWordEntities() {
        userSettingRepository.getUserSetting(object : IRepositoryCallback<UserSetting?> {

            override fun onComplete(t: UserSetting?) {
                t?.let {
                    wordRepository.setDailyDayPlanWordEntities(it.newLearningWordCount)
                }
            }

            override fun onError(t: Throwable?) {
            }
        })
    }

    private fun updateUIStatus() {
        _uiState.value = ReciteWordUIState(
            reciteStage,
            reciteWordDetailInfoItemUIState,
            totalProgress,
            currentProgress,
            isShowNext,
            isShowTranslation,
            isShowDetail,
            aiMnemonicLoadingState
        )
    }

    val uiState: LiveData<ReciteWordUIState?>
        get() = _uiState

    fun showTranslation() {
        this.isShowTranslation = true
        updateUIStatus()
    }

    private fun showDetail() {
        this.isShowDetail = true
        updateUIStatus()
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
        if (this.reciteWordDetailInfoItemUIState == null || this.reciteWordDetailInfoItemUIState!!.isEmpty()) return
        val currentWord = this.reciteWordDetailInfoItemUIState!![0]
        aiMnemonicLoadingState = LoadingState.Loading
        updateUIStatus()
        viewModelScope.launch {
            aiMnemonicLoadingState = aiMnemonicRepository.generateWordExtract(currentWord, WordExtractRequest(currentWord.wordEntity.word, false))
            updateUIStatus()
        }

    }

    fun rememberWord() {
        if (this.reciteWordDetailInfoItemUIState == null || this.reciteWordDetailInfoItemUIState!!.isEmpty()) return
        val remembered = this.reciteWordDetailInfoItemUIState!![0]
        resetShowState()
        this.reciteWordDetailInfoItemUIState!!.remove(remembered)
        val rememberedReciteStatistics = reciteStatistics.stream()
            .filter { rs: ReciteStatistics -> rs.wordId == remembered.wordEntity.id }
            .findFirst().get()
        updateReciteStatistics(remembered.wordEntity.id, 2)
        statisticsRepository.rememberWord(rememberedReciteStatistics)
        this.isShowNext = true
        updateUIStatus()
    }

    fun blurWord() {
        if (this.reciteWordDetailInfoItemUIState == null || this.reciteWordDetailInfoItemUIState!!.isEmpty()) return
        val blured = this.reciteWordDetailInfoItemUIState!![0]
        resetShowState()

        this.reciteWordDetailInfoItemUIState!!.remove(blured)
        val size = this.reciteWordDetailInfoItemUIState!!.size

        // 如果列表为空，直接添加并返回（虽然下方也会处理，但更清晰）
        if (size == 0) {
            this.reciteWordDetailInfoItemUIState!!.add(blured)
            updateUIStatus()
            return
        }

        // 计算 30%~60% 区间的整数索引
        var minPos = (0.6 * size).toInt()
        var maxPos = (size)

        // 保证不越界
        minPos = max(0, minPos)
        maxPos = min(size, maxPos)
        // 理论上 minPos <= maxPos，但防止浮点误差
        if (minPos > maxPos) minPos = maxPos

        // 随机位置（包含两端）
        val position = minPos + random.nextInt(maxPos - minPos + 1)
        this.reciteWordDetailInfoItemUIState!!.add(position, blured)
        this.isShowNext = true
        updateReciteStatistics(blured.wordEntity.getId(), 1)
        updateUIStatus()
    }

    fun forgetWord() {
        if (this.reciteWordDetailInfoItemUIState == null || this.reciteWordDetailInfoItemUIState!!.isEmpty()) return
        val forgot = this.reciteWordDetailInfoItemUIState!!.get(0)
        resetShowState()

        this.reciteWordDetailInfoItemUIState!!.remove(forgot)
        val size = this.reciteWordDetailInfoItemUIState!!.size

        // 如果列表为空，直接添加并返回（虽然下方也会处理，但更清晰）
        if (size == 0) {
            this.reciteWordDetailInfoItemUIState!!.add(forgot)
            updateUIStatus()
            return
        }

        // 计算 60%~100% 区间的整数索引
        var minPos = (0.3 * size).toInt()
        var maxPos = (0.6 * size).toInt()
        // 保证不越界
        minPos = max(0, minPos)
        maxPos = min(size, maxPos)
        // 理论上 minPos <= maxPos，但防止浮点误差
        if (minPos > maxPos) minPos = maxPos

        // 随机位置（包含两端）
        val position = minPos + random.nextInt(maxPos - minPos + 1)
        this.reciteWordDetailInfoItemUIState!!.add(position, forgot)
        this.isShowNext = true
        updateReciteStatistics(forgot.wordEntity.getId(), 0)
        updateUIStatus()
    }

    fun startReciteStatistics() {
        if (this.reciteWordDetailInfoItemUIState == null || this.reciteWordDetailInfoItemUIState!!.isEmpty()) return
        this.reciteStatistics.stream()
            .filter { reciteStatistic: ReciteStatistics? ->
                reciteStatistic!!.wordId == this.reciteWordDetailInfoItemUIState!![0].wordEntity.id
            }
            .findFirst().ifPresentOrElse(
                Consumer { reciteStatistics1: ReciteStatistics? ->
                    reciteStatistics1!!.startLearningTime = SystemClock.elapsedRealtime()
                }
            ) {
                this.reciteStatistics.add(
                    ReciteStatistics(
                        this.reciteWordDetailInfoItemUIState!![0].wordEntity.id,
                        SystemClock.elapsedRealtime()
                    )
                )
            }
    }

    fun updateReciteStatistics(wordId: Long, type: Int) {
        //type修改类型， 0 - 模糊， 1 - 忘记， 2 - 记住
        for (i in this.reciteStatistics.indices) {
            val currentReciteStatistics = this.reciteStatistics[i]
            if (currentReciteStatistics.wordId == wordId) {
                if (type == 0) {
                    currentReciteStatistics.addBlurCount()
                    currentReciteStatistics.addLearningTime(SystemClock.elapsedRealtime() - currentReciteStatistics.startLearningTime)
                } else if (type == 1) {
                    currentReciteStatistics.addForgetCount()
                    currentReciteStatistics.addLearningTime(SystemClock.elapsedRealtime() - currentReciteStatistics.startLearningTime)
                } else if (type == 2) {
                    currentReciteStatistics.addLearningTime(SystemClock.elapsedRealtime() - currentReciteStatistics.startLearningTime)
                }
            }
        }
    }

    init {
        _uiState.addSource<Int?>(wordRepository.getDailyReciteStatus(), Observer { status: Int? ->
            when (status) {
                0 -> this.reciteStage = ReciteStage.NO_VOCABULARY
                1 -> this.reciteStage = ReciteStage.FINISH
                2 -> this.reciteStage = ReciteStage.IN_PROGRESS
            }
            updateUIStatus()
        })

        _uiState.addSource<UserSetting?>(
            userSettingRepository.userSettingLiveData,
            Observer { userSetting: UserSetting? ->
                this.apiKey = userSetting!!.apiKey
                updateUIStatus()
                if (!hasSetDailyPlanWord) {
                    wordRepository.setDailyDayPlanWordEntities(userSetting.newLearningWordCount)
                    hasSetDailyPlanWord = true
                }
            })

        _uiState.addSource<MutableList<WordDetailInfo>?>(
            wordRepository.getUnfinishWordDetailInfoLiveData(),
            Observer { wordDetailInfos: MutableList<WordDetailInfo>? ->
                wordDetailInfos!!.forEach(Consumer { wordDetailInfo: WordDetailInfo? ->
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
                })
                updateOrderData(wordDetailInfos)
                updateUIStatus()
            })

        _uiState.addSource<Int?>(
            statisticsRepository.getAllPlanCountByDate(date.toString()),
            Observer { allPlanCount: Int? ->
                this.totalProgress = allPlanCount!!
                updateUIStatus()
            })

        _uiState.addSource<Int?>(
            statisticsRepository.getFinishedPlanCountByDate(date.toString()),
            Observer { finishedPlanCount: Int? ->
                this.currentProgress = finishedPlanCount!!
                updateUIStatus()
            })
    }

    class ReciteStatistics(@JvmField val wordId: Long, var startLearningTime: Long) {
        var blurCount: Int = 0
            private set
        var forgetCount: Int = 0
            private set
        var learningTime: Long = 0
            private set

        fun addLearningTime(time: Long) {
            this.learningTime = this.learningTime + time
        }

        fun addBlurCount() {
            blurCount = blurCount + 1
        }

        fun addForgetCount() {
            forgetCount = forgetCount + 1
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[AndroidViewModelFactory.APPLICATION_KEY] as MainApplication
                val savedStateHandle = createSavedStateHandle()
                ReciteWordViewModel(
                    app.wordRepository,
                    app.groupRepository,
                    app.statisticsRepository,
                    app.userSettingRepository,
                    app.aiMnemonicRepository,
                    savedStateHandle
                )
            }
        }
    }
}

