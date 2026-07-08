package com.kite.mnemoai.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.color.MaterialColors;
import com.kite.mnemoai.databinding.ItemMineSettingSelectorBinding;
import com.kite.mnemoai.model.MineBaseItem;
import com.kite.mnemoai.model.MineSelectorItem;
import com.kite.mnemoai.utils.UIUtil;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public final static int MINE_TYPE_SELECTOR = 0;
    private List<MineBaseItem> mineBaseItems = new ArrayList<>();

    public static class SelectorViewHolder extends RecyclerView.ViewHolder{
        private final ItemMineSettingSelectorBinding binding;
        private final Context context;
        public SelectorViewHolder(@NonNull ItemMineSettingSelectorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.context = binding.getRoot().getContext();
        }

        private void bind(MineSelectorItem item){
            binding.settingTitleTV.setText(item.getTitle());
            binding.segmentedControl.setItems(item.getSelected(),item.getLastSelectedIndex(), item.getOptions().toArray(new String[0]));
            binding.segmentedControl.setOnSelectionChangedListener(integer -> {
                item.onTrigger(integer);
                return Unit.INSTANCE;
            });
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType){
            case 0:
                ItemMineSettingSelectorBinding binding = ItemMineSettingSelectorBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new SelectorViewHolder(binding);
            default:
                throw new IllegalArgumentException("未知的: viewType: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MineBaseItem item = mineBaseItems.get(position);
        if(item instanceof MineSelectorItem){
            MineSelectorItem mineSelectorItem = (MineSelectorItem) item;
            SelectorViewHolder viewHolder = (SelectorViewHolder) holder;
            viewHolder.bind(mineSelectorItem);
        }
    }

    @Override
    public int getItemCount() {
        return mineBaseItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mineBaseItems.get(position).getItemType();
    }

    public void setMineBaseItems(List<MineBaseItem> mineBaseItems) {
        if(mineBaseItems == null) return;
        this.mineBaseItems = mineBaseItems;
        notifyDataSetChanged();
    }
}
