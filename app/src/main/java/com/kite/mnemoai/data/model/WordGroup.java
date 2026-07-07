package com.kite.mnemoai.data.model;

public class WordGroup {
    private long wordId;
    private long groupId;

    public WordGroup(long groupId, long wordId) {
        this.groupId = groupId;
        this.wordId = wordId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getWordId() {
        return wordId;
    }

    public void setWordId(long wordId) {
        this.wordId = wordId;
    }
}
