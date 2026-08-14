package com.kite.mnemoai.reciteword;

import android.os.Bundle;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.textview.MaterialTextView;
import com.kite.mnemoai.reciteword.R;
import com.kite.mnemoai.model.dayplan.DayPlanWord;
import com.kite.mnemoai.model.word.ExampleSentence;
import com.kite.mnemoai.model.word.Phrase;
import com.kite.mnemoai.model.word.WordDetail;
import com.kite.mnemoai.model.word.WordExtract;
import com.kite.mnemoai.model.word.WordForm;
import com.kite.mnemoai.model.word.WordMeaning;
import com.kite.mnemoai.model.word.WordTranslation;
import com.kite.mnemoai.model.Result;
import com.kite.mnemoai.reciteword.databinding.ItemWordFormBinding;
import com.kite.mnemoai.reciteword.databinding.ItemWordTranslationBinding;
import com.kite.mnemoai.reciteword.databinding.FragmentReciteWordBinding;
import com.kite.mnemoai.reciteword.databinding.ItemExampleSentenceBinding;
import com.kite.mnemoai.reciteword.databinding.ItemPhraseBinding;
import com.kite.mnemoai.ui.BannerControl;
import com.kite.mnemoai.ui.ReviewHistoryAdapter;
import com.kite.mnemoai.ui.ReviewHistoryItem;
import com.kite.mnemoai.ui.main.MainViewModel;
import com.kite.mnemoai.ui.DensityUtilKt;
import com.kite.mnemoai.ui.TextHighlighter;
import com.kite.mnemoai.common.StringConvert;
import com.kite.mnemoai.ui.TimeUtilKt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ReciteWordFragment extends Fragment {
    private FragmentReciteWordBinding binding;
    private ReciteWordViewModel viewModel;
    private BannerControl bannerControl;
    private RecyclerView reviewRV;
    private ReviewHistoryAdapter reviewAdapter;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE);

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        MainViewModel mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        mainViewModel.settitle(getResources().getString(R.string.recite_word));
        mainViewModel.setShowNavIcon(false);

        binding = FragmentReciteWordBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ReciteWordViewModel.class);
        viewModel.setDailyDayPlanWordEntities();

        bannerControl = new BannerControl(binding.statusReciteWordOK.AIGenerateBanner, getLifecycle());

        reviewRV = binding.statusReciteWordOK.aiMnemonic.studyHistoryItemsRV;
        reviewAdapter = new ReviewHistoryAdapter();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        reviewRV.setAdapter(reviewAdapter);
        reviewRV.setLayoutManager(layoutManager);
        reviewRV.setItemAnimator(null);

        // 动作按钮
        binding.statusReciteWordOK.rememberBtn.setOnClickListener(view -> viewModel.rememberWord());
        binding.statusReciteWordOK.blurBtn.setOnClickListener(view -> viewModel.blurWord());
        binding.statusReciteWordOK.forgetBtn.setOnClickListener(view -> viewModel.forgetWord());

        viewModel.getUiState().observe(getViewLifecycleOwner(), new Observer<ReciteWordUIState>() {
            @Override
            public void onChanged(ReciteWordUIState reciteWordUIState) {
                if (reciteWordUIState == null) return;
                ReciteStage reciteStage = reciteWordUIState.getReciteStage();
                if (reciteStage == null) return;
                switch (reciteStage) {
                    case IN_PROGRESS:
                        binding.statusReciteWordOK.statusReciteWordOKCL.setVisibility(View.VISIBLE);
                        binding.statusReciteWordNoVocabulary.statusReciteWordNoVocabularyCL.setVisibility(View.GONE);
                        binding.statusReciteWordFinish.statusReciteWordFinishCL.setVisibility(View.GONE);

                        int progress = (int) ((reciteWordUIState.getCurrentProgress() * 100f) / reciteWordUIState.getTotalProgress());
                        binding.statusReciteWordOK.linearProgressIndicator.setProgress(progress, true);
                        binding.statusReciteWordOK.numberProgressTV.setText(getResources().getString(R.string.number_progress, reciteWordUIState.getCurrentProgress(), reciteWordUIState.getTotalProgress()));

                        binding.statusReciteWordOK.generateAIMnemonicChip.setOnClickListener(v -> viewModel.fetchWordExtract());

                        List<WordDetail> original = reciteWordUIState.getReciteWordItemStatusOrder();
                        if (original != null && !original.isEmpty()) {
                            binding.statusReciteWordOK.wordCV.setVisibility(View.VISIBLE);
                            WordDetail currentReciteWord = original.get(0);
                            if (viewModel.isShowNext) {
                                TransitionManager.endTransitions(binding.statusReciteWordOK.wordContentCL);
                                binding.statusReciteWordOK.wordTranslationLL.setVisibility(View.GONE);
                                binding.statusReciteWordOK.showWordDefinitionLL.setVisibility(View.VISIBLE);
                                bannerControl.forceHide();
                                binding.statusReciteWordOK.aiMnemonic.getRoot().setVisibility(View.GONE);
                                binding.statusReciteWordOK.aiChipGroup.setVisibility(View.GONE);
                                binding.statusReciteWordOK.detailDivider.setVisibility(View.GONE);
                                binding.statusReciteWordOK.wordTranslationLL.removeAllViews();
                                binding.statusReciteWordOK.aiMnemonic.wordFormFBL.removeAllViews();
                                binding.statusReciteWordOK.aiMnemonic.exampleSentenceLL.removeAllViews();
                                binding.statusReciteWordOK.aiMnemonic.phraseLL.removeAllViews();
                                viewModel.isShowNext = false;
                                binding.statusReciteWordOK.wordCardVF.showNext();
                                viewModel.startReciteStatistics();
                                viewModel.resetShowState();
                            }
                            binding.statusReciteWordOK.wordTV.setText(currentReciteWord.getWord().getWord());
                            binding.statusReciteWordOK.phoneticTV.setText(currentReciteWord.getWord().getPhonetic());
                            binding.statusReciteWordOK.showWordDefinitionLL.setOnClickListener(view -> viewModel.showAll());
                            if (reciteWordUIState.isShowDetail()) {
                                TransitionManager.beginDelayedTransition(binding.statusReciteWordOK.wordContentCL,
                                        new TransitionSet().addTransition(new ChangeBounds()));

                                binding.statusReciteWordOK.showWordDefinitionLL.setVisibility(View.GONE);
                                binding.statusReciteWordOK.aiMnemonic.getRoot().setVisibility(View.VISIBLE);
                                binding.statusReciteWordOK.aiChipGroup.setVisibility(View.VISIBLE);
                                binding.statusReciteWordOK.detailDivider.setVisibility(View.VISIBLE);

                                ConstraintSet constraintSet = new ConstraintSet();
                                constraintSet.clone(binding.statusReciteWordOK.wordContentCL);
                                constraintSet.constrainHeight(R.id.wordCV, ConstraintSet.MATCH_CONSTRAINT);
                                constraintSet.applyTo(binding.statusReciteWordOK.wordContentCL);

                                ConstraintSet headerConstraintSet = new ConstraintSet();
                                headerConstraintSet.clone(binding.statusReciteWordOK.headerContainerCL);
                                headerConstraintSet.clear(R.id.headerLayout, ConstraintSet.END);
                                headerConstraintSet.applyTo(binding.statusReciteWordOK.headerContainerCL);
                                setAndShowTranslation(currentReciteWord.getTranslations());
                                setAndShowWordForm(currentReciteWord.getForms());
                                showBanner(reciteWordUIState.getAiMnemonicLoadingState());
                                setAndShowWordExtract(inflater, container, currentReciteWord.getExtract(), currentReciteWord.getForms());
                                setAndShowStudyHistory(currentReciteWord.getDayPlanWords());
                            } else {
                                binding.statusReciteWordOK.wordTranslationLL.setVisibility(View.GONE);
                                binding.statusReciteWordOK.showWordDefinitionLL.setVisibility(View.VISIBLE);
                                binding.statusReciteWordOK.aiMnemonic.getRoot().setVisibility(View.GONE);
                                binding.statusReciteWordOK.aiChipGroup.setVisibility(View.GONE);
                                binding.statusReciteWordOK.detailDivider.setVisibility(View.GONE);
                                ConstraintSet constraintSet = new ConstraintSet();
                                constraintSet.clone(binding.statusReciteWordOK.wordContentCL);
                                constraintSet.constrainHeight(R.id.wordCV, ConstraintSet.WRAP_CONTENT);
                                constraintSet.applyTo(binding.statusReciteWordOK.wordContentCL);

                                ConstraintSet headerConstraintSet = new ConstraintSet();
                                headerConstraintSet.clone(binding.statusReciteWordOK.headerContainerCL);
                                headerConstraintSet.connect(
                                        R.id.headerLayout, ConstraintSet.END,
                                        ConstraintSet.PARENT_ID, ConstraintSet.END);
                                headerConstraintSet.applyTo(binding.statusReciteWordOK.headerContainerCL);
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setAndShowTranslation(List<WordTranslation> wordTranslations) {
        if (wordTranslations == null || wordTranslations.isEmpty()) {
            binding.statusReciteWordOK.wordTranslationLL.setVisibility(View.GONE);
            binding.statusReciteWordOK.wordTranslationLL.removeAllViews();
        } else {
            binding.statusReciteWordOK.wordTranslationLL.removeAllViews();
            binding.statusReciteWordOK.wordTranslationLL.setVisibility(View.VISIBLE);
            for (WordTranslation wordTranslation : wordTranslations) {
                ItemWordTranslationBinding translationBinding = ItemWordTranslationBinding.inflate(LayoutInflater.from(this.getContext()), binding.statusReciteWordOK.wordTranslationLL, false);
                if (!wordTranslation.getPos().getPos().isEmpty()) {
                    translationBinding.wordPosTV.setVisibility(View.VISIBLE);
                    translationBinding.wordPosTV.setText(wordTranslation.getPos().getPos());
                } else {
                    translationBinding.wordPosTV.setVisibility(View.GONE);
                }
                if (!wordTranslation.getMeanings().isEmpty()) {
                    for (WordMeaning wordMeaning : wordTranslation.getMeanings()) {
                        MaterialTextView wordMeaningTV = new MaterialTextView(requireContext());
                        wordMeaningTV.setText(wordMeaning.getMeaning());
                        wordMeaningTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(com.kite.mnemoai.ui.R.dimen.text_size_body_large));
                        wordMeaningTV.setTextColor(MaterialColors.getColor(wordMeaningTV, com.google.android.material.R.attr.colorOnSurface));
                        wordMeaningTV.setPadding(DensityUtilKt.dpToPx(requireContext(), 4), 0, DensityUtilKt.dpToPx(requireContext(), 4), 0);
                        translationBinding.wordMeaningsFlow.addView(wordMeaningTV);
                    }
                }
                binding.statusReciteWordOK.wordTranslationLL.addView(translationBinding.getRoot());
            }
        }
    }

    private void setAndShowWordForm(List<WordForm> wordFormEntities) {
        if (wordFormEntities == null || wordFormEntities.isEmpty()) {
            binding.statusReciteWordOK.aiMnemonic.wordForm.setVisibility(View.GONE);
            binding.statusReciteWordOK.aiMnemonic.wordFormFBL.removeAllViews();
        } else {
            binding.statusReciteWordOK.aiMnemonic.wordFormFBL.removeAllViews();
            for (WordForm wordFormEntity : wordFormEntities) {
                ItemWordFormBinding wordFormBinding = ItemWordFormBinding.inflate(getLayoutInflater(), binding.statusReciteWordOK.aiMnemonic.wordFormFBL, false);
                if (wordFormEntity.getTypeCode().equals("0") || wordFormEntity.getTypeCode().equals("1"))
                    continue;
                wordFormBinding.wordFormNameTV.setText(StringConvert.convertWordTypeCode(wordFormEntity.getTypeCode()));
                wordFormBinding.wordFormTV.setText(wordFormEntity.getForm());
                binding.statusReciteWordOK.aiMnemonic.wordFormFBL.addView(wordFormBinding.getRoot());
            }

            binding.statusReciteWordOK.aiMnemonic.wordForm.setVisibility(View.VISIBLE);
        }
    }

    private void showBanner(Result<String> aiMnemonicLoadingState) {
        if (aiMnemonicLoadingState == null) {
            bannerControl.forceHide();
        } else if (aiMnemonicLoadingState instanceof Result.Loading) {
            bannerControl.show();
            binding.statusReciteWordOK.AIGenerateResultTV.setText(getResources().getString(R.string.ai_thinking));
            binding.statusReciteWordOK.AIGenerateBanner.setBackgroundColor(MaterialColors.getColor(binding.statusReciteWordOK.AIGenerateBanner, com.google.android.material.R.attr.colorPrimaryContainer));
            binding.statusReciteWordOK.AIGenerateResultTV.setTextColor(MaterialColors.getColor(binding.statusReciteWordOK.AIGenerateResultTV, com.google.android.material.R.attr.colorOnPrimaryContainer));
        } else if (aiMnemonicLoadingState instanceof Result.Success) {
            bannerControl.hide();
        } else if (aiMnemonicLoadingState instanceof Result.Error) {
            String data = ((Result.Error) aiMnemonicLoadingState).getException().getMessage();
            bannerControl.startTimer(3000);
            binding.statusReciteWordOK.AIGenerateResultTV.setText(data);
            binding.statusReciteWordOK.AIGenerateBanner.setBackgroundColor(MaterialColors.getColor(binding.statusReciteWordOK.AIGenerateBanner, com.google.android.material.R.attr.colorErrorContainer));
            binding.statusReciteWordOK.AIGenerateResultTV.setTextColor(MaterialColors.getColor(binding.statusReciteWordOK.AIGenerateResultTV, com.google.android.material.R.attr.colorOnErrorContainer));
        }
    }

    private void setAndShowWordExtract(LayoutInflater inflater, View view, WordExtract currentWordExtract, List<WordForm> wordFormEntities) {
        if (currentWordExtract == null) {
            binding.statusReciteWordOK.aiMnemonic.extractContent.setVisibility(View.GONE);
        } else {
            binding.statusReciteWordOK.aiMnemonic.extractContent.setVisibility(View.VISIBLE);
            int textColor = MaterialColors.getColor(view, com.google.android.material.R.attr.colorOnSurface);
            int secondTextColor = MaterialColors.getColor(view, com.google.android.material.R.attr.colorOnSurfaceVariant);
            // 前后缀
            if (currentWordExtract.getAffix() == null) {
                binding.statusReciteWordOK.aiMnemonic.affix.setVisibility(View.GONE);
            } else {
                binding.statusReciteWordOK.aiMnemonic.affix.setVisibility(View.VISIBLE);
                binding.statusReciteWordOK.aiMnemonic.affixTV.setText(currentWordExtract.getAffix().toString());
            }
            // 释义
            if (currentWordExtract.getExplain() == null || currentWordExtract.getExplain().isEmpty()) {
                binding.statusReciteWordOK.aiMnemonic.explain.setVisibility(View.GONE);
            } else {
                binding.statusReciteWordOK.aiMnemonic.explain.setVisibility(View.VISIBLE);
                binding.statusReciteWordOK.aiMnemonic.explainTV.setText(currentWordExtract.getExplain());
            }
            // 例句
            if (currentWordExtract.getExampleSentences() == null || currentWordExtract.getExampleSentences().isEmpty()) {
                binding.statusReciteWordOK.aiMnemonic.sentence.setVisibility(View.GONE);
                binding.statusReciteWordOK.aiMnemonic.exampleSentenceLL.removeAllViews();
            } else {
                binding.statusReciteWordOK.aiMnemonic.sentence.setVisibility(View.VISIBLE);
                binding.statusReciteWordOK.aiMnemonic.exampleSentenceLL.removeAllViews();
                List<ExampleSentence> exampleSentences = currentWordExtract.getExampleSentences();
                for (ExampleSentence exampleSentence : exampleSentences) {
                    ItemExampleSentenceBinding exampleSentenceBinding = ItemExampleSentenceBinding.inflate(
                            inflater, binding.statusReciteWordOK.aiMnemonic.exampleSentenceLL, false
                    );
                    String sentence = exampleSentence.getSentence();
                    String word = currentWordExtract.getWord();
                    List<String> mutableList = wordFormEntities.stream()
                            .map(WordForm::getForm)
                            .collect(Collectors.toCollection(ArrayList::new));
                    mutableList.add(word);
                    int color = MaterialColors.getColor(exampleSentenceBinding.exampleSentenceTV, android.R.attr.colorPrimary);
                    SpannableString spannableString = TextHighlighter.INSTANCE.highlightWordWithForms(sentence, mutableList, color, true, 1.1f);

                    exampleSentenceBinding.exampleSentenceTV.setText(spannableString);
                    exampleSentenceBinding.exampleSentenceTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(com.kite.mnemoai.ui.R.dimen.text_size_body_large));
                    exampleSentenceBinding.exampleSentenceTV.setTextColor(textColor);
                    exampleSentenceBinding.exampleSentenceTranslateTV.setText(exampleSentence.getTranslation());
                    exampleSentenceBinding.exampleSentenceTranslateTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(com.kite.mnemoai.ui.R.dimen.text_size_body_large));
                    exampleSentenceBinding.exampleSentenceTranslateTV.setTextColor(secondTextColor);
                    binding.statusReciteWordOK.aiMnemonic.exampleSentenceLL.addView(exampleSentenceBinding.getRoot());
                }
            }
            // 短语
            if (currentWordExtract.getPhrases() == null || currentWordExtract.getPhrases().isEmpty()) {
                binding.statusReciteWordOK.aiMnemonic.phrase.setVisibility(View.GONE);
                binding.statusReciteWordOK.aiMnemonic.phraseLL.removeAllViews();
            } else {
                binding.statusReciteWordOK.aiMnemonic.phrase.setVisibility(View.VISIBLE);
                binding.statusReciteWordOK.aiMnemonic.phraseLL.removeAllViews();
                List<Phrase> phrases = currentWordExtract.getPhrases();
                for (Phrase phrase : phrases) {
                    ItemPhraseBinding itemPhraseBinding = ItemPhraseBinding.inflate(
                            inflater, binding.statusReciteWordOK.aiMnemonic.phraseLL, false
                    );
                    String sentence = phrase.getPhrase() + " " + phrase.getMeaning();
                    String word = currentWordExtract.getWord();

                    List<String> mutableList = wordFormEntities.stream()
                            .map(WordForm::getForm)
                            .collect(Collectors.toCollection(ArrayList::new));
                    mutableList.add(word);
                    int color = MaterialColors.getColor(itemPhraseBinding.phraseTV, android.R.attr.colorPrimary);
                    SpannableString spannableString = TextHighlighter.INSTANCE.highlightWordWithForms(sentence, mutableList, color, true, 1.1f);

                    itemPhraseBinding.phraseTV.setText(spannableString);
                    itemPhraseBinding.phraseTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(com.kite.mnemoai.ui.R.dimen.text_size_body_large));
                    itemPhraseBinding.phraseTV.setTextColor(textColor);
                    binding.statusReciteWordOK.aiMnemonic.phraseLL.addView(itemPhraseBinding.getRoot());
                }
            }
        }
    }

    private void setAndShowStudyHistory(List<DayPlanWord> reviewHistories) {
        if (reviewHistories == null || reviewHistories.isEmpty()) {
            binding.statusReciteWordOK.aiMnemonic.studyHistory.setVisibility(View.GONE);
        } else {
            String today = java.time.LocalDate.now().toString();
            List<ReviewHistoryItem> reviewHistoryItems =
                    reviewHistories.stream()
                            .filter(reviewHistory -> !today.equals(reviewHistory.getDate()))
                            .map(reviewHistory -> {
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
            if (reviewHistoryItems.isEmpty()) {
                binding.statusReciteWordOK.aiMnemonic.studyHistory.setVisibility(View.GONE);
            } else {
                binding.statusReciteWordOK.aiMnemonic.studyHistory.setVisibility(View.VISIBLE);
                binding.statusReciteWordOK.aiMnemonic.studyHistoryItemsRV.post(() ->
                        reviewAdapter.submitList(new ArrayList<>(reviewHistoryItems))
                );
            }
        }
    }

    @Override
    public void onStart() {
        viewModel.startReciteStatistics();
        super.onStart();
    }

    @Override
    public void onStop() {
        viewModel.stopReciteStatistics();
        super.onStop();
    }
}
