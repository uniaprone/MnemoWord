package com.kite.mnemoai.vocabularybookgroup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kite.mnemoai.model.word.WordItem
import com.kite.mnemoai.ui.WordListAdapter
import com.kite.mnemoai.vocabularybookgroup.databinding.FragmentCollectionLearningWordsBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.stream.Collectors

@AndroidEntryPoint
class VocabularyWordsFragment : Fragment() {
    private var binding: FragmentCollectionLearningWordsBinding? = null
    private var viewModel: VocabularyGroupViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCollectionLearningWordsBinding.inflate(inflater, container, false)
        val status = requireArguments().getInt("status", 0)
        viewModel = ViewModelProvider(requireParentFragment()).get<VocabularyGroupViewModel>(
            VocabularyGroupViewModel::class.java
        )

        val recyclerView = binding!!.wordRecycleView
        recyclerView.setLayoutManager(LinearLayoutManager(this.context))
        val wordListAdapter = WordListAdapter { wordItem ->
            val args = Bundle()
            args.putLong("word_id", wordItem.id)
            findNavController(recyclerView).navigate(
                R.id.action_vocabularyGroupFragment_to_wordDetailFragment,
                args
            )
        }
        recyclerView.setAdapter(wordListAdapter)

        viewModel!!.uiState.observe(
            getViewLifecycleOwner(),
            object : Observer<VocabularyGroupUIState?> {
                override fun onChanged(vocabularyGroupUIState: VocabularyGroupUIState?) {
                    if (vocabularyGroupUIState == null) return
                    if (vocabularyGroupUIState.words != null) {
                        var wordListItems: List<WordItem> = vocabularyGroupUIState.words
                        when (status) {
                            0 -> {}
                            1 -> wordListItems = wordListItems.stream()
                                .filter { wordListItem: WordItem? -> wordListItem!!.reviewState == 0 }
                                .collect(Collectors.toList())

                            2 -> wordListItems = wordListItems.stream()
                                .filter { wordListItem: WordItem? -> wordListItem!!.reviewState == 1 }
                                .collect(Collectors.toList())

                            3 -> wordListItems = wordListItems.stream()
                                .filter { wordListItem: WordItem? -> wordListItem!!.reviewState == 2 }
                                .collect(Collectors.toList())
                        }
                        wordListAdapter.submitList(wordListItems)
                    }
                }
            })

        return binding!!.getRoot()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        fun newInstance(status: Int): VocabularyWordsFragment {
            val vocabularyWordsFragment = VocabularyWordsFragment()
            val bundle = Bundle()
            bundle.putInt("status", status)
            vocabularyWordsFragment.setArguments(bundle)
            return vocabularyWordsFragment
        }
    }
}
