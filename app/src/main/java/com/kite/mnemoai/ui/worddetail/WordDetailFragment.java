package com.kite.mnemoai.ui.worddetail;

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
import androidx.transition.Fade;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.textview.MaterialTextView;
import com.kite.mnemoai.R;
import com.kite.mnemoai.data.local.entity.DayPlanWordEntity;
import com.kite.mnemoai.data.local.entity.WordEntity;
import com.kite.mnemoai.data.local.entity.WordExtractEntity;
import com.kite.mnemoai.data.local.entity.WordFormEntity;
import com.kite.mnemoai.data.local.entity.WordMeaningEntity;
import com.kite.mnemoai.data.model.ExampleSentence;
import com.kite.mnemoai.data.model.Phrase;
import com.kite.mnemoai.data.model.WordDetailInfo;
import com.kite.mnemoai.data.model.WordExtract;
import com.kite.mnemoai.data.model.WordTranslation;
import com.kite.mnemoai.databinding.FragmentWordDetailBinding;
import com.kite.mnemoai.databinding.ItemExampleSentenceBinding;
import com.kite.mnemoai.databinding.ItemPhraseBinding;
import com.kite.mnemoai.databinding.ItemWordFormBinding;
import com.kite.mnemoai.databinding.ItemWordTranslationBinding;
import com.kite.mnemoai.stateholder.BannerControl;
import com.kite.mnemoai.ui.adapter.ReviewHistoryAdapter;
import com.kite.mnemoai.ui.adapter.ReviewHistoryItem;
import com.kite.mnemoai.ui.main.MainViewModel;
import com.kite.mnemoai.ui.model.LoadingState;
import com.kite.mnemoai.ui.reciteword.ReciteWordUIState;
import com.kite.mnemoai.utils.DensityUtilKt;
import com.kite.mnemoai.utils.StringConvert;
import com.kite.mnemoai.utils.TextHighlighter;
import com.kite.mnemoai.utils.TimeUtilKt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
            if(isGrand){
                viewModel.fetchWordExtract();
            }else{
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
        binding.generateAIMnemonicChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestPermission();
            }
        });
        viewModel = new ViewModelProvider(this,WordDetailViewModel.Companion.getFactory()).get(WordDetailViewModel.class);

        reviewRV = binding.aiMnemonic.studyHistoryItemsRV;
        reviewHistoryAdapter = new ReviewHistoryAdapter();
        reviewRV.setAdapter(reviewHistoryAdapter);
        reviewRV.setLayoutManager(new LinearLayoutManager(this.getContext()));
        reviewRV.setItemAnimator(null);

        viewModel.uiState.observe(getViewLifecycleOwner(), wordDetailUIState -> {
            if(wordDetailUIState == null || wordDetailUIState.getWordDetailInfo() == null) return;

            TransitionManager.beginDelayedTransition(binding.getRoot(),
                    new TransitionSet().addTransition(new ChangeBounds()));
            showAll(
                    wordDetailUIState.getWordDetailInfo(),
                    wordDetailUIState.getAiMnemonicLoadingState()
            );
        });

        return binding.getRoot();
    }

    private void showAll(WordDetailInfo wordDetailInfo, LoadingState<String> aiMnemonicLoadingState){
        binding.aiMnemonic.getRoot().setVisibility(View.VISIBLE);
        showWord(wordDetailInfo.getWordEntity());
        showTranslation(wordDetailInfo.getWordTranslation());
        showBanner(aiMnemonicLoadingState);
        showWordExtract(wordDetailInfo.getWordExtractEntity(), wordDetailInfo.getWordForm());
        showWordForm(wordDetailInfo.getWordForm());
        showReviewHistory(wordDetailInfo.getDayPlanWordEntities());
    }

    private void showWord(WordEntity word){
        binding.wordTV.setText(word.getWord());
        binding.phoneticTV.setText(word.getPhonetic());
    }

    private void showTranslation(List<WordTranslation> wordTranslations){
        if(wordTranslations == null || wordTranslations.isEmpty()){
            binding.wordTranslationLL.setVisibility(View.GONE);
            binding.wordTranslationLL.removeAllViews();
        }else{
            binding.wordTranslationLL.removeAllViews();
            binding.wordTranslationLL.setVisibility(View.VISIBLE);
            for (WordTranslation wordTranslation: wordTranslations){
                ItemWordTranslationBinding translationBinding = ItemWordTranslationBinding.inflate(LayoutInflater.from(this.getContext()), binding.wordTranslationLL, false);
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
                binding.wordTranslationLL.addView(translationBinding.getRoot());
            }
        }
    }

    private void showWordForm(List<WordFormEntity> wordFormEntities){
        if(wordFormEntities == null || wordFormEntities.isEmpty()){
            binding.aiMnemonic.wordForm.setVisibility(View.GONE);
            binding.aiMnemonic.wordFormFBL.removeAllViews();
        }else{
            binding.aiMnemonic.wordFormFBL.removeAllViews();
            for(WordFormEntity wordFormEntity: wordFormEntities){
                ItemWordFormBinding wordFormBinding = ItemWordFormBinding.inflate(getLayoutInflater(), binding.aiMnemonic.wordFormFBL, false);
                if(wordFormEntity.getTypeCode().equals("0")|| wordFormEntity.getTypeCode().equals("1")) continue;
                wordFormBinding.wordFormNameTV.setText(StringConvert.convertWordTypeCode(wordFormEntity.getTypeCode()));
                wordFormBinding.wordFormTV.setText(wordFormEntity.getForm());
                binding.aiMnemonic.wordFormFBL.addView(wordFormBinding.getRoot());
            }

            binding.aiMnemonic.wordForm.setVisibility(View.VISIBLE);
        }
    }

    private void showBanner(LoadingState<String> aiMnemonicLoadingState){
        String data;
        if(aiMnemonicLoadingState == null){
            bannerControl.forceHide();
        }else if(aiMnemonicLoadingState instanceof LoadingState.Loading){
            binding.AIGenerateBanner.setVisibility(View.VISIBLE);
            bannerControl.show();
            binding.AIGenerateResultTV.setText(getResources().getString(R.string.ai_thinking));
            binding.AIGenerateBanner.setBackgroundColor(MaterialColors.getColor(binding.AIGenerateBanner, R.attr.colorPrimaryContainer));
            binding.AIGenerateResultTV.setTextColor(MaterialColors.getColor(binding.AIGenerateResultTV, R.attr.colorOnPrimaryContainer));
        } else if (aiMnemonicLoadingState instanceof LoadingState.Success) {
            bannerControl.hide();
        }else if(aiMnemonicLoadingState instanceof LoadingState.Error){
            data = ((LoadingState.Error) aiMnemonicLoadingState).getException().getMessage();
            bannerControl.startTimer(3000);
            binding.AIGenerateResultTV.setText(data);
            binding.AIGenerateBanner.setBackgroundColor(MaterialColors.getColor(binding.AIGenerateBanner, R.attr.colorErrorContainer));
            binding.AIGenerateResultTV.setTextColor(MaterialColors.getColor(binding.AIGenerateResultTV, R.attr.colorOnErrorContainer));
        }
    }

    private void showWordExtract(WordExtractEntity currentWordExtract, List<WordFormEntity> wordFormEntities){
        if(currentWordExtract == null){
            binding.aiMnemonic.extractContent.setVisibility(View.GONE);
        }else{
            binding.aiMnemonic.extractContent.setVisibility(View.VISIBLE);
            int textColor = MaterialColors.getColor(binding.getRoot(), R.attr.colorOnSurface);
            int secondTextColor = MaterialColors.getColor(binding.getRoot(), R.attr.colorOnSurfaceVariant);
            WordExtract wordExtract = currentWordExtract.getExtract();
            //前后缀
            if(wordExtract.getAffix() == null){
                binding.aiMnemonic.affix.setVisibility(View.GONE);
            }else {
                binding.aiMnemonic.affix.setVisibility(View.VISIBLE);
                binding.aiMnemonic.affixTV.setText(wordExtract.getAffix().toString());
            }
            //释义
            if(wordExtract.getExplain() == null || wordExtract.getExplain().isEmpty()){
                binding.aiMnemonic.explain.setVisibility(View.GONE);
            }else{
                binding.aiMnemonic.explain.setVisibility(View.VISIBLE);
                binding.aiMnemonic.explainTV.setText(wordExtract.getExplain());
            }
            //例句
            if(wordExtract.getExampleSentence() == null || wordExtract.getExampleSentence().isEmpty()){
                binding.aiMnemonic.sentence.setVisibility(View.GONE);
                binding.aiMnemonic.exampleSentenceLL.removeAllViews();
            }else{
                binding.aiMnemonic.sentence.setVisibility(View.VISIBLE);
                binding.aiMnemonic.exampleSentenceLL.removeAllViews();
                List<ExampleSentence> exampleSentences = currentWordExtract.getExtract().getExampleSentence();
                for (ExampleSentence exampleSentence: exampleSentences){
                    ItemExampleSentenceBinding exampleSentenceBinding = ItemExampleSentenceBinding.inflate(
                            getLayoutInflater(), binding.aiMnemonic.exampleSentenceLL, false
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
                    binding.aiMnemonic.exampleSentenceLL.addView(exampleSentenceBinding.getRoot());
                }
            }
            //短语
            if(wordExtract.getPhrase() == null || wordExtract.getPhrase().isEmpty()){
                binding.aiMnemonic.phrase.setVisibility(View.GONE);
                binding.aiMnemonic.phraseLL.removeAllViews();
            }else {
                binding.aiMnemonic.phrase.setVisibility(View.VISIBLE);
                binding.aiMnemonic.phraseLL.removeAllViews();
                List<Phrase> phrases = currentWordExtract.getExtract().getPhrase();
                for(Phrase phrase: phrases){
                    ItemPhraseBinding itemPhraseBinding = ItemPhraseBinding.inflate(
                            getLayoutInflater(), binding.aiMnemonic.phraseLL, false
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
                    binding.aiMnemonic.phraseLL.addView(itemPhraseBinding.getRoot());
                }
            }

        }
    }

    private void showReviewHistory(List<DayPlanWordEntity> reviewHistories){
        if(reviewHistories == null || reviewHistories.isEmpty()){
            binding.aiMnemonic.studyHistory.setVisibility(View.GONE);
        }else{
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

    private void checkAndRequestPermission(){
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED){
            viewModel.fetchWordExtract();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.INTERNET)) {
            showRationaleDialog();
        }else{
            requestPermissionLauncher.launch(Manifest.permission.INTERNET);
        }
    }


    public void showPermissionDeniedMessage(){
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
