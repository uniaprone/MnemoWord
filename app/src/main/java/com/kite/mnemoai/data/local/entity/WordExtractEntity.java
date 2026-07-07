package com.kite.mnemoai.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.kite.mnemoai.data.local.Converters;
import com.kite.mnemoai.data.model.WordExtract;

@TypeConverters({Converters.class})
@Entity(tableName = "word_extract")
public class WordExtractEntity {
    @PrimaryKey
    @ColumnInfo(name = "word_id")
    private long wordId;

    @ColumnInfo(name = "extract")
    private WordExtract extract;

    public WordExtractEntity(long wordId, WordExtract extract) {
        this.wordId = wordId;
        this.extract = extract;
    }

    public WordExtract getExtract() {
        return extract;
    }

    public void setExtract(WordExtract extract) {
        this.extract = extract;
    }

    public long getWordId() {
        return wordId;
    }

    public void setWordId(long wordId) {
        this.wordId = wordId;
    }
}
