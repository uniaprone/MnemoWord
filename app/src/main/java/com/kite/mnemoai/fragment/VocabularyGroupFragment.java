package com.kite.mnemoai.fragment;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.kite.mnemoai.MainActivity;
import com.kite.mnemoai.R;
import com.kite.mnemoai.adapter.LearningStatusWordAdapter;
import com.kite.mnemoai.data.model.WordListItem;
import com.kite.mnemoai.databinding.FragmentVocabularyGroupBinding;
import com.kite.mnemoai.fragment.dialog.SettingAndAddNewVocabularyBookDialogFragment;
import com.kite.mnemoai.uistate.VocabularyGroupUIState;
import com.kite.mnemoai.viewmodels.VocabularyGroupViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VocabularyGroupFragment extends Fragment implements MenuProvider{
    private static final String MODIFY_KEY = "modifyVocabularyBookConfirm";
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
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        getChildFragmentManager().setFragmentResultListener("confirm", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                String name = result.getString("name");
                String desc = result.getString("desc");
                viewModel.modifyVocabularyBook(name, desc);
            }
        });

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
    }

    private void setupTitle(String title){
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.setTitleText(title);
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.vocabulary_book_setting_menu, menu);
    }

    @Override
    public void onPrepareMenu(@NonNull Menu menu) {
        MenuProvider.super.onPrepareMenu(menu);

        MenuItem menuItem = menu.findItem(R.id.setLearning);
        int isLearning = 0;
        if(viewModel.getGroup() != null){
            isLearning = viewModel.getGroup().getIsLearning();
        }
        if(menuItem != null){
            if(isLearning == 0){
                menuItem.setTitle(R.string.join_learning);
            }else if(isLearning == 1){
                menuItem.setTitle(R.string.cancel_learning);
            }else{
                menuItem.setTitle(R.string.join_learning);
            }
        }
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if(id == R.id.modifyGroup){
            SettingAndAddNewVocabularyBookDialogFragment dialogFragment =
                    SettingAndAddNewVocabularyBookDialogFragment.Companion.newInstance(viewModel.getGroup().getName(),viewModel.getGroup().getDescription());
            dialogFragment.show(getChildFragmentManager(), "MODIFYVELOCABULARYBOOK");
        } else if (id == R.id.setLearning) {
            int learningStatus = viewModel.getGroup().getIsLearning();
            if (learningStatus == 0) {
                viewModel.setVocabularyBookLearningStatus(1);
            } else if (learningStatus == 1) {
                viewModel.setVocabularyBookLearningStatus(0);
            } else {
                viewModel.setVocabularyBookLearningStatus(0);
            }
        } else if (id == R.id.deleteVocabularyBook) {
            viewModel.deleteGroup();
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
        }
        return true;
    }
}
