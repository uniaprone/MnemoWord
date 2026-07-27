package com.kite.mnemoai.ui.statistic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.kite.mnemoai.ui.main.MainActivity;
import com.kite.mnemoai.R;
import com.kite.mnemoai.data.model.StudyStatistic;
import com.kite.mnemoai.data.repository.IRepositoryCallback;
import com.kite.mnemoai.databinding.FragmentStatisticBinding;
import com.kite.mnemoai.stateholder.StatisticChart;
import com.kite.mnemoai.ui.main.MainViewModel;

import java.util.List;

public class StatisticFragment extends Fragment {
    private FragmentStatisticBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        MainViewModel mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        mainViewModel.settitle(getResources().getString(R.string.study_history));
        mainViewModel.setShowNavIcon(false);

        binding = FragmentStatisticBinding.inflate(inflater, container, false);
        StatisticViewModel viewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(StatisticViewModel.initializer)).get(StatisticViewModel.class);
        viewModel.getStudyStatistic(new IRepositoryCallback<List<StudyStatistic>>() {
            @Override
            public void onComplete(List<StudyStatistic> studyStatistics) {
                new StatisticChart(binding.historyStudyBC, studyStatistics);
            }

            @Override
            public void onError(Throwable t) {

            }
        });

        return binding.getRoot();
    }
}
