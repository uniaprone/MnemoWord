package com.kite.mnemoai.mine.model

class MineTextItem(title: String, val info: String): MineBaseItem(title) {
    override fun getItemType() = 1
}