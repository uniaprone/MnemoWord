package com.kite.mnemoai.model;

public class ReciteWord {
    private long id;
    private String word;

    private String phonetic;

    private String translation;

    private String exchange;

    private String audio;

    public ReciteWord(long id, String word, String phonetic, String audio, String translation, String exchange) {
        this.audio = audio;
        this.exchange = exchange;
        this.id = id;
        this.phonetic = phonetic;
        this.translation = translation;
        this.word = word;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
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
