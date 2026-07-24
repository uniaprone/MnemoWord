package com.kite.mnemoai.ui.mine.model

class MineTextItem(title: String, val info: String): MineBaseItem(title) {
    override fun getItemType() = 1
}