package com.kite.mnemoai.ui.mine.model;

public abstract class MineBaseItem {
    private final String title;

    public MineBaseItem(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public abstract int getItemType();
}
