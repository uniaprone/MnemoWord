package com.kite.mnemoai.database.model

data class Affix(
    val prefix: AffixPart?,
    val root: AffixPart?,
    val suffix: AffixPart?
){
    override fun toString(): String {
        return listOfNotNull(prefix, root, suffix).joinToString(" ")
    }
}
