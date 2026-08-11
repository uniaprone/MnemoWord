package com.kite.mnemoai.ui.mine

import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.color.MaterialColors
import com.kite.mnemoai.R
import com.kite.mnemoai.databinding.FragmentMineBinding
import com.kite.mnemoai.stateholder.BannerControl
import com.kite.mnemoai.ui.dialog.apikeysetting.ApiKeySettingDialogFragment
import com.kite.mnemoai.ui.main.MainViewModel
import com.kite.mnemoai.ui.model.LoadingState
import com.kite.mnemoai.ui.model.LoadingState.Loading
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MineFragment : Fragment() {
    fun String.maskApiKeyFixed(prefixVisible: Int = 4, suffixVisible: Int = 4): String {
        return if (length <= prefixVisible + suffixVisible) {
            // 太短则全部打码
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
        ) { requestKey, result -> viewModel.apiKeyTest(result.getString("api_key", "")) }
        binding.segmentedControl.setOnSelectionChangedListener { index ->
            if (index == 0) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                viewModel.saveLightDarkModel(-1)
            } else if (index == 1) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                viewModel.saveLightDarkModel(1)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                viewModel.saveLightDarkModel(2)
            }
        }
        viewModel.uIStatus.observe(getViewLifecycleOwner(), object : Observer<MineUIState?> {
            override fun onChanged(mineUIState: MineUIState?) {
                if (mineUIState == null) return
                binding.segmentedControl.setDefaultSelection(viewModel.convertLightDarkModelToOption(mineUIState.dayNightMode))
                binding.apikeyInfoTV.text = mineUIState.apikey?.maskApiKeyFixed()
                if (mineUIState.apiTestState != null) {
                    if (mineUIState.apiTestState is Loading) {
                        bannerControl!!.show()
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
                    } else if (mineUIState.apiTestState is LoadingState.Success<*>) {
                        binding.settingInfoTV.text = (mineUIState.apiTestState as LoadingState.Success<String?>).data
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
                        bannerControl!!.startTimer(3000)
                    } else if (mineUIState.apiTestState is LoadingState.Error) {
                        binding.settingInfoTV.text = (mineUIState.apiTestState as LoadingState.Error).exception.message
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
                        bannerControl!!.startTimer(3000)
                    }
                }
            }
        })
        return binding.getRoot()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
