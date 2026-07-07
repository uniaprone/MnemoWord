package com.kite.mnemoai.adapter;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kite.mnemoai.R;
import com.kite.mnemoai.data.model.GroupDetail;
import com.kite.mnemoai.databinding.ItemVocabularySelectBinding;
import com.kite.mnemoai.fragment.dialog.VocabularySelectDialogFragment;
import com.kite.mnemoai.utils.StringConvert;

import java.util.List;

public class VocabularySelectAdapter extends RecyclerView.Adapter<VocabularySelectAdapter.ViewHolder> {
    private final List<VocabularySelectDialogFragment.VocabularySelectInfo> vocabularySelectInfos;

    public VocabularySelectAdapter(List<VocabularySelectDialogFragment.VocabularySelectInfo> groupDetails) {
        this.vocabularySelectInfos = groupDetails;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private ItemVocabularySelectBinding binding;

        public ViewHolder(@NonNull ItemVocabularySelectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void binding(VocabularySelectDialogFragment.VocabularySelectInfo vocabularySelectInfo){
            Resources resources = binding.getRoot().getResources();
            String groupName = vocabularySelectInfo.getName();
            groupName = StringConvert.convertVocabularyName(groupName);
            binding.vocabularyNameTV.setText(groupName);
            if(vocabularySelectInfo.isSelect()){
                binding.vocabularyCheckboxIV.setImageDrawable(resources.getDrawable(R.drawable.baseline_check_box_24));
            }else{
                binding.vocabularyCheckboxIV.setImageDrawable(resources.getDrawable(R.drawable.baseline_check_box_outline_blank_24));
            }
            binding.vocabularySelectCL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    vocabularySelectInfo.setSelect(!vocabularySelectInfo.isSelect());
                    if(vocabularySelectInfo.isSelect()){
                        binding.vocabularyCheckboxIV.setImageDrawable(resources.getDrawable(R.drawable.baseline_check_box_24));
                    }else{
                        binding.vocabularyCheckboxIV.setImageDrawable(resources.getDrawable(R.drawable.baseline_check_box_outline_blank_24));
                    }

                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVocabularySelectBinding binding = ItemVocabularySelectBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding(vocabularySelectInfos.get(position));
    }

    @Override
    public int getItemCount() {
        return vocabularySelectInfos.size();
    }
}
