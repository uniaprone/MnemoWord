package com.kite.mnemoai.ui.vocabularybook;

import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import com.kite.mnemoai.MainApplication;
import com.kite.mnemoai.data.model.DailyStatistic;
import com.kite.mnemoai.data.local.entity.GroupEntity;
import com.kite.mnemoai.data.model.WordListItem;
import com.kite.mnemoai.data.repository.GroupRepository;
import com.kite.mnemoai.data.repository.IRepositoryCallback;
import com.kite.mnemoai.data.repository.StatisticsRepository;
import com.kite.mnemoai.data.repository.UserSettingRepository;
import com.kite.mnemoai.data.repository.WordRepository;
import com.kite.mnemoai.ui.dialog.vocabularyselect.VocabularySelectDialogFragment;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class VocabularyViewModel extends ViewModel {
    private MediatorLiveData<VocabularyUIState> uiState = new MediatorLiveData<>();
    private WordRepository wordRepository;
    private GroupRepository groupRepository;
    private StatisticsRepository statisticsRepository;
    private UserSettingRepository userSettingRepository;
    private List<LearningVocabularyBookItem> learningVocabularyBookItems;
    private List<AllVocabularyBookItem> allVocabularyBookItems;
    private DailyStatistic dailyStatistic;
    private int newLearningWordCount;

    public VocabularyViewModel(WordRepository wordRepository, GroupRepository groupRepository, StatisticsRepository statisticsRepository, UserSettingRepository userSettingRepository){
        this.wordRepository = wordRepository;
        this.groupRepository = groupRepository;
        this.statisticsRepository = statisticsRepository;
        this.userSettingRepository = userSettingRepository;
        uiState.addSource(groupRepository.getLearningVocabularyBookItem(), learningVocabularyBookItems -> {
            this.learningVocabularyBookItems = learningVocabularyBookItems;
            updateUIState();
        });
        uiState.addSource(groupRepository.getAllVocabularyBookItem(), allVocabularyBookItems -> {
            this.allVocabularyBookItems = allVocabularyBookItems;
            updateUIState();
        });

        uiState.addSource(userSettingRepository.getUserSettingLiveData(), (userSetting -> {
            this.newLearningWordCount = userSetting.getNewLearningWordCount();
            updateUIState();
        }));
        uiState.addSource(statisticsRepository.queryDailyStatisticByDate(LocalDate.now().toString()), (dailyStatistic -> {
            this.dailyStatistic = dailyStatistic;
            updateUIState();
        }));
    }

    private void updateUIState(){
        uiState.setValue(new VocabularyUIState(learningVocabularyBookItems, allVocabularyBookItems, newLearningWordCount, dailyStatistic));
    }

    public void setNewLearningWordCount(int newLearningWordCount){
        userSettingRepository.setNewLearningWordCount(newLearningWordCount);
    }

    public LiveData<VocabularyUIState> getUIState(){
        return uiState;
    }

    public void addLearningGroups(List<Long> ids){
        groupRepository.addLearningGroups(ids);
    }

    public void getVocabularySelectedInfo(Consumer<List<VocabularySelectDialogFragment.VocabularySelectInfo>> consumer){
        groupRepository.getAllUnlearningGroups(new IRepositoryCallback<List<GroupEntity>>() {
            @Override
            public void onComplete(List<GroupEntity> groups) {
                List<VocabularySelectDialogFragment.VocabularySelectInfo> vocabularySelectInfos = groups.stream()
                        .map(group ->
                                new VocabularySelectDialogFragment.VocabularySelectInfo(
                                        group.getId(),
                                        group.getName(),
                                        group.getDescription()))
                        .collect(Collectors.toList());
                consumer.accept(vocabularySelectInfos);
            }

            @Override
            public void onError(Throwable t) {

            }
        });
    }

    public void addNewVocabularyBook(String name, String desc, long timestamp){
        groupRepository.addNewOrModifyVocabularyBook(new GroupEntity(name, desc, 0, timestamp));
    }

    public int getNewLearningWordCount() {
        return newLearningWordCount;
    }

    public static final ViewModelInitializer<VocabularyViewModel> initializer = new ViewModelInitializer<>(
            VocabularyViewModel.class,
            creationExtras -> {
                MainApplication app = (MainApplication) creationExtras.get(APPLICATION_KEY);
                assert app != null;
                return new VocabularyViewModel(app.getWordRepository(), app.getGroupRepository(), app.getStatisticsRepository(), app.getUserSettingRepository());
            }
    );

}
