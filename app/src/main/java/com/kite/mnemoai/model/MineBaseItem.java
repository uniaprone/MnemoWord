package com.kite.mnemoai.model;

import android.view.View;

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
