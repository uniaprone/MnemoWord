package com.kite.mnemoai.data.model

import com.kite.mnemoai.database.model.WordListItem
import com.kite.mnemoai.model.word.WordItem

fun WordListItem.asExternalModel(): WordItem = WordItem(
    id = id,
    word = word,
    phonetic = phonetic ?: "",
    translation = translation ?: "",
    reviewState = reviewState
)

fun List<WordListItem>.asExternalModel(): List<WordItem> = map { it.asExternalModel() }
