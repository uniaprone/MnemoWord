package com.kite.mnemoai.ui.reciteword;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
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
import androidx.transition.ChangeTransform;
import androidx.transition.Explode;
import androidx.transition.Fade;
import androidx.transition.Slide;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.textview.MaterialTextView;
import com.kite.mnemoai.data.local.entity.WordEntity;
import com.kite.mnemoai.data.local.entity.WordFormEntity;
import com.kite.mnemoai.data.local.entity.WordMeaningEntity;
import com.kite.mnemoai.data.model.WordExtract;
import com.kite.mnemoai.data.model.WordTranslation;
import com.kite.mnemoai.databinding.ItemWordFormBinding;
import com.kite.mnemoai.databinding.ItemWordTranslationBinding;
import com.kite.mnemoai.R;
import com.kite.mnemoai.data.local.entity.DayPlanWordEntity;
import com.kite.mnemoai.data.local.entity.WordExtractEntity;
import com.kite.mnemoai.data.model.ExampleSentence;
import com.kite.mnemoai.data.model.Phrase;
import com.kite.mnemoai.data.model.WordDetailInfo;
import com.kite.mnemoai.databinding.FragmentReciteWordBinding;
import com.kite.mnemoai.databinding.ItemExampleSentenceBinding;
import com.kite.mnemoai.databinding.ItemPhraseBinding;
import com.kite.mnemoai.stateholder.BannerControl;
import com.kite.mnemoai.ui.adapter.ReviewHistoryAdapter;
import com.kite.mnemoai.ui.adapter.ReviewHistoryItem;
import com.kite.mnemoai.ui.main.MainViewModel;
import com.kite.mnemoai.ui.model.LoadingState;
import com.kite.mnemoai.utils.DensityUtilKt;
import com.kite.mnemoai.utils.TextHighlighter;
import com.kite.mnemoai.utils.StringConvert;
import com.kite.mnemoai.utils.TimeUtilKt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ReciteWordFragment extends Fragment{
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
        viewModel = new ViewModelProvider(this, ReciteWordViewModel.Companion.getFactory()).get(ReciteWordViewModel.class);
        viewModel.setDailyDayPlanWordEntities();

        bannerControl = new BannerControl(binding.statusReciteWordOK.AIGenerateBanner, getLifecycle());

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
                        binding.statusReciteWordOK.linearProgressIndicator.setProgress(progress, true);
                        binding.statusReciteWordOK.numberProgressTV.setText(getResources().getString(R.string.number_progress, reciteWordUIState.getCurrentProgress(), reciteWordUIState.getTotalProgress()));

                        binding.statusReciteWordOK.generateAIMnemonicChip.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                viewModel.fetchWordExtract();
                            }
                        });

                        List<WordDetailInfo> original = reciteWordUIState.getReciteWordItemStatusOrder();
                        if(original != null && !original.isEmpty()){
                            binding.statusReciteWordOK.wordCV.setVisibility(View.VISIBLE);
                            WordDetailInfo currentReciteWord = original.get(0);
                            if(viewModel.isShowNext){
                                TransitionManager.endTransitions(binding.statusReciteWordOK.wordContentCL);
                                binding.statusReciteWordOK.wordTranslationLL.setVisibility(View.GONE);
                                binding.statusReciteWordOK.showWordDefinitionLL.setVisibility(View.VISIBLE);
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
                            binding.statusReciteWordOK.wordTV.setText(currentReciteWord.getWordEntity().getWord());
                            binding.statusReciteWordOK.phoneticTV.setText(currentReciteWord.getWordEntity().getPhonetic());
                            binding.statusReciteWordOK.showWordDefinitionLL.setOnClickListener((view) -> {
                                viewModel.showAll();
                            });
                            if(reciteWordUIState.isShowDetail()){
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
                                setAndShowTranslation(currentReciteWord.getWordTranslation());
                                setAndShowWordForm(currentReciteWord.getWordForm());
                                showBanner(reciteWordUIState.getAiMnemonicLoadingState());
                                setAndShowWordExtract(inflater, container, currentReciteWord.getWordExtractEntity(), currentReciteWord.getWordForm());
                                setAndShowStudyHistory(currentReciteWord.getDayPlanWordEntities());
                            }else{
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
//        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setAndShowTranslation(List<WordTranslation> wordTranslations){
        if(wordTranslations == null || wordTranslations.isEmpty()){
            binding.statusReciteWordOK.wordTranslationLL.setVisibility(View.GONE);
            binding.statusReciteWordOK.wordTranslationLL.removeAllViews();
        }else{
            binding.statusReciteWordOK.wordTranslationLL.removeAllViews();
            binding.statusReciteWordOK.wordTranslationLL.setVisibility(View.VISIBLE);
            for (WordTranslation wordTranslation: wordTranslations){
                ItemWordTranslationBinding translationBinding = ItemWordTranslationBinding.inflate(LayoutInflater.from(this.getContext()), binding.statusReciteWordOK.wordTranslationLL, false);
                if(!wordTranslation.getWordPos().getPos().isEmpty()){
                    translationBinding.wordPosTV.setVisibility(View.VISIBLE);
                    translationBinding.wordPosTV.setText(wordTranslation.getWordPos().getPos());
                }else {
                    translationBinding.wordPosTV.setVisibility(View.GONE);
                }
                if(!wordTranslation.getWordMeanings().isEmpty()){
                    for (WordMeaningEntity wordMeaning: wordTranslation.getWordMeanings()){
                        MaterialTextView wordMeaningTV = new MaterialTextView(requireContext());
                        wordMeaningTV.setText(wordMeaning.getMeaning());
                        wordMeaningTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_body_large));
                        wordMeaningTV.setTextColor(MaterialColors.getColor(wordMeaningTV, R.attr.colorOnSurface));
                        wordMeaningTV.setPadding(DensityUtilKt.dpToPx(requireContext(), 4), 0, DensityUtilKt.dpToPx(requireContext(), 4), 0);
                        translationBinding.wordMeaningsFlow.addView(wordMeaningTV);
                    }
                }
                binding.statusReciteWordOK.wordTranslationLL.addView(translationBinding.getRoot());
            }
        }
    }

    private void setAndShowWordForm(List<WordFormEntity> wordFormEntities){
        if(wordFormEntities == null || wordFormEntities.isEmpty()){
            binding.statusReciteWordOK.aiMnemonic.wordForm.setVisibility(View.GONE);
            binding.statusReciteWordOK.aiMnemonic.wordFormFBL.removeAllViews();
        }else{
            binding.statusReciteWordOK.aiMnemonic.wordFormFBL.removeAllViews();
            for(WordFormEntity wordFormEntity: wordFormEntities){
                ItemWordFormBinding wordFormBinding = ItemWordFormBinding.inflate(getLayoutInflater(), binding.statusReciteWordOK.aiMnemonic.wordFormFBL, false);
                if(wordFormEntity.getTypeCode().equals("0")|| wordFormEntity.getTypeCode().equals("1")) continue;
                wordFormBinding.wordFormNameTV.setText(StringConvert.convertWordTypeCode(wordFormEntity.getTypeCode()));
                wordFormBinding.wordFormTV.setText(wordFormEntity.getForm());
                binding.statusReciteWordOK.aiMnemonic.wordFormFBL.addView(wordFormBinding.getRoot());
            }

            binding.statusReciteWordOK.aiMnemonic.wordForm.setVisibility(View.VISIBLE);
        }
    }

    private void showBanner(LoadingState<String> aiMnemonicLoadingState){
        String data;
        if(aiMnemonicLoadingState == null){
            bannerControl.forceHide();
        }else if(aiMnemonicLoadingState instanceof LoadingState.Loading){
            bannerControl.show();
            binding.statusReciteWordOK.AIGenerateResultTV.setText(getResources().getString(R.string.ai_thinking));
            binding.statusReciteWordOK.AIGenerateBanner.setBackgroundColor(MaterialColors.getColor(binding.statusReciteWordOK.AIGenerateBanner, R.attr.colorPrimaryContainer));
            binding.statusReciteWordOK.AIGenerateResultTV.setTextColor(MaterialColors.getColor(binding.statusReciteWordOK.AIGenerateResultTV, R.attr.colorOnPrimaryContainer));
        } else if (aiMnemonicLoadingState instanceof LoadingState.Success) {
            bannerControl.hide();
        }else if(aiMnemonicLoadingState instanceof LoadingState.Error){
            data = ((LoadingState.Error) aiMnemonicLoadingState).getException().getMessage();
            bannerControl.startTimer(3000);
            binding.statusReciteWordOK.AIGenerateResultTV.setText(data);
            binding.statusReciteWordOK.AIGenerateBanner.setBackgroundColor(MaterialColors.getColor(binding.statusReciteWordOK.AIGenerateBanner, R.attr.colorErrorContainer));
            binding.statusReciteWordOK.AIGenerateResultTV.setTextColor(MaterialColors.getColor(binding.statusReciteWordOK.AIGenerateResultTV, R.attr.colorOnErrorContainer));
        }
    }

    private void setAndShowWordExtract(LayoutInflater inflater, View view, WordExtractEntity currentWordExtract, List<WordFormEntity> wordFormEntities){
        if(currentWordExtract == null){
            binding.statusReciteWordOK.aiMnemonic.extractContent.setVisibility(View.GONE);
        }else{
            binding.statusReciteWordOK.aiMnemonic.extractContent.setVisibility(View.VISIBLE);
            int textColor = MaterialColors.getColor(view, R.attr.colorOnSurface);
            int secondTextColor = MaterialColors.getColor(view, R.attr.colorOnSurfaceVariant);
            WordExtract wordExtract = currentWordExtract.getExtract();
            //前后缀
            if(wordExtract.getAffix() == null){
                binding.statusReciteWordOK.aiMnemonic.affix.setVisibility(View.GONE);
            }else {
                binding.statusReciteWordOK.aiMnemonic.affix.setVisibility(View.VISIBLE);
                binding.statusReciteWordOK.aiMnemonic.affixTV.setText(wordExtract.getAffix().toString());
            }
            //释义
            if(wordExtract.getExplain() == null || wordExtract.getExplain().isEmpty()){
                binding.statusReciteWordOK.aiMnemonic.explain.setVisibility(View.GONE);
            }else{
                binding.statusReciteWordOK.aiMnemonic.explain.setVisibility(View.VISIBLE);
                binding.statusReciteWordOK.aiMnemonic.explainTV.setText(wordExtract.getExplain());
            }
            //例句
            if(wordExtract.getExampleSentence() == null || wordExtract.getExampleSentence().isEmpty()){
                binding.statusReciteWordOK.aiMnemonic.sentence.setVisibility(View.GONE);
                binding.statusReciteWordOK.aiMnemonic.exampleSentenceLL.removeAllViews();
            }else{
                binding.statusReciteWordOK.aiMnemonic.sentence.setVisibility(View.VISIBLE);
                binding.statusReciteWordOK.aiMnemonic.exampleSentenceLL.removeAllViews();
                List<ExampleSentence> exampleSentences = currentWordExtract.getExtract().getExampleSentence();
                for (ExampleSentence exampleSentence: exampleSentences){
                    ItemExampleSentenceBinding exampleSentenceBinding = ItemExampleSentenceBinding.inflate(
                            inflater, binding.statusReciteWordOK.aiMnemonic.exampleSentenceLL, false
                    );
                    String sentence = exampleSentence.getSentence();
                    String word = currentWordExtract.getExtract().getWord();
                    List<String> mutableList = wordFormEntities.stream()
                            .map(WordFormEntity::getForm)
                            .collect(Collectors.toCollection(ArrayList::new));
                    mutableList.add(word);
                    int color = MaterialColors.getColor(exampleSentenceBinding.exampleSentenceTV, R.attr.colorPrimary);
                    SpannableString spannableString = TextHighlighter.INSTANCE.highlightWordWithForms(sentence, mutableList, color, true, 1.1f);

                    exampleSentenceBinding.exampleSentenceTV.setText(spannableString);
                    exampleSentenceBinding.exampleSentenceTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_body_large));
                    exampleSentenceBinding.exampleSentenceTV.setTextColor(textColor);
                    exampleSentenceBinding.exampleSentenceTranslateTV.setText(exampleSentence.getTranslation());
                    exampleSentenceBinding.exampleSentenceTranslateTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_body_large));
                    exampleSentenceBinding.exampleSentenceTranslateTV.setTextColor(secondTextColor);
                    binding.statusReciteWordOK.aiMnemonic.exampleSentenceLL.addView(exampleSentenceBinding.getRoot());
                }
            }
            //短语
            if(wordExtract.getPhrase() == null || wordExtract.getPhrase().isEmpty()){
                binding.statusReciteWordOK.aiMnemonic.phrase.setVisibility(View.GONE);
                binding.statusReciteWordOK.aiMnemonic.phraseLL.removeAllViews();
            }else {
                binding.statusReciteWordOK.aiMnemonic.phrase.setVisibility(View.VISIBLE);
                binding.statusReciteWordOK.aiMnemonic.phraseLL.removeAllViews();
                List<Phrase> phrases = currentWordExtract.getExtract().getPhrase();
                for(Phrase phrase: phrases){
                    ItemPhraseBinding itemPhraseBinding = ItemPhraseBinding.inflate(
                            inflater, binding.statusReciteWordOK.aiMnemonic.phraseLL, false
                    );
                    String sentence = phrase.toString();
                    String word = currentWordExtract.getExtract().getWord();

                    List<String> mutableList = wordFormEntities.stream()
                            .map(WordFormEntity::getForm)
                            .collect(Collectors.toCollection(ArrayList::new));
                    mutableList.add(word);
                    int color = MaterialColors.getColor(itemPhraseBinding.phraseTV, R.attr.colorPrimary);
                    SpannableString spannableString = TextHighlighter.INSTANCE.highlightWordWithForms(sentence, mutableList, color, true, 1.1f);

                    itemPhraseBinding.phraseTV.setText(spannableString);
                    itemPhraseBinding.phraseTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_body_large));
                    itemPhraseBinding.phraseTV.setTextColor(textColor);
                    binding.statusReciteWordOK.aiMnemonic.phraseLL.addView(itemPhraseBinding.getRoot());
                }
            }

        }
    }

    private void setAndShowStudyHistory(List<DayPlanWordEntity> reviewHistories){
        if(reviewHistories == null || reviewHistories.isEmpty()){
            binding.statusReciteWordOK.aiMnemonic.studyHistory.setVisibility(View.GONE);
        }else{
            binding.statusReciteWordOK.aiMnemonic.studyHistory.setVisibility(View.VISIBLE);
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
}
