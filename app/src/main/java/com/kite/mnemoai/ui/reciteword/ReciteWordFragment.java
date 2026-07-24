package com.kite.mnemoai.ui.reciteword;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.ChangeBounds;
import androidx.transition.Fade;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.google.android.material.color.MaterialColors;
import com.kite.mnemoai.MainActivity;
import com.kite.mnemoai.R;
import com.kite.mnemoai.data.local.entity.DayPlanWordEntity;
import com.kite.mnemoai.data.local.entity.ReviewWordEntity;
import com.kite.mnemoai.data.local.entity.WordExtractEntity;
import com.kite.mnemoai.data.model.ExampleSentence;
import com.kite.mnemoai.data.model.Phrase;
import com.kite.mnemoai.data.model.WordDetailInfo;
import com.kite.mnemoai.databinding.FragmentReciteWordBinding;
import com.kite.mnemoai.databinding.ItemExampleSentenceBinding;
import com.kite.mnemoai.databinding.ItemPhraseBinding;
import com.kite.mnemoai.ui.adapter.ReviewHistoryAdapter;
import com.kite.mnemoai.ui.adapter.ReviewHistoryItem;
import com.kite.mnemoai.utils.DensityUtilKt;
import com.kite.mnemoai.utils.TimeUtilKt;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReciteWordFragment extends Fragment{
    private FragmentReciteWordBinding binding;
    private ReciteWordViewModel viewModel;
    private RecyclerView reviewRV;
    private ReviewHistoryAdapter reviewAdapter;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReciteWordBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(ReciteWordViewModel.initializer)).get(ReciteWordViewModel.class);
        viewModel.setDailyDayPlanWordEntities();

        reviewRV = binding.statusReciteWordOK.aiMnemonic.studyHistoryItemsRV;
        reviewAdapter = new ReviewHistoryAdapter();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        reviewRV.setAdapter(reviewAdapter);
        reviewRV.setLayoutManager(layoutManager);
        reviewRV.setItemAnimator(null);

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
                ReciteStage reciteStage = reciteWordUIState.getReciteStage();
                if(reciteStage == null) return;
                switch (reciteStage){
                    case IN_PROGRESS:
                        binding.statusReciteWordOK.statusReciteWordOKCL.setVisibility(View.VISIBLE);
                        binding.statusReciteWordNoVocabulary.statusReciteWordNoVocabularyCL.setVisibility(View.GONE);
                        binding.statusReciteWordFinish.statusReciteWordFinishCL.setVisibility(View.GONE);

                        int progress = (int) ((reciteWordUIState.getCurrentProgress() * 100f)/reciteWordUIState.getTotalProgress());
                        binding.statusReciteWordOK.linearProgressIndicator.setProgress(progress);
                        binding.statusReciteWordOK.numberProgressTV.setText(getResources().getString(R.string.number_progress, reciteWordUIState.getCurrentProgress(), reciteWordUIState.getTotalProgress()));

                        List<WordDetailInfo> original = reciteWordUIState.getReciteWordItemStatusOrder();
                        if(original != null && !original.isEmpty()){
                            WordDetailInfo currentReciteWord = original.get(0);
                            if(viewModel.isShowNext()){
                                TransitionManager.endTransitions(binding.statusReciteWordOK.wordContentCL);
                                viewModel.setShowNext(false);
                                binding.statusReciteWordOK.wordCardVF.showNext();
                                viewModel.startReciteStatistics();
                                viewModel.resetShowState();
                            }
                            binding.statusReciteWordOK.wordTV.setText(currentReciteWord.getWordEntity().getWord());
                            binding.statusReciteWordOK.phoneticTV.setText(currentReciteWord.getWordEntity().getPhonetic());
                            binding.statusReciteWordOK.showWordDefinitionLL.setOnClickListener((view) -> {
                                TransitionManager.beginDelayedTransition(binding.statusReciteWordOK.wordContentCL,
                                        new TransitionSet().addTransition(new ChangeBounds()).addTransition(new Fade()));
                                binding.statusReciteWordOK.aiMnemonicLL.setVisibility(View.VISIBLE);
                                ConstraintSet constraintSet = new ConstraintSet();
                                constraintSet.clone(binding.statusReciteWordOK.wordContentCL);
                                constraintSet.constrainHeight(R.id.wordCV, ConstraintSet.MATCH_CONSTRAINT);
                                constraintSet.clear(R.id.wordCV, ConstraintSet.BOTTOM);
                                constraintSet.connect(R.id.wordCV, ConstraintSet.BOTTOM, R.id.aiMnemonicLL, ConstraintSet.TOP, DensityUtilKt.dpToPx(requireContext(), 32));
                                constraintSet.applyTo(binding.statusReciteWordOK.wordContentCL);
                                viewModel.showAll();
                            });
                            if(reciteWordUIState.isShowDetail()){
                                Log.d("mmmmmmmmm", "showdetail");
                                binding.statusReciteWordOK.aiMnemonicLL.setVisibility(View.VISIBLE);
                                binding.statusReciteWordOK.showWordDefinitionLL.setVisibility(View.GONE);
                                binding.statusReciteWordOK.aiMnemonic.getRoot().setVisibility(View.VISIBLE);
                                binding.statusReciteWordOK.generateAIMnemonicBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        viewModel.fetchWordExtract();
                                    }
                                });

                                ConstraintSet constraintSet = new ConstraintSet();
                                constraintSet.clone(binding.statusReciteWordOK.wordContentCL);
                                constraintSet.constrainHeight(R.id.wordCV, ConstraintSet.MATCH_CONSTRAINT);
                                constraintSet.clear(R.id.wordCV, ConstraintSet.BOTTOM);
                                constraintSet.connect(R.id.wordCV, ConstraintSet.BOTTOM, R.id.aiMnemonicLL, ConstraintSet.TOP, DensityUtilKt.dpToPx(requireContext(), 8));
                                constraintSet.applyTo(binding.statusReciteWordOK.wordContentCL);

                                setAndShowTranslation(currentReciteWord.getWordEntity().getTranslation());
                                setAndShowWordExtract(inflater, container, currentReciteWord.getWordExtractEntity());
                                setAndShowStudyHistory(currentReciteWord.getDayPlanWordEntities());
                            }else{
                                Log.d("mmmmmmmmm", "notshowdetail");
                                binding.statusReciteWordOK.translateTV.setVisibility(View.GONE);
                                binding.statusReciteWordOK.aiMnemonicLL.setVisibility(View.GONE);
                                binding.statusReciteWordOK.showWordDefinitionLL.setVisibility(View.VISIBLE);
                                binding.statusReciteWordOK.aiMnemonic.getRoot().setVisibility(View.GONE);
                                ConstraintSet constraintSet = new ConstraintSet();
                                constraintSet.clone(binding.statusReciteWordOK.wordContentCL);

                                constraintSet.constrainHeight(R.id.wordCV, ConstraintSet.WRAP_CONTENT);
                                constraintSet.clear(R.id.wordCV, ConstraintSet.BOTTOM);
                                constraintSet.connect(R.id.wordCV, ConstraintSet.BOTTOM, R.id.aiMnemonicLL, ConstraintSet.TOP, 0);

                                constraintSet.applyTo(binding.statusReciteWordOK.wordContentCL);
                                Log.d("mmmmmmmmm", binding.statusReciteWordOK.showWordDefinitionLL.getVisibility() + "");
                            }
                        }
                        break;
                    case NO_VOCABULARY:
                        binding.statusReciteWordOK.statusReciteWordOKCL.setVisibility(View.GONE);
                        binding.statusReciteWordNoVocabulary.statusReciteWordNoVocabularyCL.setVisibility(View.VISIBLE);
                        binding.statusReciteWordFinish.statusReciteWordFinishCL.setVisibility(View.GONE);
                        break;
                    case FINISH:
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

    private void setAndShowTranslation(String translation){
        binding.statusReciteWordOK.translateTV.setText(Objects.requireNonNullElse(translation, "null"));
        binding.statusReciteWordOK.translateTV.setVisibility(View.VISIBLE);
    }
    private void setAndShowWordExtract(LayoutInflater inflater, View view, WordExtractEntity currentWordExtract){
        if(currentWordExtract == null){
            binding.statusReciteWordOK.aiMnemonic.noExtractContent.setVisibility(View.VISIBLE);
            binding.statusReciteWordOK.aiMnemonic.extractContent.setVisibility(View.GONE);
        }else{
            binding.statusReciteWordOK.aiMnemonic.extractContent.setVisibility(View.VISIBLE);
            binding.statusReciteWordOK.aiMnemonic.noExtractContent.setVisibility(View.GONE);
            int textColor = MaterialColors.getColor(view, com.google.android.material.R.attr.colorOnSurface);
            binding.statusReciteWordOK.aiMnemonic.coreImageTV.setText(currentWordExtract.getExtract().getCoreImage());
            binding.statusReciteWordOK.aiMnemonic.affixTV.setText(currentWordExtract.getExtract().getAffix().toString());
            binding.statusReciteWordOK.aiMnemonic.explainTV.setText(currentWordExtract.getExtract().getExplain());
            binding.statusReciteWordOK.aiMnemonic.exampleSentenceLL.removeAllViews();
            List<ExampleSentence> exampleSentences = currentWordExtract.getExtract().getExampleSentence();
            for (ExampleSentence exampleSentence: exampleSentences){
                ItemExampleSentenceBinding exampleSentenceBinding = ItemExampleSentenceBinding.inflate(
                        inflater, binding.statusReciteWordOK.aiMnemonic.exampleSentenceLL, false
                );
                exampleSentenceBinding.exampleSentenceTV.setText(exampleSentence.getSentence());
                exampleSentenceBinding.exampleSentenceTV.setTextColor(textColor);
                exampleSentenceBinding.exampleSentenceTranslateTV.setText(exampleSentence.getTranslation());
                exampleSentenceBinding.exampleSentenceTranslateTV.setTextColor(textColor);
                binding.statusReciteWordOK.aiMnemonic.exampleSentenceLL.addView(exampleSentenceBinding.getRoot());
            }
            binding.statusReciteWordOK.aiMnemonic.phraseLL.removeAllViews();
            List<Phrase> phrases = currentWordExtract.getExtract().getPhrase();
            for(Phrase phrase: phrases){
                ItemPhraseBinding itemPhraseBinding = ItemPhraseBinding.inflate(
                        inflater, binding.statusReciteWordOK.aiMnemonic.phraseLL, false
                );
                itemPhraseBinding.phraseTV.setText(phrase.toString());
                itemPhraseBinding.phraseTV.setTextColor(textColor);
                binding.statusReciteWordOK.aiMnemonic.phraseLL.addView(itemPhraseBinding.getRoot());
            }
        }
    }

    private void setAndShowStudyHistory(List<DayPlanWordEntity> reviewHistories){
        if(reviewHistories == null || reviewHistories.isEmpty()){
            binding.statusReciteWordOK.aiMnemonic.noStudyHistoryContent.setVisibility(View.VISIBLE);
            binding.statusReciteWordOK.aiMnemonic.studyHistoryContent.setVisibility(View.GONE);
        }else{
            binding.statusReciteWordOK.aiMnemonic.studyHistoryContent.setVisibility(View.VISIBLE);
            binding.statusReciteWordOK.aiMnemonic.noStudyHistoryContent.setVisibility(View.GONE);

            List<ReviewHistoryItem> reviewHistoryItems =
                    reviewHistories.stream().map(reviewHistory -> {
                        sdf.format(new Date(reviewHistory.getLearningTime()));
                        return new ReviewHistoryItem(
                                reviewHistory.getWordId(),
                                reviewHistory.getDate(),
                                TimeUtilKt.formatDuration(reviewHistory.getLearningTime()),
                                1,
                                reviewHistory.getBlurCount(),
                                reviewHistory.getForgetCount()
                            );
                        }
                    ).collect(Collectors.toList());
            binding.statusReciteWordOK.aiMnemonic.studyHistoryItemsRV.post(() ->
                    reviewAdapter.submitList(new ArrayList<>(reviewHistoryItems))
            );
        }
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
