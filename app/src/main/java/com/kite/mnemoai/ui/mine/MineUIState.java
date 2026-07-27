package com.kite.mnemoai.ui.mine;

import com.kite.mnemoai.ui.mine.model.MineBaseItem;
import com.kite.mnemoai.ui.model.LoadingState;

import java.util.List;

public class MineUIState {
    private List<MineBaseItem> mineBaseItems;
    private LoadingState<String> apiTestState;

    public MineUIState(List<MineBaseItem> mineBaseItems, LoadingState<String> apiTestState) {
        this.mineBaseItems = mineBaseItems;
        this.apiTestState = apiTestState;
    }

    public List<MineBaseItem> getMineBaseItems() {
        return mineBaseItems;
    }

    public void setMineBaseItems(List<MineBaseItem> mineBaseItems) {
        this.mineBaseItems = mineBaseItems;
    }

    public LoadingState<String> getApiTestState() {
        return apiTestState;
    }
}
