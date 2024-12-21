package com.zaed.reservationmanager.ui.messages

import com.zaed.reservationmanager.data.model.Message

data class MessagesUiState(
    val messages: List<Message> = emptyList(),
    val displayedMessages: List<Message> = emptyList(),
    val searchQuery: String = ""
)
