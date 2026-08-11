package com.kite.mnemoai.ui.search

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.kite.mnemoai.R
import com.kite.mnemoai.data.model.WordListItem
import com.kite.mnemoai.data.repository.IRepositoryCallback
import com.kite.mnemoai.databinding.FragmentSearchBinding
import com.kite.mnemoai.ui.adapter.WordListAdapter
import com.kite.mnemoai.ui.main.MainViewModel
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment: Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var binding: FragmentSearchBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel.settitle(resources.getString(R.string.search))
        mainViewModel.setShowNavIcon(true)

        binding = FragmentSearchBinding.inflate(layoutInflater, container, false)

        val adapter = WordListAdapter { word ->
            val args = Bundle()
            args.putLong("word_id", word.id)
            binding.searchResultRV.findNavController().navigate(
                R.id.action_searchFragment_to_wordDetailFragment, args
            )
        }
        binding.searchResultRV.layoutManager = LinearLayoutManager(context)
        binding.searchResultRV.adapter = adapter

        binding.searchSV.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if(!p0.isNullOrEmpty()){
                    viewModel.performSearch(p0, object: IRepositoryCallback<List<WordListItem>> {
                        override fun onComplete(t: List<WordListItem>?) {
                            adapter.setWords(t)
                        }

                        override fun onError(t: Throwable?) {

                        }

                    })
                } else{
                    adapter.setWords(listOf<WordListItem>())
                }
                return true
            }
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // 确保 SearchView 处于展开状态（非图标化）
        binding.searchSV.isIconified = false

        binding.searchSV.post {
            // 获取 SearchView 内部的 EditText (兼容 AndroidX)
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