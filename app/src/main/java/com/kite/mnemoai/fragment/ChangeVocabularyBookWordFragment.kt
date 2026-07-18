package com.kite.mnemoai.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.kite.mnemoai.R
import com.kite.mnemoai.adapter.AlterWordListAdapter
import com.kite.mnemoai.adapter.WordListAdapter
import com.kite.mnemoai.databinding.FragmentChangeVocabularyBookWordBinding
import com.kite.mnemoai.uistate.ChangeVocabularyBookWordUIState
import com.kite.mnemoai.viewmodels.ChangeVocabularyBookWordViewModel
import java.io.Serializable
import androidx.navigation.findNavController

class ChangeVocabularyBookWordFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentChangeVocabularyBookWordBinding.inflate(inflater, container, false)
        val viewModel = ViewModelProvider(this, ViewModelProvider.Factory.from(
            ChangeVocabularyBookWordViewModel.initializer))[ChangeVocabularyBookWordViewModel::class.java]

        binding.optionalWordsTV.text = resources.getString(R.string.add_words)
        binding.changeWordsTV.text = resources.getString(R.string.adding_removing_words)
        val optionalWordAdapter = WordListAdapter{ wordListItem ->
            viewModel.addAlterWords(wordListItem)
        }
        binding.optionalWordsRV.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = optionalWordAdapter
        }
        val alterWordsAdapter = AlterWordListAdapter{ vocabularyBookChangedWord ->
            viewModel.removeAlterWords(vocabularyBookChangedWord)
        }
        binding.changeWordsRV.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = alterWordsAdapter
        }
        binding.wordChangeSV.setOnQueryTextListener(object:SearchView.OnQueryTextListener{
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
            val popupMenu = this.context?.let { PopupMenu(it, v) }
            popupMenu?.inflate(R.menu.vocabulary_book_change_option_menu)
            popupMenu?.setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.add_words -> {
                        viewModel.setOperationType(ChangeVocabularyBookWordUIState.ChangeType.ADD)
                        true
                    }
                    R.id.remove_word -> {
                        viewModel.setOperationType(ChangeVocabularyBookWordUIState.ChangeType.REMOVE)
                        true
                    }
                    else -> false
                }
            }
            popupMenu?.show()
        }

        binding.sortOptionMenu.setOnClickListener { v ->
            val popupMenu = this.context?.let { PopupMenu(it, v) }
            popupMenu?.inflate(R.menu.sort_menu)
            popupMenu?.setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.operationSort -> {
                        true
                    }
                    R.id.alphabeticalOrder -> {
                        true
                    }
                    else -> false
                }
            }
        }

        binding.cancelBtn.setOnClickListener { v ->
            v.findNavController().navigateUp()
        }

        binding.confirmBtn.setOnClickListener { v ->
            viewModel.applyAlterWords()
            v.findNavController().navigateUp()
        }

        viewModel.uiStatus.observe(viewLifecycleOwner,
            Observer<ChangeVocabularyBookWordUIState> { value ->
                when(value.operationType){
                    ChangeVocabularyBookWordUIState.ChangeType.ADD -> {
                        binding.optionalWordsTV.text = resources.getString(R.string.add_words)
                        optionalWordAdapter.setWords(value.optionalWords)
                        alterWordsAdapter.submitList(value.alterWords.toList())
                    }

                    ChangeVocabularyBookWordUIState.ChangeType.REMOVE -> {
                        binding.optionalWordsTV.text = resources.getString(R.string.remove_words)
                        optionalWordAdapter.setWords(value.optionalWords)
                        alterWordsAdapter.submitList(value.alterWords.toList())
                    }
                }
            })

        return binding.root
    }
}