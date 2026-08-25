package com.kite.mnemoai.model.word

enum class WordFormType(val code: String, val displayName: String) {
    PAST("p", "过去式"),
    PAST_PARTICIPLE("d", "过去分词"),
    PRESENT_PARTICIPLE("i", "现在分词"),
    THIRD_PERSON_SINGULAR("3", "第三人称单数"),
    COMPARATIVE("r", "比较级"),
    SUPERLATIVE("t", "最高级"),
    PLURAL("s", "复数"),
    BASE("0", "原型"),
    DERIVED("1", "变形");

    companion object {
        fun fromCode(code: String): WordFormType? = entries.find { it.code == code }
    }
}