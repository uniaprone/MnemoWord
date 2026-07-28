package com.kite.mnemoai.ui.dialog.newlearningwordsetting

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kite.mnemoai.R
import com.kite.mnemoai.databinding.DialogFragmentNewLearningWordSettingBinding

class NewLearningWordSettingDialogFragment: DialogFragment() {
    private var newLearningCount: Int = 20
    private lateinit var binding: DialogFragmentNewLearningWordSettingBinding
    companion object{
        const val NEW_LEARNING_COUNT_SETTING = "NEWLEARNINGCOUNTSETTING"

        fun newInstance(count: Int): NewLearningWordSettingDialogFragment{
            val bundle = Bundle().apply {
                putInt("new_learning_count", count)
            }
            val dialogFragment = NewLearningWordSettingDialogFragment()
            dialogFragment.arguments = bundle
            return dialogFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        newLearningCount = arguments?.getInt("new_learning_count") ?: 20
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            binding = DialogFragmentNewLearningWordSettingBinding.inflate(layoutInflater, null, false)
            binding.newLearningWordCountET.setText(newLearningCount.toString())
            binding.cancelBtn.setOnClickListener { dismiss() }
            binding.confirmBtn.setOnClickListener {
                try {
                    val count: Int = binding.newLearningWordCountET.text.toString().toInt()
                    val bundle = Bundle().apply {
                        putInt("new_learning_count", count)
                    }
                    parentFragmentManager.setFragmentResult(NEW_LEARNING_COUNT_SETTING, bundle)
                    dismiss()
                }catch (e: NumberFormatException){
                    Toast.makeText(requireContext(), "非法数字！", Toast.LENGTH_LONG).show()
                }
            }
            val builder = MaterialAlertDialogBuilder(it, R.style.CustomAlertDialog)
            builder.setView(binding.root)
            builder.create()
        }?: throw IllegalStateException("页面不存在")
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        binding.newLearningWordCountET.post {
            binding.newLearningWordCountET.let {
                it.requestFocus()
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(it, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }
}