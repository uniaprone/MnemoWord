package com.kite.mnemoai.ui.worddetail;

import static androidx.lifecycle.SavedStateHandleSupport.createSavedStateHandle;
import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import com.kite.mnemoai.MainApplication;
import com.kite.mnemoai.data.model.WordDetailInfo;
import com.kite.mnemoai.data.repository.IRepositoryCallback;
import com.kite.mnemoai.data.repository.UserSettingRepository;
import com.kite.mnemoai.data.repository.WordRepository;
import com.kite.mnemoai.ui.reciteword.WordDetailStatus;

public class WordDetailViewModel extends ViewModel {
    private MediatorLiveData<WordDetailUIState> uiState = new MediatorLiveData<>();
    private WordRepository wordRepository;
    private UserSettingRepository userSettingRepository;

    private String apiKey;
    private WordDetailStatus wordDetailStatus = WordDetailStatus.hide;

    private WordDetailInfo wordDetailInfo;
    public WordDetailViewModel(WordRepository wordRepository,
                               UserSettingRepository userSettingRepository,
                               SavedStateHandle savedStateHandle){
        this.wordRepository = wordRepository;
        this.userSettingRepository = userSettingRepository;
        long wordId = 1;
        if(savedStateHandle != null && savedStateHandle.contains("word_id")){
            wordId = savedStateHandle.get("word_id");
        }
        uiState.addSource(wordRepository.getWordDetailInfoLiveDataById(wordId), wordDetailInfo -> {
            this.wordDetailInfo = wordDetailInfo;
            updateUIState();
        });

        uiState.addSource(userSettingRepository.getUserSettingLiveData(), userSetting -> {
            this.apiKey = userSetting.getApiKey();
            updateUIState();
        });
    }

    private void updateUIState(){
        uiState.setValue(new WordDetailUIState(wordDetailInfo, wordDetailStatus, apiKey));
    }

    public MediatorLiveData<WordDetailUIState> getUiState() {
        return uiState;
    }

    public void fetchWordExtract(){
        wordDetailStatus = WordDetailStatus.loading;
        updateUIState();

//        wordRepository.fetchWordExtract(wordDetailInfo.getWordEntity(), apiKey, new IRepositoryCallback<>() {
//            @Override
//            public void onComplete(Exception e) {
//                wordDetailStatus = WordDetailStatus.error;
//                updateUIState();
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                wordDetailStatus = WordDetailStatus.error;
//                updateUIState();
//            }
//        });
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
