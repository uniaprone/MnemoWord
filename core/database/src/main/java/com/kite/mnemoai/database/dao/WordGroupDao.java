package com.kite.mnemoai.database.dao;

import kotlinx.coroutines.flow.Flow;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kite.mnemoai.database.model.WordEntity;
import com.kite.mnemoai.database.model.WordGroupEntity;

import java.util.List;

@Dao
public interface WordGroupDao {
    @Insert
    public void insertWordGroup(WordGroupEntity wordGroup);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertWordGroups(List<WordGroupEntity> wordGroups);

    @Delete
    public void deleteWordGroup(WordGroupEntity wordGroup);

    @Query("SELECT words.* FROM words " +
            "INNER JOIN word_group ON words.id = word_group.word_id " +
            "WHERE word_group.group_id = :groupId")
    public Flow<List<WordEntity>> getWordsByGroupId(long groupId);

    @Query("SELECT * FROM word_group WHERE word_id = :wordId")
    public Flow<WordGroupEntity> getWordGroupLiveData(long wordId);

    @Query("DELETE FROM word_group WHERE group_id = :groupId AND word_id IN (:ids)")
    void deleteWordGroupItem(long groupId, List<Long> ids);
}
