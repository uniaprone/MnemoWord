package com.kite.mnemoai.ui.reciteword

import android.os.Parcel
import android.os.Parcelable
import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kite.mnemoai.data.local.UserSetting
import com.kite.mnemoai.data.local.entity.WordExtractEntity
import com.kite.mnemoai.data.local.entity.WordMeaningEntity
import com.kite.mnemoai.data.model.WordDetailInfo
import com.kite.mnemoai.data.model.WordTranslation
import com.kite.mnemoai.data.network.WordExtractRequest
import com.kite.mnemoai.data.repository.AiMnemonicRepository
import com.kite.mnemoai.data.repository.IRepositoryCallback
import com.kite.mnemoai.data.repository.StatisticsRepository
import com.kite.mnemoai.data.repository.UserSettingRepository
import com.kite.mnemoai.data.repository.WordRepository
import com.kite.mnemoai.ui.model.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Random
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@HiltViewModel
class ReciteWordViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val statisticsRepository: StatisticsRepository,
    private val userSettingRepository: UserSettingRepository,
    private val aiMnemonicRepository: AiMnemonicRepository,
) : ViewModel() {
    private val _uiState = MediatorLiveData<ReciteWordUIState?>()
    private var reciteStage: ReciteStage? = null
    private var reciteWordDetailInfoItemUIState: MutableList<WordDetailInfo>? = null
    private var reciteStatistics: MutableList<ReciteStatistics> = ArrayList<ReciteStatistics>()
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
            }
        )

        _uiState.addSource(
            wordRepository.getUnfinishWordDetailInfoLiveData(),
            Observer { wordDetailInfos ->
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
            }
        )

        _uiState.addSource(
            statisticsRepository.getAllPlanCountByDate(date.toString()),
            Observer { allPlanCount: Int? ->
                this.totalProgress = allPlanCount!!
                updateUIStatus()
            })

        _uiState.addSource(
            statisticsRepository.getFinishedPlanCountByDate(date.toString()),
            Observer { finishedPlanCount: Int? ->
                this.currentProgress = finishedPlanCount!!
                updateUIStatus()
            })
    }

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
                    resetShowState()
                }
            }

            val oldWordDetailInfoMap =
                this.reciteWordDetailInfoItemUIState!!.stream()
                    .collect(
                        Collectors.toMap(
                            Function { status: WordDetailInfo? -> status!!.wordEntity.id },
                            Function.identity<WordDetailInfo?>()
                        )
                    )

            // 2. 新列表中存在，旧列表中不存在
            for (newPlan in newPlans) {
                val worldId = newPlan.wordEntity.id
                val oldStatus = oldWordDetailInfoMap[worldId]
                if (oldStatus == null) {
                    val orderSize = this.reciteWordDetailInfoItemUIState!!.size
                    if (orderSize == 0) {
                        this.reciteWordDetailInfoItemUIState!!.add(newPlan)
                    } else {
                        val insertIndex = random.nextInt(orderSize) + 1
                        this.reciteWordDetailInfoItemUIState!!.add(insertIndex, newPlan)
                    }
                    isShowNext = true
                    resetShowState()
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
        updateReciteStatistics(blured.wordEntity.id, 1)
        updateUIStatus()
    }

    fun forgetWord() {
        if (this.reciteWordDetailInfoItemUIState == null || this.reciteWordDetailInfoItemUIState!!.isEmpty()) return
        val forgot = this.reciteWordDetailInfoItemUIState!![0]
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
        //type修改类型， 0 - 模糊， 1 - 忘记， 2 - 记住, 3 - 结束计时
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
                } else if (type == 3) {
                    currentReciteStatistics.addLearningTime(SystemClock.elapsedRealtime() - currentReciteStatistics.startLearningTime)
                }
            }
        }
    }

    fun stopReciteStatistics(){
        if (this.reciteWordDetailInfoItemUIState == null || this.reciteWordDetailInfoItemUIState!!.isEmpty()) return
        val currentReciteStatistics = this.reciteWordDetailInfoItemUIState!![0]
        updateReciteStatistics(currentReciteStatistics.wordEntity.id, 3)
    }

    data class ReciteStatistics(val wordId: Long, var startLearningTime: Long): Parcelable {
        constructor(parcel: Parcel): this(
            parcel.readLong(),
            0,
        ){
            this.blurCount = parcel.readInt()
            this.forgetCount = parcel.readInt()
            this.learningTime = parcel.readLong()
        }
        var blurCount: Int = 0
            private set
        var forgetCount: Int = 0
            private set
        var learningTime: Long = 0
            private set

        fun addLearningTime(time: Long) {
            this.learningTime += time
        }

        fun addBlurCount() {
            blurCount += 1
        }

        fun addForgetCount() {
            forgetCount += 1
        }

        override fun describeContents(): Int = 0

        override fun writeToParcel(p0: Parcel, p1: Int) {
            p0.writeLong(wordId)
            p0.writeLong(startLearningTime)
            p0.writeInt(blurCount)      // 写入额外字段
            p0.writeInt(forgetCount)
            p0.writeLong(learningTime)
        }

        companion object CREATOR: Parcelable.Creator<ReciteStatistics?> {
            override fun createFromParcel(p0: Parcel): ReciteStatistics {
                return ReciteStatistics(p0)
            }

            override fun newArray(p0: Int): Array<out ReciteStatistics?> {
                return arrayOfNulls(p0)
            }
        }
    }
}

