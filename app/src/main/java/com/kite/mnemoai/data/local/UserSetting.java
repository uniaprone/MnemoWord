package com.kite.mnemoai.data.local;

import androidx.appcompat.app.AppCompatDelegate;

public class UserSetting {
    private int newLearningWordCount;

    private int lightDarkModel;

    public UserSetting(int newLearningWordCount, int lightDarkModel) {
        this.newLearningWordCount = newLearningWordCount;
        this.lightDarkModel = lightDarkModel;
    }

    public int getNewLearningWordCount() {
        return newLearningWordCount;
    }

    public int getLightDarkModel() {
        return lightDarkModel;
    }
}
