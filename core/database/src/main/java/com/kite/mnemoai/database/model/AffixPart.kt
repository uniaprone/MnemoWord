package com.kite.mnemoai.database.model

data class AffixPart(
    val form: String,
    val meaning: String
){
    override fun toString(): String {
        return "$form-$meaning"
    }
}