package com.kite.mnemoai.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kite.mnemoai.MainActivity;
import com.kite.mnemoai.R;
import com.kite.mnemoai.adapter.VocabularyCardAdapter;
import com.kite.mnemoai.data.local.DTO.DailyStatistic;
import com.kite.mnemoai.data.model.Group;
import com.kite.mnemoai.data.model.GroupDetail;
import com.kite.mnemoai.data.repository.IRepositoryCallback;
import com.kite.mnemoai.databinding.FragmentVocabularyBinding;
import com.kite.mnemoai.fragment.dialog.NewLearningWordSettingDialogFragment;
import com.kite.mnemoai.fragment.dialog.VocabularySelectDialogFragment;
import com.kite.mnemoai.uistate.VocabularyUIState;
import com.kite.mnemoai.viewmodels.VocabularyViewModel;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class VocabularyFragment extends Fragment{
    private FragmentVocabularyBinding binding;
    private VocabularyViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finish();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVocabularyBinding.inflate(inflater, container, false);
        viewInit();
        assert getActivity() != null;
        viewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(VocabularyViewModel.initializer))
                .get(VocabularyViewModel.class);

        binding.newLearningWordLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = new NewLearningWordSettingDialogFragment(new NewLearningWordSettingDialogFragment.IConfirmListener() {
                    @Override
                    public void onConfirm(int count) {
                        viewModel.setNewLearningWordCount(count);
                    }
                });
                dialogFragment.show(getParentFragmentManager(), "NEWLEARNINGCOUNTSETTING");
            }
        });

        RecyclerView studyingVocabularyRV = binding.studyingVocabulary;
        studyingVocabularyRV.setNestedScrollingEnabled(false);
        VocabularyCardAdapter studyingVocabularyCardAdapter = new VocabularyCardAdapter(1, this::showVocabularySelectDialog);
        studyingVocabularyRV.setLayoutManager(new LinearLayoutManager(getContext()));
        studyingVocabularyRV.setAdapter(studyingVocabularyCardAdapter);

        RecyclerView allVocabularyRV = binding.allVocabularyRV;
        allVocabularyRV.setNestedScrollingEnabled(false);
        VocabularyCardAdapter allVocabularyCardAdapter = new VocabularyCardAdapter(0, this::showVocabularySelectDialog);
        allVocabularyRV.setLayoutManager(new GridLayoutManager(getContext(), 1));
        allVocabularyRV.setAdapter(allVocabularyCardAdapter);

        TextView addVocabularyTV = binding.addVocabularyTV;
        addVocabularyTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showVocabularySelectDialog();
            }
        });

        viewModel.getUIState().observe(getViewLifecycleOwner(), vocabularyUIState -> {
            if(vocabularyUIState == null || vocabularyUIState.getGroupDetails() == null) return;
            List<GroupDetail> allGroups = vocabularyUIState.getGroupDetails();

            List<GroupDetail> learningGroupDetails = allGroups.stream()
                    .filter(g -> g.getGroupEntity().isLearning() == 1)
                    .collect(Collectors.toList());

            List<GroupDetail> unlearningGroupDetails = allGroups.stream()
                    .filter(g -> g.getGroupEntity().isLearning() != 1)
                    .collect(Collectors.toList());

            studyingVocabularyCardAdapter.setGroups(learningGroupDetails);
            allVocabularyCardAdapter.setGroups(unlearningGroupDetails);

            binding.newLearningWordCountTV.setText(String.valueOf(vocabularyUIState.getNewLearningWordCount()));
            DailyStatistic dailyStatistic = vocabularyUIState.getDailyStatistic();
            if(dailyStatistic != null){
                binding.dailyReviewCount.setText(String.valueOf(dailyStatistic.getReviewCount()));
                binding.dailyLearnedCount.setText(String.valueOf(dailyStatistic.getLearnedCount()));
                binding.dailyReviewedCount.setText(String.valueOf(dailyStatistic.getReviewedCount()));
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

//    @Override
//    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
//        menuInflater.inflate(R.menu.app_bar_menu, menu);
//    }
//
//    @Override
//    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
//        if (menuItem.getItemId() == R.id.word_group) {
//            return true;
//        }
//        return false;
//    }

    private void viewInit(){
        setupToolbar();
    }

    private void setupToolbar(){
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.setTitleText(R.string.vocabulary_book);
        mainActivity.hideBackIV();
    }

    private void showVocabularySelectDialog() {
        viewModel.getVocabularySelectedInfo(vocabularySelectInfos -> {
            DialogFragment dialogFragment = new VocabularySelectDialogFragment(
                    vocabularySelectInfos,
                    new VocabularySelectDialogFragment.IVocabularySelectListener() {
                        @Override
                        public void onConfirm(List<Long> ids) {
                            viewModel.addLearningGroups(ids);
                        }
                        @Override
                        public void onNavigate(DialogFragment dialogFragment) {
                            // 跳转逻辑
                        }
                    }
            );
            dialogFragment.show(getParentFragmentManager(), "vocabularySelect");
        });
    }

}
