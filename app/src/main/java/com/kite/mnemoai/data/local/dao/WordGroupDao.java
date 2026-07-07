package com.kite.mnemoai.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.kite.mnemoai.data.local.entity.WordEntity;
import com.kite.mnemoai.data.local.entity.WordGroupEntity;

import java.util.List;

@Dao
public interface WordGroupDao {
    @Insert
    public void insertWordGroup(WordGroupEntity wordGroup);

    @Delete
    public void deleteWordGroup(WordGroupEntity wordGroup);

    @Query("SELECT words.* FROM words " +
            "INNER JOIN word_group ON words.id = word_group.word_id " +
            "WHERE word_group.group_id = :groupId")
    public LiveData<List<WordEntity>> getWordsByGroupId(long groupId);

    @Query("SELECT * FROM word_group WHERE word_id = :wordId")
    public LiveData<WordGroupEntity> getWordGroupLiveData(long wordId);
}
