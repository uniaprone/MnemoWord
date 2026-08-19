package com.kite.mnemoai.changevocabularybookword

import com.kite.mnemoai.model.word.WordItem

data class ChangeVocabularyBookWordUIState(
    val groupId:Long,
    val operationType: ChangeType = ChangeType.ADD,
    val searchText: String,
    val optionalWords: List<WordItem>,
    var alterWords: List<VocabularyBookChangedWord>
)