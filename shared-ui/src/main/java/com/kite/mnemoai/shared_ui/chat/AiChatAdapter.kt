package com.kite.mnemoai.shared_ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kite.mnemoai.shared_ui.databinding.ItemChatMessageOtherBinding
import com.kite.mnemoai.shared_ui.databinding.ItemChatMessageUserBinding
import io.noties.markwon.Markwon

class AiChatAdapter :
    ListAdapter<ItemChatMessage, RecyclerView.ViewHolder>(ItemChatMessage.DIFF_CALLBACK) {

    private var markwon: Markwon? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val markwon = markwon ?: Markwon.builder(parent.context).build().also { this.markwon = it }
        return when (viewType) {
            ChatMessageType.USER.ordinal ->
                ChatUserViewHolder(
                    ItemChatMessageUserBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                    markwon
                )
            ChatMessageType.TIME.ordinal ->
                ChatTimeViewHolder(
                    ItemChatMessageOtherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            else ->
                ChatOtherViewHolder(
                    ItemChatMessageOtherBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                    markwon
                )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is ChatTimeViewHolder -> holder.bind(item)
            is ChatOtherViewHolder -> holder.bind(item)
            is ChatUserViewHolder -> holder.bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int = getItem(position).type.ordinal

    class ChatOtherViewHolder(
        binding: ItemChatMessageOtherBinding,
        private val markwon: Markwon
    ) : RecyclerView.ViewHolder(binding.root) {
        private val messageTV = binding.message

        fun bind(message: ItemChatMessage) {
            markwon.setMarkdown(messageTV, message.message)
        }
    }

    class ChatUserViewHolder(
        binding: ItemChatMessageUserBinding,
        private val markwon: Markwon
    ) : RecyclerView.ViewHolder(binding.root) {
        private val messageTV = binding.message

        fun bind(message: ItemChatMessage) {
            markwon.setMarkdown(messageTV, message.message)
        }
    }

    class ChatTimeViewHolder(binding: ItemChatMessageOtherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val messageTV = binding.message

        fun bind(message: ItemChatMessage) {
            messageTV.text = message.message
        }
    }
}
