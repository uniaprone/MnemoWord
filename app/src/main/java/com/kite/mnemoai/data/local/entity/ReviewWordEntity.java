package com.kite.mnemoai.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "word_review")
public class ReviewWordEntity {
    @PrimaryKey
    @ColumnInfo(name = "id")
    private long wordId;

    @ColumnInfo(name = "review_count")
    private int reviewCount;
    @ColumnInfo(name = "last_review_time")
    private String lastReviewTime;
    @ColumnInfo(name = "next_review_time")
    private String nextReviewTime;


    public ReviewWordEntity(long wordId, int reviewCount, String lastReviewTime, String nextReviewTime) {
        this.wordId = wordId;
        this.reviewCount = reviewCount;
        this.lastReviewTime = lastReviewTime;
        this.nextReviewTime = nextReviewTime;
    }


    public ReviewWordEntity() {}

    public String getLastReviewTime() {
        return lastReviewTime;
    }

    public void setLastReviewTime(String lastReviewTime) {
        this.lastReviewTime = lastReviewTime;
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

    public long getWordId() {
        return wordId;
    }

    public void setWordId(long wordId) {
        this.wordId = wordId;
    }
}
