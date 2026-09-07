package com.kite.mnemoai.model.chat

enum class ChatRole(val storageValue: String) {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant");

    companion object {
        fun fromStorage(value: String): ChatRole =
            entries.firstOrNull { it.storageValue == value } ?: USER
    }
}
