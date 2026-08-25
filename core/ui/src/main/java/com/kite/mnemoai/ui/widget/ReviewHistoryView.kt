package com.kite.mnemoai.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.card.MaterialCardView
import com.kite.mnemoai.model.dayplan.DayPlanWord
import com.kite.mnemoai.ui.ReviewHistoryAdapter
import com.kite.mnemoai.ui.ReviewHistoryItem
import com.kite.mnemoai.ui.databinding.ViewReviewHistoryBinding
import com.kite.mnemoai.ui.formatDuration
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

class ReviewHistoryView(context: Context, attrs: AttributeSet): MaterialCardView(context, attrs){
    private val binding = ViewReviewHistoryBinding.inflate(
        LayoutInflater.from(context), this, true
    )
    private val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE)
    private val reviewAdapter: ReviewHistoryAdapter = ReviewHistoryAdapter()

    init {
        binding.studyHistoryItemsRV.adapter = reviewAdapter
        binding.studyHistoryItemsRV.layoutManager = LinearLayoutManager(context)
        binding.studyHistoryItemsRV.itemAnimator = null
        binding.studyHistoryItemsRV.isNestedScrollingEnabled = false
    }

    fun setData(reviewHistories: List<DayPlanWord>){
        val today = LocalDate.now().toString()
        val reviewHistoryItems = reviewHistories
            .filter { today != it.date }
            .map { reviewHistory ->
                sdf.format(Date(reviewHistory.learningTime))
                ReviewHistoryItem(
                    reviewHistory.wordId,
                    reviewHistory.date,
                    formatDuration(reviewHistory.learningTime),
                    1,
                    reviewHistory.blurCount,
                    reviewHistory.forgetCount
                )
            }
        reviewAdapter.submitList(ArrayList(reviewHistoryItems))
    }
}