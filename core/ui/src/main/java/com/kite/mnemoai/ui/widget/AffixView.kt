package com.kite.mnemoai.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.kite.mnemoai.model.word.Affix
import com.kite.mnemoai.model.word.AffixPart
import com.kite.mnemoai.ui.databinding.ViewAffixBinding

/**
 * 词缀展示块（前缀 / 词根 / 后缀）。
 * 显示规则：
 *  - 三个部分全部为空（或 affix 为 null）→ 整个 View 隐藏
 *  - 某个部分为空 → 只隐藏该行（含行首标签）
 */
class AffixView(context: Context, attrs: AttributeSet) : MaterialCardView(context, attrs) {

    private val binding = ViewAffixBinding.inflate(LayoutInflater.from(context), this, true)

    fun setData(affix: Affix?) {
        if (affix == null) {
            visibility = View.GONE
            return
        }
        bindPart(binding.prefixRow, binding.prefixName, binding.prefix, affix.prefix)
        bindPart(binding.rootRow, binding.rootName, binding.root, affix.root)
        bindPart(binding.suffixRow, binding.suffixName, binding.suffix, affix.suffix)

        // 前缀/词根/后缀全部为空 → 整个 View 隐藏
        val hasAny = affix.prefix != null || affix.root != null || affix.suffix != null
        visibility = if (hasAny) View.VISIBLE else View.GONE
    }

    private fun bindPart(row: View, nameTv: TextView, meaningTv: TextView, part: AffixPart?) {
        if (part == null) {
            row.visibility = View.GONE
        } else {
            nameTv.text = part.form
            meaningTv.text = part.meaning
            row.visibility = View.VISIBLE
        }
    }
}
