package com.kite.mnemoai.reciteword

import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.word.WordDetail

data class ReciteWordUIState(
    val reciteStage: ReciteStage?,
    val reciteWordItemStatusOrder: List<WordDetail>?,
    val totalProgress: Int,
    val currentProgress: Int,
    val isShowNext: Boolean,
    val isShowTranslation: Boolean,
    val isShowDetail: Boolean,
    val aiMnemonicLoadingState: Result<String>?,
)
