package com.kite.mnemoai.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kite.mnemoai.R
import com.kite.mnemoai.data.model.WordListItem
import com.kite.mnemoai.data.repository.IRepositoryCallback
import com.kite.mnemoai.databinding.FragmentSearchBinding
import com.kite.mnemoai.ui.WordListAdapter
import com.kite.mnemoai.ui.main.MainViewModel
import androidx.navigation.findNavController

class SearchFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class]
        mainViewModel.settitle(resources.getString(R.string.search))
        mainViewModel.setShowNavIcon(false)

        val binding = FragmentSearchBinding.inflate(layoutInflater, container, false)
        val viewModel: SearchViewModel = ViewModelProvider(
            this,
            ViewModelProvider.Factory.from(SearchViewModel.initializer)
        )[SearchViewModel::class.java]

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
}