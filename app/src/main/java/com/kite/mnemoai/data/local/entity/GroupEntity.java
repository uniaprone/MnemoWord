package com.kite.mnemoai.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "groups")
public class GroupEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "create_time")
    private long createTime;

    @ColumnInfo(name = "is_learning", defaultValue = "0")
    private int isLearning;

    public GroupEntity(long id, String name, String description, int isLearning, long createTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isLearning = isLearning;
        this.createTime = createTime;
    }

    @Ignore
    public GroupEntity(String name, String description, int isLearning, long createTime) {
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

    public int isLearning() {
        return isLearning;
    }

    public void setLearning(int learning) {
        isLearning = learning;
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
