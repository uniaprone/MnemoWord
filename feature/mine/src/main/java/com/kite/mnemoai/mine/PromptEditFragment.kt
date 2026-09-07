package com.kite.mnemoai.mine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.kite.mnemoai.model.data.AiPromptType
import com.kite.mnemoai.model.data.AiPromptType.CHAT
import com.kite.mnemoai.model.data.AiPromptType.WORD_EXTRACT
import com.kite.mnemoai.mine.databinding.FragmentPromptEditBinding
import com.kite.mnemoai.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PromptEditFragment : Fragment() {
    private var _binding: FragmentPromptEditBinding? = null
    private val binding get() = _binding!!
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: PromptEditViewModel by viewModels()

    private var currentType: AiPromptType = WORD_EXTRACT
    private var currentRoleIsSystem = true
    private var wordExtractSystem = ""
    private var wordExtractUser = ""
    private var chatSystem = ""
    private var chatUser = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel.settitle(getString(R.string.prompt))
        mainViewModel.setShowNavIcon(true)

        _binding = FragmentPromptEditBinding.inflate(inflater, container, false)

        binding.promptTypeSp.adapter = ArrayAdapter(
            requireContext(),
            com.kite.mnemoai.ui.R.layout.item_mai_spinner,
            com.kite.mnemoai.ui.R.id.text1,
            listOf(
                getString(R.string.prompt_type_word_extract),
                getString(R.string.prompt_type_chat)
            )
        )
        binding.promptTypeSp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newType = if (position == 0) WORD_EXTRACT else CHAT
                if (newType == currentType) return
                saveCurrentToSlot()
                currentType = newType
                binding.userRoleChip.visibility = if (newType == CHAT) View.GONE else View.VISIBLE
                if (newType == CHAT && !currentRoleIsSystem) {
                    currentRoleIsSystem = true
                    binding.systemRoleChip.isChecked = true
                }
                loadSlotToEdit()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.roleChipGroup.setOnCheckedStateChangeListener { _, _ ->
            val newIsSystem = binding.systemRoleChip.isChecked
            if (newIsSystem == currentRoleIsSystem) return@setOnCheckedStateChangeListener
            saveCurrentToSlot()
            currentRoleIsSystem = newIsSystem
            loadSlotToEdit()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.filterNotNull().first().let { state ->
                wordExtractSystem = state.wordExtractSystemPrompt
                wordExtractUser = state.wordExtractUserPrompt
                chatSystem = state.chatSystemPrompt
                chatUser = state.chatUserPrompt
                binding.systemRoleChip.isChecked = true
                loadSlotToEdit()
            }
        }

        binding.saveBtn.setOnClickListener {
            saveCurrentToSlot()
            viewModel.save(wordExtractSystem, wordExtractUser, chatSystem, chatUser)
            findNavController().popBackStack()
        }

        return binding.root
    }

    private fun saveCurrentToSlot() {
        val text = binding.promptContentET.text.toString()
        when (currentType) {
            WORD_EXTRACT -> if (currentRoleIsSystem) wordExtractSystem = text else wordExtractUser = text
            CHAT -> if (currentRoleIsSystem) chatSystem = text else chatUser = text
        }
    }

    private fun loadSlotToEdit() {
        binding.promptContentET.setText(
            when (currentType) {
                WORD_EXTRACT -> if (currentRoleIsSystem) wordExtractSystem else wordExtractUser
                CHAT -> if (currentRoleIsSystem) chatSystem else chatUser
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
