package com.zaed.reservationmanager.ui.messages

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.model.Message
import com.zaed.reservationmanager.data.repository.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MessagesViewModel(
    private val messageRepo: MessageRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(MessagesUiState())
    private val TAG = "MessagesVM"
    val uiState = _uiState
        .onStart {
            fetchMessages()
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            MessagesUiState()
        )

    private fun fetchMessages() {
        viewModelScope.launch (Dispatchers.IO){
            messageRepo.fetchMessage().collect{ result ->
                result.onSuccess { data ->
                    _uiState.update {
                        it.copy(messages = data, displayedMessages = data)
                    }
                }.onFailure { e ->
                    Log.e(TAG, "fetchMessages: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    fun handleAction(action: MessagesUiAction){
        when(action) {
            is MessagesUiAction.OnAddMessage -> addMessage(action.message, action.onSuccess)
            is MessagesUiAction.OnDeleteMessage -> deleteMessage(action.id)
            is MessagesUiAction.OnEditMessage -> updateMessage(action.message, action.onSuccess)
            is MessagesUiAction.OnSearchMessages -> filterMessages(action.query)
            else -> Unit
        }
    }

    private fun updateMessage(message: Message, onSuccess: () -> Unit) {
        viewModelScope.launch (Dispatchers.IO){
            messageRepo.updateMessage(message).onSuccess {
                Log.d(TAG, "updateMessage: success")
                onSuccess()
            }.onFailure { e ->
                Log.e(TAG, "updateMessage: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun deleteMessage(id: String) {
        viewModelScope.launch (Dispatchers.IO){
            messageRepo.deleteMessage(id).onSuccess {
                Log.d(TAG, "deleteMessage: success")
            }.onFailure { e ->
                Log.e(TAG, "deleteMessage: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun addMessage(message: Message, onSuccess: () -> Unit) {
        viewModelScope.launch (Dispatchers.IO){
            messageRepo.createMessage(message).onSuccess {
                Log.d(TAG, "addMessage: success")
                onSuccess()
            }.onFailure { e ->
                Log.e(TAG, "addMessage: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun filterMessages(query: String) {
        viewModelScope.launch {
            _uiState.update{
                it.copy(searchQuery = query)
            }
            if(query.isBlank()){
                _uiState.update { oldState ->
                    oldState.copy(displayedMessages = oldState.messages)
                }
            } else {
                val filteredMessages = uiState.value.messages.filter { it.title.contains(query) }
                _uiState.update { oldState ->
                    oldState.copy(displayedMessages = filteredMessages)
                }
            }
        }
    }
}