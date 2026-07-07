package com.kite.mnemoai.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.ViewModelInitializer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kite.mnemoai.MainActivity;
import com.kite.mnemoai.R;
import com.kite.mnemoai.adapter.MineAdapter;
import com.kite.mnemoai.databinding.FragmentMineBinding;
import com.kite.mnemoai.uistate.MineUIState;
import com.kite.mnemoai.viewmodels.MineViewModel;

public class MineFragment extends Fragment {
    private FragmentMineBinding binding;
    private MineViewModel viewModel;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewInit();
        binding = FragmentMineBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(MineViewModel.initializer)).get(MineViewModel.class);
        RecyclerView recyclerView = binding.settingRV;
        MineAdapter adapter = new MineAdapter();
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

    private void viewInit(){
        setupToolbar();
    }

    private void setupToolbar(){
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.setTitleText(R.string.mine);
        mainActivity.hideBackIV();
    }
}
