package com.kite.mnemoai.ui

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.graphics.Typeface
import androidx.annotation.ColorInt
import java.util.regex.Pattern

object TextHighlighter {

    /**
     * 在句子中高亮指定单词及其所有变体。
     *
     * @param text        完整的例句字符串
     * @param highlightTexts            原形单词（如 "a"）
     * @param forms           单词的所有变体列表（可包含原形，也可不包含）
     * @param highlightColor  高亮颜色（@ColorInt）
     * @param isBold          是否加粗
     * @param relativeSize    相对字号比例，1.0f 为默认，如 1.2f 表示放大 20%
     * @return 处理后的 SpannableString，可直接设置到 TextView
     */
    fun highlightWordWithForms(
        text: String,
        highlightTexts: List<String>,
        @ColorInt highlightColor: Int,
        style: Int = 0,
        relativeSize: Float = 1.2f
    ): SpannableString {
        val spannable = SpannableString(text)
        val targets = highlightTexts.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        if (targets.isEmpty()) return spannable
        val escapedTargets = targets.map { target -> Pattern.quote(target) }
        val regex = "\\b(${escapedTargets.joinToString("|")})\\b"
        val pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE)
        val matcher = pattern.matcher(text)

        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            // 设置颜色
            spannable.setSpan(
                ForegroundColorSpan(highlightColor),
                start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // 设置相对大小
            spannable.setSpan(
                RelativeSizeSpan(relativeSize),
                start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // 设置粗体
            val typeface = when(style){
                0 -> Typeface.NORMAL
                1 -> Typeface.BOLD
                2 -> Typeface.ITALIC
                3 -> Typeface.BOLD_ITALIC
                else -> Typeface.NORMAL
            }
            spannable.setSpan(
                StyleSpan(typeface),
                start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return spannable
    }
}