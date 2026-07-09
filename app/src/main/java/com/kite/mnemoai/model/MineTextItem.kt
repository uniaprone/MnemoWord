package com.kite.mnemoai.model

class MineTextItem(title: String, val info: String): MineBaseItem(title) {
    override fun getItemType() = 1
}