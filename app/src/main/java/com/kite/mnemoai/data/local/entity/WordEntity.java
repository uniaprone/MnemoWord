package com.kite.mnemoai.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

@Entity(tableName = "words")
public class WordEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "word")
    private String word;

    @ColumnInfo(name = "phonetic")
    private String phonetic;

    @ColumnInfo(name = "definition")
    private String definition;

    @ColumnInfo(name = "translation")
    private String translation;

    @ColumnInfo(name = "pos")
    private String pos;

    @ColumnInfo(name = "collins")
    private Integer collins;

    @ColumnInfo(name = "oxford")
    private Integer oxford;

    @ColumnInfo(name = "tag")
    private String tag;

    @ColumnInfo(name = "bnc")
    private Integer bnc;

    @ColumnInfo(name = "frq")
    private Integer frq;

    @ColumnInfo(name = "exchange")
    private String exchange;

    @ColumnInfo(name = "detail")
    private String detail;

    @ColumnInfo(name = "audio")
    private String audio;

    public WordEntity(@NonNull String word, String phonetic, String definition,
                      String translation, String pos, Integer collins, Integer oxford,
                      String tag, Integer bnc, Integer frq, String exchange,
                      String detail, String audio) {
        this.word = word;
        this.phonetic = phonetic;
        this.definition = definition;
        this.translation = translation;
        this.pos = pos;
        this.collins = collins;
        this.oxford = oxford;
        this.tag = tag;
        this.bnc = bnc;
        this.frq = frq;
        this.exchange = exchange;
        this.detail = detail;
        this.audio = audio;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getWord() { return word; }
    public void setWord(@NonNull String word) { this.word = word; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }

    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { this.translation = translation; }

    public String getPos() { return pos; }
    public void setPos(String pos) { this.pos = pos; }

    public Integer getCollins() { return collins; }
    public void setCollins(Integer collins) { this.collins = collins; }

    public Integer getOxford() { return oxford; }
    public void setOxford(Integer oxford) { this.oxford = oxford; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public Integer getBnc() { return bnc; }
    public void setBnc(Integer bnc) { this.bnc = bnc; }

    public Integer getFrq() { return frq; }
    public void setFrq(Integer frq) { this.frq = frq; }

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getAudio() { return audio; }
    public void setAudio(String audio) { this.audio = audio; }
}
