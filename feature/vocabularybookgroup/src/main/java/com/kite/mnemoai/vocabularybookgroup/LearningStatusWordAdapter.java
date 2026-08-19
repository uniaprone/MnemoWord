package com.kite.mnemoai.vocabularybookgroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class LearningStatusWordAdapter extends FragmentStateAdapter {
    public LearningStatusWordAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return VocabularyWordsFragment.Companion.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
