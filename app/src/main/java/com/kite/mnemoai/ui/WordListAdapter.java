package com.kite.mnemoai.ui;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.kite.mnemoai.R;
import com.kite.mnemoai.data.model.WordListItem;
import com.kite.mnemoai.databinding.ItemWordListBinding;

import java.util.ArrayList;
import java.util.List;

public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.ViewHolder> {
    private OnItemClickListener listener;
    private List<WordListItem> words = new ArrayList<>();

    public WordListAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private ItemWordListBinding binding;
        private WordListItem word;
        public ViewHolder(@NonNull ItemWordListBinding binding, OnItemClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            binding.wordListItemCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(word == null) return;
                    listener.onClick(word);
                }
            });
        }

        public void binding(WordListItem word){
            this.word = word;
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
        return new ViewHolder(binding, listener);
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

    public interface OnItemClickListener{
        void onClick(WordListItem wordListItem);
    }
}
