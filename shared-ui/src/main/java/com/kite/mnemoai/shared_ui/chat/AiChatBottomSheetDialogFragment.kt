package com.kite.mnemoai.shared_ui.chat

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.chat.ChatMessage
import com.kite.mnemoai.model.chat.ChatRole
import com.kite.mnemoai.shared_ui.R
import com.kite.mnemoai.ui.KeyboardUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AiChatBottomSheetDialogFragment : BottomSheetDialogFragment() {
    private val viewModel: ChatViewModel by viewModels()
    private val adapter = AiChatAdapter()
    private lateinit var aiChatRV: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var keyboardUtil: KeyboardUtil

    override fun onStart() {
        super.onStart()
        val sheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) ?: return
        sheet.layoutParams = sheet.layoutParams.apply {
            height = (resources.displayMetrics.heightPixels * 0.7f).toInt()
        }
        val typedValue = TypedValue()
        if (requireContext().theme.resolveAttribute(
                com.kite.mnemoai.ui.R.attr.colorSurfaceContainer,
                typedValue,
                true
            )
        ) {
            sheet.backgroundTintList = ColorStateList.valueOf(typedValue.data)
        }
        val behavior = BottomSheetBehavior.from(sheet)
        behavior.skipCollapsed = true
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_ai_chat, container, false)

        aiChatRV = view.findViewById(R.id.chatRV)
        linearLayoutManager = LinearLayoutManager(context)
        aiChatRV.layoutManager = linearLayoutManager
        aiChatRV.adapter = adapter
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                aiChatRV.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                aiChatRV.scrollToPosition(adapter.itemCount - 1)
            }
        })

        keyboardUtil = KeyboardUtil(view.rootView)
        keyboardUtil.registerListener(object : KeyboardUtil.OnKeyboardVisibilityListener {
            override fun onKeyboardVisibilityChanged(
                isVisible: Boolean,
                keyboardHeight: Int
            ) {
                if (isVisible) {
                    scrollToBottom()
                }

            }
        })

        val messageET = view.findViewById<EditText>(R.id.messageET)
        val sendBtn = view.findViewById<MaterialButton>(R.id.send)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.messages.collect { chatMessages ->
                        adapter.submitList(chatMessages.map(::toChatItem))
                    }
                }
                launch {
                    viewModel.sendResult.collect { result ->
                        if (result is Result.Error) {
                            Toast.makeText(
                                context,
                                result.exception.message ?: "发送失败",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        sendBtn.setOnClickListener {
            val text = messageET.text.toString()
            viewModel.send(text)
            messageET.text.clear()
        }

        return view
    }

    private fun toChatItem(chatMessage: ChatMessage): ItemChatMessage = ItemChatMessage(
        id = chatMessage.id ?: 0,
        type = when (chatMessage.role) {
            ChatRole.USER -> ChatMessageType.USER
            else -> ChatMessageType.AI
        },
        message = chatMessage.content
    )

    companion object {
        const val TAG = "AI_CHAT_BOTTOM_SHEET"

        fun newInstance(wordId: Long): AiChatBottomSheetDialogFragment =
            AiChatBottomSheetDialogFragment().apply {
                arguments = Bundle().apply { putLong(ChatViewModel.ARG_WORD_ID, wordId) }
            }
    }

    fun scrollToBottom() {
        val lastPosition = adapter.itemCount - 1
        if (lastPosition < 0) return

        val lm = linearLayoutManager
        val rv = aiChatRV
        val paddingTop = rv.paddingTop
        val paddingBottom = rv.paddingBottom

        val lastView = lm.findViewByPosition(lastPosition)
        if (lastView != null) {
            val lp = lastView.layoutParams as? RecyclerView.LayoutParams
            val topMargin = lp?.topMargin ?: 0
            val bottomMargin = lp?.bottomMargin ?: 0
            val itemTotalHeight = lastView.height + topMargin + bottomMargin

            val offset = rv.height - paddingBottom - itemTotalHeight
            lm.scrollToPositionWithOffset(lastPosition, offset)
        } else {
            lm.scrollToPositionWithOffset(lastPosition, 0)
            rv.post {
                val view = lm.findViewByPosition(lastPosition)
                if (view != null) {
                    val lp = view.layoutParams as? RecyclerView.LayoutParams
                    val topMargin = lp?.topMargin ?: 0
                    val bottomMargin = lp?.bottomMargin ?: 0
                    val itemTotalHeight = view.height + topMargin + bottomMargin
                    val offset = rv.height - paddingBottom - itemTotalHeight
                    lm.scrollToPositionWithOffset(lastPosition, offset)
                }
            }
        }
    }
}
