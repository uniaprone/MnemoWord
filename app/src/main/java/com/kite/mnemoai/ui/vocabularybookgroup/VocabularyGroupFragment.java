package com.kite.mnemoai.ui.vocabularybookgroup;

import android.os.Bundle;
import android.util.Log;
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
import com.kite.mnemoai.ui.main.MainActivity;
import com.kite.mnemoai.R;
import com.kite.mnemoai.adapter.LearningStatusWordAdapter;
import com.kite.mnemoai.data.model.WordListItem;
import com.kite.mnemoai.databinding.FragmentVocabularyGroupBinding;
import com.kite.mnemoai.ui.dialog.settingandaddnewvocabularybook.SettingAndAddNewVocabularyBookDialogFragment;
import com.kite.mnemoai.ui.main.MainViewModel;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class VocabularyGroupFragment extends Fragment implements MenuProvider{
    private static final String MODIFY_KEY = "modifyVocabularyBookConfirm";
    private FragmentVocabularyGroupBinding binding;
    private VocabularyGroupViewModel viewModel;
    private MainViewModel mainViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        mainViewModel.setShowNavIcon(true);

        binding = FragmentVocabularyGroupBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(VocabularyGroupViewModel.class);

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
                if(vocabularyGroupUIState == null) return;
                if(vocabularyGroupUIState.getWords() != null){
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
                if(vocabularyGroupUIState.getGroup() != null){
                    Log.d("名称2", vocabularyGroupUIState.getGroup().getName());
                    mainViewModel.settitle(vocabularyGroupUIState.getGroup().getName());
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
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
            isLearning = viewModel.getGroup().isLearning();
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
                    SettingAndAddNewVocabularyBookDialogFragment.Companion.newInstance((byte)0, viewModel.getGroup().getName(),viewModel.getGroup().getDescription());
            dialogFragment.show(getChildFragmentManager(), "MODIFYVELOCABULARYBOOK");
        } else if (id == R.id.setLearning) {
            int learningStatus = viewModel.getGroup().isLearning();
            if (learningStatus == 0) {
                viewModel.setVocabularyBookLearningStatus(1);
            } else if (learningStatus == 1) {
                viewModel.setVocabularyBookLearningStatus(0);
            } else {
                viewModel.setVocabularyBookLearningStatus(0);
            }
        } else if (id == R.id.alterWords) {
            Bundle bundle = new Bundle();
            bundle.putLong("group_id", viewModel.getGroup().getId());
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_vocabularyGroupFragment_to_changeVocabularyBookWordFragment, bundle);
        } else if (id == R.id.deleteVocabularyBook) {
                viewModel.deleteGroup();
                NavController navController = Navigation.findNavController(requireView());
                navController.navigateUp();
            }
            return true;
        }
}
