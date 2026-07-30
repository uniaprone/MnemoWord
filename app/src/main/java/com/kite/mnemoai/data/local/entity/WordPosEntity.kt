package com.kite.mnemoai.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "word_pos",
    foreignKeys = [ForeignKey(
        entity = WordEntity::class,
        parentColumns = ["id"],
        childColumns = ["word_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["word_id", "pos"], unique = true)]
)
data class WordPosEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = "word_id")
    val wordId: Long,
    val pos: String,
)
