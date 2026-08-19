package com.kite.mnemoai.vocabularybookgroup

import com.kite.mnemoai.model.group.Group
import com.kite.mnemoai.model.word.WordItem

data class VocabularyGroupUIState(
    val group: Group?,
    val words: List<WordItem>?,
    val allCount: Long = 0,
    val learningCount: Long = 0,
    val reviewingCount: Long = 0,
    val masteredCount: Long = 0,
)