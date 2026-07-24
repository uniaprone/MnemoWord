package com.kite.mnemoai.ui.vocabularybook;

import com.kite.mnemoai.data.model.DailyStatistic;

import java.util.List;

public class VocabularyUIState {
    private final List<LearningVocabularyBookItem> learningVocabularyBookItems;
    private final List<AllVocabularyBookItem> allVocabularyBookItems;
    private final int newLearningWordCount;
    private final DailyStatistic dailyStatistic;

    public VocabularyUIState(List<LearningVocabularyBookItem> learningVocabularyBookItems, List<AllVocabularyBookItem> allVocabularyBookItems, int newLearningWordCount, DailyStatistic dailyStatistic) {
        this.learningVocabularyBookItems = learningVocabularyBookItems;
        this.allVocabularyBookItems = allVocabularyBookItems;
        this.newLearningWordCount = newLearningWordCount;
        this.dailyStatistic = dailyStatistic;
    }


    public List<LearningVocabularyBookItem> getLearningVocabularyBookItems() {
        return learningVocabularyBookItems;
    }

    public List<AllVocabularyBookItem> getAllVocabularyBookItems() {
        return allVocabularyBookItems;
    }

    public int getNewLearningWordCount() {
        return newLearningWordCount;
    }

    public DailyStatistic getDailyStatistic() {
        return dailyStatistic;
    }
}
