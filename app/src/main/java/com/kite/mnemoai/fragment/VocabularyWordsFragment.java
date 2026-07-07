package com.kite.mnemoai.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kite.mnemoai.MainActivity;
import com.kite.mnemoai.adapter.WordListAdapter;
import com.kite.mnemoai.data.model.WordListItem;
import com.kite.mnemoai.databinding.FragmentCollectionLearningWordsBinding;
import com.kite.mnemoai.uistate.VocabularyGroupUIState;
import com.kite.mnemoai.utils.StringConvert;
import com.kite.mnemoai.viewmodels.VocabularyGroupViewModel;

import java.util.List;
import java.util.stream.Collectors;

public class VocabularyWordsFragment extends Fragment {
    private FragmentCollectionLearningWordsBinding binding;
    private VocabularyGroupViewModel viewModel;

    public static VocabularyWordsFragment newInstance(int status){
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
        viewInit();
        int status = getArguments().getInt("status", 0);
        viewModel = new ViewModelProvider(requireParentFragment(), ViewModelProvider.Factory.from(VocabularyGroupViewModel.initializer))
                .get(VocabularyGroupViewModel.class);

        RecyclerView recyclerView = binding.wordRecycleView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        WordListAdapter wordListAdapter = new WordListAdapter();
        recyclerView.setAdapter(wordListAdapter);

        viewModel.getUiState().observe(getViewLifecycleOwner(), new Observer<VocabularyGroupUIState>() {
            @Override
            public void onChanged(VocabularyGroupUIState vocabularyGroupUIState) {
                if(vocabularyGroupUIState == null) return;
                if(vocabularyGroupUIState.getWords() == null) return;
                List<WordListItem> wordListItems = vocabularyGroupUIState.getWords();
                switch (status){
                    case 0: break;
                    case 1:
                        wordListItems = wordListItems.stream()
                                .filter((wordListItem -> wordListItem.getReviewState() == 0))
                                .collect(Collectors.toList());
                        break;
                    case 2:
                        wordListItems = wordListItems.stream()
                                .filter((wordListItem -> wordListItem.getReviewState() == 1))
                                .collect(Collectors.toList());
                        break;
                    case 3:
                        wordListItems = wordListItems.stream()
                                .filter((wordListItem -> wordListItem.getReviewState() == 2))
                                .collect(Collectors.toList());
                        break;
                }
                wordListAdapter.setWords(wordListItems);
                if(vocabularyGroupUIState.getGroup() == null || vocabularyGroupUIState.getGroup().getName() == null) return;
                setupTitle(StringConvert.convertVocabularyName(vocabularyGroupUIState.getGroup().getName()));
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void viewInit(){
        setupToolbar();
    }

    private void setupToolbar(){
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.setTitleText("");
        mainActivity.hideBackIV();
    }

    private void setupTitle(String title){
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.setTitleText(title);
    }
}
