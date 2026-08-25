package com.kite.mnemoai.reciteword

import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.word.WordDetail

data class ReciteWordUIState(
    val reciteStage: ReciteStage?,
    val reciteWordItemStatusOrder: MutableList<WordDetail>?,
    val totalProgress: Int,
    val currentProgress: Int,
    val isShowTranslation: Boolean,
    val isShowDetail: Boolean,
    val aiMnemonicLoadingState: Result<String>?,
    /**
     * 单调递增的版本号：用于绕过 StateFlow 的 equals 去重，
     * 保证列表被重排回原序等"内容未变但 UI 需要刷新"的场景仍会发射。
     */
    val revision: Long = 0,
)
