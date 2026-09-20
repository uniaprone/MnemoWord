package com.kite.mnemoai.vocabularybook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kite.mnemoai.model.group.AllVocabularyBookItem
import com.kite.mnemoai.model.group.LearningVocabularyBookItem
import com.kite.mnemoai.shared_ui.SettingAndAddNewVocabularyBookDialogFragment.Companion.newInstance
import com.kite.mnemoai.shared_ui.VocabularySelectDialogFragment
import com.kite.mnemoai.shared_ui.VocabularySelectDialogFragment.VocabularySelectInfo
import com.kite.mnemoai.ui.main.MainViewModel
import com.kite.mnemoai.vocabularybook.databinding.FragmentVocabularyBinding
import com.kite.mnemoai.vocabularybook.newlearningwordsetting.NewLearningWordSettingDialogFragment
import com.kite.mnemoai.vocabularybook.newlearningwordsetting.NewLearningWordSettingDialogFragment.Companion.newInstance
import dagger.hilt.android.AndroidEntryPoint
import java.util.Arrays
import java.util.Objects
import java.util.stream.Collectors

@AndroidEntryPoint
class VocabularyFragment : Fragment() {
    private var _binding: FragmentVocabularyBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VocabularyViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

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
        mainViewModel.settitle(resources.getString(R.string.vocabulary_book))
        mainViewModel.setShowNavIcon(false)
        _binding = FragmentVocabularyBinding.inflate(inflater, container, false)

        binding.newLearningWordLL.setOnClickListener {
            val dialogFragment = newInstance(viewModel.getNewLearningWordCount())
            dialogFragment.show(getChildFragmentManager(), "NEWLEARNINGCOUNTSETTING")
        }
        getChildFragmentManager().setFragmentResultListener(
            NewLearningWordSettingDialogFragment.NEW_LEARNING_COUNT_SETTING,
            this
        ) { _, result ->
            viewModel.setNewLearningWordCount(
                result.getInt(
                    "new_learning_count",
                    20
                )
            )
        }

        binding.searchCV.setOnClickListener {
            findNavController(requireView()).navigate(R.id.action_vocabularyFragment_to_searchFragment)
        }

        val learningVocabularyBookAdapter =
            LearningVocabularyBookAdapter { _: View?, aLong: Long? ->
                val bundle = Bundle()
                bundle.putLong("group_id", aLong!!)
                findNavController(requireView()).navigate(
                    R.id.action_vocabularyFragment_to_vocabularyGroupFragment,
                    bundle
                )
            }
        binding.studyingVocabulary.isNestedScrollingEnabled = false
        binding.studyingVocabulary.setLayoutManager(LinearLayoutManager(this.context))
        binding.studyingVocabulary.setAdapter(learningVocabularyBookAdapter)

        binding.allVocabularyRV.isNestedScrollingEnabled = false
        val allVocabularyBookAdapter = AllVocabularyBookAdapter { v: View?, id: Long? ->
            val bundle = Bundle()
            bundle.putLong("group_id", id!!)
            findNavController(v!!).navigate(
                R.id.action_vocabularyFragment_to_vocabularyGroupFragment,
                bundle
            )
        }
        val linearLayoutManager = LinearLayoutManager(this.context)
        binding.allVocabularyRV.setLayoutManager(linearLayoutManager)
        binding.allVocabularyRV.setAdapter(allVocabularyBookAdapter)

        binding.addVocabularyTV.setOnClickListener { 
            viewModel.getVocabularySelectedInfo { vocabularySelectInfos: List<VocabularySelectInfo> ->
                val dialogFragment =
                    VocabularySelectDialogFragment.newInstance(vocabularySelectInfos.toMutableList())
                dialogFragment.show(getChildFragmentManager(), "vocabularySelect")
            }
        }

        getChildFragmentManager().setFragmentResultListener(
            VocabularySelectDialogFragment.VOCABULARY_BOOK_SELECT,
            this
        ) { requestKey, result ->
            viewModel.addLearningGroups(
                Arrays.stream(Objects.requireNonNull<LongArray?>(result.getLongArray("ids")))
                    .boxed().collect(
                        Collectors.toList()
                    )
            )
        }

        binding.addNewVocabularyBookBtn.setOnClickListener { v: View? ->
            val dialogFragment = newInstance(1.toByte(), null, null)
            dialogFragment.show(getChildFragmentManager(), "ADDNEWVOCABULARYBOOK")
        }

        getChildFragmentManager().setFragmentResultListener(
            "confirm",
            this
        ) { requestKey: String?, result: Bundle? ->
            val name = result!!.getString("name")
            val desc = result.getString("desc")
            viewModel.addNewVocabularyBook(name!!, desc!!, System.currentTimeMillis())
        }

        viewModel.uiState.observe(
            getViewLifecycleOwner(),
            Observer { vocabularyUIState: VocabularyUIState? ->
                if (vocabularyUIState == null || vocabularyUIState.allVocabularyBookItems == null || vocabularyUIState.learningVocabularyBookItems == null) return@Observer
                learningVocabularyBookAdapter.submitList(
                    ArrayList<LearningVocabularyBookItem?>(
                        vocabularyUIState.learningVocabularyBookItems
                    )
                )
                allVocabularyBookAdapter.submitList(
                    ArrayList<AllVocabularyBookItem?>(
                        vocabularyUIState.allVocabularyBookItems
                    )
                )

                binding.newLearningWordCountTV.text = vocabularyUIState.newLearningWordCount.toString()
                val dailyStatistic = vocabularyUIState.dailyStatistic
                if (dailyStatistic != null) {
                    binding.dailyReviewCount.text =dailyStatistic.reviewCount.toString()
                    binding.dailyLearnedCount.text = dailyStatistic.learnedCount.toString()
                    binding.dailyReviewedCount.text = dailyStatistic.reviewedCount.toString()
                }
            })

        return binding.getRoot()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
