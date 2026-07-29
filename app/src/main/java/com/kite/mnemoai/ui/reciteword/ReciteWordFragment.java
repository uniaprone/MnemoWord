package com.kite.mnemoai.ui.reciteword;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
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
import com.google.android.material.textview.MaterialTextView;
import com.kite.mnemoai.data.local.entity.WordMeaningEntity;
import com.kite.mnemoai.data.model.WordTranslation;
import com.kite.mnemoai.databinding.ItemWordTranslationBinding;
import com.kite.mnemoai.ui.main.MainActivity;
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
import com.kite.mnemoai.utils.TimeUtilKt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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

        bannerControl = new BannerControl(binding.statusReciteWordOK.aiMnemonic.AIGenerateBanner, getLifecycle());

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

                        binding.statusReciteWordOK.aiMnemonic.generateAIMnemonicChip.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                viewModel.fetchWordExtract();
                            }
                        });

                        List<WordDetailInfo> original = reciteWordUIState.getReciteWordItemStatusOrder();
                        if(original != null && !original.isEmpty()){
                            WordDetailInfo currentReciteWord = original.get(0);
                            if(viewModel.isShowNext){
                                TransitionManager.endTransitions(binding.statusReciteWordOK.wordContentCL);
                                viewModel.isShowNext = false;
                                binding.statusReciteWordOK.wordCardVF.showNext();
                                viewModel.startReciteStatistics();
                                viewModel.resetShowState();
                            }
                            binding.statusReciteWordOK.wordTV.setText(currentReciteWord.getWordEntity().getWord());
                            binding.statusReciteWordOK.phoneticTV.setText(currentReciteWord.getWordEntity().getPhonetic());
                            binding.statusReciteWordOK.showWordDefinitionLL.setOnClickListener((view) -> {
                                TransitionManager.beginDelayedTransition(binding.statusReciteWordOK.wordContentCL,
                                        new TransitionSet().addTransition(new ChangeBounds()).addTransition(new Fade()).excludeChildren(binding.statusReciteWordOK.wordTranslationLL, true));
                                ConstraintSet constraintSet = new ConstraintSet();
                                constraintSet.clone(binding.statusReciteWordOK.wordContentCL);
                                constraintSet.constrainHeight(R.id.wordCV, ConstraintSet.MATCH_CONSTRAINT);
                                constraintSet.applyTo(binding.statusReciteWordOK.wordContentCL);

                                viewModel.showAll();
                            });
                            if(reciteWordUIState.isShowDetail()){
                                TransitionManager.beginDelayedTransition(binding.statusReciteWordOK.wordContentCL,
                                        new TransitionSet().addTransition(new ChangeBounds()).addTransition(new Fade()).excludeChildren(binding.statusReciteWordOK.wordTranslationLL, true));
                                binding.statusReciteWordOK.showWordDefinitionLL.setVisibility(View.GONE);
                                binding.statusReciteWordOK.aiMnemonic.getRoot().setVisibility(View.VISIBLE);
                                ConstraintSet constraintSet = new ConstraintSet();
                                constraintSet.clone(binding.statusReciteWordOK.wordContentCL);
                                constraintSet.constrainHeight(R.id.wordCV, ConstraintSet.MATCH_CONSTRAINT);
                                constraintSet.applyTo(binding.statusReciteWordOK.wordContentCL);

                                setAndShowTranslation(currentReciteWord.getWordTranslation());
                                setAndShowWordExtract(inflater, container, reciteWordUIState.getAiMnemonicLoadingState(), currentReciteWord.getWordExtractEntity());
                                setAndShowStudyHistory(currentReciteWord.getDayPlanWordEntities());
                            }else{
                                binding.statusReciteWordOK.wordTranslationLL.setVisibility(View.GONE);
                                binding.statusReciteWordOK.showWordDefinitionLL.setVisibility(View.VISIBLE);
                                binding.statusReciteWordOK.aiMnemonic.getRoot().setVisibility(View.GONE);

                                ConstraintSet constraintSet = new ConstraintSet();
                                constraintSet.clone(binding.statusReciteWordOK.wordContentCL);
                                constraintSet.constrainHeight(R.id.wordCV, ConstraintSet.WRAP_CONTENT);
                                constraintSet.applyTo(binding.statusReciteWordOK.wordContentCL);
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
    private void setAndShowWordExtract(LayoutInflater inflater, View view, LoadingState<String> aiMnemonicLoadingState, WordExtractEntity currentWordExtract){
        String data;
        if(aiMnemonicLoadingState == null){
            bannerControl.forceHide();
        }else if(aiMnemonicLoadingState instanceof LoadingState.Loading){
            bannerControl.show();
            binding.statusReciteWordOK.aiMnemonic.AIGenerateResultTV.setText(getResources().getString(R.string.ai_thinking));
            binding.statusReciteWordOK.aiMnemonic.AIGenerateBanner.setBackgroundColor(MaterialColors.getColor(binding.statusReciteWordOK.aiMnemonic.AIGenerateBanner, com.google.android.material.R.attr.colorPrimaryContainer));
            binding.statusReciteWordOK.aiMnemonic.AIGenerateResultTV.setTextColor(MaterialColors.getColor(binding.statusReciteWordOK.aiMnemonic.AIGenerateResultTV, com.google.android.material.R.attr.colorOnPrimaryContainer));
        } else if (aiMnemonicLoadingState instanceof LoadingState.Success) {
            bannerControl.hide();
        }else if(aiMnemonicLoadingState instanceof LoadingState.Error){
            data = ((LoadingState.Error) aiMnemonicLoadingState).getException().getMessage();
            bannerControl.startTimer(3000);
            binding.statusReciteWordOK.aiMnemonic.AIGenerateResultTV.setText(data);
            binding.statusReciteWordOK.aiMnemonic.AIGenerateBanner.setBackgroundColor(MaterialColors.getColor(binding.statusReciteWordOK.aiMnemonic.AIGenerateBanner, com.google.android.material.R.attr.colorErrorContainer));
            binding.statusReciteWordOK.aiMnemonic.AIGenerateResultTV.setTextColor(MaterialColors.getColor(binding.statusReciteWordOK.aiMnemonic.AIGenerateResultTV, com.google.android.material.R.attr.colorOnErrorContainer));
        }

        if(currentWordExtract == null){
            binding.statusReciteWordOK.aiMnemonic.noExtractContent.setVisibility(View.VISIBLE);
            binding.statusReciteWordOK.aiMnemonic.extractContent.setVisibility(View.GONE);
        }else{
            binding.statusReciteWordOK.aiMnemonic.extractContent.setVisibility(View.VISIBLE);
            binding.statusReciteWordOK.aiMnemonic.noExtractContent.setVisibility(View.GONE);
            int textColor = MaterialColors.getColor(view, R.attr.colorOnSurface);
            binding.statusReciteWordOK.aiMnemonic.coreImageTV.setText(currentWordExtract.getExtract().getCoreImage());
            binding.statusReciteWordOK.aiMnemonic.affixTV.setText(currentWordExtract.getExtract().getAffix().toString());
            binding.statusReciteWordOK.aiMnemonic.explainTV.setText(currentWordExtract.getExtract().getExplain());
            binding.statusReciteWordOK.aiMnemonic.exampleSentenceLL.removeAllViews();
            List<ExampleSentence> exampleSentences = currentWordExtract.getExtract().getExampleSentence();
            for (ExampleSentence exampleSentence: exampleSentences){
                ItemExampleSentenceBinding exampleSentenceBinding = ItemExampleSentenceBinding.inflate(
                        inflater, binding.statusReciteWordOK.aiMnemonic.exampleSentenceLL, false
                );
                SpannableStringBuilder builder = new SpannableStringBuilder();
                String sentence = exampleSentence.getSentence();
                String word = currentWordExtract.getExtract().getWord();

                int wordStartIndex = sentence.indexOf(word);
                if (wordStartIndex == -1) {
                    // 单词不在例句中，直接显示原句
                    builder.append(sentence);
                } else {
                    // 1. 追加单词前的部分
                    builder.append(sentence.substring(0, wordStartIndex));
                    // 2. 记录单词在 builder 中的起始位置
                    int start = builder.length();
                    // 3. 追加单词
                    builder.append(word);
                    // 4. 记录结束位置
                    int end = builder.length();
                    // 5. 设置样式（颜色 + 粗体）
                    builder.setSpan(
                            new ForegroundColorSpan(MaterialColors.getColor(
                                    exampleSentenceBinding.exampleSentenceTV,
                                    R.attr.colorPrimary)),
                            start, end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                    builder.setSpan(
                            new RelativeSizeSpan(1.2f),
                            start, end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                    builder.setSpan(
                            new StyleSpan(Typeface.BOLD),
                            start, end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                    // 6. 追加单词后的部分
                    builder.append(sentence.substring(wordStartIndex + word.length()));
                }

                exampleSentenceBinding.exampleSentenceTV.setText(builder);
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
                SpannableStringBuilder builder = new SpannableStringBuilder();
                String sentence = phrase.toString();
                String word = currentWordExtract.getExtract().getWord();

                int wordStartIndex = sentence.indexOf(word);
                if (wordStartIndex == -1) {
                    // 单词不在例句中，直接显示原句
                    builder.append(sentence);
                } else {
                    // 1. 追加单词前的部分
                    builder.append(sentence.substring(0, wordStartIndex));
                    // 2. 记录单词在 builder 中的起始位置
                    int start = builder.length();
                    // 3. 追加单词
                    builder.append(word);
                    // 4. 记录结束位置
                    int end = builder.length();
                    // 5. 设置样式（颜色 + 粗体）
                    builder.setSpan(
                            new ForegroundColorSpan(MaterialColors.getColor(
                                    itemPhraseBinding.phraseTV,
                                    R.attr.colorPrimary)),
                            start, end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                    builder.setSpan(
                            new RelativeSizeSpan(1.2f),
                            start, end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                    builder.setSpan(
                            new StyleSpan(Typeface.BOLD),
                            start, end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                    // 6. 追加单词后的部分
                    builder.append(sentence.substring(wordStartIndex + word.length()));
                }

                itemPhraseBinding.phraseTV.setText(builder);
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
}
