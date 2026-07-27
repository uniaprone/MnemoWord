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
import com.kite.mnemoai.MainActivity;
import com.kite.mnemoai.R;
import com.kite.mnemoai.adapter.MineAdapter;
import com.kite.mnemoai.data.repository.IRepositoryCallback;
import com.kite.mnemoai.databinding.FragmentMineBinding;
import com.kite.mnemoai.ui.dialog.apikeysetting.ApiKeySettingDialogFragment;
import com.kite.mnemoai.stateholder.BannerControl;

import org.jetbrains.annotations.NotNull;

public class MineFragment extends Fragment {
    private FragmentMineBinding binding;
    private MineViewModel viewModel;
    private BannerControl bannerControl;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
                viewModel.apiKeyTest(apiKey, new IRepositoryCallback<Boolean>() {
                    @Override
                    public void onComplete(Boolean aBoolean) {
                        if(aBoolean){
                            binding.settingInfoTV.setText("测试成功，API Key可用");
                            binding.infoFL.setBackgroundColor(MaterialColors.getColor(binding.settingInfoTV, com.google.android.material.R.attr.colorPrimaryContainer));
                            binding.settingInfoTV.setTextColor(MaterialColors.getColor(binding.settingInfoTV, com.google.android.material.R.attr.colorOnPrimaryContainer));
                            bannerControl.startTimer(3000);
                            viewModel.saveApiKey(apiKey);
                        }else{
                            binding.settingInfoTV.setText("测试成功，API Key不可用");
                            binding.infoFL.setBackgroundColor(MaterialColors.getColor(binding.settingInfoTV, com.google.android.material.R.attr.colorErrorContainer));
                            binding.settingInfoTV.setTextColor(MaterialColors.getColor(binding.settingInfoTV, com.google.android.material.R.attr.colorOnErrorContainer));
                            bannerControl.startTimer(3000);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        binding.settingInfoTV.setText("测试失败，请稍后再试");
                        binding.infoFL.setBackgroundColor(MaterialColors.getColor(binding.settingInfoTV, com.google.android.material.R.attr.colorErrorContainer));
                        binding.settingInfoTV.setTextColor(MaterialColors.getColor(binding.settingInfoTV, com.google.android.material.R.attr.colorOnErrorContainer));
                        bannerControl.startTimer(3000);
                    }
                });
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        viewModel.getUIStatus().observe(getViewLifecycleOwner(), new Observer<MineUIState>() {
            @Override
            public void onChanged(MineUIState mineUIState) {
                if(mineUIState == null) return;
                adapter.setMineBaseItems(mineUIState.getMineBaseItems());
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewInit();
    }

    @Override
    public void onDestroy() {
        binding = null;
        super.onDestroy();
    }

    private void viewInit(){
        setupToolbar();
    }

    private void setupToolbar(){
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.setTitleText(R.string.mine);
    }
}
