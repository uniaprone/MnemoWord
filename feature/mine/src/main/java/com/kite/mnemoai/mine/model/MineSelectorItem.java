package com.kite.mnemoai.mine.model;

import java.util.List;

public class MineSelectorItem extends MineBaseItem{
    private List<ThemeType> options;
    private int selected;
    private int lastSelectedIndex;
    private final OnOptionSelectedListener listener;

    public interface OnOptionSelectedListener {
        void onSelected(int index);
    }

    public MineSelectorItem(String title, List<ThemeType> options, int selected, int lastSelectedIndex, OnOptionSelectedListener listener) {
        super(title);
        this.options = options;
        this.selected =selected;
        this.lastSelectedIndex = lastSelectedIndex;
        this.listener = listener;
    }

    public void onTrigger(int index){
        if(listener == null) return;
        listener.onSelected(index);
    }

    @Override
    public int getItemType() {
        return 0;
    }

    public List<ThemeType> getOptions() {
        return options;
    }

    public void setOptions(List<ThemeType> options) {
        this.options = options;
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public int getLastSelectedIndex() {
        return lastSelectedIndex;
    }

    public void setLastSelectedIndex(int lastSelectedIndex) {
        this.lastSelectedIndex = lastSelectedIndex;
    }
}
