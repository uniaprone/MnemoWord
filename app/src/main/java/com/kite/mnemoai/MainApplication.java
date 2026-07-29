package com.kite.mnemoai;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.HandlerCompat;

import com.kite.mnemoai.data.local.AppDatabase;
import com.kite.mnemoai.data.local.UserSetting;
import com.kite.mnemoai.data.network.AiServiceProvider;
import com.kite.mnemoai.data.repository.AiMnemonicRepository;
import com.kite.mnemoai.data.repository.GroupRepository;
import com.kite.mnemoai.data.repository.IRepositoryCallback;
import com.kite.mnemoai.data.repository.StatisticsRepository;
import com.kite.mnemoai.data.repository.UserSettingRepository;
import com.kite.mnemoai.data.repository.WordRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlinx.coroutines.Dispatchers;

public class MainApplication extends Application {
    private static final int NUMBER_OF_THREAD = 4;
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(NUMBER_OF_THREAD);
    private Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
    private static Context appContext;
    private AppDatabase appDatabase;
    private GroupRepository groupRepository;
    private WordRepository wordRepository;
    private StatisticsRepository statisticsRepository;
    private UserSettingRepository userSettingRepository;
    private AiMnemonicRepository aiMnemonicRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        appDatabase = AppDatabase.getInstance(appContext);
        groupRepository = new GroupRepository((Application) appContext);
        wordRepository = new WordRepository((Application) appContext, mainThreadHandler);
        statisticsRepository = new StatisticsRepository((Application) appContext, mainThreadHandler);
        userSettingRepository = new UserSettingRepository((Application) appContext);

        userSettingRepository.getUserSetting(new IRepositoryCallback<UserSetting>() {
            @Override
            public void onComplete(UserSetting userSetting) {
                if(userSetting == null){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    return;
                }
                if(userSetting.getLightDarkModel() == -1){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                }else if(userSetting.getLightDarkModel() == 1){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else if (userSetting.getLightDarkModel() == 2) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }

            }

            @Override
            public void onError(Throwable t) {

            }
        });
        aiMnemonicRepository = new AiMnemonicRepository((Application) appContext, userSettingRepository, AiServiceProvider.INSTANCE, Dispatchers.getIO());

    }
    public static ExecutorService getEXECUTOR_SERVICE() {
        return EXECUTOR_SERVICE;
    }

    public GroupRepository getGroupRepository() {
        return groupRepository;
    }

    public static Context getAppContext() {
        return appContext;
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }

    public WordRepository getWordRepository() {
        return wordRepository;
    }

    public StatisticsRepository getStatisticsRepository() {
        return statisticsRepository;
    }

    public UserSettingRepository getUserSettingRepository() {
        return userSettingRepository;
    }

    public AiMnemonicRepository getAiMnemonicRepository() {
        return aiMnemonicRepository;
    }
}
