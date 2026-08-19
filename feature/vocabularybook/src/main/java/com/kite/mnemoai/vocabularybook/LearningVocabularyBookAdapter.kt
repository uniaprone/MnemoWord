package com.kite.mnemoai.vocabularybook


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
import com.kite.mnemoai.vocabularybook.R
import com.kite.mnemoai.model.group.LearningVocabularyBookItem
import com.kite.mnemoai.vocabularybook.databinding.ItemVocabularyBookLearningBinding
import kotlin.math.roundToInt


class LearningVocabularyBookAdapter(val listener: (View, Long) -> Unit): ListAdapter<LearningVocabularyBookItem, LearningVocabularyBookAdapter.ViewHolder>(
    LearningVocabularyBookItemDiffCallback
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
            val resource = binding.root.resources
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
                android.R.attr.colorPrimary
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
            val colorSecondaryContainer = MaterialColors.getColor(
                binding.statisticPieChart,
                com.google.android.material.R.attr.colorSecondaryContainer
            )
            val colorTertiaryContainer = MaterialColors.getColor(
                binding.statisticPieChart,
                com.google.android.material.R.attr.colorTertiaryContainer
            )
            val colorOnPrimaryContainer = MaterialColors.getColor(
                binding.statisticPieChart,
                com.google.android.material.R.attr.colorOnPrimaryContainer
            )

            val pieEntries: MutableList<PieEntry?> = mutableListOf()
            pieEntries.apply {
                add(PieEntry(item.learningWords.toFloat(), resource.getString(R.string.learning)))
                add(PieEntry(item.reviewingWords.toFloat(), resource.getString(R.string.reviewing)))
                add(PieEntry(item.masteredWords.toFloat(), resource.getString(R.string.mastered)))
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
                isEnabled = true
                textColor = colorPrimary
                xEntrySpace = 2f
                formToTextSpace = 1f
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
                setExtraOffsets(-4f, 0f, 0f, -8f)
                setHoleColor(MaterialColors.getColor(
                    binding.statisticPieChart,
                    com.google.android.material.R.attr.colorSurfaceContainerLowest
                ))
                setDrawCenterText(true)
                centerText = spannable
                setData(pieData)
                invalidate()
            }
        }
    }
}