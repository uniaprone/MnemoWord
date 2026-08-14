package com.kite.mnemoai.model.word

data class WordExtract(
    val word: String,
    val phrases: List<Phrase>,
    val exampleSentences: List<ExampleSentence>,
    val affix: Affix?,
    val explain: String
)

data class Phrase(
    val phrase: String,
    val meaning: String
)

data class ExampleSentence(
    val sentence: String,
    val translation: String
)

data class Affix(
    val prefix: AffixPart?,
    val root: AffixPart?,
    val suffix: AffixPart?
) {
    override fun toString(): String = listOfNotNull(prefix, root, suffix).joinToString(" ")
}

data class AffixPart(
    val form: String,
    val meaning: String
) {
    override fun toString(): String = "$form-$meaning"
}
