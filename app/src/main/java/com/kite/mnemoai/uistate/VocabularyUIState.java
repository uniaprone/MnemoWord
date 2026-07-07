package com.kite.mnemoai.uistate;

import com.kite.mnemoai.data.local.DTO.DailyStatistic;
import com.kite.mnemoai.data.model.GroupDetail;

import java.util.List;

public class VocabularyUIState {
    private final List<GroupDetail> groupDetails;
    private final int newLearningWordCount;
    private final DailyStatistic dailyStatistic;

    public VocabularyUIState(List<GroupDetail> groupDetails, int newLearningWordCount, DailyStatistic dailyStatistic) {
        this.groupDetails = groupDetails;
        this.newLearningWordCount = newLearningWordCount;
        this.dailyStatistic = dailyStatistic;
    }

    public List<GroupDetail> getGroupDetails() {
        return groupDetails;
    }

    public int getNewLearningWordCount() {
        return newLearningWordCount;
    }

    public DailyStatistic getDailyStatistic() {
        return dailyStatistic;
    }
}
