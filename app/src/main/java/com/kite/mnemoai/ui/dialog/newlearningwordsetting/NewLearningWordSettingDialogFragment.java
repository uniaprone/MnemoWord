package com.kite.mnemoai.ui.dialog.newlearningwordsetting;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kite.mnemoai.databinding.DialogFragmentNewLearningWordSettingBinding;

public class NewLearningWordSettingDialogFragment extends DialogFragment {
    private IConfirmListener listener;

    public NewLearningWordSettingDialogFragment(IConfirmListener listener){
        this.listener = listener;
    }
    public interface IConfirmListener{
        void onConfirm(int count);
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentFragment().getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        DialogFragmentNewLearningWordSettingBinding binding = DialogFragmentNewLearningWordSettingBinding.inflate(inflater, null, false);
        binding.confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String countString = binding.newLearningWordCountET.getText().toString();
                if(countString.isEmpty()){
                    return;
                }
                int count = Integer.parseInt(binding.newLearningWordCountET.getText().toString());
                listener.onConfirm(count);
                dismiss();
            }
        });
        builder.setView(binding.getRoot());
        return builder.create();
    }
}
