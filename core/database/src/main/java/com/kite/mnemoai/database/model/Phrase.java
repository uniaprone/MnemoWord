package com.kite.mnemoai.database.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class Phrase {
    @SerializedName("phrase")
    private String phrase;
    private String meaning;

    public Phrase(String phrase, String meaning) {
        this.phrase = phrase;
        this.meaning = meaning;
    }
    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    @NonNull
    @Override
    public String toString() {
        return phrase + " " + meaning;
    }
}
