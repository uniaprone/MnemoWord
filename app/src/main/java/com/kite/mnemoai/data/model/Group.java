package com.kite.mnemoai.data.model;

import androidx.room.ColumnInfo;

public class Group {
    private long id;
    private String name;
    private long createTime;
    private int isLearning;

    public Group(long id, String name, int isLearning, long createTime) {
        this.id = id;
        this.name = name;
        this.isLearning = isLearning;
        this.createTime = createTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getIsLearning() {
        return isLearning;
    }

    public void setIsLearning(int isLearning) {
        this.isLearning = isLearning;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
