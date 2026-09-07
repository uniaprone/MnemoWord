package com.kite.mnemoai.shared_ui.chat

import androidx.recyclerview.widget.DiffUtil

data class ItemChatMessage(
    val id: Long,
    val type: ChatMessageType,
    val message: String
) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ItemChatMessage>() {
            override fun areItemsTheSame(
                oldItem: ItemChatMessage,
                newItem: ItemChatMessage
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ItemChatMessage,
                newItem: ItemChatMessage
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
