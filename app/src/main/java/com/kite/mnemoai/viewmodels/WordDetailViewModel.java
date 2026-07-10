package com.kite.mnemoai.viewmodels;

import static androidx.lifecycle.SavedStateHandleSupport.createSavedStateHandle;
import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import com.kite.mnemoai.MainApplication;
import com.kite.mnemoai.data.local.DTO.WordWithExtractAndDayPlanEntity;
import com.kite.mnemoai.data.repository.IRepositoryCallback;
import com.kite.mnemoai.data.repository.UserSettingRepository;
import com.kite.mnemoai.data.repository.WordRepository;
import com.kite.mnemoai.model.WordDetailStatus;
import com.kite.mnemoai.model.WordWithExtractAndDayPlan;
import com.kite.mnemoai.uistate.WordDetailUIState;

public class WordDetailViewModel extends ViewModel {
    private MediatorLiveData<WordDetailUIState> uiState = new MediatorLiveData<>();
    private WordRepository wordRepository;
    private UserSettingRepository userSettingRepository;

    private String apiKey;
    private WordDetailStatus wordDetailStatus = WordDetailStatus.hide;

    private WordWithExtractAndDayPlan wordWithExtractAndDayPlan;
    public WordDetailViewModel(WordRepository wordRepository,
                               UserSettingRepository userSettingRepository,
                               SavedStateHandle savedStateHandle){
        this.wordRepository = wordRepository;
        this.userSettingRepository = userSettingRepository;
        long wordId = 1;
        if(savedStateHandle != null && savedStateHandle.contains("word_id")){
            wordId = savedStateHandle.get("word_id");
        }
        uiState.addSource(wordRepository.getWordWithExtractAndDayPlanLiveDataById(wordId), wordWithExtractAndDayPlan -> {
            if(wordWithExtractAndDayPlan == null) return;
            this.wordWithExtractAndDayPlan = wordWithExtractAndDayPlan;
            if(wordWithExtractAndDayPlan.getWordExtract() != null){
                this.wordDetailStatus = WordDetailStatus.show;
            }
            updateUIState();
        });

        uiState.addSource(userSettingRepository.getUserSettingLiveData(), userSetting -> {
            this.apiKey = userSetting.getApiKey();
            updateUIState();
        });
    }

    private void updateUIState(){
        uiState.setValue(new WordDetailUIState(wordWithExtractAndDayPlan, wordDetailStatus, apiKey));
    }

    public MediatorLiveData<WordDetailUIState> getUiState() {
        return uiState;
    }

    public void fetchWordExtract(){
        wordDetailStatus = WordDetailStatus.loading;
        updateUIState();

        wordRepository.fetchWordExtract(wordWithExtractAndDayPlan.getWord(), apiKey, new IRepositoryCallback<>() {
            @Override
            public void onComplete(Exception e) {
                wordDetailStatus = WordDetailStatus.error;
                updateUIState();
            }

            @Override
            public void onError(Throwable e) {
                wordDetailStatus = WordDetailStatus.error;
                updateUIState();
            }
        });
    }

    public static ViewModelInitializer<WordDetailViewModel> initializer = new ViewModelInitializer<>(
            WordDetailViewModel.class,
            creationExtras -> {
                MainApplication app = (MainApplication) creationExtras.get(APPLICATION_KEY);
                assert app != null;
                SavedStateHandle savedStateHandle = createSavedStateHandle(creationExtras);
                return new WordDetailViewModel(app.getWordRepository(), app.getUserSettingRepository(), savedStateHandle);
            }
    );
}
