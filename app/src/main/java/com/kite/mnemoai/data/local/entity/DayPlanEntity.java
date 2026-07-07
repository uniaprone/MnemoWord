package com.kite.mnemoai.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;


@Entity(tableName = "day_plan")
public class DayPlanEntity {
    @NonNull
    @PrimaryKey
    private String date;
    @ColumnInfo(name = "learn_goal", defaultValue = "20")
    private long learnGole;

    public DayPlanEntity(String date, long learnGole) {
        this.date = date;
        this.learnGole = learnGole;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getLearnGole() {
        return learnGole;
    }

    public void setLearnGole(long learnGole) {
        this.learnGole = learnGole;
    }
}
