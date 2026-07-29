package com.kite.mnemoai.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.kite.mnemoai.data.local.entity.WordMeaningEntity
import com.kite.mnemoai.data.local.entity.WordPosEntity

data class WordTranslation(
    @Embedded
    val wordPos: WordPosEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "pos_id"
    )
    val wordMeanings: List<WordMeaningEntity>
): Comparable<WordTranslation>{
    companion object{
        private val posOrder = mapOf(
            "n." to 0, "n" to 0,
            "vt." to 1, "v." to 1, "vi." to 2,   // 及物动词优先于不及物
            "adj." to 3, "a." to 3,
            "adv." to 4,
            "pron." to 5,
            "num." to 6,
            "prep." to 7,
            "conj." to 8,
            "art." to 9,
            "int." to 10, "interj." to 10,
            "other." to 11
        )
    }
    override fun compareTo(other: WordTranslation): Int {
        val p1 = posOrder[wordPos.pos] ?: 12
        val p2 = posOrder[other.wordPos.pos] ?: 12
        return p1 - p2
    }

}
