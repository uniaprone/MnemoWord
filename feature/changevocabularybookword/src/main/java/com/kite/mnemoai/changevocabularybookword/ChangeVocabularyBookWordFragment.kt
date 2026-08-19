package com.kite.mnemoai.changevocabularybookword

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kite.mnemoai.changevocabularybookword.databinding.FragmentChangeVocabularyBookWordBinding
import com.kite.mnemoai.ui.WordListAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangeVocabularyBookWordFragment: Fragment() {
    private val viewModel: ChangeVocabularyBookWordViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentChangeVocabularyBookWordBinding.inflate(inflater, container, false)

        binding.optionalWordsTV.text = resources.getString(R.string.add_words)
        binding.changeWordsTV.text = resources.getString(R.string.adding_removing_words)
        val optionalWordAdapter = WordListAdapter { wordItem ->
            viewModel.addAlterWords(wordItem)
        }
        binding.optionalWordsRV.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = optionalWordAdapter
            itemAnimator = null
        }
        val alterWordsAdapter = AlterWordListAdapter { vocabularyBookChangedWord ->
            viewModel.removeAlterWords(vocabularyBookChangedWord)
        }
        binding.changeWordsRV.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = alterWordsAdapter
            itemAnimator = null
        }
        binding.wordChangeSV.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                p0?.let {
                    viewModel.setSearchText(it)
                }
                return true
            }
        })

        binding.addRemoveOptionMenu.setOnClickListener { v ->
            val popupMenu = this.context?.let { PopupMenu(it, v, Gravity.NO_GRAVITY,
                0, com.kite.mnemoai.ui.R.style.OverflowMenuStyle) }
            popupMenu?.inflate(R.menu.vocabulary_book_change_option_menu)
            popupMenu?.setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.add_words -> {
                        viewModel.setOperationType(ChangeType.ADD)
                        true
                    }
                    R.id.remove_word -> {
                        viewModel.setOperationType(ChangeType.REMOVE)
                        true
                    }
                    else -> false
                }
            }
            popupMenu?.show()
        }

        // TODO: 排序功能暂未实现，先注释掉（含布局中的 sortOptionMenu）
//        binding.sortOptionMenu.setOnClickListener { v ->
//            val popupMenu = this.context?.let { PopupMenu(it, v) }
//            popupMenu?.inflate(R.menu.sort_menu)
//            popupMenu?.setOnMenuItemClickListener { item ->
//                when(item.itemId){
//                    R.id.operationSort -> {
//                        true
//                    }
//                    R.id.alphabeticalOrder -> {
//                        true
//                    }
//                    else -> false
//                }
//            }
//        }

        binding.cancelBtn.setOnClickListener { v ->
            v.findNavController().navigateUp()
        }

        binding.confirmBtn.setOnClickListener { v ->
            viewModel.applyAlterWords()
            v.findNavController().navigateUp()
        }

        viewModel.uiStatus.observe(viewLifecycleOwner,
            Observer<ChangeVocabularyBookWordUIState> { value ->
                when (value.operationType) {
                    ChangeType.ADD -> {
                        binding.optionalWordsTV.text = resources.getString(R.string.add_words)
                        optionalWordAdapter.submitList(value.optionalWords.toList())
                        alterWordsAdapter.submitList(value.alterWords.toList())
                    }

                    ChangeType.REMOVE -> {
                        binding.optionalWordsTV.text = resources.getString(R.string.remove_words)
                        optionalWordAdapter.submitList(value.optionalWords.toList())
                        alterWordsAdapter.submitList(value.alterWords.toList())
                    }
                }
            })

        return binding.root
    }
}