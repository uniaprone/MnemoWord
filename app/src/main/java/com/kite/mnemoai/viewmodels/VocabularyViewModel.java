package com.kite.mnemoai.viewmodels;

import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import com.kite.mnemoai.MainApplication;
import com.kite.mnemoai.data.local.DTO.DailyStatistic;
import com.kite.mnemoai.data.local.UserSetting;
import com.kite.mnemoai.data.model.Group;
import com.kite.mnemoai.data.model.GroupDetail;
import com.kite.mnemoai.data.repository.GroupRepository;
import com.kite.mnemoai.data.repository.IRepositoryCallback;
import com.kite.mnemoai.data.repository.StatisticsRepository;
import com.kite.mnemoai.data.repository.UserSettingRepository;
import com.kite.mnemoai.fragment.dialog.VocabularySelectDialogFragment;
import com.kite.mnemoai.uistate.VocabularyUIState;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class VocabularyViewModel extends ViewModel {
    private MediatorLiveData<VocabularyUIState> uiState = new MediatorLiveData<>();
    private GroupRepository groupRepository;
    private StatisticsRepository statisticsRepository;
    private UserSettingRepository userSettingRepository;
    private List<GroupDetail> groupDetails;
    private DailyStatistic dailyStatistic;
    private int newLearningWordCount;

    public VocabularyViewModel(GroupRepository groupRepository, StatisticsRepository statisticsRepository, UserSettingRepository userSettingRepository){
        this.groupRepository = groupRepository;
        this.statisticsRepository = statisticsRepository;
        this.userSettingRepository = userSettingRepository;
        uiState.addSource(groupRepository.getAllGroupLiveData(), (groupDetails1 -> {
            groupDetails = groupDetails1;
            updateUIState();
        }));
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
        uiState.setValue(new VocabularyUIState(groupDetails, newLearningWordCount, dailyStatistic));
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
        groupRepository.getAllUnlearningGroups(new IRepositoryCallback<List<Group>>() {
            @Override
            public void onComplete(List<Group> groups) {
                List<VocabularySelectDialogFragment.VocabularySelectInfo> vocabularySelectInfos = groups.stream()
                        .map(group ->
                                new VocabularySelectDialogFragment.VocabularySelectInfo(
                                        group.getId(),
                                        group.getName()))
                        .collect(Collectors.toList());
                consumer.accept(vocabularySelectInfos);
            }

            @Override
            public void onError(Throwable t) {

            }
        });
    }

    public static final ViewModelInitializer<VocabularyViewModel> initializer = new ViewModelInitializer<>(
            VocabularyViewModel.class,
            creationExtras -> {
                MainApplication app = (MainApplication) creationExtras.get(APPLICATION_KEY);
                assert app != null;
                return new VocabularyViewModel(app.getGroupRepository(), app.getStatisticsRepository(), app.getUserSettingRepository());
            }
    );

}
