package com.kite.mnemoai.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.kite.mnemoai.model.word.WordItem
import com.kite.mnemoai.search.databinding.FragmentSearchBinding
import com.kite.mnemoai.ui.WordListAdapter
import com.kite.mnemoai.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: WordListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel.settitle(resources.getString(com.kite.mnemoai.search.R.string.search))
        mainViewModel.setShowNavIcon(true)

        binding = FragmentSearchBinding.inflate(inflater, container, false)

        adapter = WordListAdapter { word ->
            val args = Bundle()
            args.putLong("word_id", word.id)
            findNavController().navigate(
                com.kite.mnemoai.search.R.id.action_searchFragment_to_wordDetailFragment, args
            )
        }
        binding.searchResultRV.layoutManager = LinearLayoutManager(context)
        binding.searchResultRV.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.setWords(state.words)
                }
            }
        }

        binding.searchSV.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    viewModel.searchWord(newText)
                } else {
                    adapter.setWords(emptyList())
                }
                return true
            }
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.searchSV.isIconified = false

        binding.searchSV.post {
            val editText = binding.searchSV.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            editText?.let {
                it.requestFocus()
                it.isFocusable = true
                it.isFocusableInTouchMode = true
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(it, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }
}
