package com.kite.mnemoai.ui.dialog.apikeysetting

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kite.mnemoai.R
import com.kite.mnemoai.databinding.DialogFragmentApiKeySettingBinding

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
                parentFragmentManager.setFragmentResult(API_KEY_SETTING, bundle)
                dismiss()
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
        binding.apiKeyET.post {
            binding.apiKeyET.requestFocus()
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.apiKeyET, InputMethodManager.SHOW_FORCED)
        }
    }
}