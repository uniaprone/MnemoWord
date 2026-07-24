package com.kite.mnemoai.ui.mine;

import com.kite.mnemoai.ui.mine.model.MineBaseItem;

import java.util.List;

public class MineUIState {
    private List<MineBaseItem> mineBaseItems;

    public MineUIState(List<MineBaseItem> mineBaseItems) {
        this.mineBaseItems = mineBaseItems;
    }

    public List<MineBaseItem> getMineBaseItems() {
        return mineBaseItems;
    }

    public void setMineBaseItems(List<MineBaseItem> mineBaseItems) {
        this.mineBaseItems = mineBaseItems;
    }
}
