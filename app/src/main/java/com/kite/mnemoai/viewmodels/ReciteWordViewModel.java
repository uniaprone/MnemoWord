package com.kite.mnemoai.viewmodels;

import static androidx.lifecycle.SavedStateHandleSupport.createSavedStateHandle;
import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;

import android.os.SystemClock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import com.kite.mnemoai.MainApplication;
import com.kite.mnemoai.data.local.UserSetting;
import com.kite.mnemoai.data.model.DayPlanWord;
import com.kite.mnemoai.data.model.WordExtract;
import com.kite.mnemoai.data.repository.GroupRepository;
import com.kite.mnemoai.data.repository.IRepositoryCallback;
import com.kite.mnemoai.data.repository.StatisticsRepository;
import com.kite.mnemoai.data.repository.UserSettingRepository;
import com.kite.mnemoai.data.repository.WordRepository;
import com.kite.mnemoai.model.WordDetailStatus;
import com.kite.mnemoai.model.WordWithExtractAndDayPlan;
import com.kite.mnemoai.uistate.ReciteWordUIState;
import com.kite.mnemoai.model.WordDetailInfo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReciteWordViewModel extends ViewModel {
    private WordRepository wordRepository;
    private GroupRepository groupRepository;
    private StatisticsRepository statisticsRepository;
    private UserSettingRepository userSettingRepository;
    private final MediatorLiveData<ReciteWordUIState> _uiState = new MediatorLiveData<>();
    private ReciteWordUIState.ReciteWordStatus reciteWordStatus;
    private List<ReciteWordUIState.ReciteWordItemStatus> reciteWordItemStatuses;
    private List<ReciteWordUIState.ReciteWordItemStatus> reciteWordItemStatusesOrder;
    private List<ReciteStatistics> reciteStatistics = new ArrayList<>();
    private int totalProgress;
    private int currentProgress;
    private boolean shouldAdvance;
    private Random random = new Random();
    private LocalDate date = LocalDate.now();
    private boolean hasSetDailyPlanWord = false;

    public ReciteWordViewModel(WordRepository wordRepository, GroupRepository groupRepository, StatisticsRepository statisticsRepository, UserSettingRepository userSettingRepository, SavedStateHandle savedStateHandle){
        this.wordRepository = wordRepository;
        this.groupRepository = groupRepository;
        this.statisticsRepository = statisticsRepository;
        this.userSettingRepository = userSettingRepository;
        _uiState.addSource(wordRepository.getDailyReciteStatus(), (status) -> {
            switch (status){
                case 0:
                    this.reciteWordStatus = ReciteWordUIState.ReciteWordStatus.no_vocabulary;
                    break;
                case 1:
                    this.reciteWordStatus = ReciteWordUIState.ReciteWordStatus.finish;
                    break;
                case 2:
                    this.reciteWordStatus = ReciteWordUIState.ReciteWordStatus.ok;
                    break;
            }
            updateUIStatus();
        });

        _uiState.addSource(userSettingRepository.getUserSettingLiveData(), userSetting -> {
            wordRepository.setDailyDayPlanWordEntities(userSetting.getNewLearningWordCount());
            hasSetDailyPlanWord = true;
            _uiState.removeSource(userSettingRepository.getUserSettingLiveData());
        });

        _uiState.addSource(wordRepository.getUnfinishPlanWordDetailLiveData(), wordWithExtractAndDayPlans -> {
            wordWithExtractAndDayPlans.forEach(info -> {
                String phonetic = "\\" + info.getWord().getPhonetic() + "\\";
                String translation = info.getWord().getTranslation().replace("\\n", " ");
                info.getWord().setPhonetic(phonetic);
                info.getWord().setTranslation(translation);
            });

            updateOrderData(wordWithExtractAndDayPlans);
            updateUIStatus();
        });

        _uiState.addSource(statisticsRepository.getAllPlanCountByDate(date.toString()), (allPlanCount) -> {
            this.totalProgress = allPlanCount;
            updateUIStatus();
        });

        _uiState.addSource(statisticsRepository.getFinishedPlanCountByDate(date.toString()), (finishedPlanCount) -> {
            this.currentProgress = finishedPlanCount;
            updateUIStatus();
        });
    }

    private void updateOrderData(List<WordWithExtractAndDayPlan> newPlans) {
        if (this.reciteWordItemStatusesOrder == null) {
            // 首次初始化：直接创建新的状态列表
            this.reciteWordItemStatusesOrder = newPlans.stream()
                    .map(plan -> new ReciteWordUIState.ReciteWordItemStatus(plan, WordDetailStatus.hide, false))
                    .collect(Collectors.toList());
        } else {

            Map<Long, WordWithExtractAndDayPlan> newPlanMap = newPlans.stream()
                    .collect(Collectors.toMap(
                            plan -> plan.getWord().getId(),
                            Function.identity(),
                            (existing, replacement) -> replacement
                    ));

            // 1. 新列表不存在，旧列表存在
            Iterator<ReciteWordUIState.ReciteWordItemStatus> iterator = this.reciteWordItemStatusesOrder.iterator();
            while (iterator.hasNext()){
                ReciteWordUIState.ReciteWordItemStatus reciteWordItemStatus = iterator.next();
                long wordId = reciteWordItemStatus.getWordWithExtractAndDayPlan().getWord().getId();
                WordWithExtractAndDayPlan wordWithExtractAndDayPlan = newPlanMap.get(wordId);
                if(wordWithExtractAndDayPlan == null){
                    iterator.remove();
                }
            }

            Map<Long, ReciteWordUIState.ReciteWordItemStatus> oldStatusMap =
                    this.reciteWordItemStatusesOrder.stream()
                            .collect(Collectors.toMap(
                                    status -> status.getWordWithExtractAndDayPlan().getWord().getId(),
                                    Function.identity()
                            ));

            // 2. 新列表存在，旧序列不存在
            for(WordWithExtractAndDayPlan newPlan: newPlans){
                long worldId = newPlan.getWord().getId();
                ReciteWordUIState.ReciteWordItemStatus oldStatus = oldStatusMap.get(worldId);
                if(oldStatus == null){
                    int orderSize = this.reciteWordItemStatusesOrder.size();
                    if(orderSize == 0){
                        this.reciteWordItemStatusesOrder.add(0, new ReciteWordUIState.ReciteWordItemStatus(newPlan, WordDetailStatus.hide, false));
                    }else{
                        int insertIndex = random.nextInt(orderSize) + 1;
                        this.reciteWordItemStatusesOrder.add(insertIndex, new ReciteWordUIState.ReciteWordItemStatus(newPlan, WordDetailStatus.hide, false));
                    }
                }else{
                    //赋新值
                    oldStatus.setWordWithExtractAndDayPlan(newPlan);
                }
            }
        }
    }

    public void setDailyDayPlanWordEntities(){
        userSettingRepository.getUserSetting(new IRepositoryCallback<UserSetting>() {
            @Override
            public void onComplete(UserSetting userSetting) {
                wordRepository.setDailyDayPlanWordEntities(userSetting.getNewLearningWordCount());
            }

            @Override
            public void onError(Throwable t) {

            }
        });
    }

    private void updateUIStatus(){
        _uiState.setValue(new ReciteWordUIState(reciteWordStatus, reciteWordItemStatuses, reciteWordItemStatusesOrder, totalProgress, currentProgress, shouldAdvance));
    }

    public void fetchWordExtract(WordWithExtractAndDayPlan wordWithExtractAndDayPlan, IRepositoryCallback<WordExtract> callback){
        wordRepository.fetchWordExtract(wordWithExtractAndDayPlan, callback);
    }

    public LiveData<ReciteWordUIState> getUiState() {
        return _uiState;
    }

    public List<ReciteStatistics> getReciteStatistics() {
        return reciteStatistics;
    }

    public void showTranslation(){
        if(this.reciteWordItemStatusesOrder == null || this.reciteWordItemStatusesOrder.isEmpty()) return;
        ReciteWordUIState.ReciteWordItemStatus showed = this.reciteWordItemStatusesOrder.get(0);
        showed.setShowTranslation(true);
        if(showed.getWordWithExtractAndDayPlan().getWordExtract() == null){
            showed.setWordDetailStatus(WordDetailStatus.loading);
            fetchWordExtract(showed.getWordWithExtractAndDayPlan(), new IRepositoryCallback<WordExtract>() {
                @Override
                public void onComplete(WordExtract wordExtract) {
                    if(reciteWordItemStatusesOrder.get(0) == showed) showed.setWordDetailStatus(WordDetailStatus.show);
                    else showed.setWordDetailStatus(WordDetailStatus.hide);
                }

                @Override
                public void onError(Throwable t) {
                    if(reciteWordItemStatusesOrder.get(0) == showed) showed.setWordDetailStatus(WordDetailStatus.error);
                    else showed.setWordDetailStatus(WordDetailStatus.hide);
                }
            });
        }else {
            showed.setWordDetailStatus(WordDetailStatus.show);
        }
        updateUIStatus();
    }
    public void rememberWord(){
        if(this.reciteWordItemStatusesOrder == null || this.reciteWordItemStatusesOrder.isEmpty()) return;
        ReciteWordUIState.ReciteWordItemStatus remembered = this.reciteWordItemStatusesOrder.get(0);
        this.reciteWordItemStatusesOrder.remove(remembered);
        ReciteStatistics rememberedReciteStatistics = reciteStatistics.stream()
                .filter(rs -> rs.getWordId() == remembered.getWordWithExtractAndDayPlan().getWord().getId()).findFirst().get();
        updateReciteStatistics(remembered.getWordWithExtractAndDayPlan().getWord().getId(), 2);
        statisticsRepository.rememberWord(rememberedReciteStatistics);
        this.shouldAdvance = true;
        updateUIStatus();
    }

    public void blurWord() {
        if(this.reciteWordItemStatusesOrder == null || this.reciteWordItemStatusesOrder.isEmpty()) return;
        ReciteWordUIState.ReciteWordItemStatus blured = this.reciteWordItemStatusesOrder.get(0);
        resetShowStatus(blured);

        this.reciteWordItemStatusesOrder.remove(blured);
        int size = this.reciteWordItemStatusesOrder.size();

        // 如果列表为空，直接添加并返回（虽然下方也会处理，但更清晰）
        if (size == 0) {
            this.reciteWordItemStatusesOrder.add(blured);
            updateUIStatus();
            return;
        }

        // 计算 30%~60% 区间的整数索引
        int minPos = (int) (0.6 * size);
        int maxPos = (int) (size);

        // 保证不越界
        minPos = Math.max(0, minPos);
        maxPos = Math.min(size, maxPos);
        // 理论上 minPos <= maxPos，但防止浮点误差
        if (minPos > maxPos) minPos = maxPos;

        // 随机位置（包含两端）
        int position = minPos + random.nextInt(maxPos - minPos + 1);
        this.reciteWordItemStatusesOrder.add(position, blured);
        this.shouldAdvance = true;
        updateReciteStatistics(blured.getWordWithExtractAndDayPlan().getWord().getId(), 1);
        updateUIStatus();
    }

    public void forgetWord() {
        if(this.reciteWordItemStatusesOrder == null || this.reciteWordItemStatusesOrder.isEmpty()) return;
        ReciteWordUIState.ReciteWordItemStatus forgot = this.reciteWordItemStatusesOrder.get(0);
        resetShowStatus(forgot);

        this.reciteWordItemStatusesOrder.remove(forgot);
        int size = this.reciteWordItemStatusesOrder.size();

        // 如果列表为空，直接添加并返回（虽然下方也会处理，但更清晰）
        if (size == 0) {
            this.reciteWordItemStatusesOrder.add(forgot);
            updateUIStatus();
            return;
        }

        // 计算 60%~100% 区间的整数索引
        int minPos = (int) (0.3 * size);
        int maxPos = (int) (0.6 * size);
        // 保证不越界
        minPos = Math.max(0, minPos);
        maxPos = Math.min(size, maxPos);
        // 理论上 minPos <= maxPos，但防止浮点误差
        if (minPos > maxPos) minPos = maxPos;

        // 随机位置（包含两端）
        int position = minPos + random.nextInt(maxPos - minPos + 1);
        this.reciteWordItemStatusesOrder.add(position, forgot);
        this.shouldAdvance = true;
        updateReciteStatistics(forgot.getWordWithExtractAndDayPlan().getWord().getId(), 0);
        updateUIStatus();
    }

    public boolean isShouldAdvance() {
        return shouldAdvance;
    }

    public void setShouldAdvance(boolean shouldAdvance) {
        this.shouldAdvance = shouldAdvance;
    }

    public void addReciteStatistics(){
        if(this.reciteWordItemStatusesOrder == null || this.reciteWordItemStatusesOrder.isEmpty())return;
        this.reciteStatistics.add(new ReciteStatistics(this.reciteWordItemStatusesOrder.get(0).getWordWithExtractAndDayPlan().getWord().getId(), SystemClock.elapsedRealtime()));
    }

    public void updateReciteStatistics(long wordId, int type){
        //type修改类型， 0 - 模糊， 1 - 忘记， 2 - 记住
        for (int i = 0; i< this.reciteStatistics.size(); i++){
            ReciteStatistics currentReciteStatistics = this.reciteStatistics.get(i);
            if(currentReciteStatistics.getWordId() == wordId){
                if(type == 0){
                    currentReciteStatistics.addBlurCount();
                    currentReciteStatistics.addLearningTime(SystemClock.elapsedRealtime() - currentReciteStatistics.getStartLearningTime());
                } else if (type == 1) {
                    currentReciteStatistics.addForgetCount();
                    currentReciteStatistics.addLearningTime(SystemClock.elapsedRealtime() - currentReciteStatistics.getStartLearningTime());
                } else if (type == 2) {
                    currentReciteStatistics.addLearningTime(SystemClock.elapsedRealtime() - currentReciteStatistics.getStartLearningTime());
                }
            }
        }
    }

    public void resetShowStatus(ReciteWordUIState.ReciteWordItemStatus reciteWordItemStatus){
        if(reciteWordItemStatus == null) return;
        reciteWordItemStatus.setShowTranslation(false);
        reciteWordItemStatus.setWordDetailStatus(WordDetailStatus.hide);
    }

    public static final ViewModelInitializer<ReciteWordViewModel> initializer = new ViewModelInitializer<>(
            ReciteWordViewModel.class,
            creationExtras -> {
                MainApplication app = (MainApplication) creationExtras.get(APPLICATION_KEY);
                assert app != null;
                SavedStateHandle savedStateHandle = createSavedStateHandle(creationExtras);
                return new ReciteWordViewModel(app.getWordRepository(), app.getGroupRepository(), app.getStatisticsRepository(), app.getUserSettingRepository(), savedStateHandle);
            }
    );

    public static class ReciteStatistics{
        private long wordId;
        private int blurCount;
        private int forgetCount;
        private long startLearningTime;
        private long learningTime;

        public ReciteStatistics(long wordId, long startLearningTime) {
            this.wordId = wordId;
            this.startLearningTime = startLearningTime;
        }

        public long getWordId() {
            return wordId;
        }

        public long getStartLearningTime() {
            return startLearningTime;
        }

        public void addLearningTime(long time){
            this.learningTime = this.learningTime + time;
        }

        public void addBlurCount(){
            blurCount = blurCount + 1;
        }
        public void addForgetCount(){
            forgetCount = forgetCount + 1;
        }

        public int getBlurCount() {
            return blurCount;
        }

        public int getForgetCount() {
            return forgetCount;
        }

        public long getLearningTime() {
            return learningTime;
        }
    }
}

