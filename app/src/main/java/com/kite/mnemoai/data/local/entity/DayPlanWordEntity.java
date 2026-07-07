package com.kite.mnemoai.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "day_plan_word", primaryKeys = {"date", "word_id"})
public class DayPlanWordEntity {
    @NotNull
    private String date;
    @ColumnInfo(name = "word_id")
    private long wordId;
    private int type;   //0-新学， 1-复习
    private int status;   //0-未完成， 1-已完成
    @ColumnInfo(name = "blur_count")
    private int blurCount;
    @ColumnInfo(name = "forget_count")
    private int forgetCount;
    @ColumnInfo(name = "learning_time")
    private long learningTime;
    @ColumnInfo(name = "complete_time")
    private String completeTime;

    public DayPlanWordEntity(long wordId, @NonNull String date, int type, int status, int blurCount, int forgetCount, long learningTime, String completeTime) {
        this.wordId = wordId;
        this.date = date;
        this.type = type;
        this.status = status;
        this.blurCount = blurCount;
        this.forgetCount = forgetCount;
        this.learningTime = learningTime;
        this.completeTime = completeTime;
    }

    public String getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(String completeTime) {
        this.completeTime = completeTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getWordId() {
        return wordId;
    }

    public void setWordId(long wordId) {
        this.wordId = wordId;
    }

    public int getBlurCount() {
        return blurCount;
    }

    public void setBlurCount(int blurCount) {
        this.blurCount = blurCount;
    }

    public int getForgetCount() {
        return forgetCount;
    }

    public void setForgetCount(int forgetCount) {
        this.forgetCount = forgetCount;
    }

    public long getLearningTime() {
        return learningTime;
    }

    public void setLearningTime(long learningTime) {
        this.learningTime = learningTime;
    }
}
