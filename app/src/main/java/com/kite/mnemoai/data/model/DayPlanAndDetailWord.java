package com.kite.mnemoai.data.model;

import com.kite.mnemoai.data.local.entity.DayPlanWordEntity;

public class DayPlanAndDetailWord {
    private Word word;
    private WordExtract wordExtract;
    private WordReview wordReview;
    private DayPlanWordEntity dayPlanWordEntity;

    public DayPlanAndDetailWord(Word word, WordExtract wordExtract, WordReview wordReview, DayPlanWordEntity dayPlanWordEntity) {
        this.word = word;
        this.wordExtract = wordExtract;
        this.wordReview = wordReview;
        this.dayPlanWordEntity = dayPlanWordEntity;
    }

    public DayPlanWordEntity getDayPlanWordEntity() {
        return dayPlanWordEntity;
    }

    public void setDayPlanWordEntity(DayPlanWordEntity dayPlanWordEntity) {
        this.dayPlanWordEntity = dayPlanWordEntity;
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
