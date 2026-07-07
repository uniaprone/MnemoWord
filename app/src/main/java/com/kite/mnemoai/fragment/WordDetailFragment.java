package com.kite.mnemoai.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.kite.mnemoai.customview.WordDetailCardContentView;
import com.kite.mnemoai.databinding.FragmentWordDetailBinding;
import com.kite.mnemoai.uistate.ReciteWordUIState;
import com.kite.mnemoai.viewmodels.WordDetailViewModel;

public class WordDetailFragment extends Fragment {
    private FragmentWordDetailBinding binding;
    private WordDetailViewModel viewModel;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGrand -> {
            if(isGrand){
                viewModel.fetchWordExtract();
            }else{
                showPermissionDeniedMessage();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWordDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.wordDetailCard.wordCardView.setOnAiMnemoaiClickListener(new WordDetailCardContentView.IOnAiMnemoaiClickListener() {
            @Override
            public void onClick() {
                checkAndRequestPermission();
            }
        });
        binding.wordDetailCard.wordCardView.bindClickListener();
        viewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(WordDetailViewModel.initializer)).get(WordDetailViewModel.class);
        viewModel.getUiState().observe(getViewLifecycleOwner(), wordDetailUIState -> {
            if(wordDetailUIState == null) return;
            ReciteWordUIState.ReciteWordItemStatus reciteWordItemStatus =
                    new ReciteWordUIState.ReciteWordItemStatus(wordDetailUIState.getWordWithExtractAndDayPlan(), wordDetailUIState.getWordDetailStatus(), true);
            binding.wordDetailCard.wordCardView.bind(reciteWordItemStatus);
        });
    }

    private void checkAndRequestPermission(){
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED){
            viewModel.fetchWordExtract();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.INTERNET)) {
            showRationaleDialog();
        }else{
            requestPermissionLauncher.launch(Manifest.permission.INTERNET);
        }
    }


    public void showPermissionDeniedMessage(){
        Toast.makeText(getContext(), "无法获取ai助记信息", Toast.LENGTH_SHORT).show();
    }

    private void showRationaleDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("需要相机权限")
                .setMessage("我们需要相机权限来扫描二维码")
                .setPositiveButton("允许", (dialog, which) -> requestPermissionLauncher.launch(Manifest.permission.CAMERA))
                .setNegativeButton("取消", null)
                .show();
    }
}
