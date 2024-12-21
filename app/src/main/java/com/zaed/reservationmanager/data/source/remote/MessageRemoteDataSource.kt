package com.zaed.reservationmanager.data.source.remote

import com.zaed.reservationmanager.data.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRemoteDataSource {
    fun fetchMessages(): Flow<Result<List<Message>>>
    suspend fun createMessage(message: Message): Result<Unit>
    suspend fun updateMessage(message: Message): Result<Unit>
    suspend fun deleteMessage(messageId: String): Result<Unit>
}