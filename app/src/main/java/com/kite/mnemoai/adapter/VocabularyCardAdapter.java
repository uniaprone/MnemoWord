package com.kite.mnemoai.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.kite.mnemoai.R;
import com.kite.mnemoai.data.model.GroupDetail;
import com.kite.mnemoai.databinding.AddVocabularyBinding;
import com.kite.mnemoai.databinding.ItemVocabularyCardBinding;
import com.kite.mnemoai.utils.StringConvert;

import java.util.ArrayList;
import java.util.List;

public class VocabularyCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_NONE = 0;
    private static final int TYPE_NOTNULL = 1;
    private int groupType;
    private IActionListener actionListener;
    private List<GroupDetail> groupDetails = new ArrayList<>();

    public interface IActionListener{
        void onAddVocabularyClick();
    }
    
    public static class AddVocabularyViewHolder extends RecyclerView.ViewHolder{
        private AddVocabularyBinding binding;

        public AddVocabularyViewHolder(AddVocabularyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void binding(int groupType, IActionListener actionListener){
            if(groupType == 1){
                binding.addVocabularyCV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        actionListener.onAddVocabularyClick();
                    }
                });
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private ItemVocabularyCardBinding binding;
        public ViewHolder(@NonNull ItemVocabularyCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void binding(GroupDetail groupDetail){
            Resources resources = binding.getRoot().getResources();
            String groupName = groupDetail.getGroupEntity().getName();
            groupName = StringConvert.convertVocabularyName(groupName);
            binding.vocabularyGroupTV.setText(groupName);
            String totalCount = resources.getString(R.string.total_count, groupDetail.getTotalWords());
            binding.totalCountTV.setText(totalCount);
            String learningCount = resources.getString(R.string.learning_count, groupDetail.getLearningCount());
            binding.learningCountTV.setText(learningCount);
            String reviewingCount = resources.getString(R.string.reviewing_count, groupDetail.getReviewingCount());
            binding.reviewingCountTV.setText(reviewingCount);
            String masteredCount = resources.getString(R.string.mastered_count, groupDetail.getMasteredCount());
            binding.masteredCountTV.setText(masteredCount);
            List<PieEntry> pieEntries = new ArrayList<>();
            if(groupDetail.getLearningCount() > 0) pieEntries.add(new PieEntry(groupDetail.getLearningCount(), resources.getString(R.string.learning)));
            if(groupDetail.getReviewingCount() > 0) pieEntries.add(new PieEntry(groupDetail.getReviewingCount(), resources.getString(R.string.reviewing)));
            if(groupDetail.getMasteredCount() > 0) pieEntries.add(new PieEntry(groupDetail.getMasteredCount(), resources.getString(R.string.mastered)));
            PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
            pieDataSet.setSliceSpace(1f);
            pieDataSet.setColors(MaterialColors.getColor(binding.statisticPieChart, androidx.appcompat.R.attr.colorPrimary),
                    MaterialColors.getColor(binding.statisticPieChart, com.google.android.material.R.attr.colorSecondary),
                    MaterialColors.getColor(binding.statisticPieChart, com.google.android.material.R.attr.colorTertiary));
            PieData pieData = new PieData(pieDataSet);
            Legend legend = binding.statisticPieChart.getLegend();
            legend.setTextColor(MaterialColors.getColor(binding.statisticPieChart, com.google.android.material.R.attr.colorPrimary));

            binding.statisticPieChart.setBackgroundColor(Color.TRANSPARENT);
            binding.statisticPieChart.setDrawHoleEnabled(true);
            binding.statisticPieChart.setHoleColor(Color.TRANSPARENT);
            binding.statisticPieChart.setTransparentCircleRadius(0f);
            binding.statisticPieChart.setDrawEntryLabels(false);
            binding.statisticPieChart.setDescription(null);
            binding.statisticPieChart.setData(pieData);
            binding.statisticPieChart.invalidate();
            binding.vocabularyCV.setOnClickListener((view) -> {
                Bundle bundle = new Bundle();
                bundle.putLong("group_id", groupDetail.getGroupEntity().getId());
                bundle.putString("group_name", groupDetail.getGroupEntity().getName());
                Navigation.findNavController(view).navigate(R.id.action_vocabularyFragment_to_vocabularyGroupFragment, bundle);
            });
        }
    }

    public VocabularyCardAdapter(int groupType, IActionListener actionListener) {
        this.groupType = groupType;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0){
            AddVocabularyBinding binding = AddVocabularyBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new AddVocabularyViewHolder(binding);
        }else{
            ItemVocabularyCardBinding binding = ItemVocabularyCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder.getItemViewType() == TYPE_NONE){
            ((AddVocabularyViewHolder) holder).binding(groupType, actionListener);
        }else{
            ((ViewHolder) holder).binding(groupDetails.get(position));
        }

    }

    @Override
    public int getItemCount() {
        return groupDetails.isEmpty() ? 1 : groupDetails.size();
    }

    @Override
    public int getItemViewType(int position) {
        return groupDetails.isEmpty() ? TYPE_NONE : TYPE_NOTNULL;
    }

    public void setGroups(List<GroupDetail> groupDetails) {
        if(groupDetails == null) return;
        this.groupDetails = groupDetails;
        notifyDataSetChanged();
    }
}
