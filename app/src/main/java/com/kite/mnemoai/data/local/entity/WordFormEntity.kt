package com.kite.mnemoai.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "word_form",
    foreignKeys = [ForeignKey(
        entity = WordEntity::class,
        parentColumns = ["id"],
        childColumns = ["word_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["word_id", "type_code"], unique = true)]
)
data class WordFormEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo("word_id")
    val wordId:Long,
    @ColumnInfo(name = "type_code")
    val typeCode: String,
    val form: String
)
