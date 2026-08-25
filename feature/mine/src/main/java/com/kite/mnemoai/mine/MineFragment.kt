package com.kite.mnemoai.mine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.kite.mnemoai.mine.databinding.FragmentMineBinding
import com.kite.mnemoai.ui.BannerControl
import com.kite.mnemoai.ui.main.MainViewModel
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.data.ThemeType
import com.kite.mnemoai.ui.dpToPx
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
    private var bannerControl: BannerControl? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel.settitle(resources.getString(R.string.mine))
        mainViewModel.setShowNavIcon(false)

        _binding = FragmentMineBinding.inflate(inflater, container, false)
        bannerControl = BannerControl(binding.infoFL, lifecycle)

        binding.apikeySetting.setOnClickListener {
            val apiKeyDialogFragment = ApiKeySettingDialogFragment()
            apiKeyDialogFragment.show(getChildFragmentManager(), "api_key")
        }
        getChildFragmentManager().setFragmentResultListener(
            ApiKeySettingDialogFragment.API_KEY_SETTING,
            this
        ) { _, result -> viewModel.apiKeyTest(result.getString("api_key", "")) }

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

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.apiTestResult.collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            bannerControl?.show()
                            binding.settingInfoTV.text = "正在测试API Key是否可用"
                            binding.infoFL.setBackgroundColor(
                                MaterialColors.getColor(
                                    binding.settingInfoTV,
                                    com.google.android.material.R.attr.colorPrimaryContainer
                                )
                            )
                            binding.settingInfoTV.setTextColor(
                                MaterialColors.getColor(
                                    binding.settingInfoTV,
                                    com.google.android.material.R.attr.colorOnPrimaryContainer
                                )
                            )
                        }

                        is Result.Success<*> -> {
                            binding.settingInfoTV.text = result.data as? String
                            binding.infoFL.setBackgroundColor(
                                MaterialColors.getColor(
                                    binding.settingInfoTV,
                                    com.google.android.material.R.attr.colorPrimaryContainer
                                )
                            )
                            binding.settingInfoTV.setTextColor(
                                MaterialColors.getColor(
                                    binding.settingInfoTV,
                                    com.google.android.material.R.attr.colorOnPrimaryContainer
                                )
                            )
                            bannerControl?.startTimer(3000)
                        }

                        is Result.Error -> {
                            binding.settingInfoTV.text = result.exception.message
                            binding.infoFL.setBackgroundColor(
                                MaterialColors.getColor(
                                    binding.settingInfoTV,
                                    com.google.android.material.R.attr.colorErrorContainer
                                )
                            )
                            binding.settingInfoTV.setTextColor(
                                MaterialColors.getColor(
                                    binding.settingInfoTV,
                                    com.google.android.material.R.attr.colorOnErrorContainer
                                )
                            )
                            bannerControl?.startTimer(3000)
                        }
                    }
                }
            }
        }

        viewModel.uIStatus.observe(getViewLifecycleOwner(), Observer { mineUIState ->
            if (mineUIState == null) return@Observer
            binding.segmentedControl.setDefaultSelection(
                mineUIState.dayNightMode.ordinal
            )
            binding.apikeyInfoTV.text = mineUIState.apikey?.maskApiKeyFixed()
        })
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
