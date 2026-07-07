package com.kite.mnemoai.data.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.kite.mnemoai.MainApplication;
import com.kite.mnemoai.data.local.AppDatabase;
import com.kite.mnemoai.data.local.UserSetting;
import com.kite.mnemoai.data.local.dao.DayPlanWordDao;
import com.kite.mnemoai.data.local.dao.WordDao;
import com.kite.mnemoai.data.local.entity.DayPlanWordEntity;
import com.kite.mnemoai.data.local.entity.WordEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;


public class UserSettingRepository {
    private Context context;
    private ExecutorService executor;
    private AppDatabase db;
    private DayPlanWordDao dayPlanWordDao;
    private WordDao wordDao;
    private final MediatorLiveData<UserSetting> _userSettingMediatorLiveData = new MediatorLiveData<>();
    public UserSettingRepository(Application app) {
        this.context = ((MainApplication) app).getApplicationContext();
        this.executor = MainApplication.getEXECUTOR_SERVICE();
        this.db = ((MainApplication) app).getAppDatabase();
        this.dayPlanWordDao = db.dayPlanWordDao();
        this.wordDao = db.wordDao();
        init();
    }

    private void init(){
        SharedPreferences sp = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE);
        int newStudyCount = sp.getInt("study_word_count", 20);
        int lightDarkModel = sp.getInt("light_dark_model", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        UserSetting userSetting = new UserSetting(newStudyCount, lightDarkModel);
        _userSettingMediatorLiveData.setValue(userSetting);
    }

    private void updateUserSetting(UserSetting userSetting){
        saveToSP(userSetting);
        _userSettingMediatorLiveData.postValue(userSetting);
    }

    private void saveToSP(UserSetting userSetting){
        executor.execute(() -> {
            try{
                SharedPreferences preferences = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor= preferences.edit();
                editor.putInt("study_word_count", userSetting.getNewLearningWordCount());
                editor.putInt("light_dark_model", userSetting.getLightDarkModel());
                editor.apply();
            } catch (Exception ignored) {
            }
        });
    }

    public LiveData<UserSetting> getUserSettingLiveData() {
        return _userSettingMediatorLiveData;
    }

    public void getUserSetting(IRepositoryCallback<UserSetting> callback){
        executor.execute(() -> {
            SharedPreferences sp = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE);
            int newStudyCount = sp.getInt("study_word_count", 20);
            int lightDarkModel = sp.getInt("light_dark_model", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            UserSetting userSetting = new UserSetting(newStudyCount, lightDarkModel);
            callback.onComplete(userSetting);
        });
    }

    public void setNewLearningWordCount(int targetCount){
        executor.execute(() -> {
            String date = LocalDate.now().toString();
            List<DayPlanWordEntity> dayPlanWordEntities = dayPlanWordDao.queryDayPlanWordsByDate(date);
            int planCount = (int) dayPlanWordEntities.stream()
                    .filter(dayPlanWordEntity -> dayPlanWordEntity.getType() == 0).count();
            int finishCount = (int) dayPlanWordEntities.stream()
                    .filter(dayPlanWordEntity -> dayPlanWordEntity.getType() == 0 && dayPlanWordEntity.getStatus() == 1).count();

            if(targetCount > planCount){
                addNewLearningDayPlanWordEntities(targetCount - planCount);
            } else if (targetCount < planCount) {
                if(targetCount > finishCount){
                    removeNewLearningDayPlanWordEntities(planCount - targetCount);
                }
            }
            SharedPreferences sp = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE);
            int lightDarkModel = sp.getInt("light_dark_model", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            UserSetting userSetting = new UserSetting(targetCount, lightDarkModel);
            updateUserSetting(userSetting);
        });
    }

    public void setLightDarkModel(int model){
        executor.execute(() -> {
            SharedPreferences sp = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE);
            int newStudyCount = sp.getInt("study_word_count", 20);
            UserSetting userSetting = new UserSetting(newStudyCount, model);
            updateUserSetting(userSetting);
        });
    }

    private void addNewLearningDayPlanWordEntities(int learningCount){
        String date = LocalDate.now().toString();
        List<WordEntity> newLearningWordEntities = wordDao.selectTodayNewLearningWordEntities(learningCount, date);
        List<DayPlanWordEntity> allDayPlanWordEntities = newLearningWordEntities.stream()
                .map(wordEntity -> new DayPlanWordEntity(wordEntity.getId(), date, 0, 0, 0, 0, 0, null))
                .collect(Collectors.toList());
        dayPlanWordDao.InsertDayPlanWord(allDayPlanWordEntities);
    }

    private void removeNewLearningDayPlanWordEntities(int removeCount){
        dayPlanWordDao.deleteRandomNewLearningWord(LocalDate.now().toString(), removeCount);
    }
}
