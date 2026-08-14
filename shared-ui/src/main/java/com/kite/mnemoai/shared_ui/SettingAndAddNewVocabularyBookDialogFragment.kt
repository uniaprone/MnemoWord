package com.kite.mnemoai.shared_ui

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kite.mnemoai.shared_ui.databinding.DialogFragmentAddNewVocabularyBookBinding

class SettingAndAddNewVocabularyBookDialogFragment: DialogFragment() {
    // operation 0修改 1添加
    private var operation: Byte = 0
    private lateinit var name: String
    private lateinit var desc: String

    companion object{
        fun newInstance(operation: Byte, name: String?, desc: String?): SettingAndAddNewVocabularyBookDialogFragment{
            val fragment = SettingAndAddNewVocabularyBookDialogFragment()
            val bundle = Bundle().apply {
                putByte("operation", operation)
                putString("name", name)
                putString("desc", desc)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        operation = arguments?.getByte("operation") ?: 0
        name = arguments?.getString("name") ?: ""
        desc = arguments?.getString("desc") ?: ""
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = MaterialAlertDialogBuilder(it, R.style.CustomAlertDialog)
            val inflate = requireActivity().layoutInflater
            val binding = DialogFragmentAddNewVocabularyBookBinding.inflate(inflate, null, false)

            if(operation == 0.toByte()){
                binding.titleTV.text = binding.root.resources.getString(R.string.modify_vocabulary_book)
            }else if(operation == 1.toByte()){
                binding.titleTV.text = binding.root.resources.getString(R.string.add_new_vocabulary)
            }

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