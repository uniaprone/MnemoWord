package com.kite.mnemoai.ui.worddetail;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.kite.mnemoai.R;
import com.kite.mnemoai.data.local.entity.DayPlanWordEntity;
import com.kite.mnemoai.data.local.entity.WordEntity;
import com.kite.mnemoai.data.local.entity.WordExtractEntity;
import com.kite.mnemoai.data.model.ExampleSentence;
import com.kite.mnemoai.data.model.Phrase;
import com.kite.mnemoai.data.model.WordDetailInfo;
import com.kite.mnemoai.data.model.WordExtract;
import com.kite.mnemoai.databinding.FragmentWordDetailBinding;
import com.kite.mnemoai.databinding.ItemExampleSentenceBinding;
import com.kite.mnemoai.databinding.ItemPhraseBinding;
import com.kite.mnemoai.stateholder.BannerControl;
import com.kite.mnemoai.ui.adapter.ReviewHistoryAdapter;
import com.kite.mnemoai.ui.adapter.ReviewHistoryItem;
import com.kite.mnemoai.ui.main.MainViewModel;
import com.kite.mnemoai.ui.model.LoadingState;
import com.kite.mnemoai.ui.reciteword.ReciteWordUIState;
import com.kite.mnemoai.utils.TimeUtilKt;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WordDetailFragment extends Fragment {
    private FragmentWordDetailBinding binding;
    private WordDetailViewModel viewModel;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private RecyclerView reviewRV;
    private ReviewHistoryAdapter reviewHistoryAdapter;
    private BannerControl bannerControl;

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
        bannerControl = new BannerControl(binding.aiMnemonic.AIGenerateBanner, this.getLifecycle());
        binding.aiMnemonic.generateAIMnemonicChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestPermission();
            }
        });
        viewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(WordDetailViewModel.initializer)).get(WordDetailViewModel.class);

        reviewRV = binding.aiMnemonic.studyHistoryItemsRV;
        reviewHistoryAdapter = new ReviewHistoryAdapter();
        reviewRV.setAdapter(reviewHistoryAdapter);
        reviewRV.setLayoutManager(new LinearLayoutManager(this.getContext()));
        reviewRV.setItemAnimator(null);

        viewModel.getUiState().observe(getViewLifecycleOwner(), wordDetailUIState -> {
            if(wordDetailUIState == null || wordDetailUIState.getWordDetailInfo() == null) return;

            TransitionManager.beginDelayedTransition(binding.aiMnemonic.getRoot(),
                    new TransitionSet().addTransition(new ChangeBounds()).addTransition(new Fade()));
            showAll(
                    inflater,
                    wordDetailUIState.getWordDetailInfo().getWordEntity(),
                    wordDetailUIState.getAiMnemonicLoadingState(),
                    wordDetailUIState.getWordDetailInfo().getWordExtractEntity(),
                    wordDetailUIState.getWordDetailInfo().getDayPlanWordEntities()
            );
        });

        return binding.getRoot();
    }

    private void showAll(LayoutInflater inflater, WordEntity wordEntity, LoadingState<String> aiMnemonicLoadingState, WordExtractEntity wordExtractEntity, List<DayPlanWordEntity> dayPlanWordEntities){
        showWord(wordEntity);
        binding.aiMnemonic.getRoot().setVisibility(View.VISIBLE);
        showWordExtract(inflater, aiMnemonicLoadingState, wordExtractEntity);
        showReviewHistory(dayPlanWordEntities);
    }

    private void showWord(WordEntity word){
        binding.wordTV.setText(word.getWord());
        binding.phoneticTV.setText(word.getPhonetic());
        binding.translateTV.setText(word.getTranslation());
    }
    
    private void showWordExtract(LayoutInflater inflater, LoadingState<String> aiMnemonicLoadingState, WordExtractEntity wordExtractEntity){
        String data;
        if(aiMnemonicLoadingState instanceof LoadingState.Loading){
            bannerControl.show();
            binding.aiMnemonic.AIGenerateResultTV.setText(getResources().getString(R.string.ai_thinking));
        } else if (aiMnemonicLoadingState instanceof LoadingState.Success) {
            bannerControl.hide();
        }else if(aiMnemonicLoadingState instanceof LoadingState.Error){
            data = ((LoadingState.Error) aiMnemonicLoadingState).getException().getMessage();
            bannerControl.startTimer(3000);
            binding.aiMnemonic.AIGenerateResultTV.setText(data);
        }

        if(wordExtractEntity == null || wordExtractEntity.getExtract() == null){
            binding.aiMnemonic.noExtractContent.setVisibility(View.VISIBLE);
            binding.aiMnemonic.extractContent.setVisibility(View.GONE);
        }else{
            WordExtract wordExtract = wordExtractEntity.getExtract();
            binding.aiMnemonic.noExtractContent.setVisibility(View.GONE);
            binding.aiMnemonic.extractContent.setVisibility(View.VISIBLE);

            int textColor = MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorOnSurface);
            binding.aiMnemonic.coreImageTV.setText(wordExtract.getCoreImage());
            binding.aiMnemonic.affixTV.setText(wordExtract.getAffix().toString());
            binding.aiMnemonic.explainTV.setText(wordExtract.getExplain());
            binding.aiMnemonic.exampleSentenceLL.removeAllViews();
            List<ExampleSentence> exampleSentences = wordExtract.getExampleSentence();
            for (ExampleSentence exampleSentence: exampleSentences){
                ItemExampleSentenceBinding exampleSentenceBinding = ItemExampleSentenceBinding.inflate(
                        inflater, binding.aiMnemonic.exampleSentenceLL, false
                );
                exampleSentenceBinding.exampleSentenceTV.setText(exampleSentence.getSentence());
                exampleSentenceBinding.exampleSentenceTV.setTextColor(textColor);
                exampleSentenceBinding.exampleSentenceTranslateTV.setText(exampleSentence.getTranslation());
                exampleSentenceBinding.exampleSentenceTranslateTV.setTextColor(textColor);
                binding.aiMnemonic.exampleSentenceLL.addView(exampleSentenceBinding.getRoot());
            }
            binding.aiMnemonic.phraseLL.removeAllViews();
            List<Phrase> phrases = wordExtract.getPhrase();
            for(Phrase phrase: phrases){
                ItemPhraseBinding itemPhraseBinding = ItemPhraseBinding.inflate(
                        inflater, binding.aiMnemonic.phraseLL, false
                );
                itemPhraseBinding.phraseTV.setText(phrase.toString());
                itemPhraseBinding.phraseTV.setTextColor(textColor);
                binding.aiMnemonic.phraseLL.addView(itemPhraseBinding.getRoot());
            }
        }
    }
    
    private void showReviewHistory(List<DayPlanWordEntity> dayPlanWordEntities){
        if(dayPlanWordEntities == null || dayPlanWordEntities.isEmpty()){
            binding.aiMnemonic.studyHistoryContent.setVisibility(View.GONE);
            binding.aiMnemonic.noStudyHistoryContent.setVisibility(View.VISIBLE);
        }else{
            binding.aiMnemonic.studyHistoryContent.setVisibility(View.VISIBLE);
            binding.aiMnemonic.noStudyHistoryContent.setVisibility(View.GONE);
            
            List<ReviewHistoryItem> reviewHistoryItems = 
                    dayPlanWordEntities.stream().map(dayPlanWordEntity -> 
                            new ReviewHistoryItem(dayPlanWordEntity.getWordId(),
                                    dayPlanWordEntity.getDate(),
                                    TimeUtilKt.formatDuration(dayPlanWordEntity.getLearningTime()),
                                    1,
                                    dayPlanWordEntity.getBlurCount(),
                                    dayPlanWordEntity.getForgetCount())
                    ).collect(Collectors.toList());
            reviewHistoryAdapter.submitList(new ArrayList<>(reviewHistoryItems));
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
