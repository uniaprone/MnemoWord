package com.kite.mnemoai.uistate;

import com.kite.mnemoai.data.model.WordListItem;
import com.kite.mnemoai.data.model.Group;

import java.util.List;

public class VocabularyGroupUIState {
    private Group group;
    private List<WordListItem> words;

    public VocabularyGroupUIState(List<WordListItem> words) {
        this.words = words;
    }

    public VocabularyGroupUIState(Group group, List<WordListItem> words) {
        this.group = group;
        this.words = words;
    }

    public List<WordListItem> getWords() {
        return words;
    }

    public VocabularyGroupUIState(Group group) {
        this.group = group;
    }

    public Group getGroup() {
        return group;
    }
}
