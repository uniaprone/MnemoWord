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
        viewInit();
        binding = FragmentStatisticBinding.inflate(inflater, container, false);
        StatisticViewModel viewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(StatisticViewModel.initializer)).get(StatisticViewModel.class);
        viewModel.getStudyStatistic(new IRepositoryCallback<List<StudyStatistic>>() {
            @Override
            public void onComplete(List<StudyStatistic> studyStatistics) {
                List<String> date = new ArrayList<>();
                List<BarEntry> stackedEntry = new ArrayList<>();
                List<Entry> lineEntry = new ArrayList<>();
                int LeftMaxHeight = 0;
                long rightMaxHeight = 0;
                for(int i = 0; i < studyStatistics.size(); i++){
                    date.add(studyStatistics.get(i).getDate());
                    stackedEntry.add(new BarEntry((float) (i + 0.5), new float[]{studyStatistics.get(i).getNewLearnedCount(), studyStatistics.get(i).getReviewedCount()}));

                    long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(studyStatistics.get(i).getTotalLearningTime());
                    lineEntry.add(new Entry((float) (i + 0.5), totalSeconds));

                    int currentEntryHeight = studyStatistics.get(i).getNewLearnedCount() +  studyStatistics.get(i).getReviewedCount();
                    if(currentEntryHeight > LeftMaxHeight) LeftMaxHeight = currentEntryHeight;

                    if(totalSeconds > rightMaxHeight) rightMaxHeight = totalSeconds;
                }
                if(LeftMaxHeight <= 100) LeftMaxHeight = 100;
                else LeftMaxHeight = LeftMaxHeight + LeftMaxHeight;

                if(rightMaxHeight <= 60) rightMaxHeight = 60 * 60;

                BarDataSet barDataSet = new BarDataSet(stackedEntry, "");
                barDataSet.setDrawValues(false);
                barDataSet.setStackLabels(new String[]{"新学", "复习"});
                barDataSet.setColors(
                        MaterialColors.getColor(binding.historyStudyBC, androidx.appcompat.R.attr.colorPrimary),
                        MaterialColors.getColor(binding.historyStudyBC, com.google.android.material.R.attr.colorSecondary)
                );

                BarData barData = new BarData(barDataSet);
                barData.setBarWidth(0.6f);

                LineDataSet lineDataSet = new LineDataSet(lineEntry, "");
//                lineDataSet.setColor(Color.BLACK);
//                lineDataSet.setCircleColor(Color.BLACK);
                lineDataSet.setLabel("学习时长");
                lineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
                lineDataSet.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return DateUtils.formatElapsedTime((long) value);
                    }
                });

                LineData lineData = new LineData(lineDataSet);

                int axisTextColor = MaterialColors.getColor(binding.historyStudyBC, com.google.android.material.R.attr.colorOnSurfaceVariant);
                int axisGridColor = MaterialColors.getColor(binding.historyStudyBC, com.google.android.material.R.attr.colorSurfaceContainerLow);
                XAxis xAxis = binding.historyStudyBC.getXAxis();
                xAxis.setTextColor(axisTextColor);
                xAxis.setGridColor(axisGridColor);
                xAxis.setCenterAxisLabels(true);
                xAxis.setAxisMinimum(0);
                xAxis.setAxisMaximum(stackedEntry.size() + 7);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularity(1f);
                xAxis.setGranularityEnabled(true);
                xAxis.setLabelCount(10);
                xAxis.setLabelRotationAngle(-45f);
                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        if(date.isEmpty()){
                            return LocalDate.now().plusDays((long) value).toString();
                        }else {
                            LocalDate localDate = LocalDate.parse(date.get(0));
                            return localDate.plusDays((long) value).toString();
                        }
                    }
                });

                YAxis leftAxis = binding.historyStudyBC.getAxisLeft();
                leftAxis.setGridColor(axisGridColor);
                leftAxis.setTextSize(15f);
                leftAxis.setTextColor(axisTextColor);
                Typeface typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD);
                leftAxis.setTypeface(typeface);
                leftAxis.setLabelCount(10);
                leftAxis.setAxisMinimum(0f);
                leftAxis.setAxisMaximum(LeftMaxHeight);
                leftAxis.setGranularity(10f);
                leftAxis.setGranularityEnabled(true);
                leftAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return String.valueOf((int) value);
                    }
                });

                YAxis rightAxis =  binding.historyStudyBC.getAxisRight();
                rightAxis.setGridColor(axisGridColor);
                rightAxis.setTextSize(15f);
                rightAxis.setTextColor(axisTextColor);
                rightAxis.setTypeface(typeface);
                rightAxis.setLabelCount(6);
                rightAxis.setAxisMinimum(0f);
                rightAxis.setAxisMaximum(rightMaxHeight * 1.2f);
                rightAxis.setGranularity(10f);
                rightAxis.setGranularityEnabled(true);
                rightAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return DateUtils.formatElapsedTime((long) value);
                    }
                });

                Legend legend = binding.historyStudyBC.getLegend();
                legend.setTextColor(MaterialColors.getColor(binding.historyStudyBC, android.R.attr.colorPrimary));
                legend.setXEntrySpace(35);
                legend.setTextSize(15);
                legend.setFormSize(15);
                legend.setYOffset(10);
                legend.setForm(Legend.LegendForm.SQUARE);

                CombinedData combinedData = new CombinedData();
                combinedData.setData(lineData);
                combinedData.setData(barData);

                binding.historyStudyBC.setDrawGridBackground(false);
                binding.historyStudyBC.setBorderWidth(1f);
                binding.historyStudyBC.setExtraBottomOffset(10f);
                binding.historyStudyBC.setScaleYEnabled(false);
                binding.historyStudyBC.setScaleXEnabled(false);
                binding.historyStudyBC.setDescription(null);
                binding.historyStudyBC.setVisibleXRangeMaximum(10);
                binding.historyStudyBC.setVisibleXRangeMinimum(2);
                binding.historyStudyBC.setData(combinedData);
                binding.historyStudyBC.setDrawOrder(new CombinedChart.DrawOrder[]{
                        CombinedChart.DrawOrder.BAR,  // 先画柱状图
                        CombinedChart.DrawOrder.LINE  // 再画折线图（如果有的话）
                });
                binding.historyStudyBC.invalidate();
            }

            @Override
            public void onError(Throwable t) {

            }
        });

        return binding.getRoot();
    }

    private void viewInit(){
        setupToolbar();
    }

    private void setupToolbar(){
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.setTitleText(R.string.study_history);
        mainActivity.hideBackIV();
    }
}
