package com.kite.mnemoai.vocabularybookgroup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kite.mnemoai.vocabularybookgroup.R;
import com.kite.mnemoai.model.word.WordItem;
import com.kite.mnemoai.ui.WordListAdapter;
import com.kite.mnemoai.vocabularybookgroup.databinding.FragmentCollectionLearningWordsBinding;

import java.util.List;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class VocabularyWordsFragment extends Fragment {
    private FragmentCollectionLearningWordsBinding binding;
    private VocabularyGroupViewModel viewModel;

    public static VocabularyWordsFragment newInstance(int status) {
        VocabularyWordsFragment vocabularyWordsFragment = new VocabularyWordsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("status", status);
        vocabularyWordsFragment.setArguments(bundle);
        return vocabularyWordsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCollectionLearningWordsBinding.inflate(inflater, container, false);
        int status = getArguments().getInt("status", 0);
        viewModel = new ViewModelProvider(requireParentFragment()).get(VocabularyGroupViewModel.class);

        RecyclerView recyclerView = binding.wordRecycleView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        WordListAdapter wordListAdapter = new WordListAdapter(new WordListAdapter.OnItemClickListener() {
            @Override
            public void onClick(WordItem word) {
                Bundle args = new Bundle();
                args.putLong("word_id", word.getId());
                Navigation.findNavController(recyclerView).navigate(R.id.action_vocabularyGroupFragment_to_wordDetailFragment, args);
            }
        });
        recyclerView.setAdapter(wordListAdapter);

        viewModel.getUiState().observe(getViewLifecycleOwner(), new Observer<VocabularyGroupUIState>() {
            @Override
            public void onChanged(VocabularyGroupUIState vocabularyGroupUIState) {
                if (vocabularyGroupUIState == null) return;
                if (vocabularyGroupUIState.getWords() != null) {
                    List<WordItem> wordListItems = vocabularyGroupUIState.getWords();
                    switch (status) {
                        case 0:
                            break;
                        case 1:
                            wordListItems = wordListItems.stream()
                                    .filter(wordListItem -> wordListItem.getReviewState() == 0)
                                    .collect(Collectors.toList());
                            break;
                        case 2:
                            wordListItems = wordListItems.stream()
                                    .filter(wordListItem -> wordListItem.getReviewState() == 1)
                                    .collect(Collectors.toList());
                            break;
                        case 3:
                            wordListItems = wordListItems.stream()
                                    .filter(wordListItem -> wordListItem.getReviewState() == 2)
                                    .collect(Collectors.toList());
                            break;
                    }
                    wordListAdapter.setWords(wordListItems);
                }
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
