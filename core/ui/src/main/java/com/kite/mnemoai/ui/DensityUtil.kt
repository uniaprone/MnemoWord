package com.kite.mnemoai.ui

import android.content.Context
import android.util.TypedValue

/**
 * 将 dp 值转换为 px（像素）
 */
fun Context.dpToPx(dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        resources.displayMetrics
    ).toInt()
}

/**
 * 将 dp 值转换为 px（Float 类型，适合需要精确值的场景）
 */
fun Context.dpToPxFloat(dp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        resources.displayMetrics
    )
}