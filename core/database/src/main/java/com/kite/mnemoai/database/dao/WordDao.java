package com.kite.mnemoai.database.dao;

import kotlinx.coroutines.flow.Flow;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.kite.mnemoai.database.model.WordEntity;
import com.kite.mnemoai.database.model.WordDetailInfo;
import com.kite.mnemoai.database.model.WordListItem;

import java.util.List;

@Dao
public interface WordDao {
    @Insert
    public void insertWord(WordEntity wordEntity);

    @Delete
    public void deleteWord(WordEntity wordEntity);

    @Query("SELECT * FROM words WHERE word = :word")
    public Flow<WordEntity> getWordLiveData(String word);

    @Query("SELECT * FROM words WHERE id = :id")
    public Flow<WordEntity> getWordLiveDataById(long id);

    @Query("SELECT * FROM words WHERE id IN (:ids)")
    List<WordEntity> getWordsByIds(List<Long> ids);

    @Query("SELECT * FROM words WHERE id IN (:ids)")
    Flow<List<WordEntity>> getWordsLiveDataByIds(List<Long> ids);

    @Transaction
    @Query("SELECT * FROM words WHERE id IN (" +
            "SELECT word_id FROM day_plan_word " +
            "WHERE date = :date AND status = 0" +
            ")")
    Flow<List<WordDetailInfo>> getUnfinishWordDetailInfoLiveData(String date);

    @Transaction
    @Query("SELECT * FROM words WHERE id = :id")
    Flow<WordDetailInfo> getWordDetailInfoLiveDataById(long id);

    @Query("SELECT * FROM words INNER JOIN word_review ON words.id = word_review.id")
    List<WordEntity> getReviewWords();

    @Query("SELECT w.id," +
            "w.word, " +
            "w.phonetic, " +
            "w.translation, " +
            "wr.review_status " +
            "FROM words w " +
            "INNER JOIN word_group wg ON wg.word_id = w.id " +
            "LEFT JOIN word_review wr ON wr.id = w.id " +
            "WHERE wg.group_id = :groupId " +
            "ORDER BY w.word")
    Flow<List<WordListItem>> getWordListItemLiveDataByGroupId(long groupId);

    @Query("SELECT w.* " +
            "FROM words w " +
            "INNER JOIN DAY_PLAN_WORD dpw WHERE w.id = dpw.word_id AND dpw.date = :date")
    Flow<List<WordEntity>> getLearningWordEntityByDate(String date);

//    @Query("SELECT w.* " +
//            "   FROM words w " +
//            "   INNER JOIN word_group wg ON wg.word_id = w.id " +
//            "   INNER JOIN `groups` g ON g.id = wg.group_id AND g.is_learning = 1 " +
//            "   LEFT JOIN word_review r ON r.id = w.id " +
//            "   WHERE r.id IS NULL" +
//            "   ORDER BY RANDOM() " +
//            "   LIMIT :newCount")
//    List<WordEntity> selectTodayNewLearningWordEntities(int newCount);

    @Query("SELECT DISTINCT w.* " +
            "   FROM words w " +
            "   INNER JOIN word_group wg ON wg.word_id = w.id " +
            "   INNER JOIN `groups` g ON g.id = wg.group_id AND g.is_learning = 1 " +
            "   LEFT JOIN word_review r ON r.id = w.id " +
            "   LEFT JOIN day_plan_word dpw ON dpw.word_id = w.id AND dpw.date = :date" +
            "   WHERE r.id IS NULL AND dpw.word_id IS NULL" +
            "   ORDER BY RANDOM() " +
            "   LIMIT :newCount")
    List<WordEntity> selectTodayNewLearningWordEntities(int newCount, String date);

    @Query("SELECT DISTINCT w.* " +
            "   FROM words w " +
            "   INNER JOIN word_group wg ON wg.word_id = w.id " +
            "   INNER JOIN `groups` g ON g.id = wg.group_id AND g.is_learning = 1 " +
            "   LEFT JOIN word_review r ON r.id = w.id " +
            "   LEFT JOIN day_plan_word dpw ON dpw.word_id = w.id AND dpw.date = :date" +
            "   WHERE r.id IS NULL AND dpw.word_id IS NULL AND w.id NOT IN (:addedWordIds)" +
            "   ORDER BY RANDOM() " +
            "   LIMIT :newCount")
    List<WordEntity> addTodayNewLearningWordEntities(int newCount, String date, List<Long> addedWordIds);

    @Query("SELECT w.* " +
            "   FROM words w " +
            "   INNER JOIN word_review r ON r.id = w.id " +
            "   WHERE r.next_review_time = :today")
    List<WordEntity> selectTodayReviewingWordEntities(String today);

    @Query("SELECT w.* " +
            "   FROM words w " +
            "   INNER JOIN word_review r ON r.id = w.id " +
            "   WHERE r.next_review_time = :today")
    Flow<List<WordEntity>> selectTodayReviewingWordEntitiesLiveData(String today);

    @Query("SELECT w.id," +
            "w.word, " +
            "w.phonetic, " +
            "w.translation, " +
            "wr.review_status " +
            "FROM words w " +
            "LEFT JOIN word_review wr ON wr.id = w.id " +
            "WHERE w.word " +
            "LIKE :searchText || '%'")
    List<WordListItem> performSearch(String searchText);

    @Query("SELECT DISTINCT w.id," +
            "w.word, " +
            "w.phonetic, " +
            "w.translation, " +
            "wr.review_status " +
            "FROM words w " +
            "LEFT JOIN word_review wr ON wr.id = w.id " +
            "WHERE w.word "+
            "LIKE :searchText || '%' " +
            "AND NOT EXISTS ( " +
            "SELECT 1 FROM word_group wg " +
            "WHERE wg.word_id = w.id " +
            "AND wg.group_id = :groupId" +
            ") " +
            "LIMIT 10")
    List<WordListItem> performAddOptionSearch(long groupId, String searchText);

    @Query("SELECT DISTINCT w.id," +
            "w.word, " +
            "w.phonetic, " +
            "w.translation, " +
            "wr.review_status " +
            "FROM words w " +
            "LEFT JOIN word_group wg On w.id = wg.word_id " +
            "INNER JOIN `groups` g ON wg.group_id = g.id AND g.id = :groupId " +
            "LEFT JOIN word_review wr ON wr.id = w.id " +
            "WHERE w.word "+
            "LIKE :searchText || '%' " +
            "LIMIT 10")
    List<WordListItem> performRemoveOptionSearch(long groupId, String searchText);
}
