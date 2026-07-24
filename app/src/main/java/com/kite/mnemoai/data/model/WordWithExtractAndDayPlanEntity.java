package com.kite.mnemoai.data.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.kite.mnemoai.data.local.entity.DayPlanWordEntity;
import com.kite.mnemoai.data.local.entity.WordEntity;
import com.kite.mnemoai.data.local.entity.WordExtractEntity;

import java.util.List;

public class WordWithExtractAndDayPlanEntity {
    @Embedded
    private WordEntity word;
    @Relation(
            parentColumn = "id",
            entityColumn = "word_id"
    )
    private WordExtractEntity wordExtractEntity;
    @Relation(
            parentColumn = "id",
            entityColumn = "word_id"
    )
    private List<DayPlanWordEntity> dayPlanWordEntities;

    public WordWithExtractAndDayPlanEntity(WordEntity word, WordExtractEntity wordExtractEntity, List<DayPlanWordEntity> dayPlanWordEntities) {
        this.word = word;
        this.wordExtractEntity = wordExtractEntity;
        this.dayPlanWordEntities = dayPlanWordEntities;
    }

    public List<DayPlanWordEntity> getDayPlanWordEntities() {
        return dayPlanWordEntities;
    }

    public void setDayPlanWordEntities(List<DayPlanWordEntity> dayPlanWordEntities) {
        this.dayPlanWordEntities = dayPlanWordEntities;
    }

    public WordEntity getWord() {
        return word;
    }

    public void setWord(WordEntity word) {
        this.word = word;
    }

    public WordExtractEntity getWordExtract() {
        return wordExtractEntity;
    }

    public void setWordExtract(WordExtractEntity wordExtractEntity) {
        this.wordExtractEntity = wordExtractEntity;
    }
}
