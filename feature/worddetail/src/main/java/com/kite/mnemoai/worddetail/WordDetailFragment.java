package com.kite.mnemoai.worddetail;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.textview.MaterialTextView;
import com.kite.mnemoai.worddetail.R;
import com.kite.mnemoai.model.dayplan.DayPlanWord;
import com.kite.mnemoai.model.word.ExampleSentence;
import com.kite.mnemoai.model.word.Phrase;
import com.kite.mnemoai.model.word.Word;
import com.kite.mnemoai.model.word.WordDetail;
import com.kite.mnemoai.model.word.WordExtract;
import com.kite.mnemoai.model.word.WordForm;
import com.kite.mnemoai.model.word.WordMeaning;
import com.kite.mnemoai.model.word.WordTranslation;
import com.kite.mnemoai.model.Result;
import com.kite.mnemoai.worddetail.databinding.FragmentWordDetailBinding;
import com.kite.mnemoai.worddetail.databinding.ItemExampleSentenceBinding;
import com.kite.mnemoai.worddetail.databinding.ItemPhraseBinding;
import com.kite.mnemoai.worddetail.databinding.ItemWordFormBinding;
import com.kite.mnemoai.worddetail.databinding.ItemWordTranslationBinding;
import com.kite.mnemoai.ui.BannerControl;
import com.kite.mnemoai.ui.ReviewHistoryAdapter;
import com.kite.mnemoai.ui.ReviewHistoryItem;
import com.kite.mnemoai.ui.main.MainViewModel;
import com.kite.mnemoai.ui.DensityUtilKt;
import com.kite.mnemoai.common.StringConvert;
import com.kite.mnemoai.ui.TextHighlighter;
import com.kite.mnemoai.ui.TimeUtilKt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WordDetailFragment extends Fragment {
    private FragmentWordDetailBinding binding;
    private WordDetailViewModel viewModel;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private RecyclerView reviewRV;
    private ReviewHistoryAdapter reviewHistoryAdapter;
    private BannerControl bannerControl;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGrand -> {
            if (isGrand) {
                viewModel.fetchWordExtract();
            } else {
                showPermissionDeniedMessage();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        MainViewModel mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        mainViewModel.settitle(getResources().getString(R.string.word_detail));
        mainViewModel.setShowNavIcon(true);

        binding = FragmentWordDetailBinding.inflate(inflater, container, false);
        bannerControl = new BannerControl(binding.AIGenerateBanner, this.getLifecycle());
        binding.generateAIMnemonicChip.setOnClickListener(v -> checkAndRequestPermission());
        viewModel = new ViewModelProvider(this).get(WordDetailViewModel.class);

        reviewRV = binding.aiMnemonic.studyHistoryItemsRV;
        reviewHistoryAdapter = new ReviewHistoryAdapter();
        reviewRV.setAdapter(reviewHistoryAdapter);
        reviewRV.setLayoutManager(new LinearLayoutManager(this.getContext()));
        reviewRV.setItemAnimator(null);

        viewModel.getUiState().observe(getViewLifecycleOwner(), wordDetailUIState -> {
            if (wordDetailUIState == null || wordDetailUIState.getWordDetail() == null) return;

            TransitionManager.beginDelayedTransition(binding.getRoot(),
                    new TransitionSet().addTransition(new ChangeBounds()));
            showAll(
                    wordDetailUIState.getWordDetail(),
                    wordDetailUIState.getAiMnemonicLoadingState()
            );
        });

        return binding.getRoot();
    }

    private void showAll(WordDetail wordDetail, Result<String> aiMnemonicLoadingState) {
        binding.aiMnemonic.getRoot().setVisibility(View.VISIBLE);
        showWord(wordDetail.getWord());
        showTranslation(wordDetail.getTranslations());
        showBanner(aiMnemonicLoadingState);
        showWordExtract(wordDetail.getExtract(), wordDetail.getForms());
        showWordForm(wordDetail.getForms());
        showReviewHistory(wordDetail.getDayPlanWords());
    }

    private void showWord(Word word) {
        binding.wordTV.setText(word.getWord());
        binding.phoneticTV.setText(word.getPhonetic());
    }

    private void showTranslation(List<WordTranslation> wordTranslations) {
        if (wordTranslations == null || wordTranslations.isEmpty()) {
            binding.wordTranslationLL.setVisibility(View.GONE);
            binding.wordTranslationLL.removeAllViews();
        } else {
            binding.wordTranslationLL.removeAllViews();
            binding.wordTranslationLL.setVisibility(View.VISIBLE);
            for (WordTranslation wordTranslation : wordTranslations) {
                ItemWordTranslationBinding translationBinding = ItemWordTranslationBinding.inflate(LayoutInflater.from(this.getContext()), binding.wordTranslationLL, false);
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
                binding.wordTranslationLL.addView(translationBinding.getRoot());
            }
        }
    }

    private void showWordForm(List<WordForm> wordFormEntities) {
        if (wordFormEntities == null || wordFormEntities.isEmpty()) {
            binding.aiMnemonic.wordForm.setVisibility(View.GONE);
            binding.aiMnemonic.wordFormFBL.removeAllViews();
        } else {
            binding.aiMnemonic.wordFormFBL.removeAllViews();
            for (WordForm wordFormEntity : wordFormEntities) {
                ItemWordFormBinding wordFormBinding = ItemWordFormBinding.inflate(getLayoutInflater(), binding.aiMnemonic.wordFormFBL, false);
                if (wordFormEntity.getTypeCode().equals("0") || wordFormEntity.getTypeCode().equals("1"))
                    continue;
                wordFormBinding.wordFormNameTV.setText(StringConvert.convertWordTypeCode(wordFormEntity.getTypeCode()));
                wordFormBinding.wordFormTV.setText(wordFormEntity.getForm());
                binding.aiMnemonic.wordFormFBL.addView(wordFormBinding.getRoot());
            }

            binding.aiMnemonic.wordForm.setVisibility(View.VISIBLE);
        }
    }

    private void showBanner(Result<String> aiMnemonicLoadingState) {
        if (aiMnemonicLoadingState == null) {
            bannerControl.forceHide();
        } else if (aiMnemonicLoadingState instanceof Result.Loading) {
            binding.AIGenerateBanner.setVisibility(View.VISIBLE);
            bannerControl.show();
            binding.AIGenerateResultTV.setText(getResources().getString(R.string.ai_thinking));
            binding.AIGenerateBanner.setBackgroundColor(MaterialColors.getColor(binding.AIGenerateBanner, com.google.android.material.R.attr.colorPrimaryContainer));
            binding.AIGenerateResultTV.setTextColor(MaterialColors.getColor(binding.AIGenerateResultTV, com.google.android.material.R.attr.colorOnPrimaryContainer));
        } else if (aiMnemonicLoadingState instanceof Result.Success) {
            bannerControl.hide();
        } else if (aiMnemonicLoadingState instanceof Result.Error) {
            String data = ((Result.Error) aiMnemonicLoadingState).getException().getMessage();
            bannerControl.startTimer(3000);
            binding.AIGenerateResultTV.setText(data);
            binding.AIGenerateBanner.setBackgroundColor(MaterialColors.getColor(binding.AIGenerateBanner, com.google.android.material.R.attr.colorErrorContainer));
            binding.AIGenerateResultTV.setTextColor(MaterialColors.getColor(binding.AIGenerateResultTV, com.google.android.material.R.attr.colorOnErrorContainer));
        }
    }

    private void showWordExtract(WordExtract currentWordExtract, List<WordForm> wordFormEntities) {
        if (currentWordExtract == null) {
            binding.aiMnemonic.extractContent.setVisibility(View.GONE);
        } else {
            binding.aiMnemonic.extractContent.setVisibility(View.VISIBLE);
            int textColor = MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorOnSurface);
            int secondTextColor = MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorOnSurfaceVariant);
            // 前后缀
            if (currentWordExtract.getAffix() == null) {
                binding.aiMnemonic.affix.setVisibility(View.GONE);
            } else {
                binding.aiMnemonic.affix.setVisibility(View.VISIBLE);
                binding.aiMnemonic.affixTV.setText(currentWordExtract.getAffix().toString());
            }
            // 释义
            if (currentWordExtract.getExplain() == null || currentWordExtract.getExplain().isEmpty()) {
                binding.aiMnemonic.explain.setVisibility(View.GONE);
            } else {
                binding.aiMnemonic.explain.setVisibility(View.VISIBLE);
                binding.aiMnemonic.explainTV.setText(currentWordExtract.getExplain());
            }
            // 例句
            if (currentWordExtract.getExampleSentences() == null || currentWordExtract.getExampleSentences().isEmpty()) {
                binding.aiMnemonic.sentence.setVisibility(View.GONE);
                binding.aiMnemonic.exampleSentenceLL.removeAllViews();
            } else {
                binding.aiMnemonic.sentence.setVisibility(View.VISIBLE);
                binding.aiMnemonic.exampleSentenceLL.removeAllViews();
                List<ExampleSentence> exampleSentences = currentWordExtract.getExampleSentences();
                for (ExampleSentence exampleSentence : exampleSentences) {
                    ItemExampleSentenceBinding exampleSentenceBinding = ItemExampleSentenceBinding.inflate(
                            getLayoutInflater(), binding.aiMnemonic.exampleSentenceLL, false
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
                    binding.aiMnemonic.exampleSentenceLL.addView(exampleSentenceBinding.getRoot());
                }
            }
            // 短语
            if (currentWordExtract.getPhrases() == null || currentWordExtract.getPhrases().isEmpty()) {
                binding.aiMnemonic.phrase.setVisibility(View.GONE);
                binding.aiMnemonic.phraseLL.removeAllViews();
            } else {
                binding.aiMnemonic.phrase.setVisibility(View.VISIBLE);
                binding.aiMnemonic.phraseLL.removeAllViews();
                List<Phrase> phrases = currentWordExtract.getPhrases();
                for (Phrase phrase : phrases) {
                    ItemPhraseBinding itemPhraseBinding = ItemPhraseBinding.inflate(
                            getLayoutInflater(), binding.aiMnemonic.phraseLL, false
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
                    binding.aiMnemonic.phraseLL.addView(itemPhraseBinding.getRoot());
                }
            }
        }
    }

    private void showReviewHistory(List<DayPlanWord> reviewHistories) {
        if (reviewHistories == null || reviewHistories.isEmpty()) {
            binding.aiMnemonic.studyHistory.setVisibility(View.GONE);
        } else {
            binding.aiMnemonic.studyHistory.setVisibility(View.VISIBLE);
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
            binding.aiMnemonic.studyHistoryItemsRV.post(() ->
                    reviewHistoryAdapter.submitList(new ArrayList<>(reviewHistoryItems))
            );
        }
    }

    private void checkAndRequestPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
            viewModel.fetchWordExtract();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.INTERNET)) {
            showRationaleDialog();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.INTERNET);
        }
    }

    public void showPermissionDeniedMessage() {
        Toast.makeText(getContext(), "无法获取ai助记信息", Toast.LENGTH_SHORT).show();
    }

    private void showRationaleDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("需要相机权限")
                .setMessage("我们需要相机权限来扫描二维码")
                .setPositiveButton("允许", (dialog, which) -> requestPermissionLauncher.launch(Manifest.permission.CAMERA))
                .setNegativeButton("取消", null)
                .show();
    }
}
