package com.kite.mnemoai.ui.vocabularybook


import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.color.MaterialColors
import com.kite.mnemoai.R
import com.kite.mnemoai.databinding.ItemVocabularyBookLearningBinding
import kotlin.math.roundToInt


class LearningVocabularyBookAdapter(val listener: (View, Long) -> Unit): ListAdapter<LearningVocabularyBookItem, LearningVocabularyBookAdapter.ViewHolder>(
    LearningVocabularyBookItem.DIFF_CALLBACK
) {
    override fun onCreateViewHolder(
        p0: ViewGroup,
        p1: Int
    ): ViewHolder {
        val binding = ItemVocabularyBookLearningBinding.inflate(LayoutInflater.from(p0.context), p0, false)
        return ViewHolder(binding, listener)
    }

    override fun onBindViewHolder(
        p0: ViewHolder,
        p1: Int
    ) {
        p0.bind(getItem(p1))
    }

    class ViewHolder(
        val binding: ItemVocabularyBookLearningBinding,
        val listener: (View, Long) -> Unit
    ) : RecyclerView.ViewHolder(binding.root){
        private var item: LearningVocabularyBookItem? = null
        init {
            binding.itemVocabularyBookLearningCV.setOnClickListener { v ->
                item?.let { listener(v, it.id) }
            }
        }

        fun bind(item: LearningVocabularyBookItem){
            this.item = item
            binding.learningVocabularyBookNameTV.text = item.name
            binding.totalWordsCountTV.text = item.totalWords.toString()
            binding.learningWordsCountTV.text = item.learningWords.toString()
            binding.reviewingWordsCountTV.text = item.reviewingWords.toString()
            binding.masteredWordsCountTV.text = item.masteredWords.toString()
            binding.learningProgressPI.setProgress((item.masteredProgress * 100).roundToInt(), true)
            binding.learningProgressTV.text = "${(item.masteredProgress * 100).roundToInt()} %"

            val colorPrimary = MaterialColors.getColor(
                binding.statisticPieChart,
                androidx.appcompat.R.attr.colorPrimary
            )
            val colorSecondary = MaterialColors.getColor(
                binding.statisticPieChart,
                com.google.android.material.R.attr.colorSecondary
            )
            val colorTertiary = MaterialColors.getColor(
                binding.statisticPieChart,
                com.google.android.material.R.attr.colorTertiary
            )
            val colorOnPrimary = MaterialColors.getColor(
                binding.statisticPieChart,
                com.google.android.material.R.attr.colorOnPrimary
            )
            val colorPrimaryContainer = MaterialColors.getColor(
                binding.statisticPieChart,
                com.google.android.material.R.attr.colorPrimaryContainer
            )
            val colorOnPrimaryContainer = MaterialColors.getColor(
                binding.statisticPieChart,
                com.google.android.material.R.attr.colorOnPrimaryContainer
            )

            val pieEntries: MutableList<PieEntry?> = mutableListOf()
            pieEntries.apply {
                add(PieEntry(item.learningWords.toFloat()))
                add(PieEntry(item.reviewingWords.toFloat()))
                add(PieEntry(item.masteredWords.toFloat()))
            }
            val pieDataSet = PieDataSet(pieEntries, "").apply {
                setDrawValues(false)
                setSliceSpace(1f)
                setColors(
                    colorPrimary,
                    colorSecondary,
                    colorTertiary
                )
            }
            val pieData = PieData(pieDataSet)
            binding.statisticPieChart.legend.apply {
                isEnabled = false
                textColor = colorPrimary
            }

            val fullText = "${(item.learningProgress * 100).roundToInt()} %\n待学习"
            val spannable = SpannableString(fullText)
            val firstLineEnd = fullText.indexOf('\n') // 第一行的结束位置

// 为第一行设置样式（红色，大号）
            spannable.setSpan(
                ForegroundColorSpan(colorOnPrimaryContainer),
                0,
                firstLineEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                RelativeSizeSpan(1.5f),
                0,
                firstLineEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

// 为第二行设置样式（灰色，小号）
            spannable.setSpan(
                ForegroundColorSpan(colorOnPrimaryContainer),
                firstLineEnd + 1,
                fullText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                RelativeSizeSpan(0.8f),
                firstLineEnd + 1,
                fullText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.statisticPieChart.apply {
                setBackgroundColor(Color.TRANSPARENT)
                isDrawHoleEnabled = true
                transparentCircleRadius = 0f
                setDrawEntryLabels(false)
                description = null
                holeRadius = 60f
                setHoleColor(MaterialColors.getColor(
                    binding.statisticPieChart,
                    com.google.android.material.R.attr.colorPrimaryContainer
                ))
                setDrawCenterText(true)
                centerText = spannable
                setData(pieData)
                invalidate()
            }
        }
    }
}