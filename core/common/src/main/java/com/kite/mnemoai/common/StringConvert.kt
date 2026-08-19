package com.kite.mnemoai.common

object StringConvert {
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
