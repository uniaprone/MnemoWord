package com.kite.mnemoai.ui.reciteword;

import com.kite.mnemoai.data.model.WordDetailInfo;
import com.kite.mnemoai.ui.model.LoadingState;

import java.util.List;

public class ReciteWordUIState {
    private List<WordDetailInfo> reciteWordItemStatusOrder;
    private ReciteStage reciteStage;
    private int currentProgress;
    private int totalProgress;
    private boolean isShowNext;
    private boolean isShowTranslation;
    private boolean isShowDetail;
    private LoadingState aiMnemonicLoadingState;

    public ReciteWordUIState(ReciteStage reciteStage,
                             List<WordDetailInfo> reciteWordItemStatusOrder,
                             int totalProgress,
                             int currentProgress,
                             boolean isShowNext,
                             boolean isShowTranslation,
                             boolean isShowDetail,
                             LoadingState aiMnemonicLoadingState) {
        this.reciteStage = reciteStage;
        this.reciteWordItemStatusOrder = reciteWordItemStatusOrder;
        this.totalProgress = totalProgress;
        this.currentProgress = currentProgress;
        this.isShowNext = isShowNext;
        this.isShowTranslation = isShowTranslation;
        this.isShowDetail = isShowDetail;
        this.aiMnemonicLoadingState = aiMnemonicLoadingState;
    }

    public List<WordDetailInfo> getReciteWordItemStatusOrder() {
        return reciteWordItemStatusOrder;
    }

    public void setReciteWordItemStatusOrder(List<WordDetailInfo> reciteWordItemStatusOrder) {
        this.reciteWordItemStatusOrder = reciteWordItemStatusOrder;
    }

    public boolean isShowNext() {
        return isShowNext;
    }

    public void setShowNext(boolean showNext) {
        this.isShowNext = showNext;
    }

    public ReciteStage getReciteStage() {
        return reciteStage;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public int getTotalProgress() {
        return totalProgress;
    }

    public LoadingState getAiMnemonicLoadingState() {
        return aiMnemonicLoadingState;
    }

    public boolean isShowDetail() {
        return isShowDetail;
    }

    public boolean isShowTranslation() {
        return isShowTranslation;
    }
}
