package com.kite.mnemoai.ui.widget.wheelwidget

interface WheelAdapter {
    val itemCount: Int
    fun getItemText(position: Int): String
}