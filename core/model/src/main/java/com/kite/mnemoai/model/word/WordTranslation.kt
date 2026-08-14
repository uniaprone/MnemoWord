package com.kite.mnemoai.model.word

data class WordTranslation(
    val pos: WordPos,
    val meanings: List<WordMeaning>
) : Comparable<WordTranslation> {
    companion object {
        private val posOrder = mapOf(
            "n." to 0, "n" to 0,
            "vt." to 1, "v." to 1, "vi." to 2,
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
        val p1 = posOrder[pos.pos] ?: 12
        val p2 = posOrder[other.pos.pos] ?: 12
        return p1 - p2
    }
}
