package com.kite.mnemoai.viewmodels;

import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import com.kite.mnemoai.MainApplication;
import com.kite.mnemoai.data.local.DTO.StudyStatistic;
import com.kite.mnemoai.data.repository.IRepositoryCallback;
import com.kite.mnemoai.data.repository.StatisticsRepository;

import java.util.List;

public class StatisticViewModel extends ViewModel {
    private StatisticsRepository statisticsRepository;

    public StatisticViewModel(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    public void getStudyStatistic(IRepositoryCallback<List<StudyStatistic>> callback){
        statisticsRepository.getStudyStatistic(callback);
    }

    public final static ViewModelInitializer<StatisticViewModel> initializer = new ViewModelInitializer<>(
            StatisticViewModel.class,
            creationExtras -> {
                MainApplication app = (MainApplication) creationExtras.get(APPLICATION_KEY);
                assert app != null;
                return new StatisticViewModel(app.getStatisticsRepository());
            }
    );

}
