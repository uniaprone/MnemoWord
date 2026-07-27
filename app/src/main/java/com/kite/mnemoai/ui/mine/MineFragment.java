package com.kite.mnemoai.ui.mine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.kite.mnemoai.R;
import com.kite.mnemoai.data.repository.IRepositoryCallback;
import com.kite.mnemoai.databinding.FragmentMineBinding;
import com.kite.mnemoai.ui.dialog.apikeysetting.ApiKeySettingDialogFragment;
import com.kite.mnemoai.stateholder.BannerControl;
import com.kite.mnemoai.ui.main.MainViewModel;
import com.kite.mnemoai.ui.model.LoadingState;

public class MineFragment extends Fragment {
    private FragmentMineBinding binding;
    private MineViewModel viewModel;
    private BannerControl bannerControl;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        MainViewModel mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        mainViewModel.settitle(getResources().getString(R.string.mine));
        mainViewModel.setShowNavIcon(false);

        binding = FragmentMineBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(MineViewModel.initializer)).get(MineViewModel.class);
        bannerControl = new BannerControl(binding.infoFL, getLifecycle());

        RecyclerView recyclerView = binding.settingRV;
        MineAdapter adapter = new MineAdapter();
        adapter.setApiKeyItemListener(new MineAdapter.ApiKeyItemListener() {
            @Override
            public void onClick() {
                ApiKeySettingDialogFragment apiKeyDialogFragment = new ApiKeySettingDialogFragment();
                apiKeyDialogFragment.show(getChildFragmentManager(), "api_key");
            }
        });

        getChildFragmentManager().setFragmentResultListener(ApiKeySettingDialogFragment.API_KEY_SETTING, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                String apiKey = result.getString("api_key");
                bannerControl.show();
                binding.settingInfoTV.setText("正在测试API Key是否可用");
                binding.infoFL.setBackgroundColor(MaterialColors.getColor(binding.settingInfoTV, com.google.android.material.R.attr.colorPrimaryContainer));
                binding.settingInfoTV.setTextColor(MaterialColors.getColor(binding.settingInfoTV, com.google.android.material.R.attr.colorOnPrimaryContainer));
                viewModel.apiKeyTest(apiKey);
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        viewModel.getUIStatus().observe(getViewLifecycleOwner(), new Observer<MineUIState>() {
            @Override
            public void onChanged(MineUIState mineUIState) {
                if(mineUIState == null) return;
                adapter.setMineBaseItems(mineUIState.getMineBaseItems());
                if(mineUIState.getApiTestState() != null){
                    if(mineUIState.getApiTestState() instanceof LoadingState.Success){
                        binding.settingInfoTV.setText(((LoadingState.Success<String>) mineUIState.getApiTestState()).getData());
                        binding.infoFL.setBackgroundColor(MaterialColors.getColor(binding.settingInfoTV, com.google.android.material.R.attr.colorPrimaryContainer));
                        binding.settingInfoTV.setTextColor(MaterialColors.getColor(binding.settingInfoTV, com.google.android.material.R.attr.colorOnPrimaryContainer));
                        bannerControl.startTimer(3000);
                    } else if (mineUIState.getApiTestState() instanceof LoadingState.Error) {
                        binding.settingInfoTV.setText("测试失败，请稍后再试");
                        binding.infoFL.setBackgroundColor(MaterialColors.getColor(binding.settingInfoTV, com.google.android.material.R.attr.colorErrorContainer));
                        binding.settingInfoTV.setTextColor(MaterialColors.getColor(binding.settingInfoTV, com.google.android.material.R.attr.colorOnErrorContainer));
                        bannerControl.startTimer(3000);
                    }
                }
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
