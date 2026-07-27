package com.kite.mnemoai.ui.dialog.newlearningwordsetting

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kite.mnemoai.R
import com.kite.mnemoai.databinding.DialogFragmentNewLearningWordSettingBinding

class NewLearningWordSettingDialogFragment: DialogFragment() {
    private var newLearningCount: Int = 20
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
            val binding = DialogFragmentNewLearningWordSettingBinding.inflate(layoutInflater, null, false)
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
}