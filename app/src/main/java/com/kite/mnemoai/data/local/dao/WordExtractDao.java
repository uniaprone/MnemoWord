package com.kite.mnemoai.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.TypeConverters;

import com.kite.mnemoai.data.local.Converters;
import com.kite.mnemoai.data.local.entity.WordExtractEntity;

@TypeConverters({Converters.class})
@Dao
public interface WordExtractDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWordExtractEntity(WordExtractEntity wordExtractEntity);

    @Query("SELECT * FROM word_extract WHERE word_id = :id")
    public LiveData<WordExtractEntity> getWordExtractEntityJsonLiveDataById(long id);

    @Query("SELECT * FROM word_extract WHERE word_id = :id")
    public WordExtractEntity getWordExtractEntityJsonById(long id);
}
