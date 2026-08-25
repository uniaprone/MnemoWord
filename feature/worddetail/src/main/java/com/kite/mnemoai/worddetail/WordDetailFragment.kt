package com.kite.mnemoai.worddetail

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.google.android.material.color.MaterialColors
import com.google.android.material.textview.MaterialTextView
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.word.Word
import com.kite.mnemoai.model.word.WordDetail
import com.kite.mnemoai.model.word.WordExtract
import com.kite.mnemoai.model.word.WordForm
import com.kite.mnemoai.model.word.WordTranslation
import com.kite.mnemoai.ui.BannerControl
import com.kite.mnemoai.ui.ReviewHistoryAdapter
import com.kite.mnemoai.ui.dpToPx
import com.kite.mnemoai.ui.main.MainViewModel
import com.kite.mnemoai.worddetail.databinding.FragmentWordDetailBinding
import com.kite.mnemoai.worddetail.databinding.ItemWordTranslationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.getValue

@AndroidEntryPoint
class WordDetailFragment : Fragment() {
    private var _binding: FragmentWordDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WordDetailViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel.settitle(getString(R.string.word_detail))
        mainViewModel.setShowNavIcon(true)

        _binding = FragmentWordDetailBinding.inflate(inflater, container, false)
        binding.reciteWordView.setGenerateAIMnemonicListener { viewModel.fetchWordExtract() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.uiState.collect { wordDetailUIState ->
                    if (wordDetailUIState == null || wordDetailUIState.wordDetail == null) return@collect
                    TransitionManager.beginDelayedTransition(
                        binding.getRoot(),
                        TransitionSet().addTransition(ChangeBounds())
                    )
                    showAll(
                        wordDetailUIState.wordDetail,
                        wordDetailUIState.aiMnemonicLoadingState
                    )
                }
            }
        }

        return binding.getRoot()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showAll(wordDetail: WordDetail, aiMnemonicLoadingState: Result<String>?) {
        binding.reciteWordView.setReciteDetailStyle(wordDetail)
        binding.reciteWordView.showBanner(aiMnemonicLoadingState)
        setAndShowWordForm(wordDetail.forms)
        setAndShowExplain(wordDetail.extract)
        setAndShowExampleSentence(wordDetail.extract, wordDetail.forms)
        setAndShowPhrase(wordDetail.extract, wordDetail.forms)
        setAndShowAffix(wordDetail.extract)
        setAndShowStudyHistory(wordDetail)
    }

    private fun setAndShowWordForm(wordForms: List<WordForm>?) {
        if (wordForms.isNullOrEmpty()) {
            binding.reciteMorphologyView.visibility = View.GONE
        } else {
            binding.reciteMorphologyView.setData(wordForms)
            binding.reciteMorphologyView.visibility = View.VISIBLE
        }
    }
    private fun setAndShowExplain(currentWordExtract: WordExtract?){
        val view = binding.reciteExplainView
        val explain = currentWordExtract?.explain
        if (explain != null) {
            view.apply {
                visibility = View.VISIBLE
                setData(explain)
            }
        } else {
            view.visibility = View.GONE
        }
    }
    private fun setAndShowExampleSentence(currentWordExtract: WordExtract?, wordForms: List<WordForm>){
        val view = binding.recitTranslationView
        val exampleSentences = currentWordExtract?.exampleSentences
        if(!exampleSentences.isNullOrEmpty()){
            view.visibility = View.VISIBLE
            val highlightTexts = wordForms.map { it.form }.toMutableList()
            highlightTexts.add(currentWordExtract.word)
            view.setData(exampleSentences, highlightTexts)
        }else{
            view.visibility = View.GONE
        }
    }
    private fun setAndShowPhrase(currentWordExtract: WordExtract?, wordForms: List<WordForm>){
        val view = binding.recitePhraseView
        val phrases = currentWordExtract?.phrases
        if(!phrases.isNullOrEmpty()){
            view.visibility = View.VISIBLE
            val highlightTexts = wordForms.map { it.form }.toMutableList()
            highlightTexts.add(currentWordExtract.word)
            view.setData(phrases, highlightTexts)
        }else{
            view.visibility = View.GONE
        }
    }
    private fun setAndShowAffix(currentWordExtract: WordExtract?){
        val view = binding.reciteAffixView
        val affix = currentWordExtract?.affix
        if(affix != null){
            view.visibility = View.VISIBLE
            view.setData(affix)
        }else{
            view.visibility = View.GONE
        }
    }
    private fun setAndShowStudyHistory(currentWord: WordDetail?) {
        val view = binding.reciteReviewHistoryView
        val reviewHistories= currentWord?.dayPlanWords
        if (!reviewHistories.isNullOrEmpty()) {
            view.visibility = View.VISIBLE
            view.setData(reviewHistories)
        } else {
            view.visibility = View.GONE
        }
    }
}
