package com.kite.mnemoai.reciteword

import android.animation.LayoutTransition
import android.app.AlertDialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.MenuProvider
import androidx.core.view.updateLayoutParams
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
import com.kite.mnemoai.model.recite.PronounceType
import com.kite.mnemoai.model.word.WordDetail
import com.kite.mnemoai.model.word.WordExtract
import com.kite.mnemoai.model.word.WordForm
import com.kite.mnemoai.reciteword.databinding.FragmentReciteWordBinding
import com.kite.mnemoai.shared_ui.chat.AiChatBottomSheetDialogFragment
import com.kite.mnemoai.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReciteWordFragment : Fragment(), MenuProvider {
    private var _binding: FragmentReciteWordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReciteWordViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private var player: Player? = null
    private var autoPronounceMenuItem: MenuItem? = null
    private var pronounceTypeMenuItem: MenuItem? = null
    private val layoutTransition = LayoutTransition()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel.settitle(getString(R.string.recite_word))
        mainViewModel.setShowNavIcon(false)

        _binding = FragmentReciteWordBinding.inflate(inflater, container, false)
        viewModel.setDailyDayPlanWordEntities()
        // 动作按钮
        binding.statusReciteWordOK.rememberBtn.setOnClickListener { viewModel.rememberWord() }
        binding.statusReciteWordOK.blurBtn.setOnClickListener { viewModel.blurWord() }
        binding.statusReciteWordOK.forgetBtn.setOnClickListener { viewModel.forgetWord() }

        binding.root.layoutTransition = layoutTransition
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        initInProgressBtn()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { state -> renderReciteState(state) } }
                launch { viewModel.showNext.collect {
                        showNextCard()
                    }
                }
                launch { viewModel.aiMnemonicLoadingState.collect {
                    when(it){
                        is Result.Success -> binding.bannerView.setSuccess(it.data)
                        is Result.Error -> binding.bannerView.setFailure(it.exception.message)
                        is Result.Loading -> binding.bannerView.setLoading(getString(com.kite.mnemoai.ui.R.string.ai_thinking))
                        else -> {}
                    }
                } }
            }
        }

        return binding.root
    }

    override fun onStart() {
        if(Build.VERSION.SDK_INT > 23){
            player = ExoPlayer.Builder(requireContext()).build()
        }
        viewModel.onResume()
        viewModel.startReciteStatistics()
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT <= 23 || player == null) {
            player = ExoPlayer.Builder(requireContext()).build()
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopReciteStatistics()
        if (Build.VERSION.SDK_INT > 23) {
            player?.let {
                it.release()
            }
            player = null
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT <= 23) {
            player?.let {
                it.release()
            }
            player = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun renderReciteState(reciteWordUIState: ReciteWordUIState?) {
        if (reciteWordUIState == null) return
        updateMenuState(reciteWordUIState)
        val reciteStage = reciteWordUIState.reciteStage ?: return
        when (reciteStage) {
            ReciteStage.IN_PROGRESS -> {
                binding.statusReciteWordOK.statusReciteWordOKCL.visibility = View.VISIBLE
                binding.statusReciteWordNoVocabulary.statusReciteWordNoVocabularyCL.visibility =
                    View.GONE
                binding.statusReciteWordFinish.statusReciteWordFinishCL.visibility = View.GONE
                binding.statusReciteWordNotStarted.statusReciteWordNotStartedCL.visibility =
                    View.GONE

                val progress =if(reciteWordUIState.totalProgress == 0){
                    0
                }else{
                    ((reciteWordUIState.currentProgress * 100f) / reciteWordUIState.totalProgress).toInt()
                }
                binding.statusReciteWordOK.linearProgressIndicator.setProgress(progress, true)
                binding.statusReciteWordOK.numberProgressTV.text = getString(
                    R.string.number_progress,
                    reciteWordUIState.currentProgress,
                    reciteWordUIState.totalProgress
                )

                val original: MutableList<WordDetail>? = reciteWordUIState.reciteWordItemStatusOrder
                if (!original.isNullOrEmpty()) {
                    val currentReciteWord = original[0]
                    binding.statusReciteWordOK.reciteWordView.setReciteStyle(currentReciteWord)
                    if (reciteWordUIState.isShowDetail) {
                        binding.statusReciteWordOK.reciteWordView.updateLayoutParams {
                            height = ConstraintLayout.LayoutParams.WRAP_CONTENT
                        }

                        binding.statusReciteWordOK.reciteWordView.setReciteDetailStyle(currentReciteWord)

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
                    }
                }
            }

            ReciteStage.NO_VOCABULARY -> {
                binding.statusReciteWordOK.statusReciteWordOKCL.visibility = View.GONE
                binding.statusReciteWordNoVocabulary.statusReciteWordNoVocabularyCL.visibility =
                    View.VISIBLE
                binding.statusReciteWordFinish.statusReciteWordFinishCL.visibility = View.GONE
                binding.statusReciteWordNotStarted.statusReciteWordNotStartedCL.visibility =
                    View.GONE
            }

            ReciteStage.FINISH -> {
                binding.statusReciteWordOK.statusReciteWordOKCL.visibility = View.GONE
                binding.statusReciteWordNoVocabulary.statusReciteWordNoVocabularyCL.visibility =
                    View.GONE
                binding.statusReciteWordFinish.statusReciteWordFinishCL.visibility = View.VISIBLE
                binding.statusReciteWordNotStarted.statusReciteWordNotStartedCL.visibility =
                    View.GONE
            }

            ReciteStage.NOT_STARTED -> {
                binding.statusReciteWordOK.statusReciteWordOKCL.visibility = View.GONE
                binding.statusReciteWordNoVocabulary.statusReciteWordNoVocabularyCL.visibility =
                    View.GONE
                binding.statusReciteWordFinish.statusReciteWordFinishCL.visibility = View.GONE
                binding.statusReciteWordNotStarted.statusReciteWordNotStartedCL.visibility =
                    View.VISIBLE
            }
        }
    }
    private fun initInProgressBtn(){
        binding.statusReciteWordOK.reciteWordView.setShowReciteDetailListener {
            viewModel.showAll()
        }
        binding.statusReciteWordOK.reciteWordView.setGenerateAIMnemonicListener {
            viewModel.fetchWordExtract()
        }
        binding.statusReciteWordOK.reciteWordView.setCommunicateWithAIListener {
            val wordId = viewModel.uiState.value
                ?.reciteWordItemStatusOrder
                ?.firstOrNull()
                ?.word
                ?.id
            if (wordId != null) {
                AiChatBottomSheetDialogFragment.newInstance(wordId).show(
                    requireActivity().supportFragmentManager,
                    AiChatBottomSheetDialogFragment.TAG
                )
            }
        }
        binding.statusReciteWordOK.reciteWordView.setSpeechListener {
            playCurrentWord()
        }
    }
    private fun showNextCard() {
        hideAll()
        viewModel.startReciteStatistics()
        viewModel.resetShowState()
        if (viewModel.isAutoPronounceEnabled()) {
            playCurrentWord()
        }
    }
    private fun hideAll(){
        binding.statusReciteWordOK.reciteWordView.updateLayoutParams {
            height = ConstraintLayout.LayoutParams.MATCH_PARENT
        }
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

    override fun onCreateMenu(p0: Menu, p1: MenuInflater) {
        p1.inflate(R.menu.recite_word_menu, p0)
        autoPronounceMenuItem = p0.findItem(R.id.auto_pronounce).apply {
            isCheckable = true
            isChecked = viewModel.isAutoPronounceEnabled()
        }
        pronounceTypeMenuItem = p0.findItem(R.id.pronounce_type).apply {
            title = getString(R.string.pronounce_type, typeLabel(viewModel.currentPronounceType()))
        }
    }

    override fun onMenuItemSelected(p0: MenuItem): Boolean {
        when (p0.itemId) {
            R.id.auto_pronounce -> {
                val enabled = !viewModel.isAutoPronounceEnabled()
                autoPronounceMenuItem?.isChecked = enabled
                viewModel.saveEnableAutoPronounce(enabled)
                return true
            }
            R.id.pronounce_type -> {
                val pronounceType = viewModel.currentPronounceType()
                pronounceTypeMenuItem?.title = when(pronounceType){
                    PronounceType.USA -> {
                        viewModel.savePronounceType(PronounceType.UK)
                        getString(R.string.pronounce_type, typeLabel(PronounceType.UK))
                    }
                    PronounceType.UK -> {
                        viewModel.savePronounceType(PronounceType.USA)
                        getString(R.string.pronounce_type, typeLabel(PronounceType.USA))
                    }
                }
                return true
            }
        }
        return false
    }

    private fun updateMenuState(state: ReciteWordUIState) {
        autoPronounceMenuItem?.isChecked = state.autoPronounce
        pronounceTypeMenuItem?.title = getString(R.string.pronounce_type, typeLabel(state.pronounceType))
    }

    private fun typeLabel(type: PronounceType): String = when (type) {
        PronounceType.USA -> getString(R.string.pronounce_usa)
        PronounceType.UK -> getString(R.string.pronounce_uk)
    }

    private fun playCurrentWord() {
        val uri = viewModel.getMediaItemURI()
        if (uri.isBlank()) return
        val p = player ?: return
        if (p.playbackState != Player.STATE_IDLE) {
            p.stop()
        }
        p.setMediaItem(MediaItem.fromUri(uri))
        p.prepare()
        p.play()
    }
}
