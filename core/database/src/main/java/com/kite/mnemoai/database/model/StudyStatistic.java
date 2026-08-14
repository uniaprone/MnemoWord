package com.kite.mnemoai.database.model;

import androidx.room.ColumnInfo;

public class StudyStatistic {
    private String date;
    @ColumnInfo(name = "reviewed_count")
    private int reviewedCount;
    @ColumnInfo(name = "new_learned_count")
    private int newLearnedCount;

    @ColumnInfo(name= "total_learning_time")
    private long totalLearningTime;
    public StudyStatistic(String date, int newLearnedCount, int reviewedCount, long totalLearningTime) {
        this.date = date;
        this.newLearnedCount = newLearnedCount;
        this.reviewedCount = reviewedCount;
        this.totalLearningTime = totalLearningTime;
    }

    public long getTotalLearningTime() {
        return totalLearningTime;
    }

    public void setTotalLearningTime(long totalLearningTime) {
        this.totalLearningTime = totalLearningTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getNewLearnedCount() {
        return newLearnedCount;
    }

    public void setNewLearnedCount(int newLearnedCount) {
        this.newLearnedCount = newLearnedCount;
    }

    public int getReviewedCount() {
        return reviewedCount;
    }

    public void setReviewedCount(int reviewedCount) {
        this.reviewedCount = reviewedCount;
    }
}
