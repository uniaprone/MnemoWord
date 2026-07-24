package com.kite.mnemoai.ui.worddetail;

import com.kite.mnemoai.data.model.WordDetailInfo;
import com.kite.mnemoai.ui.reciteword.WordDetailStatus;

public class WordDetailUIState {
    private WordDetailInfo wordDetailInfo;
    private WordDetailStatus wordDetailStatus;

    private String apiKey;

    public WordDetailUIState(WordDetailInfo wordDetailInfo, WordDetailStatus wordDetailStatus, String apiKey) {
        this.wordDetailInfo = wordDetailInfo;
        this.wordDetailStatus = wordDetailStatus;
        this.apiKey = apiKey;
    }

    public WordDetailInfo getWordDetailInfo() {
        return wordDetailInfo;
    }

    public WordDetailStatus getWordDetailStatus() {
        return wordDetailStatus;
    }

    public String getApiKey() {
        return apiKey;
    }
}
