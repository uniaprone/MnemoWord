package com.kite.mnemoai.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "word_group",
    primaryKeys = ["word_id", "group_id"],
    foreignKeys = [ForeignKey(
        entity = WordEntity::class,
        parentColumns = ["id"],
        childColumns = ["word_id"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = GroupEntity::class,
        parentColumns = ["id"],
        childColumns = ["group_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class WordGroupEntity(
    @ColumnInfo(name = "word_id")
    var wordId: Long = 0,
    @ColumnInfo(name = "group_id")
    var groupId: Long = 0
)
