package com.zaed.reservationmanager.ui.messages

import com.zaed.reservationmanager.data.model.Message

sealed interface MessagesUiAction {
    data object OnShowNavDrawer: MessagesUiAction
    data class OnSearchMessages(val query: String): MessagesUiAction
    data class OnDeleteMessage(val id: String): MessagesUiAction
    data class OnCopyMessage(val message: String): MessagesUiAction
    data class OnEditMessage(val message: Message, val onSuccess: () -> Unit): MessagesUiAction
    data class OnAddMessage(val message: Message, val onSuccess: () -> Unit): MessagesUiAction
}