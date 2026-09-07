package com.kite.mnemoai.reciteword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AiChatBottomSheetDialog: BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflater.inflate(R.layout.bottom_sheet_ai_chat, container, true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    companion object{
        const val TAG = ""
    }
}