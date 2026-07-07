package com.kite.mnemoai.data.local.entity;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(
        tableName = "word_group",
        primaryKeys = {"word_id", "group_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = WordEntity.class,
                        parentColumns = "id",
                        childColumns = "word_id",
                        onDelete = CASCADE
                ),
                @ForeignKey(
                        entity = GroupEntity.class,
                        parentColumns = "id",
                        childColumns = "group_id",
                        onDelete = CASCADE
                )
        }
)
public class WordGroupEntity {
    @ColumnInfo(name = "word_id")
    private long wordId;
    @ColumnInfo(name = "group_id")
    private long groupId;

    public WordGroupEntity(long groupId, long wordId) {
        this.groupId = groupId;
        this.wordId = wordId;
    }

    public WordGroupEntity(){};

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getWordId() {
        return wordId;
    }

    public void setWordId(long wordId) {
        this.wordId = wordId;
    }
}
