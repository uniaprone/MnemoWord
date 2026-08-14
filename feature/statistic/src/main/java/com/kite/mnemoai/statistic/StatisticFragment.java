package com.kite.mnemoai.statistic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.kite.mnemoai.statistic.R;
import com.kite.mnemoai.model.statistic.StudyStatistic;
import com.kite.mnemoai.statistic.databinding.FragmentStatisticBinding;
import com.kite.mnemoai.ui.main.MainViewModel;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StatisticFragment extends Fragment {
    private FragmentStatisticBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        MainViewModel mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        mainViewModel.settitle(getResources().getString(R.string.study_history));
        mainViewModel.setShowNavIcon(false);

        binding = FragmentStatisticBinding.inflate(inflater, container, false);
        StatisticViewModel viewModel = new ViewModelProvider(this).get(StatisticViewModel.class);
        viewModel.getStudyStatistic(studyStatistics -> {
            new StatisticChart(binding.historyStudyBC, studyStatistics);
        });

        return binding.getRoot();
    }
}
