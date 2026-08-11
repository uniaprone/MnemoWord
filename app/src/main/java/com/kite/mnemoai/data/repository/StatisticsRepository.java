package com.kite.mnemoai.data.repository;

import android.app.Application;
import android.os.Handler;

import androidx.lifecycle.LiveData;

import com.kite.mnemoai.MainApplication;
import com.kite.mnemoai.data.local.AppDatabase;
import com.kite.mnemoai.data.model.DailyStatistic;
import com.kite.mnemoai.data.model.StudyStatistic;
import com.kite.mnemoai.data.local.dao.DayPlanDao;
import com.kite.mnemoai.data.local.dao.DayPlanWordDao;
import com.kite.mnemoai.data.local.dao.ReviewWordDao;
import com.kite.mnemoai.data.local.entity.DayPlanEntity;
import com.kite.mnemoai.data.local.entity.DayPlanWordEntity;
import com.kite.mnemoai.data.local.entity.ReviewWordEntity;
import com.kite.mnemoai.utils.MemoryAlgorithm;
import com.kite.mnemoai.ui.reciteword.ReciteWordViewModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StatisticsRepository {
    private DayPlanDao dayPlanDao;
    private DayPlanWordDao dayPlanWordDao;
    private ReviewWordDao reviewWordDao;
    private ExecutorService executor;
    private Handler handler;

    @Inject
    public StatisticsRepository(
            Handler handler,
            ExecutorService executor,
            DayPlanWordDao dayPlanWordDao,
            DayPlanDao dayPlanDao,
            ReviewWordDao reviewWordDao
    ) {
        this.executor = executor;
        this.dayPlanDao = dayPlanDao;
        this.dayPlanWordDao = dayPlanWordDao;
        this.reviewWordDao = reviewWordDao;
        this.handler = handler;
    }

    public LiveData<DayPlanEntity> getDayPlanEntityLiveData(String date){
        return dayPlanDao.getDayPlanEntityLiveData(date);
    }

    public LiveData<Integer> getAllPlanCountByDate(String date){
        return dayPlanWordDao.getAllPlanCountByDate(date);
    }

    public LiveData<Integer> getFinishedPlanCountByDate(String date){
        return dayPlanWordDao.getFinishedPlanCountByDate(date);
    }

    public LiveData<DailyStatistic> queryDailyStatisticByDate(String date){
        return dayPlanWordDao.queryDailyStatisticByDate(date);
    }

    public void rememberWord(ReciteWordViewModel.ReciteStatistics reciteStatistics){
        executor.execute(() -> {
            long wordId = reciteStatistics.getWordId();
            String dateTime = LocalDateTime.now().toString();
            LocalDate date= LocalDate.now();
            DayPlanWordEntity dayPlanWordEntity = dayPlanWordDao.queryDayPlanWordBywordIdAndDate(wordId, date.toString());
            if(dayPlanWordEntity != null){
                dayPlanWordEntity.setStatus(1);
                dayPlanWordEntity.setBlurCount(reciteStatistics.getBlurCount());
                dayPlanWordEntity.setForgetCount(reciteStatistics.getForgetCount());
                dayPlanWordEntity.setLearningTime(reciteStatistics.getLearningTime());
                dayPlanWordEntity.setCompleteTime(dateTime);
                dayPlanWordDao.InsertDayPlanWord(dayPlanWordEntity);
                ReviewWordEntity reviewWordEntity = reviewWordDao.getReviewWordEntityById(wordId);
                if(reviewWordEntity != null){
                    int reviewCount = reviewWordEntity.getReviewCount() + 1;
                    reviewWordEntity.setReviewCount(reviewCount);
                    reviewWordEntity.setNextReviewTime(
                            MemoryAlgorithm.calculateNextReviewDate(
                                    reviewCount,
                                    reciteStatistics.getBlurCount(),
                                    reciteStatistics.getForgetCount(),
                                    reciteStatistics.getLearningTime(),
                                    date
                            ).toString()
                    );
                    reviewWordDao.insertReviewWord(reviewWordEntity);
                }else{
                    reviewWordEntity =
                            new ReviewWordEntity(wordId, 1, 0,
                                    MemoryAlgorithm.calculateNextReviewDate(0, reciteStatistics.getBlurCount(),
                                            reciteStatistics.getForgetCount(),
                                            reciteStatistics.getLearningTime(),
                                            date).toString()
                            );
                    reviewWordDao.insertReviewWord(reviewWordEntity);
                }
            }
        });
    }

    public void getStudyStatistic(IRepositoryCallback<List<StudyStatistic>> callback){
        executor.execute(() -> {
            List<StudyStatistic> studyStatistics = dayPlanWordDao.getStudyStatistic();
            handler.post(() -> callback.onComplete(studyStatistics));
        });
    }
}
