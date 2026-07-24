package com.kite.mnemoai.ui.reciteword;

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
import com.kite.mnemoai.data.local.entity.WordExtractEntity;
import com.kite.mnemoai.data.model.WordDetailInfo;
import com.kite.mnemoai.data.repository.GroupRepository;
import com.kite.mnemoai.data.repository.IRepositoryCallback;
import com.kite.mnemoai.data.repository.StatisticsRepository;
import com.kite.mnemoai.data.repository.UserSettingRepository;
import com.kite.mnemoai.data.repository.WordRepository;
import com.kite.mnemoai.ui.model.LoadingState;

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
    private ReciteStage reciteStage;
    private List<WordDetailInfo> reciteWordDetailInfoItemUIState;
    private final List<ReciteStatistics> reciteStatistics = new ArrayList<>();
    private int totalProgress;
    private int currentProgress;
    private boolean isShowNext = true;
    private boolean isShowTranslation;
    private boolean isShowDetail;
    private LoadingState aiMnemonicLoadingState;
    private final Random random = new Random();
    private LocalDate date = LocalDate.now();
    private String apiKey;
    private boolean hasSetDailyPlanWord = false;

    public ReciteWordViewModel(
            WordRepository wordRepository,
            GroupRepository groupRepository,
            StatisticsRepository statisticsRepository,
            UserSettingRepository userSettingRepository,
            SavedStateHandle savedStateHandle
    ){
        this.wordRepository = wordRepository;
        this.groupRepository = groupRepository;
        this.statisticsRepository = statisticsRepository;
        this.userSettingRepository = userSettingRepository;
        _uiState.addSource(wordRepository.getDailyReciteStatus(), (status) -> {
            switch (status){
                case 0:
                    this.reciteStage = ReciteStage.NO_VOCABULARY;
                    break;
                case 1:
                    this.reciteStage = ReciteStage.FINISH;
                    break;
                case 2:
                    this.reciteStage = ReciteStage.IN_PROGRESS;
                    break;
            }
            updateUIStatus();
        });

        _uiState.addSource(userSettingRepository.getUserSettingLiveData(), userSetting -> {
            this.apiKey = userSetting.getApiKey();
            updateUIStatus();
            if(!hasSetDailyPlanWord){
                wordRepository.setDailyDayPlanWordEntities(userSetting.getNewLearningWordCount());
                hasSetDailyPlanWord = true;
            }
        });

        _uiState.addSource(wordRepository.getUnfinishWordDetailInfoLiveData(), wordDetailInfos -> {
            wordDetailInfos.forEach(wordDetailInfo -> {
                String phonetic = "\\" + wordDetailInfo.getWordEntity().getPhonetic() + "\\";
                String translation = wordDetailInfo.getWordEntity().getTranslation().replace("\\n", " ");
                wordDetailInfo.getWordEntity().setPhonetic(phonetic);
                wordDetailInfo.getWordEntity().setTranslation(translation);
            });

            updateOrderData(wordDetailInfos);
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

    private void updateOrderData(List<WordDetailInfo> newPlans) {
        if (this.reciteWordDetailInfoItemUIState == null) {
            // 首次初始化：直接创建新的状态列表
            this.reciteWordDetailInfoItemUIState = newPlans;
        } else {
            Map<Long, WordDetailInfo> newPlanMap = newPlans.stream()
                    .collect(Collectors.toMap(
                            plan -> plan.getWordEntity().getId(),
                            Function.identity(),
                            (existing, replacement) -> replacement
                    ));

            // 1. 新列表中不存在，旧列表中存在
            Iterator<WordDetailInfo> iterator = this.reciteWordDetailInfoItemUIState.iterator();
            while (iterator.hasNext()){
                WordDetailInfo wordDetailInfo = iterator.next();
                long wordId = wordDetailInfo.getWordEntity().getId();
                if(newPlanMap.get(wordId) == null){
                    iterator.remove();
                }
            }

            Map<Long, WordDetailInfo> oldWordDetailInfoMap =
                    this.reciteWordDetailInfoItemUIState.stream()
                            .collect(Collectors.toMap(
                                    status -> status.getWordEntity().getId(),
                                    Function.identity()
                            ));

            // 2. 新列表中存在，旧列表中不存在
            for(WordDetailInfo newPlan: newPlans){
                long worldId = newPlan.getWordEntity().getId();
                WordDetailInfo oldStatus = oldWordDetailInfoMap.get(worldId);
                if(oldStatus == null){
                    int orderSize = this.reciteWordDetailInfoItemUIState.size();
                    if(orderSize == 0){
                        this.reciteWordDetailInfoItemUIState.add(newPlan);
                    }else{
                        int insertIndex = random.nextInt(orderSize) + 1;
                        this.reciteWordDetailInfoItemUIState.add(insertIndex, newPlan);
                    }
                }else{
                    //赋新值
                    oldStatus.setWordEntity(newPlan.getWordEntity());
                    oldStatus.setWordExtractEntity(newPlan.getWordExtractEntity());
                    oldStatus.setDayPlanWordEntities(newPlan.getDayPlanWordEntities());
                }
            }
        }
    }

    public void setDailyDayPlanWordEntities(){
        userSettingRepository.getUserSetting(new IRepositoryCallback<>() {
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
        _uiState.setValue(new ReciteWordUIState(reciteStage,
                reciteWordDetailInfoItemUIState,
                totalProgress,
                currentProgress,
                isShowNext,
                isShowTranslation,
                isShowDetail,
                aiMnemonicLoadingState));
    }

    public LiveData<ReciteWordUIState> getUiState() {
        return _uiState;
    }

    public List<ReciteStatistics> getReciteStatistics() {
        return reciteStatistics;
    }

    public void showTranslation(){
        this.isShowTranslation = true;
        updateUIStatus();
    }
    private void showDetail(){
        this.isShowDetail = true;
        updateUIStatus();
    }

    public void showAll(){
        this.isShowTranslation = true;
        this.isShowDetail = true;
        updateUIStatus();
    }

    public void resetShowState(){
        this.isShowTranslation = false;
        this.isShowDetail = false;
        updateUIStatus();
    }

    public void fetchWordExtract(){
        if(this.reciteWordDetailInfoItemUIState == null || this.reciteWordDetailInfoItemUIState.isEmpty()) return;
        WordDetailInfo currentWord = this.reciteWordDetailInfoItemUIState.get(0);
        aiMnemonicLoadingState = LoadingState.LOADING;
        updateUIStatus();
        wordRepository.fetchWordExtract(currentWord.getWordEntity(), apiKey, new IRepositoryCallback<WordExtractEntity>() {
            @Override
            public void onComplete(WordExtractEntity wordExtract) {
                if(reciteWordDetailInfoItemUIState.get(0) == currentWord){
                    aiMnemonicLoadingState = LoadingState.SUCCESS;
                    updateUIStatus();
                }
            }

            @Override
            public void onError(Throwable t) {
                if(reciteWordDetailInfoItemUIState.get(0) == currentWord){
                    aiMnemonicLoadingState = LoadingState.SUCCESS;
                    updateUIStatus();
                }
            }
        });
    }

//    public void showTranslation(){
//        if(this.reciteWordItemStatusesOrder == null || this.reciteWordItemStatusesOrder.isEmpty()) return;
//        ReciteWordUIState.ReciteWordItemStatus showed = this.reciteWordItemStatusesOrder.get(0);
//        showed.setShowTranslation(true);
//        if(showed.getWordWithExtractAndDayPlan().getWordExtract() == null){
//            showed.setWordDetailStatus(WordDetailStatus.loading);
//            wordRepository.fetchWordExtract(showed.getWordWithExtractAndDayPlan(), apiKey, new IRepositoryCallback<WordExtract>() {
//                @Override
//                public void onComplete(WordExtract wordExtract) {
//                    if(reciteWordItemStatusesOrder.get(0) == showed){
//                        showed.setWordDetailStatus(WordDetailStatus.show);
//                        updateUIStatus();
//                    }
//                    else showed.setWordDetailStatus(WordDetailStatus.hide);
//                }
//
//                @Override
//                public void onError(Throwable t) {
//                    if(reciteWordItemStatusesOrder.get(0) == showed){
//                        showed.setWordDetailStatus(WordDetailStatus.error);
//                        updateUIStatus();
//                    }
//                    else showed.setWordDetailStatus(WordDetailStatus.hide);
//                }
//            });
//        }else {
//            showed.setWordDetailStatus(WordDetailStatus.show);
//        }
//        updateUIStatus();
//    }
    public void rememberWord(){
        if(this.reciteWordDetailInfoItemUIState == null || this.reciteWordDetailInfoItemUIState.isEmpty()) return;
        WordDetailInfo remembered = this.reciteWordDetailInfoItemUIState.get(0);
        this.reciteWordDetailInfoItemUIState.remove(remembered);
        ReciteStatistics rememberedReciteStatistics = reciteStatistics.stream()
                .filter(rs -> rs.getWordId() == remembered.getWordEntity().getId()).findFirst().get();
        updateReciteStatistics(remembered.getWordEntity().getId(), 2);
        statisticsRepository.rememberWord(rememberedReciteStatistics);
        this.isShowNext = true;
        updateUIStatus();
    }

    public void blurWord() {
        if(this.reciteWordDetailInfoItemUIState == null || this.reciteWordDetailInfoItemUIState.isEmpty()) return;
        WordDetailInfo blured = this.reciteWordDetailInfoItemUIState.get(0);
        resetShowState();

        this.reciteWordDetailInfoItemUIState.remove(blured);
        int size = this.reciteWordDetailInfoItemUIState.size();

        // 如果列表为空，直接添加并返回（虽然下方也会处理，但更清晰）
        if (size == 0) {
            this.reciteWordDetailInfoItemUIState.add(blured);
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
        this.reciteWordDetailInfoItemUIState.add(position, blured);
        this.isShowNext = true;
        updateReciteStatistics(blured.getWordEntity().getId(), 1);
        updateUIStatus();
    }

    public void forgetWord() {
        if(this.reciteWordDetailInfoItemUIState == null || this.reciteWordDetailInfoItemUIState.isEmpty()) return;
        WordDetailInfo forgot = this.reciteWordDetailInfoItemUIState.get(0);
        resetShowState();

        this.reciteWordDetailInfoItemUIState.remove(forgot);
        int size = this.reciteWordDetailInfoItemUIState.size();

        // 如果列表为空，直接添加并返回（虽然下方也会处理，但更清晰）
        if (size == 0) {
            this.reciteWordDetailInfoItemUIState.add(forgot);
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
        this.reciteWordDetailInfoItemUIState.add(position, forgot);
        this.isShowNext = true;
        updateReciteStatistics(forgot.getWordEntity().getId(), 0);
        updateUIStatus();
    }

    public boolean isShowNext() {
        return isShowNext;
    }

    public void setShowNext(boolean showNext) {
        this.isShowNext = showNext;
    }

    public void startReciteStatistics(){
        if(this.reciteWordDetailInfoItemUIState == null || this.reciteWordDetailInfoItemUIState.isEmpty()) return;
        this.reciteStatistics.stream()
                .filter(reciteStatistic -> reciteStatistic.wordId == this.reciteWordDetailInfoItemUIState.get(0).getWordEntity().getId())
                .findFirst().ifPresentOrElse(reciteStatistics1 ->
                        reciteStatistics1.setStartLearningTime(SystemClock.elapsedRealtime()),
                        () -> this.reciteStatistics.add(
                                new ReciteStatistics(this.reciteWordDetailInfoItemUIState.get(0).getWordEntity().getId(),
                                        SystemClock.elapsedRealtime()
                        )
                ));
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
        public void setStartLearningTime(long startLearningTime) {
            this.startLearningTime = startLearningTime;
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

