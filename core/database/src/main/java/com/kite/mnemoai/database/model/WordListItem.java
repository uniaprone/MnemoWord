package com.kite.mnemoai.database.model;

import androidx.room.ColumnInfo;

public class WordListItem {
    private long id;
    private String word;
    private String phonetic;
    private String translation;
    @ColumnInfo(name = "review_status")
    private int reviewState;

    public WordListItem(long id, String phonetic, int reviewState, String translation, String word) {
        this.id = id;
        this.phonetic = phonetic;
        this.reviewState = reviewState;
        this.translation = translation;
        this.word = word;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhonetic() {
        return phonetic;
    }

    public void setPhonetic(String phonetic) {
        this.phonetic = phonetic;
    }

    public int getReviewState() {
        return reviewState;
    }

    public void setReviewState(int reviewState) {
        this.reviewState = reviewState;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
