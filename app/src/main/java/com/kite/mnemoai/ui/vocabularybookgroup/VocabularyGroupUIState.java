package com.kite.mnemoai.ui.vocabularybookgroup;

import com.kite.mnemoai.data.local.entity.GroupEntity;
import com.kite.mnemoai.data.model.WordListItem;

import java.util.List;

public class VocabularyGroupUIState {
    private GroupEntity group;
    private List<WordListItem> words;

    public VocabularyGroupUIState(List<WordListItem> words) {
        this.words = words;
    }

    public VocabularyGroupUIState(GroupEntity group, List<WordListItem> words) {
        this.group = group;
        this.words = words;
    }

    public List<WordListItem> getWords() {
        return words;
    }

    public VocabularyGroupUIState(GroupEntity group) {
        this.group = group;
    }

    public GroupEntity getGroup() {
        return group;
    }
}
