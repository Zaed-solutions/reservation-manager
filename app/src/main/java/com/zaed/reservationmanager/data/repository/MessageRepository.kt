package com.zaed.reservationmanager.data.repository

import com.zaed.reservationmanager.data.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun fetchMessage(): Flow<Result<List<Message>>>
    suspend fun createMessage(message: Message): Result<Unit>
    suspend fun updateMessage(message: Message): Result<Unit>
    suspend fun deleteMessage(messageId: String): Result<Unit>
}