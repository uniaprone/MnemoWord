package com.kite.mnemoai.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kite.mnemoai.R;
import com.kite.mnemoai.databinding.ItemMineSettingSelectorBinding;
import com.kite.mnemoai.databinding.ItemMineSettingTextBinding;
import com.kite.mnemoai.ui.mine.model.MineBaseItem;
import com.kite.mnemoai.ui.mine.model.MineSelectorItem;
import com.kite.mnemoai.ui.mine.model.MineTextItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kotlin.Unit;

public class MineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public final static int MINE_TYPE_SELECTOR = 0;
    private ApiKeyItemListener apiKeyItemListener;

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
            item.setLastSelectedIndex(item.getSelected());
            binding.segmentedControl.setOnSelectionChangedListener(integer -> {
                item.onTrigger(integer);
                return Unit.INSTANCE;
            });
        }
    }

    public static class TextViewHolder extends RecyclerView.ViewHolder{
        private ItemMineSettingTextBinding binding;
        private MineTextItem mineTextItem;
        public TextViewHolder(@NonNull ItemMineSettingTextBinding binding, ApiKeyItemListener apiKeyItemListener) {
            super(binding.getRoot());
            this.binding = binding;
            binding.settingCL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mineTextItem == null) return;
                    if (Objects.equals(mineTextItem.getTitle(), binding.getRoot().getResources().getString(R.string.setting_title_api_key))) {
                        apiKeyItemListener.onClick();
                    }
                }
            });
        }

        public void bind(MineTextItem mineTextItem){
            this.mineTextItem = mineTextItem;
            binding.settingTitleTV.setText(mineTextItem.getTitle());
            binding.infoTV.setText(mineTextItem.getInfo());
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType){
            case 0:
                ItemMineSettingSelectorBinding binding = ItemMineSettingSelectorBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new SelectorViewHolder(binding);
            case 1:
                ItemMineSettingTextBinding itemMineSettingTextBinding = ItemMineSettingTextBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new TextViewHolder(itemMineSettingTextBinding, apiKeyItemListener);
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
        if(item instanceof MineTextItem){
            MineTextItem mineTextItem = (MineTextItem) item;
            TextViewHolder viewHolder = (TextViewHolder) holder;
            viewHolder.bind(mineTextItem);
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

    public ApiKeyItemListener getApiKeyItemListener() {
        return apiKeyItemListener;
    }

    public void setApiKeyItemListener(ApiKeyItemListener apiKeyItemListener) {
        this.apiKeyItemListener = apiKeyItemListener;
    }

    public interface ApiKeyItemListener{
        void onClick();
    }
}
