package com.kite.mnemoai.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "day_plan_word", primaryKeys = ["date", "word_id"])
class DayPlanWordEntity(
    @field:ColumnInfo(name = "word_id") var wordId: Long,
    var date: String,
    var type: Int, //0-新学，1-复习
    var status: Int, //0-未完成，1-已完成
    @field:ColumnInfo(name = "blur_count") var blurCount: Int,
    @field:ColumnInfo(
        name = "forget_count"
    ) var forgetCount: Int,
    @field:ColumnInfo(name = "learning_time") var learningTime: Long,
    @field:ColumnInfo(
        name = "complete_time"
    ) var completeTime: String?
)
