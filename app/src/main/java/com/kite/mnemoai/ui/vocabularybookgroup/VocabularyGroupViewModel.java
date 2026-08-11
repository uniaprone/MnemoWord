package com.kite.mnemoai.ui.vocabularybookgroup;

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
import com.kite.mnemoai.data.repository.GroupRepository;
import com.kite.mnemoai.data.repository.WordRepository;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class VocabularyGroupViewModel extends ViewModel {
    private MediatorLiveData<VocabularyGroupUIState> uiState = new MediatorLiveData<>();
    private WordRepository wordRepository;
    private GroupRepository groupRepository;
    private GroupEntity group;
    private List<WordListItem> words;

    @Inject
    public VocabularyGroupViewModel(WordRepository wordRepository, GroupRepository groupRepository, SavedStateHandle savedStateHandle) {
        this.wordRepository = wordRepository;
        this.groupRepository = groupRepository;
        long groupId = 0;
        if(savedStateHandle.contains("group_id") && savedStateHandle.get("group_id") != null){
             groupId = (long) savedStateHandle.get("group_id");
        }

        uiState.addSource(groupRepository.getGroupLiveDataById(groupId), (group) -> {
            this.group = group;
            updateUIState();
        });
        uiState.addSource(wordRepository.getWordListItemLiveDataById(groupId), (words) -> {
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

    public GroupEntity getGroup() {
        return group;
    }

    public void modifyVocabularyBook(String name, String desc){
        groupRepository.modifyVocabularyBook(new GroupEntity(group.getId(), name, desc, group.isLearning(), group.getCreateTime()));
    }

    public void setVocabularyBookLearningStatus(int status){
        groupRepository.setVocabularyBookLearningStatus(group.getId(), status);
    }

    public void deleteGroup(){
        groupRepository.deleteGroup(group.getId());
    }

    public LiveData<VocabularyGroupUIState> getUiState(){
        return uiState;
    }

}
