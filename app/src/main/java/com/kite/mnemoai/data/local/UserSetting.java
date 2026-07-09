package com.kite.mnemoai.data.local;

import androidx.appcompat.app.AppCompatDelegate;

public class UserSetting {
    private int newLearningWordCount;

    private int lightDarkModel;

    private String apiKey;

    public UserSetting(int newLearningWordCount, int lightDarkModel, String apiKey) {
        this.newLearningWordCount = newLearningWordCount;
        this.lightDarkModel = lightDarkModel;
        this.apiKey = apiKey;
    }

    public int getNewLearningWordCount() {
        return newLearningWordCount;
    }

    public int getLightDarkModel() {
        return lightDarkModel;
    }

    public String getApiKey() {
        return apiKey;
    }
}
