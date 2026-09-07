package com.kite.mnemoai.statistic

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kite.mnemoai.model.statistic.DateInterval
import com.kite.mnemoai.model.statistic.DateIntervalType
import com.kite.mnemoai.model.statistic.StudyStatistic
import com.kite.mnemoai.statistic.databinding.FragmentStatisticBinding
import com.kite.mnemoai.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

@AndroidEntryPoint
class StatisticFragment : Fragment() {
    private var _binding: FragmentStatisticBinding? = null
    private val binding get() = _binding!!
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: StatisticViewModel by viewModels()
    private var statisticDateInterval: DateInterval? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(enabled = true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel.settitle(getResources().getString(R.string.statistic))
        mainViewModel.setShowNavIcon(false)

        _binding = FragmentStatisticBinding.inflate(inflater, container, false)
        
        
        binding.pastDateAdvanceSegmentControl.setOnSelectionChangedListener { index -> 
            when(index){
                0 -> viewModel.setStatisticDataInterval(DateIntervalType.IN_THE_LAST_SEVEN_DAY,
                    DateInterval(
                        startDate = LocalDate.now().minusDays(6),
                        endDate = LocalDate.now()
                    ))
                1 -> viewModel.setStatisticDataInterval(DateIntervalType.IN_THE_LAST_MONTH,
                    DateInterval(
                        startDate = LocalDate.now().minusMonths(1),
                        endDate = LocalDate.now()
                    ))
                2 -> viewModel.setStatisticDataInterval(DateIntervalType.IN_THE_LAST_THREE_MONTH,
                    DateInterval(
                        startDate = LocalDate.now().minusMonths(3),
                        endDate = LocalDate.now()
                    ))
                else -> {}
            }

        }
        binding.pastDateAdvanceSegmentControl.setCustomSegmentClickListener {
            var currentStatisticDateInterval = statisticDateInterval
            if(currentStatisticDateInterval == null){
                currentStatisticDateInterval = DateInterval(LocalDate.now(), LocalDate.now())
            }
            val dialog: MaiDatePickerDialog = MaiDatePickerDialog.newInstance(currentStatisticDateInterval)
            dialog.show(childFragmentManager, "datePicker")
        }

        childFragmentManager.setFragmentResultListener(
            MaiDatePickerDialog.REQUEST_KEY,
            viewLifecycleOwner
        ){ _, bundle ->
            val startYear = bundle.getInt("start_year")
            val startMonth = bundle.getInt("start_month")
            val startDay = bundle.getInt("start_day")
            val startLocalDate = LocalDate.of(startYear, startMonth, startDay)
            
            val year = bundle.getInt("end_year")
            val month = bundle.getInt("end_month")
            val day = bundle.getInt("end_day")
            val localDate = LocalDate.of(year, month, day)
            val dateInterval = DateInterval(startLocalDate, localDate)
            binding.pastDateAdvanceSegmentControl.setSelectedIndex(3)
            viewModel.setStatisticDataInterval(DateIntervalType.CUSTOM, dateInterval)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.filterNotNull().first().let { state ->
                binding.pastDateAdvanceSegmentControl.setDefaultSelection(state.dateIntervalType.ordinal)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.uiState.collect {
                    it?.let {
                        it.dateInterval.let { dateInterval ->
                            statisticDateInterval = dateInterval
                        }

                        binding.selectedDateInterval.text = it.dateInterval.toString()
                        it.studyStatistics?.let { studyStatistics ->
                            StatisticChart(binding.historyStudyBC, studyStatistics)
                        }
                        setStatisticData(it.studyStatistics)
                    }
                }
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setStatisticData(studyStatistics: List<StudyStatistic>?) {
        val totalStudyTimeMs = studyStatistics?.sumOf { it.totalLearningTime } ?: 0
        val totalStudyTimeSec = totalStudyTimeMs / 1000L
        val totalStudyTimeString = DateUtils.formatElapsedTime(totalStudyTimeSec)

        val averageTimeSec = if (studyStatistics.isNullOrEmpty()) 0L else {
            totalStudyTimeSec / studyStatistics.size
        }
        val averageStudyTimeString = DateUtils.formatElapsedTime(averageTimeSec)

        val learnedWords = studyStatistics?.sumOf { it.newLearnedCount } ?: 0
        val reviewedWords = studyStatistics?.sumOf { it.reviewedCount } ?: 0

        binding.totalLearnedTimeSDHV.setData(totalStudyTimeString)
        binding.averageLearnedTimeSDHV.setData(averageStudyTimeString)
        binding.newLearnedWordSDHV.setData(learnedWords.toString())
        binding.reviewedWordSDHV.setData(reviewedWords.toString())
    }
}
