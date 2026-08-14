package com.kite.mnemoai.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "word_meaning",
    foreignKeys = [ForeignKey(
        entity = WordPosEntity::class,
        parentColumns = ["id"],
        childColumns = ["pos_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["pos_id", "meaning"], unique = true)]
)
data class WordMeaningEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = "pos_id")
    val posId:Long,
    var meaning: String
)