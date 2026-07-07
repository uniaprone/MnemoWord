package com.kite.mnemoai.model;

import com.kite.mnemoai.data.model.DayPlanWord;
import com.kite.mnemoai.data.model.Word;
import com.kite.mnemoai.data.model.WordExtract;

import java.util.List;

public class WordWithExtractAndDayPlan {
    private Word word;
    private WordExtract wordExtract;
    private List<DayPlanWord> dayPlanWords;

    public WordWithExtractAndDayPlan(Word word, WordExtract wordExtract, List<DayPlanWord> dayPlanWords) {
        this.word = word;
        this.wordExtract = wordExtract;
        this.dayPlanWords = dayPlanWords;
    }

    public List<DayPlanWord> getDayPlanWords() {
        return dayPlanWords;
    }

    public void setDayPlanWords(List<DayPlanWord> dayPlanWords) {
        this.dayPlanWords = dayPlanWords;
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
}
