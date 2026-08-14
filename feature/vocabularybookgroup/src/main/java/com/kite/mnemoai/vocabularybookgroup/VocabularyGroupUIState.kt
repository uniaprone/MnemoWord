package com.kite.mnemoai.vocabularybookgroup

import com.kite.mnemoai.model.group.Group
import com.kite.mnemoai.model.word.WordItem

data class VocabularyGroupUIState(
    val group: Group?,
    val words: List<WordItem>?,
) {
    constructor(words: List<WordItem>?) : this(null, words)
    constructor(group: Group?) : this(group, null)
}
