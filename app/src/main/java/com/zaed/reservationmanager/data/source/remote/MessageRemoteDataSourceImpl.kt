package com.zaed.reservationmanager.data.source.remote

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.zaed.reservationmanager.data.model.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.bouncycastle.asn1.x500.style.RFC4519Style.c

class MessageRemoteDataSourceImpl (
    private val firestore: FirebaseFirestore,
    private val crashlytics: FirebaseCrashlytics
): MessageRemoteDataSource {
    private val MESSAGES_COLLECTION = "messages"
    override fun fetchMessages(): Flow<Result<List<Message>>> = callbackFlow {
        try {
            firestore.collection(MESSAGES_COLLECTION).addSnapshotListener{value, error ->
                if(error!=null){
                    trySend(Result.failure(error))
                } else {
                    val messages = value?.toObjects(Message::class.java)?: emptyList()
                    trySend(Result.success(messages))
                }
            }
        } catch (e: Exception){
            crashlytics.recordException(e)
            trySend(Result.failure(e))
        }
        awaitClose {  }
    }

    override suspend fun createMessage(message: Message): Result<Unit> {
        return try {
            val messageRef = firestore.collection(MESSAGES_COLLECTION).document()
            messageRef.set(message.copy(id = messageRef.id)).await()
            Result.success(Unit)
        } catch (e: Exception){
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun updateMessage(message: Message): Result<Unit> {
        return try {
            val messageRef = firestore.collection(MESSAGES_COLLECTION).document(message.id)
            messageRef.set(message).await()
            Result.success(Unit)
        } catch (e: Exception){
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            val messageRef = firestore.collection(MESSAGES_COLLECTION).document(messageId)
            messageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception){
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }
}