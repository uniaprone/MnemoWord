package com.kite.mnemoai.adapter;

import android.content.res.ColorStateList;
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

import java.util.ArrayList;
import java.util.List;

public class MineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public final static int MINE_TYPE_SELECTOR = 0;
    private List<MineBaseItem> mineBaseItems = new ArrayList<>();

    public static class SelectorViewHolder extends RecyclerView.ViewHolder{
        private final ItemMineSettingSelectorBinding binding;
        public SelectorViewHolder(@NonNull ItemMineSettingSelectorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(MineSelectorItem item){
            binding.settingTitleTV.setText(item.getTitle());
            binding.modelBTG.setPadding(5, 5, 5 ,5);
            binding.modelBTG.removeAllViews();
            List<String> optionsName = item.getOptions();
            int[] ids = new int[optionsName.size()];
            for (int i = 0; i < optionsName.size(); i++){
                MaterialButton button = new MaterialButton(itemView.getContext());
                button.setCheckable(true);
                int id = View.generateViewId();
                button.setId(id);
                ids[i] = id;
                button.setText(optionsName.get(i));

                int checkedColor = MaterialColors.getColor(button, com.google.android.material.R.attr.colorPrimary);
                int uncheckedColor = MaterialColors.getColor(button, com.google.android.material.R.attr.colorPrimaryContainer);

                int[][] states = new int[][]{
                        new int[]{android.R.attr.state_checked}, // 选中状态
                        new int[]{-android.R.attr.state_checked} // 未选中状态（添加负号表示“非选中”）
                };
                int[] colors = new int[]{
                        checkedColor,
                        uncheckedColor
                };
                ColorStateList backgroundTintList = new ColorStateList(states, colors);

                // 3. 应用到按钮
                button.setBackgroundTintList(backgroundTintList);
                binding.modelBTG.addView(button);
            }

            binding.modelBTG.clearOnButtonCheckedListeners();
            int selectedIndex = item.getSelected(); // 假设 MineSelectorItem 有这个 getter
            if (selectedIndex >= 0 && selectedIndex < ids.length) {
                // 注意：此时还没有注册监听器，所以调用 check 不会触发 onTrigger 回调！
                binding.modelBTG.check(ids[selectedIndex]);
            }

            binding.modelBTG.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
                @Override
                public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                    if(!isChecked) return;
                    int index = group.indexOfChild(group.findViewById(checkedId));
                    item.onTrigger(index);
                }
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
