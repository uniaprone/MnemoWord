package com.kite.mnemoai.ui.worddetail;

import com.kite.mnemoai.data.model.WordDetailInfo;
import com.kite.mnemoai.ui.model.LoadingState;
import com.kite.mnemoai.ui.reciteword.WordDetailStatus;

public class WordDetailUIState {
    private WordDetailInfo wordDetailInfo;
    private LoadingState<String> aiMnemonicLoadingState;
    private String apiKey;

    public WordDetailUIState(WordDetailInfo wordDetailInfo, LoadingState<String> aiMnemonicLoadingState, String apiKey) {
        this.wordDetailInfo = wordDetailInfo;
        this.aiMnemonicLoadingState = aiMnemonicLoadingState;
        this.apiKey = apiKey;
    }

    public WordDetailInfo getWordDetailInfo() {
        return wordDetailInfo;
    }

    public LoadingState<String> getAiMnemonicLoadingState() {
        return aiMnemonicLoadingState;
    }

    public String getApiKey() {
        return apiKey;
    }
}
