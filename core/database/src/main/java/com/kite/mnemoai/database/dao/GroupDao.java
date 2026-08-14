package com.kite.mnemoai.database.dao;

import kotlinx.coroutines.flow.Flow;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;


import com.kite.mnemoai.database.model.AllVocabularyBookItem;
import com.kite.mnemoai.database.model.GroupDetail;
import com.kite.mnemoai.database.model.GroupEntity;
import com.kite.mnemoai.database.model.LearningVocabularyBookItem;

import java.util.List;

@Dao
public interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertGroup(GroupEntity groupEntity);

    @Update
    void updateGroup(GroupEntity groupEntity);

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
    public Flow<List<GroupDetail>> getAllGroupDeatilsLiveData();

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
    public Flow<List<GroupDetail>> getGroupDetailLiveDataById(long id);

    @Query("SELECT * FROM `groups` WHERE is_learning = 0")
    public List<GroupEntity> getAllUnlearningGroupEntities();

    @Query("UPDATE `groups` SET is_learning = 1 WHERE id IN (:ids)")
    void addLearningGroups(List<Long> ids);

    @Query("UPDATE `groups` SET is_learning = :status WHERE id = :id")
    void setVocabularyBookLearningStatus(Long id, int status);

    @Query("SELECT * FROM `groups` WHERE id = :id")
    Flow<GroupEntity> getGroupEntityById(long id);

    @Query("SELECT EXISTS (SELECT 1 FROM `groups` WHERE is_learning = 1)")
    Flow<Integer> hasLearningGroup();

    @Query("DELETE FROM `groups` WHERE id = :id")
    void deleteGroupById(Long id);

    @Query("SELECT " +
            "g.id," +
            "g.name," +
            "g.description," +
            "COUNT(DISTINCT CASE WHEN wg.word_id THEN wg.word_id END) AS total_words," +
            "COUNT(DISTINCT CASE WHEN wr.review_status = 0 OR wr.id IS NULL THEN wg.word_id END) AS learning_words," +
            "COUNT(DISTINCT CASE WHEN wr.review_status = 1 THEN wg.word_id END) AS reviewing_words," +
            "COUNT(DISTINCT CASE WHEN wr.review_status = 2 THEN wg.word_id END) AS mastered_words " +
            "FROM `groups` g " +
            "LEFT JOIN word_group wg ON wg.group_id = g.id " +
            "LEFT JOIN word_review wr ON wr.id = wg.word_id " +
            "WHERE g.is_learning = 1 " +
            "GROUP BY g.id")
    Flow<List<LearningVocabularyBookItem>> getLearningVocabularyBookItem();

    @Query("SELECT " +
            "g.id," +
            "g.name," +
            "COUNT(DISTINCT CASE WHEN wg.word_id THEN wg.word_id END) AS total_words," +
            "COUNT(DISTINCT CASE WHEN wr.review_status = 2 THEN wr.id END) AS mastered_words " +
            "FROM `groups` g " +
            "LEFT JOIN word_group wg ON wg.group_id = g.id " +
            "LEFT JOIN word_review wr ON wr.id = wg.word_id " +
            "WHERE g.is_learning = 0 " +
            "GROUP BY g.id")
    Flow<List<AllVocabularyBookItem>> getAllVocabularyBookItem();
}
