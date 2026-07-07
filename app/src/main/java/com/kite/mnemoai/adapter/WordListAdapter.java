package com.kite.mnemoai.adapter;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.kite.mnemoai.R;
import com.kite.mnemoai.data.model.WordListItem;
import com.kite.mnemoai.databinding.ItemWordListBinding;

import java.util.ArrayList;
import java.util.List;

public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.ViewHolder> {
    private List<WordListItem> words = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private ItemWordListBinding binding;
        public ViewHolder(@NonNull ItemWordListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void binding(WordListItem word){
            binding.wordListItemCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle args = new Bundle();
                    args.putLong("word_id", word.getId());
                    Navigation.findNavController(view).navigate(R.id.action_vocabularyGroupFragment_to_wordDetailFragment, args);
                }
            });
            binding.wordTextView.setText(word.getWord());
            binding.phoneticTextView.setText(word.getPhonetic());
            binding.meaningTextView.setText(word.getTranslation());
            int color;
            switch (word.getReviewState()) {
                case 0:
                    color = MaterialColors.getColor(binding.statusView, R.attr.wordAwaitingLearning);
                    break;
                case 1:
                    color = MaterialColors.getColor(binding.statusView, R.attr.wordReviewing);
                    break;
                case 2:
                    color = MaterialColors.getColor(binding.statusView, R.attr.wordMastered);
                    break;
                default:
                    color = MaterialColors.getColor(binding.statusView, R.attr.wordAwaitingLearning);
            }
            binding.statusView.setBackgroundTintList(
                    ColorStateList.valueOf(color)
            );
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWordListBinding binding = ItemWordListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding(words.get(position));
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    public void setWords(List<WordListItem> words) {
        if(words == null) return;
        this.words = words;
        notifyDataSetChanged();
    }
}
