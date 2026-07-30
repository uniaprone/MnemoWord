package com.kite.mnemoai.ui.vocabularybook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.kite.mnemoai.R;
import com.kite.mnemoai.data.model.DailyStatistic;
import com.kite.mnemoai.databinding.FragmentVocabularyBinding;
import com.kite.mnemoai.ui.dialog.settingandaddnewvocabularybook.SettingAndAddNewVocabularyBookDialogFragment;
import com.kite.mnemoai.ui.vocabularybook.dialog.newlearningwordsetting.NewLearningWordSettingDialogFragment;
import com.kite.mnemoai.ui.dialog.vocabularyselect.VocabularySelectDialogFragment;
import com.kite.mnemoai.ui.main.MainViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

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
        MainViewModel mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        mainViewModel.settitle(getResources().getString(R.string.vocabulary_book));
        mainViewModel.setShowNavIcon(false);

        binding = FragmentVocabularyBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(VocabularyViewModel.initializer))
                .get(VocabularyViewModel.class);


        binding.newLearningWordLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewLearningWordSettingDialogFragment dialogFragment = NewLearningWordSettingDialogFragment.Companion.newInstance(viewModel.getNewLearningWordCount());
                dialogFragment.show(getChildFragmentManager(), "NEWLEARNINGCOUNTSETTING");
            }
        });
        getChildFragmentManager().setFragmentResultListener(NewLearningWordSettingDialogFragment.NEW_LEARNING_COUNT_SETTING, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                viewModel.setNewLearningWordCount(result.getInt("new_learning_count", 20));
            }
        });

        binding.searchLL.setOnClickListener((view -> {
            Navigation.findNavController(view).navigate(R.id.action_vocabularyFragment_to_searchFragment);
        }));

        LearningVocabularyBookAdapter learningVocabularyBookAdapter = new LearningVocabularyBookAdapter(new Function2<View, Long, Unit>() {
            @Override
            public Unit invoke(View view, Long aLong) {
                Bundle bundle = new Bundle();
                bundle.putLong("group_id", aLong);
                Navigation.findNavController(view).navigate(R.id.action_vocabularyFragment_to_vocabularyGroupFragment, bundle);
                return null;
            }
        });
        binding.studyingVocabulary.setNestedScrollingEnabled(false);
        binding.studyingVocabulary.setLayoutManager(new LinearLayoutManager(this.getContext()));
        binding.studyingVocabulary.setAdapter(learningVocabularyBookAdapter);

        binding.allVocabularyRV.setNestedScrollingEnabled(false);
        AllVocabularyBookAdapter allVocabularyBookAdapter = new AllVocabularyBookAdapter((v, id) -> {
            Bundle bundle = new Bundle();
            bundle.putLong("group_id", id);
            Navigation.findNavController(v).navigate(R.id.action_vocabularyFragment_to_vocabularyGroupFragment, bundle);
            return Unit.INSTANCE;
        });
        binding.allVocabularyRV.setLayoutManager(new LinearLayoutManager(this.getContext()));
        binding.allVocabularyRV.setAdapter(allVocabularyBookAdapter);

        binding.addVocabularyTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.getVocabularySelectedInfo(vocabularySelectInfos -> {
                    VocabularySelectDialogFragment dialogFragment = VocabularySelectDialogFragment.Companion.newInstance(vocabularySelectInfos);
                    dialogFragment.show(getChildFragmentManager(), "vocabularySelect");
                });
            }
        });

        getChildFragmentManager().setFragmentResultListener(VocabularySelectDialogFragment.VOCABULARY_BOOK_SELECT, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                viewModel.addLearningGroups(Arrays.stream(Objects.requireNonNull(result.getLongArray("ids"))).boxed().collect(Collectors.toList()));
            }
        });

        binding.addNewVocabularyBookBtn.setOnClickListener(v -> {
            SettingAndAddNewVocabularyBookDialogFragment dialogFragment = SettingAndAddNewVocabularyBookDialogFragment.Companion.newInstance((byte)1, null, null);
            dialogFragment.show(getChildFragmentManager(), "ADDNEWVOCABULARYBOOK");
        });

        getChildFragmentManager().setFragmentResultListener("confirm", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                String name = result.getString("name");
                String desc = result.getString("desc");
                viewModel.addNewVocabularyBook(name, desc, System.currentTimeMillis());
            }
        });


        viewModel.getUIState().observe(getViewLifecycleOwner(), vocabularyUIState -> {
            if(vocabularyUIState == null || vocabularyUIState.getAllVocabularyBookItems() == null || vocabularyUIState.getLearningVocabularyBookItems() == null) return;
            learningVocabularyBookAdapter.submitList(new ArrayList<>(vocabularyUIState.getLearningVocabularyBookItems()));
            allVocabularyBookAdapter.submitList(new ArrayList<>(vocabularyUIState.getAllVocabularyBookItems()));

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
//        menuInflater.inflate(R.menu.vocabulary_menu, menu);
//    }
//
//    @Override
//    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
//        if (menuItem.getItemId() == R.id.add_new_vocabulary) {
//            SettingAndAddNewVocabularyBookDialogFragment dialogFragment = new SettingAndAddNewVocabularyBookDialogFragment();
//            dialogFragment.show(getChildFragmentManager(), "ADDNEWVOCABULARYBOOK");
//            return true;
//        }
//        return false;
//    }

}
