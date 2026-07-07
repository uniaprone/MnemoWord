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
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.kite.mnemoai.MainActivity;
import com.kite.mnemoai.R;
import com.kite.mnemoai.adapter.LearningStatusWordAdapter;
import com.kite.mnemoai.data.model.WordListItem;
import com.kite.mnemoai.databinding.FragmentVocabularyGroupBinding;
import com.kite.mnemoai.uistate.VocabularyGroupUIState;
import com.kite.mnemoai.viewmodels.VocabularyGroupViewModel;

import java.util.List;

public class VocabularyGroupFragment extends Fragment {
    private FragmentVocabularyGroupBinding binding;
    private VocabularyGroupViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVocabularyGroupBinding.inflate(inflater, container, false);
        viewInit();
        viewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(VocabularyGroupViewModel.initializer))
                .get(VocabularyGroupViewModel.class);



        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ViewPager2 viewPager2 = binding.learningWordVP;
        LearningStatusWordAdapter adapter = new LearningStatusWordAdapter(this);
        viewPager2.setAdapter(adapter);
        TabLayout tabLayout = binding.learningStatusTL;
        viewModel.getUiState().observe(getViewLifecycleOwner(), new Observer<VocabularyGroupUIState>() {
            @Override
            public void onChanged(VocabularyGroupUIState vocabularyGroupUIState) {
                if(vocabularyGroupUIState == null || vocabularyGroupUIState.getWords() == null) return;
                List<WordListItem> listItems = vocabularyGroupUIState.getWords();
                long allCount = listItems.size();
                long learningCount = listItems.stream().filter(wordListItem -> wordListItem.getReviewState() == 0).count();
                long reviewingCount = listItems.stream().filter(wordListItem -> wordListItem.getReviewState() == 1).count();
                long masteredCount = listItems.stream().filter(wordListItem -> wordListItem.getReviewState() == 2).count();
                new TabLayoutMediator(tabLayout, viewPager2, ((tab, position) -> {
                    switch (position){
                        case 0:
                            tab.setText(getString(R.string.all_word, allCount));
                            break;
                        case 1:
                            tab.setText(getString(R.string.learning_count, learningCount));
                            break;
                        case 2:
                            tab.setText(getString(R.string.reviewing_count, reviewingCount));
                            break;
                        case 3:
                            tab.setText(getString(R.string.mastered_count, masteredCount));
                            break;
                    }
                })).attach();
            }
        });
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
