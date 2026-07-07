package com.kite.mnemoai.model;

import java.util.List;

public class MineSelectorItem extends MineBaseItem{
    private String title;
    private List<String> options;
    private int selected;
    private OnOptionSelectedListener listener;

    public interface OnOptionSelectedListener {
        void onSelected(int index);
    }

    public MineSelectorItem(String title, List<String> options, int selected, OnOptionSelectedListener listener) {
        this.title = title;
        this.options = options;
        this.selected =selected;
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

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
