package com.kite.mnemoai.ui.changevocabularybookword

import com.kite.mnemoai.data.model.WordListItem
import com.kite.mnemoai.ui.changevocabularybookword.VocabularyBookChangedWord

data class ChangeVocabularyBookWordUIState(
    val groupId:Long,
    val operationType: ChangeType = ChangeType.ADD,
    val searchText: String,
    val optionalWords: List<WordListItem>,
    var alterWords: List<VocabularyBookChangedWord>
){
    enum class ChangeType{
        ADD, REMOVE
    }
}