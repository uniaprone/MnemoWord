package com.kite.mnemoai.viewmodels;

import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import com.kite.mnemoai.MainApplication;
import com.kite.mnemoai.R;
import com.kite.mnemoai.data.repository.UserSettingRepository;
import com.kite.mnemoai.model.MineBaseItem;
import com.kite.mnemoai.model.MineSelectorItem;
import com.kite.mnemoai.uistate.MineUIState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MineViewModel extends ViewModel {
    private MediatorLiveData<MineUIState> _uiStatus = new MediatorLiveData<>();
    private List<MineBaseItem> mineBaseItems = new ArrayList<>();
    private UserSettingRepository userSettingRepository;

    public MineViewModel(@NonNull Application app, UserSettingRepository userSettingRepository) {
        this.userSettingRepository = userSettingRepository;
        _uiStatus.addSource(userSettingRepository.getUserSettingLiveData(), userSetting -> {
            mineBaseItems = List.of(new MineSelectorItem(app.getResources().getString(R.string.light_dark_model),
                    List.of(
                            app.getResources().getString(R.string.fallow_system),
                            app.getResources().getString(R.string.light_model),
                            app.getResources().getString(R.string.dark_model)
                    ),
                    convertLightDarkModelToOption(userSetting.getLightDarkModel()),
                    getThemeLastSelectedIndex(app.getResources().getString(R.string.light_dark_model), convertLightDarkModelToOption(userSetting.getLightDarkModel())),
                    (index) -> {
                        int lightDarkModel = userSetting.getLightDarkModel();
                        if (index == 0) {
                            if(lightDarkModel == -1) return;
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            saveLightDarkModel(-1);
                        } else if (index == 1) {
                            if(lightDarkModel == 1) return;
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            saveLightDarkModel(1);
                        } else {
                            if(lightDarkModel == 2) return;
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            saveLightDarkModel(2);
                        }
                    }));
            updateUIStatus();
        });
    }

    private int convertLightDarkModelToOption(int lightDarkModel){
        if(lightDarkModel == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM){
            return 0;
        }else if(lightDarkModel == AppCompatDelegate.MODE_NIGHT_NO){
            return 1;
        }else {
            return 2;
        }
    }

    private int getThemeLastSelectedIndex(String title, int lastSelectedIndex){
        for(MineBaseItem mineBaseItem: mineBaseItems){
            if(mineBaseItem instanceof MineSelectorItem){
                MineSelectorItem themeSetting = (MineSelectorItem) mineBaseItem;
                if(Objects.equals(themeSetting.getTitle(), title)){
                    return themeSetting.getSelected();
                }
            }
        }
        return lastSelectedIndex;
    }

    private void updateUIStatus(){
        _uiStatus.setValue(new MineUIState(mineBaseItems));
    }

    public LiveData<MineUIState> getUIStatus() {
        return _uiStatus;
    }

    private void saveLightDarkModel(int index){
        userSettingRepository.setLightDarkModel(index);
    }

    public final static ViewModelInitializer<MineViewModel> initializer = new ViewModelInitializer<MineViewModel>(
            MineViewModel.class,
            creationExtras -> {
                MainApplication app = (MainApplication) creationExtras.get(APPLICATION_KEY);
                assert app != null;
                return new MineViewModel(app, app.getUserSettingRepository());
            }
    );
}
