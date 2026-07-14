package com.kite.mnemoai.fragment;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.kite.mnemoai.MainActivity;
import com.kite.mnemoai.R;
import com.kite.mnemoai.databinding.FragmentReciteWordBinding;
import com.kite.mnemoai.uistate.ReciteWordUIState;
import com.kite.mnemoai.viewmodels.ReciteWordViewModel;

import java.util.List;

public class ReciteWordFragment extends Fragment{
    private FragmentReciteWordBinding binding;
    private ReciteWordViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReciteWordBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(ReciteWordViewModel.initializer)).get(ReciteWordViewModel.class);
        viewModel.setDailyDayPlanWordEntities();
        //禁用手势
        //动作按钮
        binding.statusReciteWordOK.rememberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.rememberWord();
            }
        });

        binding.statusReciteWordOK.blurBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.blurWord();
            }
        });

        binding.statusReciteWordOK.forgetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.forgetWord();
            }
        });
        viewModel.getUiState().observe(getViewLifecycleOwner(), new Observer<ReciteWordUIState>() {
            @Override
            public void onChanged(ReciteWordUIState reciteWordUIState) {
                if(reciteWordUIState == null) return;
                ReciteWordUIState.ReciteWordStatus reciteWordStatus = reciteWordUIState.getReciteWordStatus();
                if(reciteWordStatus == null) return;
                switch (reciteWordStatus){
                    case ok:
                        binding.statusReciteWordOK.statusReciteWordOKCL.setVisibility(View.VISIBLE);
                        binding.statusReciteWordNoVocabulary.statusReciteWordNoVocabularyCL.setVisibility(View.GONE);
                        binding.statusReciteWordFinish.statusReciteWordFinishCL.setVisibility(View.GONE);
                        //进度条
                        int progress = (int) ((reciteWordUIState.getCurrentProgress() * 100f)/reciteWordUIState.getTotalProgress());
                        binding.statusReciteWordOK.linearProgressIndicator.setProgress(progress);
                        binding.statusReciteWordOK.numberProgressTV.setText(getResources().getString(R.string.number_progress, reciteWordUIState.getCurrentProgress(), reciteWordUIState.getTotalProgress()));
                        List<ReciteWordUIState.ReciteWordItemStatus> original = reciteWordUIState.getReciteWordItemStatusOrder();
                        if(original != null && !original.isEmpty()){
                            ReciteWordUIState.ReciteWordItemStatus currentReciteWord = original.get(0);
                            if(viewModel.getReciteStatistics().isEmpty()){
                                viewModel.addReciteStatistics();
                            }
                            binding.statusReciteWordOK.wordCardView.wordCardView.bind(currentReciteWord);
                            binding.statusReciteWordOK.wordCardView.wordCardView.setOnAiMnemoaiClickListener(() -> {
                                viewModel.showTranslation();
                            });
                            binding.statusReciteWordOK.wordCardView.wordCardView.bindClickListener();
                            if(viewModel.isShouldAdvance()){
                                viewModel.setShouldAdvance(false);
                                binding.statusReciteWordOK.wordCardVF.showNext();
                                viewModel.addReciteStatistics();
                            }
                        }
                        break;
                    case no_vocabulary:
                        binding.statusReciteWordOK.statusReciteWordOKCL.setVisibility(View.GONE);
                        binding.statusReciteWordNoVocabulary.statusReciteWordNoVocabularyCL.setVisibility(View.VISIBLE);
                        binding.statusReciteWordFinish.statusReciteWordFinishCL.setVisibility(View.GONE);
                        break;
                    case finish:
                        binding.statusReciteWordOK.statusReciteWordOKCL.setVisibility(View.GONE);
                        binding.statusReciteWordNoVocabulary.statusReciteWordNoVocabularyCL.setVisibility(View.GONE);
                        binding.statusReciteWordFinish.statusReciteWordFinishCL.setVisibility(View.VISIBLE);
                        break;
                }

            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    public void onResume() {
        super.onResume();
        viewInit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
        mainActivity.setTitleText(R.string.recite_word);
    }
}
