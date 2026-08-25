package com.kite.mnemoai.reciteword

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.kite.mnemoai.model.word.WordDetail
import com.kite.mnemoai.model.word.WordExtract
import com.kite.mnemoai.model.word.WordForm
import com.kite.mnemoai.reciteword.databinding.FragmentReciteWordBinding
import com.kite.mnemoai.ui.main.MainViewModel
import com.kite.mnemoai.ui.widget.BannerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReciteWordFragment : Fragment() {
    private var _binding: FragmentReciteWordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReciteWordViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var bannerView: BannerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel.settitle(getString(R.string.recite_word))
        mainViewModel.setShowNavIcon(false)

        _binding = FragmentReciteWordBinding.inflate(inflater, container, false)
        viewModel.setDailyDayPlanWordEntities()
        bannerView = BannerView(requireContext())
        // 动作按钮
        binding.statusReciteWordOK.rememberBtn.setOnClickListener { viewModel.rememberWord() }
        binding.statusReciteWordOK.blurBtn.setOnClickListener { viewModel.blurWord() }
        binding.statusReciteWordOK.forgetBtn.setOnClickListener { viewModel.forgetWord() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { state -> renderReciteState(state) } }
                launch { viewModel.showNext.collect { showNextCard() } }
            }
        }

        return binding.root
    }

    private fun renderReciteState(reciteWordUIState: ReciteWordUIState?) {
        if (reciteWordUIState == null) return
        val reciteStage = reciteWordUIState.reciteStage ?: return
        when (reciteStage) {
            ReciteStage.IN_PROGRESS -> {
                binding.statusReciteWordOK.statusReciteWordOKCL.visibility = View.VISIBLE
                binding.statusReciteWordNoVocabulary.statusReciteWordNoVocabularyCL.visibility =
                    View.GONE
                binding.statusReciteWordFinish.statusReciteWordFinishCL.visibility = View.GONE

                val progress =
                    ((reciteWordUIState.currentProgress * 100f) / reciteWordUIState.totalProgress).toInt()
                binding.statusReciteWordOK.linearProgressIndicator.setProgress(progress, true)
                binding.statusReciteWordOK.numberProgressTV.text = getString(
                    R.string.number_progress,
                    reciteWordUIState.currentProgress,
                    reciteWordUIState.totalProgress
                )
                binding.statusReciteWordOK.reciteWordView.setShowReciteDetailListener {
                    viewModel.showAll()
                }
                binding.statusReciteWordOK.reciteWordView.setGenerateAIMnemonicListener {
                    viewModel.fetchWordExtract()
                }
                val original: MutableList<WordDetail>? = reciteWordUIState.reciteWordItemStatusOrder
                if (!original.isNullOrEmpty()) {
                    val currentReciteWord = original[0]
                    binding.statusReciteWordOK.reciteWordView.setReciteStyle(currentReciteWord)
                    if (reciteWordUIState.isShowDetail) {
                        TransitionManager.beginDelayedTransition(
                            binding.statusReciteWordOK.NestedScrollContainer,
                            TransitionSet().apply {
                                addTransition(ChangeBounds())
                                addTransition(Fade().addTarget(bannerView))
                            }
                        )

                        binding.statusReciteWordOK.NestedScrollContainer.gravity = Gravity.START

                        binding.statusReciteWordOK.reciteWordView.setReciteDetailStyle(currentReciteWord)
                        binding.statusReciteWordOK.reciteWordView.showBanner(reciteWordUIState.aiMnemonicLoadingState)
                        setAndShowWordForm(currentReciteWord.forms)
                        setAndShowAffix(currentReciteWord.extract)
                        setAndShowExplain(currentReciteWord.extract)
                        setAndShowExampleSentence(
                            currentReciteWord.extract,
                            currentReciteWord.forms
                        )
                        setAndShowPhrase(
                            currentReciteWord.extract,
                            currentReciteWord.forms
                        )
                        setAndShowStudyHistory(currentReciteWord)
                    } else {

                        hideAll()
                        binding.statusReciteWordOK.NestedScrollContainer.gravity = Gravity.CENTER
                    }
                }
            }

            ReciteStage.NO_VOCABULARY -> {
                binding.statusReciteWordOK.statusReciteWordOKCL.visibility = View.GONE
                binding.statusReciteWordNoVocabulary.statusReciteWordNoVocabularyCL.visibility =
                    View.VISIBLE
                binding.statusReciteWordFinish.statusReciteWordFinishCL.visibility = View.GONE
            }

            ReciteStage.FINISH -> {
                binding.statusReciteWordOK.statusReciteWordOKCL.visibility = View.GONE
                binding.statusReciteWordNoVocabulary.statusReciteWordNoVocabularyCL.visibility =
                    View.GONE
                binding.statusReciteWordFinish.statusReciteWordFinishCL.visibility = View.VISIBLE
            }
        }
    }

    /**
     * 翻到下一张单词卡片：重置卡片视图并开始新一轮学习统计。
     * 由 ViewModel 的 showNext 一次性事件触发。
     */
    private fun showNextCard() {
        TransitionManager.endTransitions(binding.statusReciteWordOK.NestedScrollContainer)
        hideAll()
        binding.statusReciteWordOK.wordCardVF.showNext()
        viewModel.startReciteStatistics()
        viewModel.resetShowState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun hideAll(){
        (bannerView.parent as? ViewGroup)?.removeView(bannerView)

        binding.statusReciteWordOK.reciteMorphologyView.visibility = View.GONE
        binding.statusReciteWordOK.reciteExplainView.visibility = View.GONE
        binding.statusReciteWordOK.recitTranslationView.visibility = View.GONE
        binding.statusReciteWordOK.recitePhraseView.visibility = View.GONE
        binding.statusReciteWordOK.reciteAffixView.visibility = View.GONE
        binding.statusReciteWordOK.reciteReviewHistoryView.visibility = View.GONE
    }
    private fun setAndShowWordForm(wordForms: List<WordForm>?) {
        if (wordForms.isNullOrEmpty()) {
            binding.statusReciteWordOK.reciteMorphologyView.visibility = View.GONE
        } else {
            binding.statusReciteWordOK.reciteMorphologyView.setData(wordForms)
            binding.statusReciteWordOK.reciteMorphologyView.visibility = View.VISIBLE
        }
    }

    private fun setAndShowExplain(currentWordExtract: WordExtract?){
        val view = binding.statusReciteWordOK.reciteExplainView
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
        val view = binding.statusReciteWordOK.recitTranslationView
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
        val view = binding.statusReciteWordOK.recitePhraseView
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
        val view = binding.statusReciteWordOK.reciteAffixView
        val affix = currentWordExtract?.affix
        if(affix != null){
            view.visibility = View.VISIBLE
            view.setData(affix)
        }else{
            view.visibility = View.GONE
        }
    }
    private fun setAndShowStudyHistory(currentWord: WordDetail?) {
        val view = binding.statusReciteWordOK.reciteReviewHistoryView
        val reviewHistories= currentWord?.dayPlanWords
        if (!reviewHistories.isNullOrEmpty()) {
            view.visibility = View.VISIBLE
            view.setData(reviewHistories)
        } else {
            view.visibility = View.GONE
        }
    }

    override fun onStart() {
        viewModel.startReciteStatistics()
        super.onStart()
    }

    override fun onStop() {
        viewModel.stopReciteStatistics()
        super.onStop()
    }
}
