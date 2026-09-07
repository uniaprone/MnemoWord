package com.kite.mnemoai.statistic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kite.mnemoai.model.statistic.StudyStatistic
import com.kite.mnemoai.statistic.databinding.FragmentStatisticBinding
import com.kite.mnemoai.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.function.Consumer

@AndroidEntryPoint
class StatisticFragment : Fragment() {
    private var binding: FragmentStatisticBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainViewModel =
            ViewModelProvider(requireActivity()).get<MainViewModel>(MainViewModel::class.java)
        mainViewModel.settitle(getResources().getString(R.string.study_history))
        mainViewModel.setShowNavIcon(false)

        binding = FragmentStatisticBinding.inflate(inflater, container, false)
        val viewModel =
            ViewModelProvider(this).get<StatisticViewModel>(StatisticViewModel::class.java)

        viewModel.getStudyStatistic(Consumer { studyStatistics: MutableList<StudyStatistic?>? ->
            StatisticChart(binding!!.historyStudyBC, studyStatistics)
        })

        return binding!!.getRoot()
    }
}
