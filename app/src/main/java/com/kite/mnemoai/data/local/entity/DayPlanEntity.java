package com.kite.mnemoai.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "day_plan")
public class DayPlanEntity {
    @NonNull
    @PrimaryKey
    private String date;
    @ColumnInfo(name = "learn_goal", defaultValue = "20")
    private long learnGole;

    public DayPlanEntity(@NonNull String date, long learnGole) {
        this.date = date;
        this.learnGole = learnGole;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }

    public long getLearnGole() {
        return learnGole;
    }

    public void setLearnGole(long learnGole) {
        this.learnGole = learnGole;
    }
}
