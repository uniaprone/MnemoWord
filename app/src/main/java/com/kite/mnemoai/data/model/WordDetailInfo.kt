package com.kite.mnemoai.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.kite.mnemoai.data.local.entity.DayPlanWordEntity
import com.kite.mnemoai.data.local.entity.WordEntity
import com.kite.mnemoai.data.local.entity.WordExtractEntity

data class WordDetailInfo(
    @Embedded
    var wordEntity: WordEntity,
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