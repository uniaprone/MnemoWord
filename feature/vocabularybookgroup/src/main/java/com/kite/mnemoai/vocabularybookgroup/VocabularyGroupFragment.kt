package com.kite.mnemoai.vocabularybookgroup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kite.mnemoai.shared_ui.SettingAndAddNewVocabularyBookDialogFragment.Companion.newInstance
import com.kite.mnemoai.ui.main.MainViewModel
import com.kite.mnemoai.vocabularybookgroup.databinding.FragmentVocabularyGroupBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VocabularyGroupFragment : Fragment(), MenuProvider {
    private var binding: FragmentVocabularyGroupBinding? = null
    private var viewModel: VocabularyGroupViewModel? = null
    private var mainViewModel: MainViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        mainViewModel!!.setShowNavIcon(true)

        binding = FragmentVocabularyGroupBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[VocabularyGroupViewModel::class.java]

        return binding!!.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED)
        getChildFragmentManager().setFragmentResultListener(
            "confirm",
            this
        ) { _, result ->
            val name = result.getString("name")
            val desc = result.getString("desc")
            viewModel!!.modifyVocabularyBook(name!!, desc!!)
        }

        val viewPager2 = binding!!.learningWordVP
        val adapter = LearningStatusWordAdapter(this)
        viewPager2.setAdapter(adapter)
        val tabLayout = binding!!.learningStatusTL

        // attach 只执行一次：结构（页数 + 联动）在这里定死
        TabLayoutMediator(
            tabLayout,
            viewPager2
        ) { tab: TabLayout.Tab?, position: Int ->
            when (position) {
                0 -> tab!!.text = getString(R.string.all_word, 0)
                1 -> tab!!.text = getString(R.string.learning_count, 0)
                2 -> tab!!.text = getString(R.string.reviewing_count, 0)
                3 -> tab!!.text = getString(R.string.mastered_count, 0)
            }
        }.attach()

        viewModel!!.uiState.observe(
            getViewLifecycleOwner(),
            object : Observer<VocabularyGroupUIState?> {
                override fun onChanged(state: VocabularyGroupUIState?) {
                    if (state == null) return
                    if (state.words != null) {
                        updateTabCounts(state) // 数据变化只改文本，不再重建 Tab
                    }
                    if (state.group != null) {
                        mainViewModel!!.settitle(state.group.name)
                    }
                }
            })
    }

    private fun updateTabCounts(state: VocabularyGroupUIState) {
        val tabLayout = binding!!.learningStatusTL
        tabLayout.getTabAt(0)!!.text = getString(R.string.all_word, state.allCount)
        tabLayout.getTabAt(1)!!.text = getString(R.string.learning_count, state.learningCount)
        tabLayout.getTabAt(2)!!.text = getString(R.string.reviewing_count, state.reviewingCount)
        tabLayout.getTabAt(3)!!.text = getString(R.string.mastered_count, state.masteredCount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.vocabulary_book_setting_menu, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)

        val menuItem = menu.findItem(R.id.setLearning)
        var isLearning = 0
        if (viewModel!!.getGroup() != null) {
            isLearning = viewModel!!.getGroup()!!.isLearning
        }
        if (menuItem != null) {
            if (isLearning == 0) {
                menuItem.setTitle(R.string.join_learning)
            } else if (isLearning == 1) {
                menuItem.setTitle(R.string.cancel_learning)
            } else {
                menuItem.setTitle(R.string.join_learning)
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        val id = menuItem.getItemId()
        if (id == R.id.modifyGroup) {
            val dialogFragment =
                newInstance(
                    0.toByte(),
                    viewModel!!.getGroup()!!.name,
                    viewModel!!.getGroup()!!.description
                )
            dialogFragment.show(getChildFragmentManager(), "MODIFYVELOCABULARYBOOK")
        } else if (id == R.id.setLearning) {
            val learningStatus = viewModel!!.getGroup()!!.isLearning
            if (learningStatus == 0) {
                viewModel!!.setVocabularyBookLearningStatus(1)
            } else if (learningStatus == 1) {
                viewModel!!.setVocabularyBookLearningStatus(0)
            } else {
                viewModel!!.setVocabularyBookLearningStatus(0)
            }
        } else if (id == R.id.alterWords) {
            val bundle = Bundle()
            bundle.putLong("group_id", viewModel!!.getGroup()!!.id)
            findNavController(binding!!.getRoot()).navigate(
                R.id.action_vocabularyGroupFragment_to_changeVocabularyBookWordFragment,
                bundle
            )
        } else if (id == R.id.deleteVocabularyBook) {
            viewModel!!.deleteGroup()
            val navController = findNavController(requireView())
            navController.navigateUp()
        }
        return true
    }
}