package com.kite.mnemoai.ui.adapter


import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kite.mnemoai.R
import com.kite.mnemoai.databinding.ItemReviewHistoryBinding

class ReviewHistoryAdapter: ListAdapter<ReviewHistoryItem, ReviewHistoryAdapter.ViewHolder>(
    ReviewHistoryItem.DIFF_CALLBACK
) {
    override fun onCreateViewHolder(
        p0: ViewGroup,
        p1: Int
    ): ViewHolder {
        val binding = ItemReviewHistoryBinding.inflate(LayoutInflater.from(p0.context), p0, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        p0: ViewHolder,
        p1: Int
    ) {
        p0.bind(getItem(p1))
    }



    class ViewHolder(
        val binding: ItemReviewHistoryBinding
    ): RecyclerView.ViewHolder(binding.root){
        fun bind(item: ReviewHistoryItem){
            val resource: Resources = binding.root.resources
            binding.studyDateTV.text = item.reviewDate
            binding.learningTimeTV.text = resource.getString(R.string.learned_time, item.reviewTime)
            binding.rememberTimesChip.text = resource.getString(R.string.rememberTimes_count, item.rememberCount)
            binding.blurTimesChip.text = resource.getString(R.string.blurTimes_count, item.blurCount)
            binding.forgetTimesChip.text = resource.getString(R.string.forgetTimes_count, item.forgetCount)
        }
    }
}