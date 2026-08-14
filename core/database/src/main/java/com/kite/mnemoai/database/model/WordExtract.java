package com.kite.mnemoai.database.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WordExtract {
    private String word;
    @SerializedName("phrases")
    private List<Phrase> phrase;
    @SerializedName("example_sentences")
    private List<ExampleSentence> exampleSentence;

    private Affix affix;

    private String explain;

    public WordExtract(String word, String explain, List<Phrase> phrase, List<ExampleSentence> exampleSentence, Affix affix) {
        this.word = word;
        this.explain = explain;
        this.phrase = phrase;
        this.exampleSentence = exampleSentence;
        this.affix = affix;
    }

    public Affix getAffix() {
        return affix;
    }

    public void setAffix(Affix affix) {
        this.affix = affix;
    }

    public List<ExampleSentence> getExampleSentence() {
        return exampleSentence;
    }

    public void setExampleSentence(List<ExampleSentence> exampleSentence) {
        this.exampleSentence = exampleSentence;
    }

    public String getExplain() {
        return explain;
    }

    public void setExplain(String explain) {
        this.explain = explain;
    }

    public List<Phrase> getPhrase() {
        return phrase;
    }

    public void setPhrase(List<Phrase> phrase) {
        this.phrase = phrase;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
