package com.kite.mnemoai.data.model;

import androidx.room.ColumnInfo;

public class WordReview {
    private long wordId;
    private int reviewState;
    private String nextReviewTime;
    private int reviewCount;

    public WordReview(long wordId, int reviewState, int reviewCount, String nextReviewTime) {
        this.wordId = wordId;
        this.reviewState = reviewState;
        this.reviewCount = reviewCount;
        this.nextReviewTime = nextReviewTime;
    }

    public String getNextReviewTime() {
        return nextReviewTime;
    }

    public void setNextReviewTime(String nextReviewTime) {
        this.nextReviewTime = nextReviewTime;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public int getReviewState() {
        return reviewState;
    }

    public void setReviewState(int reviewState) {
        this.reviewState = reviewState;
    }

    public long getWordId() {
        return wordId;
    }

    public void setWordId(long wordId) {
        this.wordId = wordId;
    }
}
