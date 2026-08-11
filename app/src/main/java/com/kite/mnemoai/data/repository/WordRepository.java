package com.kite.mnemoai.data.repository;

import android.os.Handler;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.kite.mnemoai.data.local.AppDatabase;
import com.kite.mnemoai.data.local.dao.DayPlanWordDao;
import com.kite.mnemoai.data.local.dao.GroupDao;
import com.kite.mnemoai.data.local.dao.WordDao;
import com.kite.mnemoai.data.local.dao.WordExtractDao;
import com.kite.mnemoai.data.local.entity.DayPlanWordEntity;
import com.kite.mnemoai.data.local.entity.WordEntity;
import com.kite.mnemoai.data.model.WordDetailInfo;
import com.kite.mnemoai.data.model.WordListItem;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WordRepository {
    private final ExecutorService executors;
    private final Handler handler;
    private final WordDao wordDao;
    private final GroupDao groupDao;
    private final DayPlanWordDao dayPlanWordDao;
    private final Object lock = new Object();

    @Inject
    public WordRepository(
            Handler handler,
            ExecutorService executors,
            WordDao wordDao,
            DayPlanWordDao dayPlanWordDao,
            GroupDao groupDao
    ) {
        this.executors = executors;
        this.handler = handler;
        this.wordDao = wordDao;
        this.dayPlanWordDao = dayPlanWordDao;
        this.groupDao = groupDao;
    }

    public LiveData<List<WordListItem>> getWordListItemLiveDataById(long id){
        return wordDao.getWordListItemLiveDataByGroupId(id);
    }

    public void setDailyDayPlanWordEntities(int learningCount){
        executors.execute(() -> {
            synchronized (lock){
                //1.获取今日的背诵单词
                String date = LocalDate.now().toString();
                List<DayPlanWordEntity> dayPlanWordEntities = dayPlanWordDao.queryDayPlanWordsByDate(date);
                if(dayPlanWordEntities.isEmpty()){
                    //2.1今日首次获取
                    List<WordEntity> newLearningWordEntities = wordDao.selectTodayNewLearningWordEntities(learningCount, date);
                    List<WordEntity> reviewingWordEntities = wordDao.selectTodayReviewingWordEntities(date);
                    //插入选择的单词到数据库
                    //1.转化实体
                    List<DayPlanWordEntity> allDayPlanWordEntities = Stream.concat(
                            newLearningWordEntities.stream().map((wordEntity -> new DayPlanWordEntity(wordEntity.getId(), date, 0, 0, 0, 0, 0, null))),
                            reviewingWordEntities.stream().map((wordEntity -> new DayPlanWordEntity(wordEntity.getId(), date, 1, 0, 0, 0, 0, null)))
                    ).collect(Collectors.toList());
                    dayPlanWordDao.InsertDayPlanWord(allDayPlanWordEntities);
                }
                List<Long> newLearningWordIds = dayPlanWordDao.getTodayNewLearningWordsId(date);
                if(newLearningWordIds.size() < learningCount){
                    List<WordEntity> newLearningWordEntities = wordDao.addTodayNewLearningWordEntities(learningCount, date, newLearningWordIds);
                    dayPlanWordDao.InsertDayPlanWord(newLearningWordEntities.stream()
                            .map((wordEntity ->
                                    new DayPlanWordEntity(wordEntity.getId(), date, 0, 0, 0, 0, 0, null)))
                            .collect(Collectors.toList()));
                }

            }
        });
    }

    public LiveData<Integer> getDailyReciteStatus(){
        String date = LocalDate.now().toString();
        return Transformations.switchMap(groupDao.hasLearningGroup(), (hasLearningGroup) -> {
            if(hasLearningGroup == 0){
                return new MutableLiveData<>(0);
            }else{
                return Transformations.switchMap(dayPlanWordDao.hasDayPlanWord(date), (hasDayPlanWord) -> {
                    if(hasDayPlanWord == 0){
                        return new MutableLiveData<>(1);
                    }else {
                        return Transformations.map(dayPlanWordDao.hasUnfinishedDayPlanWord(date), (hasUnfinishedDayPlanWord) -> {
                            if(hasUnfinishedDayPlanWord == 0){
                                return 1;
                            }else {
                                return 2;
                            }
                        });
                    }
                });
            }
        });

    }

    public LiveData<List<WordDetailInfo>> getUnfinishWordDetailInfoLiveData(){
        String date = LocalDate.now().toString();
        return Transformations.map(wordDao.getUnfinishWordDetailInfoLiveData(date), wordDetailInfos -> {
                for(WordDetailInfo wordDetailInfo: wordDetailInfos){
                    wordDetailInfo.setDayPlanWordEntities(
                            wordDetailInfo.getDayPlanWordEntities().stream()
                                    .filter(dayPlanWordEntity -> !Objects.equals(dayPlanWordEntity.getDate(), date))
                                    .collect(Collectors.toList())
                    );
                }
                return wordDetailInfos;
            }
        );
    }

    public LiveData<WordDetailInfo> getWordDetailInfoLiveDataById(long id){
        return wordDao.getWordDetailInfoLiveDataById(id);
    }

    public void performSearch(String searchText, IRepositoryCallback<List<WordListItem>> callback){
        executors.execute(() -> {
            List<WordListItem> searchResult = wordDao.performSearch(searchText);
            handler.post(() -> callback.onComplete(searchResult)
            );
        });
    }

    public void performVocabularyBookAddOptionWordsSearch(long groupId, String searchText, IRepositoryCallback<List<WordListItem>> callback){
        executors.execute(() -> {
            List<WordListItem> addOptionWords = wordDao.performAddOptionSearch(groupId, searchText);
            handler.post(() -> callback.onComplete(addOptionWords));
        });
    }

    public void performVocabularyBookRemoveOptionWordsSearch(long groupId, String searchText, IRepositoryCallback<List<WordListItem>> callback){
        executors.execute(() -> {
            List<WordListItem> removeOptionWords = wordDao.performRemoveOptionSearch(groupId, searchText);
            handler.post(() -> callback.onComplete(removeOptionWords));
        });
    }
}
