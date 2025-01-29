package com.zaed.reservationmanager.data.source.remote.menus

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.zaed.reservationmanager.data.model.Menu
import com.zaed.reservationmanager.data.repository.Menus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MenusDataRemoteDataSourceImpl(
    private val firestore: FirebaseFirestore
) : MenusDataRemoteDataSource {
    override fun getMenuByName(menuName: Menus): Flow<Result<Menu>> = callbackFlow {
        try {
            firestore.collection("menus").document(menuName.name)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val menu = snapshot.toObject(Menu::class.java)
                        if (menu != null) {
                            trySend(Result.success(menu))
                        } else {
                            trySend(Result.failure(Exception("Menu is null")))
                        }
                    } else {
                        Menus.entries.forEach {
                            firestore.collection("menus").document(it.name)
                                .set(mapOf("data" to emptyList<String>()))
                        }
                    }
                }

        } catch (e: Exception) {
            close(e)
            e.printStackTrace()
            trySend(Result.failure(e))
        }
        awaitClose{}
    }

    override suspend fun addItemToMenu(menuName: Menus, item: String): Result<Unit> {
        try {
            val result = firestore.collection("menus").document(menuName.name)
                .update("data", FieldValue.arrayUnion(item))

            if (result.isSuccessful) {
                return Result.success(Unit)
            } else {
                return Result.failure(Exception("Failed to add item to menu"))
            }

        } catch (e: Exception) {
            e.printStackTrace()
             return Result.failure(e)
        }
    }

    override suspend fun removeItemFromMenu(menuName: Menus, item: String): Result<Unit> {
        try {
            val result = firestore.collection("menus").document(menuName.name)
                .update("data", FieldValue.arrayRemove(item))

            if (result.isSuccessful) {
                 return Result.success(Unit)
            } else {
                 return Result.failure(Exception("Failed to add item to menu"))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }
    }
}