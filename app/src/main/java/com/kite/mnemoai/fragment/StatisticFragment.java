package com.kite.mnemoai.fragment;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.TimeUtils;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.renderer.YAxisRenderer;
import com.google.android.material.color.MaterialColors;
import com.kite.mnemoai.MainActivity;
import com.kite.mnemoai.R;
import com.kite.mnemoai.data.local.DTO.StudyStatistic;
import com.kite.mnemoai.data.repository.IRepositoryCallback;
import com.kite.mnemoai.databinding.FragmentStatisticBinding;
import com.kite.mnemoai.stateholder.StatisticChart;
import com.kite.mnemoai.viewmodels.StatisticViewModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StatisticFragment extends Fragment {
    private FragmentStatisticBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

    public void onResume() {
        super.onResume();
        viewInit();
    }

    private void viewInit(){
        setupToolbar();
    }

    private void setupToolbar(){
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.setTitleText(R.string.study_history);
    }
}
