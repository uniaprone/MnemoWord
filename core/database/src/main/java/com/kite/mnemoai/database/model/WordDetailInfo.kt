package com.kite.mnemoai.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class WordDetailInfo(
    @Embedded
    var wordEntity: WordEntity,
    @Relation(
        entity = WordPosEntity::class,
        parentColumn = "id",
        entityColumn = "word_id"
    )
    var wordTranslation: MutableList<WordTranslation>,
    @Relation(
        entity = WordFormEntity::class,
        parentColumn = "id",
        entityColumn = "word_id"
    )
    var wordForm: MutableList<WordFormEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "word_id"
    )
    var wordExtractEntity: WordExtractEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "word_id"
    )
    var dayPlanWordEntities: List<DayPlanWordEntity>
)