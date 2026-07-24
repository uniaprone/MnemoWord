package com.kite.mnemoai.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.kite.mnemoai.data.local.entity.DayPlanWordEntity;
import com.kite.mnemoai.data.local.entity.WordEntity;
import com.kite.mnemoai.data.local.entity.WordExtractEntity;
import com.kite.mnemoai.data.model.WordDetailInfo;
import com.kite.mnemoai.databinding.WordDetailCardContentBinding;

import java.util.List;

public class WordDetailCardContentView extends MaterialCardView {
    public interface IOnAiMnemoaiClickListener{
        void onClick();
    }
    private IOnAiMnemoaiClickListener onAiMnemoaiClickListener;
    private WordDetailCardContentBinding binding;
    private final LayoutInflater inflater;

    public WordDetailCardContentView(Context context) {
        super(context);
        inflater = LayoutInflater.from(getContext());
        binding = WordDetailCardContentBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public WordDetailCardContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflater = LayoutInflater.from(getContext());
        binding = WordDetailCardContentBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public WordDetailCardContentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflater = LayoutInflater.from(getContext());
        binding = WordDetailCardContentBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public void bind(WordDetailInfo reciteWordItemStatus){
        int textColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface);
//        WordDetailStatus wordDetailStatus = reciteWordItemStatus.get;
        WordEntity word = reciteWordItemStatus.getWordEntity();
        WordExtractEntity wordExtract = reciteWordItemStatus.getWordExtractEntity();
        List<DayPlanWordEntity> wordReview = reciteWordItemStatus.getDayPlanWordEntities();
        if(word == null) return;
//        if(reciteWordItemStatus.isShowTranslation()){
//            binding.translateTV.setVisibility(View.VISIBLE);
//        }else{
//            binding.translateTV.setVisibility(View.INVISIBLE);
//        }
        binding.wordTV.setText(word.getWord());
        binding.phoneticTV.setText(word.getPhonetic());
        binding.translateTV.setText(word.getTranslation());

//        switch (wordDetailStatus){
//            case show:
//                binding.aiMnemonic.wordDetailLL.setVisibility(View.VISIBLE);
//                binding.aiMnemonicError.statusAiMneMonicErrorCL.setVisibility(View.GONE);
//                binding.aiMnemonicHide.statusAiMneMonicHideCL.setVisibility(View.GONE);
//                binding.aiMnemonicLoading.statusAiMneMonicLoadingCL.setVisibility(View.GONE);
//                if(wordReview != null){
//
//                }
//                if(wordExtract == null){
//
//                }else{
//                    binding.aiMnemonic.coreImageTV.setText(wordExtract.getCoreImage());
//                    binding.aiMnemonic.affixTV.setText(wordExtract.getAffix().toString());
//                    binding.aiMnemonic.explainTV.setText(wordExtract.getExplain());
//                    binding.aiMnemonic.exampleSentenceLL.removeAllViews();
//                    List<ExampleSentence> exampleSentences = wordExtract.getExampleSentence();
//                    for (ExampleSentence exampleSentence: exampleSentences){
//                        ItemExampleSentenceBinding exampleSentenceBinding = ItemExampleSentenceBinding.inflate(
//                                inflater, binding.aiMnemonic.exampleSentenceLL, false
//                        );
//                        exampleSentenceBinding.exampleSentenceTV.setText(exampleSentence.getSentence());
//                        exampleSentenceBinding.exampleSentenceTV.setTextColor(textColor);
//                        exampleSentenceBinding.exampleSentenceTranslateTV.setText(exampleSentence.getTranslation());
//                        exampleSentenceBinding.exampleSentenceTranslateTV.setTextColor(textColor);
//                        binding.aiMnemonic.exampleSentenceLL.addView(exampleSentenceBinding.getRoot());
//                    }
//                    binding.aiMnemonic.phraseLL.removeAllViews();
//                    List<Phrase> phrases = wordExtract.getPhrase();
//                    for(Phrase phrase: phrases){
//                        ItemPhraseBinding itemPhraseBinding = ItemPhraseBinding.inflate(
//                                inflater, binding.aiMnemonic.phraseLL, false
//                        );
//                        itemPhraseBinding.phraseTV.setText(phrase.toString());
//                        itemPhraseBinding.phraseTV.setTextColor(textColor);
//                        binding.aiMnemonic.phraseLL.addView(itemPhraseBinding.getRoot());
//                    }
//                }
//                break;
//            case hide:
//                binding.aiMnemonic.wordDetailLL.setVisibility(View.GONE);
//                binding.aiMnemonicError.statusAiMneMonicErrorCL.setVisibility(View.GONE);
//                binding.aiMnemonicHide.statusAiMneMonicHideCL.setVisibility(View.VISIBLE);
//                binding.aiMnemonicLoading.statusAiMneMonicLoadingCL.setVisibility(View.GONE);
//                break;
//            case loading:
//                binding.aiMnemonic.wordDetailLL.setVisibility(View.GONE);
//                binding.aiMnemonicError.statusAiMneMonicErrorCL.setVisibility(View.GONE);
//                binding.aiMnemonicHide.statusAiMneMonicHideCL.setVisibility(View.GONE);
//                binding.aiMnemonicLoading.statusAiMneMonicLoadingCL.setVisibility(View.VISIBLE);
//                break;
//            case error:
//                binding.aiMnemonic.wordDetailLL.setVisibility(View.GONE);
//                binding.aiMnemonicError.statusAiMneMonicErrorCL.setVisibility(View.VISIBLE);
//                binding.aiMnemonicHide.statusAiMneMonicHideCL.setVisibility(View.GONE);
//                binding.aiMnemonicLoading.statusAiMneMonicLoadingCL.setVisibility(View.GONE);
//                break;
//        }

    }

    public void bindClickListener(){
        binding.aiMnemonicHide.statusAiMneMonicHideCL.setOnClickListener((v) -> {
            if(this.onAiMnemoaiClickListener != null){
                this.onAiMnemoaiClickListener.onClick();
            }
        });
        binding.aiMnemonicError.statusAiMneMonicErrorCL.setOnClickListener(v -> {
            if(this.onAiMnemoaiClickListener != null){
                this.onAiMnemoaiClickListener.onClick();
            }
        });
    }

    public void setOnAiMnemoaiClickListener(IOnAiMnemoaiClickListener onAiMnemoaiClickListener) {
        this.onAiMnemoaiClickListener = onAiMnemoaiClickListener;
    }
}
