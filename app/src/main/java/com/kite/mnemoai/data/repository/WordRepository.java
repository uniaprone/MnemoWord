package com.kite.mnemoai.data.repository;

import android.accounts.NetworkErrorException;
import android.app.Application;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.kite.mnemoai.MainApplication;
import com.kite.mnemoai.data.local.AppDatabase;
import com.kite.mnemoai.data.local.Converters;
import com.kite.mnemoai.data.local.DTO.WordWithExtractAndDayPlanEntity;
import com.kite.mnemoai.data.local.dao.DayPlanWordDao;
import com.kite.mnemoai.data.local.dao.GroupDao;
import com.kite.mnemoai.data.local.dao.ReviewWordDao;
import com.kite.mnemoai.data.local.dao.WordDao;
import com.kite.mnemoai.data.local.dao.WordExtractDao;
import com.kite.mnemoai.data.local.dao.WordGroupDao;
import com.kite.mnemoai.data.local.entity.DayPlanWordEntity;
import com.kite.mnemoai.data.local.entity.WordEntity;
import com.kite.mnemoai.data.local.entity.WordExtractEntity;
import com.kite.mnemoai.data.model.DayPlanWord;
import com.kite.mnemoai.data.model.WordExtract;
import com.kite.mnemoai.data.model.WordListItem;
import com.kite.mnemoai.data.model.Word;
import com.kite.mnemoai.data.model.WordReview;
import com.kite.mnemoai.data.network.DeepseekRequestBody;
import com.kite.mnemoai.data.network.DeepseekResponseBody;
import com.kite.mnemoai.data.network.DeepseekService;
import com.kite.mnemoai.data.utils.ModelTransformer;
import com.kite.mnemoai.model.WordDetailInfo;
import com.kite.mnemoai.model.WordWithExtractAndDayPlan;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WordRepository {
    private AppDatabase db;
    private ExecutorService executors;
    private Handler handler;
    private WordGroupDao wordGroupDao;
    private ReviewWordDao reviewWordDao;
    private WordDao wordDao;
    private GroupDao groupDao;
    private WordExtractDao wordExtractDao;
    private DayPlanWordDao dayPlanWordDao;
    private final Object lock = new Object();

    public WordRepository(Application app, Handler handler) {
        this.executors = MainApplication.getEXECUTOR_SERVICE();
        this.handler = handler;
        this.db = ((MainApplication) app).getAppDatabase();
        this.wordGroupDao = db.wordGroupDao();
        this.reviewWordDao = db.reviewWordDao();
        this.wordDao = db.wordDao();
        this.wordExtractDao = db.wordExtractDao();
        this.dayPlanWordDao = db.dayPlanWordDao();
        this.groupDao = db.groupDao();
    }

    public LiveData<List<Word>> getWordsByGroupId(long groupId){
        return Transformations.map(wordGroupDao.getWordsByGroupId(groupId), ModelTransformer::transformWordEntityToWord);
    }

    public void getReviewWords(IRepositoryCallback<List<Word>> callback){
        executors.execute(() -> {
            List<WordEntity> wordEntities = wordDao.getReviewWords();
            List<Word> words = ModelTransformer.transformWordEntityToWord(wordEntities);
            callback.onComplete(words);
        });
    }

    public LiveData<List<WordListItem>> getWordListItemLiveDataById(long id){
        return wordDao.getWordListItemLiveDataByGroupId(id);
    }

    public LiveData<Word> getWordById(long id){
        return Transformations.map(wordDao.getWordLiveDataById(id), ModelTransformer::transformWordEntityToWord);
    }

    public LiveData<WordReview> getWordReviewById(long id){
        return Transformations.map(reviewWordDao.getReviewWordEntityLiveData(id), ModelTransformer::transformWordReviewEntityToWordReview);
    }

    public LiveData<WordExtract> getWordExtractLiveDataById(long id){
        return  Transformations.map(wordExtractDao.getWordExtractEntityJsonLiveDataById(id), (wordExtractEntity -> {
            if(wordExtractEntity == null) return null;
            return wordExtractEntity.getExtract();
        }));
    }

    public void getWordExtractById(long id, IRepositoryCallback<WordExtract> callback){
        executors.execute(() -> {
            WordExtractEntity wordExtractEntity = wordExtractDao.getWordExtractEntityJsonById(id);
            if(wordExtractEntity == null){
                handler.post(() -> callback.onComplete(null));
            }else {
                handler.post(() -> callback.onComplete(wordExtractEntity.getExtract()));
            }
        });
    }

//    public void setDailyDayPlanWordEntities(int learningCount, IRepositoryCallback<List<WordDetailInfo>> callback){
//        executors.execute(() -> {
//            //1.获取今日的背诵单词
//            String date = LocalDate.now().toString();
//            List<DayPlanWordEntity> dayPlanWordEntities = dayPlanWordDao.queryDayPlanWordsByDate(date);
//            if(dayPlanWordEntities.isEmpty()){
//                //2.1今日首次获取
//                List<WordEntity> newLearningWordEntities = wordDao.selectTodayNewLearningWordEntities(learningCount);
//                List<WordEntity> reviewingWordEntities = wordDao.selectTodayReviewingWordEntities(date);
//                List<WordDetailInfo> wordDetailInfos = new ArrayList<>();
//                for (WordEntity word: newLearningWordEntities){
//                    wordDetailInfos.add(new WordDetailInfo(ModelTransformer.transformWordEntityToWord(word), null, null));
//                }
//                for (WordEntity word: reviewingWordEntities){
//                    wordDetailInfos.add(new WordDetailInfo(ModelTransformer.transformWordEntityToWord(word), null, null));
//                }
//                handler.post(() -> callback.onComplete(wordDetailInfos));
//                //插入选择的单词到数据库
//                //1.转化实体
//                List<DayPlanWordEntity> allDayPlanWordEntities = Stream.concat(
//                        newLearningWordEntities.stream().map((wordEntity -> new DayPlanWordEntity(wordEntity.getId(), date, 0, 0, null))),
//                        reviewingWordEntities.stream().map((wordEntity -> new DayPlanWordEntity(wordEntity.getId(), date, 1, 0, null)))
//                ).collect(Collectors.toList());
//                dayPlanWordDao.InsertDayPlanWord(allDayPlanWordEntities);
//            }else{
//                //2.2已经获取过
//                List<Long> reciteWordsIds = dayPlanWordEntities.stream().map((DayPlanWordEntity::getWordId)).collect(Collectors.toList());
//                List<WordEntity> wordEntities = wordDao.getWordsByIds(reciteWordsIds);
//                List<WordDetailInfo> wordDetailInfos = new ArrayList<>();
//                for (WordEntity word: wordEntities){
//                    wordDetailInfos.add(new WordDetailInfo(ModelTransformer.transformWordEntityToWord(word), null, null));
//                }
//                handler.post(() -> callback.onComplete(wordDetailInfos));
//            }
//        });
//    }

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
                            reviewingWordEntities.stream().map((wordEntity -> new DayPlanWordEntity(wordEntity.getId(), date, 0, 0, 0, 0, 0, null)))
                    ).collect(Collectors.toList());
                    dayPlanWordDao.InsertDayPlanWord(allDayPlanWordEntities);
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

    public LiveData<List<WordWithExtractAndDayPlan>> getUnfinishPlanWordDetailLiveData(){
        String date = LocalDate.now().toString();
        return Transformations.map(wordDao.getUnfinishPlanWordDetailLiveData(date), wordWithExtractAndDayPlanEntities ->
                wordWithExtractAndDayPlanEntities.stream()
                        .map(wordWithExtractAndDayPlanEntity -> {
                            Word word = ModelTransformer.transformWordEntityToWord(wordWithExtractAndDayPlanEntity.getWord());
                            WordExtract wordExtract;
                            WordExtractEntity wordExtractEntity = wordWithExtractAndDayPlanEntity.getWordExtract();
                            List<DayPlanWord> dayPlanWords = ModelTransformer.transformDayPlanWordEntityToDayPlanWord(wordWithExtractAndDayPlanEntity.getDayPlanWordEntities());
                            if(wordExtractEntity == null){
                                wordExtract = null;
                            }else {
                                wordExtract = wordExtractEntity.getExtract();
                            }
                            return new WordWithExtractAndDayPlan(word, wordExtract, dayPlanWords);
                        })
                        .collect(Collectors.toList()));
    }

    public LiveData<WordWithExtractAndDayPlan> getWordWithExtractAndDayPlanLiveDataById(long id){
        return Transformations.map(wordDao.getWordWithExtractAndDayPlanLiveDataById(id), wordWithExtractAndDayPlanEntity -> {
            Word word = ModelTransformer.transformWordEntityToWord(wordWithExtractAndDayPlanEntity.getWord());
            WordExtract wordExtract;
            WordExtractEntity wordExtractEntity = wordWithExtractAndDayPlanEntity.getWordExtract();
            List<DayPlanWord> dayPlanWords = ModelTransformer.transformDayPlanWordEntityToDayPlanWord(wordWithExtractAndDayPlanEntity.getDayPlanWordEntities());
            if(wordExtractEntity == null){
                wordExtract = null;
            }else {
                wordExtract = wordExtractEntity.getExtract();
            }
            return new WordWithExtractAndDayPlan(word, wordExtract, dayPlanWords);
        });
    }

    public LiveData<List<WordDetailInfo>> getDayPlanWordEntitiesLiveData() {
        String date = LocalDate.now().toString();
        return Transformations.switchMap(dayPlanWordDao.queryUnfinishedDayPlanWordsLiveDataByDate(date), dayPlanWordEntities -> {
            // 已有数据：直接根据 IDs 查询并转换
            List<Long> ids = dayPlanWordEntities.stream().map(DayPlanWordEntity::getWordId).collect(Collectors.toList());
            return Transformations.map(wordDao.getWordsLiveDataByIds(ids),
                    wordEntities -> wordEntities.stream()
                            .map(w -> new WordDetailInfo(ModelTransformer.transformWordEntityToWord(w), null, null))
                            .collect(Collectors.toList())
            );
        });
    }

    public void fetchWordExtract(Word word, IRepositoryCallback<Exception> callback){
        DeepseekService.Factory.getInstance().getDeepseekResponseBody(new DeepseekRequestBody(word.getWord(), false)).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<DeepseekResponseBody> call, Response<DeepseekResponseBody> response) {
                if(response.isSuccessful() && response.body() != null){
                    executors.execute(() -> {
                        String wordExtractString = response.body().getChoices().get(0).getMessage().getContent();
                        Log.d("接收的json数据", wordExtractString);
                        Gson gson = new Gson();
                        try{
                            WordExtract wordExtract = gson.fromJson(wordExtractString, WordExtract.class);
                            wordExtractDao.insertWordExtractEntity(new WordExtractEntity(word.getId(), Converters.stringToWordExtract(wordExtractString)));
                        } catch (JsonSyntaxException e) {
                            handler.post(() -> callback.onError(new JsonSyntaxException("接收数据格式错误")));
                        }

                    });
                }
            }
            @Override
            public void onFailure(Call<DeepseekResponseBody> call, Throwable t) {
                handler.post(() -> callback.onError(new NetworkErrorException("网络异常")));
            }
        });
    }

    public void fetchWordExtract(WordWithExtractAndDayPlan wordWithExtractAndDayPlan, IRepositoryCallback<WordExtract> callback){
        DeepseekService.Factory.getInstance().getDeepseekResponseBody(new DeepseekRequestBody(wordWithExtractAndDayPlan.getWord().getWord(), false)).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<DeepseekResponseBody> call, Response<DeepseekResponseBody> response) {
                if(response.isSuccessful() && response.body() != null){
                    executors.execute(() -> {
                        String wordExtractString = response.body().getChoices().get(0).getMessage().getContent();
                        Log.d("接收的json数据", wordExtractString);
                        try{
                            WordExtract wordExtract = Converters.stringToWordExtract(wordExtractString);
                            wordExtractDao.insertWordExtractEntity(new WordExtractEntity(wordWithExtractAndDayPlan.getWord().getId(), wordExtract));
                            handler.post(() -> callback.onComplete(wordExtract));
                        }catch (JsonSyntaxException e){
                            handler.post(() -> callback.onError(new JsonSyntaxException("接收数据格式错误")));
                        }

                    });
                }
            }
            @Override
            public void onFailure(Call<DeepseekResponseBody> call, Throwable t) {
                callback.onError(null);
            }
        });
    }


}
