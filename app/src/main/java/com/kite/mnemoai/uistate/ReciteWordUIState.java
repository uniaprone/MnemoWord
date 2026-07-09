package com.kite.mnemoai.uistate;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.kite.mnemoai.model.WordDetailStatus;
import com.kite.mnemoai.model.WordWithExtractAndDayPlan;

import java.util.List;
import java.util.Objects;

public class ReciteWordUIState {
    private List<ReciteWordItemStatus> newReciteWordItemStatuses;
    private List<ReciteWordItemStatus> reciteWordItemStatusOrder;
    private ReciteWordStatus reciteWordStatus;
    private int currentProgress;
    private int totalProgress;
    private boolean shouldAdvance;
    private String apiKey;

    public ReciteWordUIState(ReciteWordStatus reciteWordStatus, List<ReciteWordItemStatus> newReciteWordItemStatuses, List<ReciteWordItemStatus> reciteWordItemStatusOrder, int totalProgress, int currentProgress, boolean shouldAdvance) {
        this.reciteWordStatus = reciteWordStatus;
        this.newReciteWordItemStatuses = newReciteWordItemStatuses;
        this.reciteWordItemStatusOrder = reciteWordItemStatusOrder;
        this.totalProgress = totalProgress;
        this.currentProgress = currentProgress;
        this.shouldAdvance = shouldAdvance;
    }

    public List<ReciteWordItemStatus> getReciteWordItemStatusOrder() {
        return reciteWordItemStatusOrder;
    }

    public void setReciteWordItemStatusOrder(List<ReciteWordItemStatus> reciteWordItemStatusOrder) {
        this.reciteWordItemStatusOrder = reciteWordItemStatusOrder;
    }

    public boolean isShouldAdvance() {
        return shouldAdvance;
    }

    public void setShouldAdvance(boolean shouldAdvance) {
        this.shouldAdvance = shouldAdvance;
    }

    public ReciteWordStatus getReciteWordStatus() {
        return reciteWordStatus;
    }

    public List<ReciteWordItemStatus> getNewReciteWordItemStatuses() {
        return newReciteWordItemStatuses;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public int getTotalProgress() {
        return totalProgress;
    }

    public String getApiKey() {
        return apiKey;
    }

    public enum ReciteWordStatus{
        no_vocabulary, ok, finish;
    }

    public static class ReciteWordItemStatus{
        private WordWithExtractAndDayPlan wordWithExtractAndDayPlan;
        private WordDetailStatus wordDetailStatus;
        private boolean isShowTranslation;

        public ReciteWordItemStatus(WordWithExtractAndDayPlan wordWithExtractAndDayPlan, WordDetailStatus wordDetailStatus, boolean isShowTranslation) {
            this.wordWithExtractAndDayPlan = wordWithExtractAndDayPlan;
            this.wordDetailStatus = wordDetailStatus;
            this.isShowTranslation = isShowTranslation;
        }

        public static final DiffUtil.ItemCallback<ReciteWordItemStatus> DIFF_CALLBACK = new DiffUtil.ItemCallback<ReciteWordItemStatus>() {
            @Override
            public boolean areItemsTheSame(@NonNull ReciteWordItemStatus oldItem, @NonNull ReciteWordItemStatus newItem) {
                return oldItem.getWordWithExtractAndDayPlan().getWord().getWord().equals(newItem.getWordWithExtractAndDayPlan().getWord().getWord());
            }

            @Override
            public boolean areContentsTheSame(@NonNull ReciteWordItemStatus oldItem, @NonNull ReciteWordItemStatus newItem) {
                return oldItem.equals(newItem);
            }
        };

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ReciteWordItemStatus that = (ReciteWordItemStatus) o;
            return isShowTranslation == that.isShowTranslation && Objects.equals(wordWithExtractAndDayPlan, that.wordWithExtractAndDayPlan) && wordDetailStatus == that.wordDetailStatus;
        }

        @Override
        public int hashCode() {
            return Objects.hash(wordWithExtractAndDayPlan, wordDetailStatus, isShowTranslation);
        }

        public boolean isShowTranslation() {
            return isShowTranslation;
        }

        public WordDetailStatus getWordDetailStatus() {
            return wordDetailStatus;
        }

        public WordWithExtractAndDayPlan getWordWithExtractAndDayPlan() {
            return wordWithExtractAndDayPlan;
        }

        public void setWordDetailStatus(WordDetailStatus wordDetailStatus) {
            this.wordDetailStatus = wordDetailStatus;
        }

        public void setShowTranslation(boolean showTranslation) {
            isShowTranslation = showTranslation;
        }

        public void setWordWithExtractAndDayPlan(WordWithExtractAndDayPlan wordWithExtractAndDayPlanEntity) {
            this.wordWithExtractAndDayPlan = wordWithExtractAndDayPlanEntity;
        }
    }
}
