package com.kite.mnemoai.common

object StringConvert {
    @JvmStatic
    fun convertVocabularyName(name: String): String {
        return when (name) {
            "zk" -> "中考词汇"
            "gk" -> "高考词汇"
            "cet4" -> "四级词汇"
            "cet6" -> "六级词汇"
            "ky" -> "考研词汇"
            "toefl" -> "托福词汇"
            "gre" -> "美国高考词汇"
            "ielts" -> "雅思词汇"
            else -> name
        }
    }

    @JvmStatic
    fun convertWordTypeCode(typeCode: String): String = when (typeCode) {
        "p" -> "过去式"
        "d" -> "过去分词"
        "i" -> "现在分词"
        "3" -> "第三人称单数"
        "r" -> "比较级"
        "t" -> "最高级"
        "s" -> "复数"
        "0" -> "原型"
        "1" -> "变形"
        else -> typeCode
    }
}
