package com.kite.mnemoai.ui

import androidx.recyclerview.widget.DiffUtil

data class ReviewHistoryItem(
    val id: Long,
    val reviewDate: String,
    val reviewTime: String,
    val rememberCount: Int,
    val blurCount: Int,
    val forgetCount: Int
) {
    companion object{
        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<ReviewHistoryItem>() {
            override fun areItemsTheSame(
                p0: ReviewHistoryItem,
                p1: ReviewHistoryItem
            ): Boolean {
                return p0.id == p1.id
            }

            override fun areContentsTheSame(
                p0: ReviewHistoryItem,
                p1: ReviewHistoryItem
            ): Boolean {
                return p0 == p1
            }
        }
    }
}