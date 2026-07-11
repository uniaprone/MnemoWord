package com.kite.mnemoai.data.model;

import androidx.room.ColumnInfo;

public class Group {
    private long id;
    private String name;
    private String description;
    private long createTime;
    private int isLearning;

    public Group(long id, String name, String description, int isLearning, long createTime) {
        this.id = id;
        this.name = name;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
