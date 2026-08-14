package com.kite.mnemoai.mine

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.fragment.app.DialogFragment
import com.kite.mnemoai.mine.databinding.DialogFragmentApiKeySettingBinding
import com.kite.mnemoai.ui.R

class ApiKeySettingDialogFragment: DialogFragment() {
    companion object{
        const val API_KEY_SETTING = "APIKEYSETTING"
    }
    private lateinit var binding: DialogFragmentApiKeySettingBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = MaterialAlertDialogBuilder(it, R.style.CustomAlertDialog)
            val inflater = requireActivity().layoutInflater

            binding = DialogFragmentApiKeySettingBinding.inflate(inflater, null, false)
            binding.confirmBtn.setOnClickListener {
                val apiKey: String = binding.apiKeyET.text.toString()
                val bundle = Bundle().apply {
                    putString("api_key", apiKey)
                }
                dismiss()
                parentFragmentManager.setFragmentResult(API_KEY_SETTING, bundle)
            }

            binding.cancelBtn.setOnClickListener {
                dismiss()
            }

            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("页面不存在")
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        binding.apiKeyET.post {
            binding.apiKeyET.let {
                it.requestFocus()
                it.isFocusable = true
                it.isFocusableInTouchMode = true
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(it, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }
}