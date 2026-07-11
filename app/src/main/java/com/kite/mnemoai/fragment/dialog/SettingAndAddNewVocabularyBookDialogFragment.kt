package com.kite.mnemoai.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.kite.mnemoai.databinding.DialogFragmentAddNewVocabularyBookBinding

class SettingAndAddNewVocabularyBookDialogFragment: DialogFragment() {
    private lateinit var name: String
    private lateinit var desc: String

    companion object{
        fun newInstance(name: String?, desc: String?): SettingAndAddNewVocabularyBookDialogFragment{
            val fragment = SettingAndAddNewVocabularyBookDialogFragment()
            val bundle = Bundle().apply {
                putString("name", name)
                putString("desc", desc)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        name = requireArguments().getString("name", "")
        desc = requireArguments().getString("desc", "")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflate = requireActivity().layoutInflater
            val binding = DialogFragmentAddNewVocabularyBookBinding.inflate(inflate, null, false)
            binding.vocabularyNameET.setText(name)
            binding.vocabularyDescET.setText(desc)

            binding.cancelBtn.setOnClickListener {
                dismiss()
            }
            binding.confirmBtn.setOnClickListener {
                val name = binding.vocabularyNameET.text.toString()
                val desc = binding.vocabularyDescET.text.toString()
                val bundle = Bundle().apply {
                    putString("name", name)
                    putString("desc", desc)
                }
                parentFragmentManager.setFragmentResult("confirm", bundle)
                dismiss()
            }
            builder.setView(binding.root)
            builder.create()
        }?: throw IllegalStateException("页面不存在")
    }
}