package com.kite.mnemoai.vocabularybook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
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
import java.util.function.Consumer
import java.util.stream.Collectors

@AndroidEntryPoint
class VocabularyFragment : Fragment() {
    private var binding: FragmentVocabularyBinding? = null
    private var viewModel: VocabularyViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().getOnBackPressedDispatcher()
            .addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainViewModel =
            ViewModelProvider(requireActivity()).get<MainViewModel>(MainViewModel::class.java)
        mainViewModel.settitle(getResources().getString(R.string.vocabulary_book))
        mainViewModel.setShowNavIcon(false)

        binding = FragmentVocabularyBinding.inflate(inflater, container, false)
        viewModel =
            ViewModelProvider(this).get<VocabularyViewModel>(VocabularyViewModel::class.java)

        binding!!.newLearningWordLL.setOnClickListener(View.OnClickListener { view: View? ->
            val dialogFragment = newInstance(viewModel!!.getNewLearningWordCount())
            dialogFragment.show(getChildFragmentManager(), "NEWLEARNINGCOUNTSETTING")
        })
        getChildFragmentManager().setFragmentResultListener(
            NewLearningWordSettingDialogFragment.NEW_LEARNING_COUNT_SETTING,
            this,
            object : FragmentResultListener {
                override fun onFragmentResult(requestKey: String, result: Bundle) {
                    viewModel!!.setNewLearningWordCount(result.getInt("new_learning_count", 20))
                }
            })

        binding!!.searchCV.setOnClickListener(View.OnClickListener { view: View? ->
            findNavController(
                view!!
            ).navigate(R.id.action_vocabularyFragment_to_searchFragment)
        })

        val learningVocabularyBookAdapter =
            LearningVocabularyBookAdapter { view: View?, aLong: Long? ->
                val bundle = Bundle()
                bundle.putLong("group_id", aLong!!)
                findNavController(view!!).navigate(
                    R.id.action_vocabularyFragment_to_vocabularyGroupFragment,
                    bundle
                )
                Unit
            }
        binding!!.studyingVocabulary.setNestedScrollingEnabled(false)
        binding!!.studyingVocabulary.setLayoutManager(LinearLayoutManager(this.getContext()))
        binding!!.studyingVocabulary.setAdapter(learningVocabularyBookAdapter)

        binding!!.allVocabularyRV.setNestedScrollingEnabled(false)
        val allVocabularyBookAdapter = AllVocabularyBookAdapter { v: View?, id: Long? ->
            val bundle = Bundle()
            bundle.putLong("group_id", id!!)
            findNavController(v!!).navigate(
                R.id.action_vocabularyFragment_to_vocabularyGroupFragment,
                bundle
            )
            Unit
        }
        binding!!.allVocabularyRV.setLayoutManager(LinearLayoutManager(this.getContext()))
        binding!!.allVocabularyRV.setAdapter(allVocabularyBookAdapter)

        binding!!.addVocabularyTV.setOnClickListener(View.OnClickListener { view: View? ->
            viewModel!!.getVocabularySelectedInfo(
                Consumer { vocabularySelectInfos: MutableList<VocabularySelectInfo?>? ->
                    val dialogFragment =
                        VocabularySelectDialogFragment.Companion.newInstance(vocabularySelectInfos!!)
                    dialogFragment.show(getChildFragmentManager(), "vocabularySelect")
                })
        })

        getChildFragmentManager().setFragmentResultListener(
            VocabularySelectDialogFragment.VOCABULARY_BOOK_SELECT,
            this,
            object : FragmentResultListener {
                override fun onFragmentResult(requestKey: String, result: Bundle) {
                    viewModel!!.addLearningGroups(
                        Arrays.stream(Objects.requireNonNull<LongArray?>(result.getLongArray("ids")))
                            .boxed().collect(
                                Collectors.toList()
                            )
                    )
                }
            })

        binding!!.addNewVocabularyBookBtn.setOnClickListener(View.OnClickListener { v: View? ->
            val dialogFragment = newInstance(1.toByte(), null, null)
            dialogFragment.show(getChildFragmentManager(), "ADDNEWVOCABULARYBOOK")
        })

        getChildFragmentManager().setFragmentResultListener(
            "confirm",
            this,
            FragmentResultListener { requestKey: String?, result: Bundle? ->
                val name = result!!.getString("name")
                val desc = result.getString("desc")
                viewModel!!.addNewVocabularyBook(name!!, desc!!, System.currentTimeMillis())
            })

        viewModel!!.uiState.observe(
            getViewLifecycleOwner(),
            Observer { vocabularyUIState: VocabularyUIState? ->
                if (vocabularyUIState == null || vocabularyUIState.allVocabularyBookItems == null || vocabularyUIState.learningVocabularyBookItems == null) return@observe
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

                binding!!.newLearningWordCountTV.setText(vocabularyUIState.newLearningWordCount.toString())
                val dailyStatistic = vocabularyUIState.dailyStatistic
                if (dailyStatistic != null) {
                    binding!!.dailyReviewCount.setText(dailyStatistic.reviewCount.toString())
                    binding!!.dailyLearnedCount.setText(dailyStatistic.learnedCount.toString())
                    binding!!.dailyReviewedCount.setText(dailyStatistic.reviewedCount.toString())
                }
            })

        return binding!!.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
