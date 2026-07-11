package com.kite.mnemoai.viewmodels;

import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;
import static androidx.lifecycle.SavedStateHandleSupport.createSavedStateHandle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import com.kite.mnemoai.MainApplication;
import com.kite.mnemoai.data.local.entity.GroupEntity;
import com.kite.mnemoai.data.model.WordListItem;
import com.kite.mnemoai.data.model.Group;
import com.kite.mnemoai.data.repository.GroupRepository;
import com.kite.mnemoai.data.repository.WordRepository;
import com.kite.mnemoai.uistate.VocabularyGroupUIState;

import java.util.List;
import java.util.stream.Collectors;

public class VocabularyGroupViewModel extends ViewModel {
    private MediatorLiveData<VocabularyGroupUIState> uiState = new MediatorLiveData<>();
    private WordRepository wordRepository;
    private GroupRepository groupRepository;
    private Group group;
    private List<WordListItem> words;

    public VocabularyGroupViewModel(WordRepository wordRepository, GroupRepository groupRepository, SavedStateHandle savedStateHandle) {
        this.wordRepository = wordRepository;
        this.groupRepository = groupRepository;
        long groupId = 0;
        if(savedStateHandle.contains("group_id") && savedStateHandle.get("group_id") != null){
             groupId = (long) savedStateHandle.get("group_id");
        }

        LiveData<Group> groupLiveData = groupRepository.getGroupLiveDataById(groupId);
        LiveData<List<WordListItem>> wordListLiveData = wordRepository.getWordListItemLiveDataById(groupId);
        uiState.addSource(groupLiveData, (group) -> {
            this.group = group;
            updateUIState();
        });
        uiState.addSource(wordListLiveData, (words) -> {
            this.words = words.stream().peek(word -> {
                word.setPhonetic("/" + word.getPhonetic() + "/");
                word.setTranslation(word.getTranslation().replace("\\n", " "));
            }).collect(Collectors.toList());
            updateUIState();
        });
    }

    public void updateUIState(){
        uiState.setValue(new VocabularyGroupUIState(group, words));
    }

    public Group getGroup() {
        return group;
    }

    public void modifyVocabularyBook(String name, String desc){
        groupRepository.addNewOrModifyVocabularyBook(new GroupEntity(group.getId(), name, desc, group.getIsLearning(), group.getCreateTime()));
    }

    public void setVocabularyBookLearningStatus(int status){
        groupRepository.setVocabularyBookLearningStatus(getGroup().getId(), status);
    }

    public void deleteGroup(){
        groupRepository.deleteGroup(getGroup().getId());
    }

    public LiveData<VocabularyGroupUIState> getUiState(){
        return uiState;
    }

    public static final ViewModelInitializer<VocabularyGroupViewModel> initializer = new ViewModelInitializer<>(
            VocabularyGroupViewModel.class,
            creationExtras -> {
                MainApplication app = (MainApplication) creationExtras.get(APPLICATION_KEY);
                assert app != null;
                SavedStateHandle savedStateHandle = createSavedStateHandle(creationExtras);
                return new VocabularyGroupViewModel(app.getWordRepository(), app.getGroupRepository(), savedStateHandle);
            }
    );
}
