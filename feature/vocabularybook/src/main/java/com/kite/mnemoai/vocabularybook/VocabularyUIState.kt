package com.kite.mnemoai.vocabularybook

import com.kite.mnemoai.model.group.AllVocabularyBookItem
import com.kite.mnemoai.model.group.LearningVocabularyBookItem
import com.kite.mnemoai.model.statistic.DailyStatistic

data class VocabularyUIState(
    val learningVocabularyBookItems: List<LearningVocabularyBookItem>?,
    val allVocabularyBookItems: List<AllVocabularyBookItem>?,
    val newLearningWordCount: Int,
    val dailyStatistic: DailyStatistic?,
)
