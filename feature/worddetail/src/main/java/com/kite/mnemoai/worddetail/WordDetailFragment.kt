package com.kite.mnemoai.worddetail

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.word.WordDetail
import com.kite.mnemoai.model.word.WordExtract
import com.kite.mnemoai.model.word.WordForm
import com.kite.mnemoai.shared_ui.chat.AiChatBottomSheetDialogFragment
import com.kite.mnemoai.ui.main.MainViewModel
import com.kite.mnemoai.worddetail.databinding.FragmentWordDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WordDetailFragment : Fragment() {
    private var _binding: FragmentWordDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WordDetailViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private var player: Player? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel.settitle(getString(R.string.word_detail))
        mainViewModel.setShowNavIcon(true)

        _binding = FragmentWordDetailBinding.inflate(inflater, container, false)
        binding.root.layoutTransition = LayoutTransition()
        binding.reciteWordView.setGenerateAIMnemonicListener { viewModel.fetchWordExtract() }
        binding.reciteWordView.setCommunicateWithAIListener {
            val wordId = viewModel.uiState.value?.wordDetail?.word?.id
            if (wordId != null) {
                AiChatBottomSheetDialogFragment.newInstance(wordId).show(
                    requireActivity().supportFragmentManager,
                    AiChatBottomSheetDialogFragment.TAG
                )
            }
        }
        binding.reciteWordView.setSpeechListener {
            playVoice()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { wordDetailUIState ->
                        if (wordDetailUIState == null || wordDetailUIState.wordDetail == null) return@collect
                        showAll(wordDetailUIState.wordDetail)
                    }
                }
                launch {
                    viewModel.aiMnemonicLoadingState.collect { state ->
                        when (state) {
                            is Result.Success -> binding.bannerView.setSuccess(state.data)
                            is Result.Error -> binding.bannerView.setFailure(state.exception.message)
                            is Result.Loading -> binding.bannerView.setLoading(
                                getString(com.kite.mnemoai.ui.R.string.ai_thinking)
                            )
                            null -> {}
                        }
                    }
                }
            }
        }

        return binding.getRoot()
    }

    override fun onStart() {
        super.onStart()
        if (player == null) {
            player = ExoPlayer.Builder(requireContext()).build()
        }
    }

    override fun onStop() {
        player?.release()
        player = null
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun playVoice() {
        val uri = viewModel.getWordVoiceUri()
        if (uri.isBlank()) return
        val p = player ?: return
        if (p.playbackState != Player.STATE_IDLE) {
            p.stop()
        }
        p.setMediaItem(MediaItem.fromUri(uri))
        p.prepare()
        p.play()
    }

    private fun showAll(wordDetail: WordDetail) {
        binding.reciteWordView.setReciteDetailStyle(wordDetail)
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
