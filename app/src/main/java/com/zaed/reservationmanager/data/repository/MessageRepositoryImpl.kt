package com.zaed.reservationmanager.data.repository

import com.zaed.reservationmanager.data.model.Message
import com.zaed.reservationmanager.data.source.remote.MessageRemoteDataSource
import kotlinx.coroutines.flow.Flow

class MessageRepositoryImpl (
    private val remoteSource: MessageRemoteDataSource
): MessageRepository {
    override fun fetchMessage(): Flow<Result<List<Message>>> {
        return remoteSource.fetchMessages()
    }

    override suspend fun createMessage(message: Message): Result<Unit> {
        return remoteSource.createMessage(message)
    }

    override suspend fun updateMessage(message: Message): Result<Unit> {
        return remoteSource.updateMessage(message)
    }

    override suspend fun deleteMessage(messageId: String): Result<Unit> {
        return remoteSource.deleteMessage(messageId)
    }
}