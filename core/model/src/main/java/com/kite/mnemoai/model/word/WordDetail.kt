package com.kite.mnemoai.model.word

import com.kite.mnemoai.model.dayplan.DayPlanWord

data class WordDetail(
    val word: Word,
    val translations: List<WordTranslation>,
    val forms: List<WordForm>,
    val extract: WordExtract?,
    val dayPlanWords: List<DayPlanWord>
)
