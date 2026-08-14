package com.kite.mnemoai.worddetail

import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.word.WordDetail

data class WordDetailUIState(
    val wordDetail: WordDetail?,
    val aiMnemonicLoadingState: Result<String>?,
    val apiKey: String?
)
