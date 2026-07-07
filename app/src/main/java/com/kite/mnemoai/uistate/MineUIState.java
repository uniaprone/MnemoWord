package com.kite.mnemoai.uistate;

import com.kite.mnemoai.model.MineBaseItem;
import com.kite.mnemoai.model.MineSelectorItem;

import java.util.ArrayList;
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
