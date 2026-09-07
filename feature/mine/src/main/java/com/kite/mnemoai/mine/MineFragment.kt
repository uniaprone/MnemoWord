package com.kite.mnemoai.mine

import android.Manifest
import android.animation.LayoutTransition
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.textview.MaterialTextView
import com.kite.mnemoai.mine.databinding.FragmentMineBinding
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.data.AiProvider
import com.kite.mnemoai.model.data.ThemeType
import com.kite.mnemoai.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.math.ceil

@AndroidEntryPoint
class MineFragment : Fragment(){
    fun String.maskApiKeyFixed(prefixVisible: Int = 4, suffixVisible: Int = 4): String {
        return if (length <= prefixVisible + suffixVisible) {
            "*".repeat(length)
        } else {
            take(prefixVisible) + "****" + takeLast(suffixVisible)
        }
    }
    private var _binding: FragmentMineBinding? = null
    private val binding get() = _binding!!
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: MineViewModel by viewModels()
    private var pendingReminderIndex = 0
    private var loadedModelIds: List<String>? = null
    private var suppressSubModelSelection = false
    private var isFirstUiStateApply = false
    private var launcher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
        if (pendingReminderIndex == 1) {
            if (isGranted) {
                viewModel.setEnableReminder(requireContext(), true)
            } else {
                binding.enableStudyReminder.setSelectedIndex(0)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(enabled = true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel.settitle(resources.getString(R.string.mine))
        mainViewModel.setShowNavIcon(false)

        _binding = FragmentMineBinding.inflate(inflater, container, false)

        binding.mineRoot.layoutTransition = LayoutTransition()
        isFirstUiStateApply = true

        binding.apikeySetting.setOnClickListener {
            val apiKeyDialogFragment = ApiKeySettingDialogFragment()
            apiKeyDialogFragment.show(getChildFragmentManager(), "api_key")
        }
        getChildFragmentManager().setFragmentResultListener(
            ApiKeySettingDialogFragment.API_KEY_SETTING,
            this
        ) { _, result -> viewModel.apiKeyTest(result.getString("api_key", "")) }

        val aiProviders = AiProvider.entries.toList()

        binding.aiModelSp.adapter = object : ArrayAdapter<AiProvider>(
            requireContext(),
            com.kite.mnemoai.ui.R.layout.item_mai_spinner,
            com.kite.mnemoai.ui.R.id.text1,
            aiProviders
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<MaterialTextView>(com.kite.mnemoai.ui.R.id.text1)
                textView.text = getItem(position)?.name?.lowercase()
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<MaterialTextView>(com.kite.mnemoai.ui.R.id.text1)
                textView.text = getItem(position)?.name?.lowercase()
                return view
            }
        }
        binding.aiModelSp.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                p0: AdapterView<*>?,
                p1: View?,
                p2: Int,
                p3: Long
            ) {
                val selected = aiProviders[p2]
                when(selected){
                    AiProvider.DEEPSEEK -> {
                        viewModel.saveAiProvider(selected)
                        loadedModelIds = null
                        prefillSubModel(viewModel.currentModelType())
                        fitSpinnerToSelection(binding.aiModelSp, selected.name.lowercase())
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        binding.segmentedControl.setOnSelectionChangedListener { index ->
            when (index) {
                0 -> {
                    viewModel.saveLightDarkModel(ThemeType.FOLLOW_SYS) {
                        AppCompatDelegate.setDefaultNightMode(
                            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        )
                    }
                }
                1 -> {
                    viewModel.saveLightDarkModel(ThemeType.DAY) {
                        AppCompatDelegate.setDefaultNightMode(
                            AppCompatDelegate.MODE_NIGHT_NO
                        )
                    }
                }
                else -> {
                    viewModel.saveLightDarkModel(ThemeType.NIGHT) {
                        AppCompatDelegate.setDefaultNightMode(
                            AppCompatDelegate.MODE_NIGHT_YES
                        )
                    }
                }
            }
        }
        binding.subModelSp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                p0: AdapterView<*>?,
                p1: View?,
                p2: Int,
                p3: Long
            ) {
                if (suppressSubModelSelection) return
                val models = loadedModelIds ?: return
                if (p2 == 0) return
                val model = models.getOrNull(p2 - 1) ?: return
                viewModel.saveSubModel(model)
                fitSpinnerToSelection(binding.subModelSp, model)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
        binding.subModelSp.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP && loadedModelIds == null) {
                loadModelsAndOpen()
                true
            } else {
                false
            }
        }

        binding.thinkingMode.setOnSelectionChangedListener { index ->
            when(index){
                0 -> viewModel.saveThinking(false)
                1 -> viewModel.saveThinking(true)
            }
        }
        binding.generalSettingContainerLL.layoutTransition = LayoutTransition()

        binding.promptSetting.setOnClickListener {
            findNavController().navigate(R.id.action_mineFragment_to_promptEditFragment)
        }

        binding.enableStudyReminder.setOnSelectionChangedListener { index ->
            when (index) {
                0 -> viewModel.setEnableReminder(requireContext(), false)
                1 -> {
                    if (hasPostNotificationPermission()) {
                        viewModel.setEnableReminder(requireContext(), true)
                    } else {
                        pendingReminderIndex = index
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }
            }
        }

        binding.reminderTimeSetting.setOnClickListener {
            ReminderTimerDialogFragment.newInstance(viewModel.uIStatus.value?.reminderTime)
                .show(childFragmentManager, "reminder_time_setting")
        }

        childFragmentManager.setFragmentResultListener(
            ReminderTimerDialogFragment.REQUEST_KEY
            ,this
        ){_, bundle ->
            val time = bundle.getString("time")
            viewModel.setReminderTime(requireContext(), LocalTime.parse(time))
        }

        binding.autoGenerateDailyWordExtract.setOnSelectionChangedListener { index ->
            when(index){
                0 -> viewModel.saveEnableAutoGenerateDailyWordExtract(false)
                1 -> viewModel.saveEnableAutoGenerateDailyWordExtract(true)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.apiTestResult.collect { result ->
                    when (result) {
                        is Result.Loading ->
                            binding.bannerView.setLoading(getString(R.string.testing_api_key_available))
                        is Result.Success<*> ->
                            binding.bannerView.setSuccess(result.data as? String)
                        is Result.Error ->
                            binding.bannerView.setFailure(result.exception.message)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.uIStatus.collect { uIState ->
                    if(uIState != null){
                        if (isFirstUiStateApply) {
                            binding.mineRoot.layoutTransition = null
                            binding.generalSettingContainerLL.layoutTransition = null
                        }
                        binding.segmentedControl.setDefaultSelection(
                            uIState.dayNightMode.ordinal
                        )

                        if(uIState.aiProvider != null){
                            when(uIState.aiProvider){
                                AiProvider.DEEPSEEK -> {
                                    binding.aiModelSp.setSelection(uIState.aiProvider.ordinal)
                                    fitSpinnerToSelection(
                                        binding.aiModelSp,
                                        uIState.aiProvider?.name?.lowercase()
                                    )
                                    if(uIState.apikey.isNullOrEmpty()){
                                        binding.apikeyInfoTV.text = getString(R.string.no_set)
                                    }else{
                                        binding.apikeyInfoTV.text = uIState.apikey.maskApiKeyFixed()
                                    }

                                    if(uIState.enableThinking){
                                        binding.thinkingMode.setDefaultSelection(1)
                                    }else{
                                        binding.thinkingMode.setDefaultSelection(0)
                                    }

                                    prefillSubModel(uIState.deepseekModelType)
                                }
                            }
                        }

                        if(uIState.enableReminder) {
                            binding.reminderTimeSetting.visibility = View.VISIBLE
                            binding.enableStudyReminder.setDefaultSelection(1)
                            binding.reminderTimeTV.text = uIState.reminderTime.toString()
                        }else{
                            binding.reminderTimeSetting.visibility = View.GONE
                            binding.enableStudyReminder.setDefaultSelection(0)
                        }

                        if(uIState.enableAutoGenerateDailyWordExtract){
                            binding.autoGenerateDailyWordExtract.setDefaultSelection(1)
                        }else{
                            binding.autoGenerateDailyWordExtract.setDefaultSelection(0)
                        }

                        if (isFirstUiStateApply) {
                            isFirstUiStateApply = false
                            binding.mineRoot.layoutTransition = LayoutTransition()
                            binding.generalSettingContainerLL.layoutTransition = LayoutTransition()
                        }
                    }
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun prefillSubModel(saved: String?) {
        if (loadedModelIds != null) return
        suppressSubModelSelection = true
        val display = saved?.takeIf { it.isNotBlank() } ?: getString(R.string.select_model)
        binding.subModelSp.adapter = ArrayAdapter(
            requireContext(),
            com.kite.mnemoai.ui.R.layout.item_mai_spinner,
            com.kite.mnemoai.ui.R.id.text1,
            listOf(display)
        )
        binding.subModelSp.setSelection(0)
        suppressSubModelSelection = false
        fitSpinnerToSelection(binding.subModelSp, display)
    }

    private fun loadModelsAndOpen() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = viewModel.loadModels()) {
                is Result.Success -> {
                    val models = result.data
                    loadedModelIds = models
                    showModelsInSpinner(models, viewModel.currentModelType())
                    binding.subModelSp.performClick()
                }
                is Result.Error -> Toast.makeText(
                    requireContext(),
                    result.exception.message ?: "模型列表获取失败",
                    Toast.LENGTH_SHORT
                ).show()
                Result.Loading -> {}
            }
        }
    }

    private fun showModelsInSpinner(models: List<String>, saved: String?) {
        suppressSubModelSelection = true
        val items = buildList {
            add(getString(R.string.select_model))
            addAll(models)
        }
        binding.subModelSp.adapter = ArrayAdapter(
            requireContext(),
            com.kite.mnemoai.ui.R.layout.item_mai_spinner,
            com.kite.mnemoai.ui.R.id.text1,
            items
        )
        val savedIndex = saved?.let { models.indexOf(it) } ?: -1
        binding.subModelSp.setSelection(if (savedIndex >= 0) savedIndex + 1 else 0)
        suppressSubModelSelection = false
        fitSpinnerToSelection(
            binding.subModelSp,
            if (savedIndex >= 0) models[savedIndex] else getString(R.string.select_model)
        )
    }

    private fun fitSpinnerToSelection(spinner: Spinner, text: String?) {
        val display = text?.takeIf { it.isNotBlank() } ?: return
        spinner.post {
            val measureTV = MaterialTextView(spinner.context).apply {
                setText(display)
                textSize = 14f
            }
            val textWidth = ceil(measureTV.paint.measureText(display)).toInt()
            val targetWidth = textWidth + spinner.paddingLeft + spinner.paddingRight + 48
            if (spinner.layoutParams?.width != targetWidth) {
                spinner.layoutParams = spinner.layoutParams?.apply { width = targetWidth }
            }
        }
    }

    private fun hasPostNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
