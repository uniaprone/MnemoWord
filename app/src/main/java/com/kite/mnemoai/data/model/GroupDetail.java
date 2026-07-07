package com.kite.mnemoai.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

import com.kite.mnemoai.data.local.entity.GroupEntity;

public class GroupDetail {
    @Embedded
    private GroupEntity groupEntity;

    @ColumnInfo(name = "total_count")
    private int totalWords;

    @ColumnInfo(name = "learning_count")
    private int learningCount;
    @ColumnInfo(name = "reviewing_count")
    private int reviewingCount;
    @ColumnInfo(name = "mastered_count")
    private int masteredCount;

    public GroupDetail(GroupEntity groupEntity, int totalWords, int totalWaitStart, int reviewingCount, int masteredCount) {
        this.totalWords = totalWords;
        this.learningCount = totalWaitStart;
        this.reviewingCount = reviewingCount;
        this.masteredCount = masteredCount;
    }

    public GroupDetail() {}

    public GroupEntity getGroupEntity() {
        return groupEntity;
    }

    public void setGroupEntity(GroupEntity groupEntity) {
        this.groupEntity = groupEntity;
    }

    public int getLearningCount() {
        return learningCount;
    }

    public void setLearningCount(int learningCount) {
        this.learningCount = learningCount;
    }

    public int getMasteredCount() {
        return masteredCount;
    }

    public void setMasteredCount(int masteredCount) {
        this.masteredCount = masteredCount;
    }

    public int getReviewingCount() {
        return reviewingCount;
    }

    public void setReviewingCount(int reviewingCount) {
        this.reviewingCount = reviewingCount;
    }

    public int getTotalWords() {
        return totalWords;
    }

    public void setTotalWords(int totalWords) {
        this.totalWords = totalWords;
    }
}
