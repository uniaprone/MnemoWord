package com.kite.mnemoai.fragment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kite.mnemoai.R;
import com.kite.mnemoai.adapter.VocabularySelectAdapter;
import com.kite.mnemoai.databinding.DialogFragmentVocabularySelectBinding;

import java.util.List;
import java.util.stream.Collectors;

public class VocabularySelectDialogFragment extends DialogFragment {
    private List<VocabularySelectInfo> vocabularySelectInfos;
    private IVocabularySelectListener vocabularySelectListener;

    public VocabularySelectDialogFragment(List<VocabularySelectInfo> vocabularySelectInfos, IVocabularySelectListener vocabularySelectListener){
        this.vocabularySelectInfos = vocabularySelectInfos;
        this.vocabularySelectListener = vocabularySelectListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        assert getParentFragment() != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentFragment().requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        DialogFragmentVocabularySelectBinding binding = DialogFragmentVocabularySelectBinding.inflate(inflater, null, false);
        RecyclerView vocabularyRV = binding.vocabularyRV;
        VocabularySelectAdapter vocabularySelectAdapter = new VocabularySelectAdapter(vocabularySelectInfos);
        vocabularyRV.setLayoutManager(new LinearLayoutManager(getContext()));
        vocabularyRV.setAdapter(vocabularySelectAdapter);
        builder.setView(binding.getRoot())
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        List<Long> ids = vocabularySelectInfos.stream()
                                .filter(vocabularySelectInfo -> vocabularySelectInfo.isSelect)
                                .map(VocabularySelectDialogFragment.VocabularySelectInfo::getId)
                                .collect(Collectors.toList());
                        vocabularySelectListener.onConfirm(ids);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        vocabularySelectListener.onNavigate(VocabularySelectDialogFragment.this);
                    }
                });
        return builder.create();
    }

    public List<VocabularySelectInfo> getVocabularySelectInfos() {
        return vocabularySelectInfos;
    }

    public interface IVocabularySelectListener{
        public void onConfirm(List<Long> ids);
        public void onNavigate(DialogFragment dialogFragment);
    }

    public static class VocabularySelectInfo{
        private long id;
        private String name;
        private String describe;
        private boolean isSelect;

        public VocabularySelectInfo(long id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getDescribe() {
            return describe;
        }

        public void setDescribe(String describe) {
            this.describe = describe;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public boolean isSelect() {
            return isSelect;
        }

        public void setSelect(boolean select) {
            isSelect = select;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
