package com.kite.mnemoai.data.local.DTO;

import androidx.room.ColumnInfo;

public class DailyStatistic {
    @ColumnInfo(name = "review_count")
    private int reviewCount;
    @ColumnInfo(name = "learned_count")
    private int learnedCount;
    @ColumnInfo(name = "reviewed_count")
    private int reviewedCount;

    public DailyStatistic(int reviewCount, int learnedCount, int reviewedCount) {
        this.reviewCount = reviewCount;
        this.learnedCount = learnedCount;
        this.reviewedCount = reviewedCount;
    }

    public int getLearnedCount() {
        return learnedCount;
    }

    public void setLearnedCount(int learnedCount) {
        this.learnedCount = learnedCount;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public int getReviewedCount() {
        return reviewedCount;
    }

    public void setReviewedCount(int reviewedCount) {
        this.reviewedCount = reviewedCount;
    }
}
