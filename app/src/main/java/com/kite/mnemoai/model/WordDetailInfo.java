package com.kite.mnemoai.model;

import com.kite.mnemoai.data.model.Word;
import com.kite.mnemoai.data.model.WordExtract;
import com.kite.mnemoai.data.model.WordReview;

public class WordDetailInfo {
    private Word word;
    private WordReview wordReview;
    private WordExtract wordExtract;

    public WordDetailInfo(Word word, WordExtract wordExtract, WordReview wordReview) {
        this.word = word;
        this.wordExtract = wordExtract;
        this.wordReview = wordReview;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public WordExtract getWordExtract() {
        return wordExtract;
    }

    public void setWordExtract(WordExtract wordExtract) {
        this.wordExtract = wordExtract;
    }

    public WordReview getWordReview() {
        return wordReview;
    }

    public void setWordReview(WordReview wordReview) {
        this.wordReview = wordReview;
    }
}
