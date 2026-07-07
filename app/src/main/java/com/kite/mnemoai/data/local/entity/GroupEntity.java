package com.kite.mnemoai.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "groups")
public class GroupEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "create_time")
    private long createTime;

    @ColumnInfo(name = "is_learning", defaultValue = "false")
    private int isLearning;

    public GroupEntity(long id, String name, int isLearning, long createTime) {
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
}
