package com.kite.mnemoai.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.kite.mnemoai.databinding.DialogFragmentApiKeySettingBinding

class ApiKeySettingDialogFragment(val apiKeyDialogListener: ApiKeyDialogListener): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater

            val binding = DialogFragmentApiKeySettingBinding.inflate(inflater, null, false)
            binding.apiKeyConfirmBtn.setOnClickListener {
                val apiKey: String = binding.apiKeyET.text.toString()
                apiKeyDialogListener.onConfirm(apiKey)
                dismiss()
            }

            binding.apiKeyCancelBtn.setOnClickListener {
                apiKeyDialogListener.onCancel()
                dismiss()
            }

            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("页面不存在")
    }

    interface ApiKeyDialogListener{
        fun onConfirm(apiKey: String)
        fun onCancel()
    }
}