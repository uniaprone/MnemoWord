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
import com.kite.mnemoai.data.local.dao.DayPlanWordDao;
import com.kite.mnemoai.data.local.dao.GroupDao;
import com.kite.mnemoai.data.local.dao.WordDao;
import com.kite.mnemoai.data.local.dao.WordExtractDao;
import com.kite.mnemoai.data.local.entity.DayPlanWordEntity;
import com.kite.mnemoai.data.local.entity.WordEntity;
import com.kite.mnemoai.data.local.entity.WordExtractEntity;
import com.kite.mnemoai.data.model.WordExtract;
import com.kite.mnemoai.data.model.WordListItem;
import com.kite.mnemoai.data.network.DeepseekRequestBody;
import com.kite.mnemoai.data.network.DeepseekResponseBody;
import com.kite.mnemoai.data.network.DeepseekService;
import com.kite.mnemoai.data.model.WordDetailInfo;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WordRepository {
    private AppDatabase db;
    private final ExecutorService executors;
    private final Handler handler;
    private final WordDao wordDao;
    private final GroupDao groupDao;
    private final WordExtractDao wordExtractDao;
    private final DayPlanWordDao dayPlanWordDao;
    private final Object lock = new Object();

    public WordRepository(Application app, Handler handler) {
        this.executors = MainApplication.getEXECUTOR_SERVICE();
        this.handler = handler;
        this.db = ((MainApplication) app).getAppDatabase();
        this.wordDao = db.wordDao();
        this.wordExtractDao = db.wordExtractDao();
        this.dayPlanWordDao = db.dayPlanWordDao();
        this.groupDao = db.groupDao();
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

//    public LiveData<List<WordDetailInfo>> getDayPlanWordEntitiesLiveData() {
//        String date = LocalDate.now().toString();
//        return Transformations.switchMap(dayPlanWordDao.queryUnfinishedDayPlanWordsLiveDataByDate(date), dayPlanWordEntities -> {
//            // 已有数据：直接根据 IDs 查询并转换
//            List<Long> ids = dayPlanWordEntities.stream().map(DayPlanWordEntity::getWordId).collect(Collectors.toList());
//            return Transformations.map(wordDao.getWordsLiveDataByIds(ids),
//                    wordEntities -> wordEntities.stream()
//                            .map(w -> new WordDetailInfo(ModelTransformer.transformWordEntityToWord(w), null, null))
//                            .collect(Collectors.toList())
//            );
//        });
//    }

    public void fetchWordExtract(WordEntity word, String apiKey, IRepositoryCallback<WordExtractEntity> callback){
        DeepseekService.Factory.getInstance().getDeepseekResponseBody(new DeepseekRequestBody(word.getWord(), false), "Bearer " + apiKey).enqueue(new Callback<>() {
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

    public void fetchWordExtract(WordDetailInfo wordDetailInfo, String apiKey, IRepositoryCallback<WordExtractEntity> callback){
        DeepseekService.Factory.getInstance().getDeepseekResponseBody(new DeepseekRequestBody(wordDetailInfo.getWordEntity().getWord(), false), "Bearer " + apiKey).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<DeepseekResponseBody> call, Response<DeepseekResponseBody> response) {
                if(response.isSuccessful() && response.body() != null){
                    executors.execute(() -> {
                        String wordExtractString = response.body().getChoices().get(0).getMessage().getContent();
                        Log.d("接收的json数据", wordExtractString);
                        try{
                            WordExtract wordExtract = Converters.stringToWordExtract(wordExtractString);
                            WordExtractEntity wordExtractEntity = new WordExtractEntity(wordDetailInfo.getWordEntity().getId(), wordExtract);
                            wordExtractDao.insertWordExtractEntity(wordExtractEntity);
                            handler.post(() -> callback.onComplete(wordExtractEntity));
                        }catch (JsonSyntaxException e){
                            handler.post(() -> callback.onError(new JsonSyntaxException("接收数据格式错误")));
                        }

                    });
                }else{
                    handler.post(() -> callback.onError(new NetworkErrorException(String.valueOf(response.errorBody()))));
                }
            }
            @Override
            public void onFailure(Call<DeepseekResponseBody> call, Throwable t) {
                callback.onError(null);
            }
        });
    }

    public void apiKeyValidTest(String apiKey, IRepositoryCallback<Boolean> callback){
        DeepseekService.Factory.getInstance().deepseekConnectiveTest(new DeepseekRequestBody("hello",false), "Bearer " + apiKey).enqueue(new Callback<DeepseekResponseBody>() {
            @Override
            public void onResponse(Call<DeepseekResponseBody> call, Response<DeepseekResponseBody> response) {
                if(response.isSuccessful() && response.body() != null){
                    handler.post(() -> callback.onComplete(true));
                }else{
                    handler.post(() -> callback.onComplete(false));
                }
            }

            @Override
            public void onFailure(Call<DeepseekResponseBody> call, Throwable t) {
                handler.post(() -> callback.onError(new NetworkErrorException("网络异常，稍后再试")));
            }
        });
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
