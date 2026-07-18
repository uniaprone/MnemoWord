package com.kite.mnemoai.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kite.mnemoai.data.local.entity.GroupEntity;
import com.kite.mnemoai.data.model.GroupDetail;

import java.util.List;

@Dao
public interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertGroup(GroupEntity groupEntity);

    @Delete
    public void deleteGroup(GroupEntity groupEntity);

    @Query("SELECT g.*, " +
            "COUNT(DISTINCT wg.word_id) AS total_count, " +
            "COUNT(DISTINCT CASE WHEN wr.id IS NULL OR wr.review_status = 0 THEN wg.word_id END) AS learning_count ," +
            "COUNT(DISTINCT CASE WHEN wr.review_status = 1 THEN wg.word_id END) AS reviewing_count ," +
            "COUNT(DISTINCT CASE WHEN wr.review_status = 2 THEN wg.word_id END) AS mastered_count " +
            "FROM `groups` g " +
            "LEFT JOIN word_group wg ON g.id = wg.group_id " +
            "LEFT JOIN words w ON wg.word_id = w.id " +
            "LEFT JOIN word_review wr ON w.id = wr.id " +
            "GROUP BY g.id")
    public LiveData<List<GroupDetail>> getAllGroupDeatilsLiveData();

    @Query("SELECT g.*, " +
            "COUNT(DISTINCT wg.word_id) AS total_count, " +
            "COUNT(DISTINCT CASE WHEN wr.id IS NULL OR wr.review_status = 0  THEN wg.word_id END) AS learning_count ," +
            "COUNT(DISTINCT CASE WHEN wr.review_status = 1 THEN wg.word_id END) AS reviewing_count ," +
            "COUNT(DISTINCT CASE WHEN wr.review_status = 2 THEN wg.word_id END) AS mastered_count " +
            "FROM `groups` g " +
            "LEFT JOIN word_group wg ON g.id = wg.group_id " +
            "LEFT JOIN words w ON wg.word_id = w.id " +
            "LEFT JOIN word_review wr ON w.id = wr.id " +
            "WHERE g.id = :id " +
            "GROUP BY g.id")
    public LiveData<List<GroupDetail>> getGroupDetailLiveDataById(long id);

    @Query("SELECT * FROM `groups` WHERE is_learning = 0")
    public List<GroupEntity> getAllUnlearningGroupEntities();

    @Query("UPDATE `groups` SET is_learning = 1 WHERE id IN (:ids)")
    void addLearningGroups(List<Long> ids);

    @Query("UPDATE `groups` SET is_learning = :status WHERE id = :id")
    void setVocabularyBookLearningStatus(Long id, int status);

    @Query("SELECT * FROM `groups` WHERE id = :id")
    LiveData<GroupEntity> getGroupEntityById(long id);

    @Query("SELECT EXISTS (SELECT 1 FROM `groups` WHERE is_learning = 1)")
    LiveData<Integer> hasLearningGroup();

    @Query("DELETE FROM `groups` WHERE id = :id")
    void deleteGroupById(Long id);
}
