package com.kite.mnemoai.ui

import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver

class KeyboardUtil(private val rootView: View) {

    private var lastHeight = 0
    private val listeners = mutableSetOf<OnKeyboardVisibilityListener>()

    fun registerListener(listener: OnKeyboardVisibilityListener) {
        listeners.add(listener)
        rootView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    fun unregisterListener(listener: OnKeyboardVisibilityListener) {
        listeners.remove(listener)
        if (listeners.isEmpty()) {
            rootView.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        }
    }

    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)
        val visibleHeight = rect.height()
        // 判断软键盘是否弹出：根布局总高度 > 可见区域高度 + 阈值
        val screenHeight = rootView.rootView.height
        val heightDiff = screenHeight - visibleHeight

        // 阈值通常设置为 200dp 或屏幕高度的 1/4
        val threshold = (200 * rootView.resources.displayMetrics.density).toInt()
        val isKeyboardVisible = heightDiff > threshold

        if (isKeyboardVisible != lastKeyboardState) {
            lastKeyboardState = isKeyboardVisible
            listeners.forEach { it.onKeyboardVisibilityChanged(isKeyboardVisible, heightDiff) }
        }
    }

    private var lastKeyboardState = false

    interface OnKeyboardVisibilityListener {
        fun onKeyboardVisibilityChanged(isVisible: Boolean, keyboardHeight: Int)
    }
}