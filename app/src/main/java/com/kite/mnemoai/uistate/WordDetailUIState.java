package com.kite.mnemoai.uistate;

import com.kite.mnemoai.data.local.DTO.WordWithExtractAndDayPlanEntity;
import com.kite.mnemoai.model.WordDetailStatus;
import com.kite.mnemoai.model.WordWithExtractAndDayPlan;

public class WordDetailUIState {
    private WordWithExtractAndDayPlan wordWithExtractAndDayPlan;
    private WordDetailStatus wordDetailStatus;

    public WordDetailUIState(WordWithExtractAndDayPlan wordWithExtractAndDayPlan, WordDetailStatus wordDetailStatus) {
        this.wordWithExtractAndDayPlan = wordWithExtractAndDayPlan;
        this.wordDetailStatus = wordDetailStatus;
    }

    public WordWithExtractAndDayPlan getWordWithExtractAndDayPlan() {
        return wordWithExtractAndDayPlan;
    }

    public WordDetailStatus getWordDetailStatus() {
        return wordDetailStatus;
    }
}
