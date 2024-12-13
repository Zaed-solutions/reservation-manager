package com.zaed.reservationmanager.data.source.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.zaed.reservationmanager.data.model.Customer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CustomerRemoteDataSourceImpl(
    private val firestore: FirebaseFirestore
) : CustomerRemoteDataSource {
    companion object {
        private val TAG = "CustomerRemoteDataSource"
        private val CUSTOMER_COLLECTION = "customers"
    }

    override fun createCustomer(customer: Customer): Flow<Result<String>> = callbackFlow {
        try {
            firestore.collection(CUSTOMER_COLLECTION)
                .whereEqualTo("phoneNumber", customer.phoneNumber).get()
                .addOnSuccessListener { data ->
                    if (data.isEmpty) {
                        val document = firestore.collection(CUSTOMER_COLLECTION).document()
                        document.set(customer.copy(id = document.id)).addOnSuccessListener {
                            trySend(Result.success(document.id))
                        }.addOnFailureListener { e ->
                            trySend(Result.failure(e))
                        }
                    } else {
                        firestore.collection(CUSTOMER_COLLECTION).document(customer.id)
                            .set(customer)
                            .addOnSuccessListener {
                                trySend(Result.success(customer.id))
                            }.addOnFailureListener { e ->
                                trySend(Result.failure(e))
                            }
                    }
                }.addOnFailureListener { e ->
                trySend(Result.failure(e))
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override suspend fun getCustomerByNumber(number: String): Result<Customer> {
        return try {
            val task = firestore
                .collection(CUSTOMER_COLLECTION)
                .whereEqualTo("phoneNumber", number)
                .get().await()
            if (task.isEmpty) {
                Result.failure(Exception("Customer not found"))
            } else {
                val customer = task.toObjects(Customer::class.java).first()
                Result.success(customer)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override fun updateCustomer(customer: Customer): Flow<Result<Unit>> = callbackFlow {
        try {
            firestore.collection(CUSTOMER_COLLECTION).document(customer.id).set(customer)
                .addOnSuccessListener {
                    trySend(Result.success(Unit))
                }.addOnFailureListener { e ->
                trySend(Result.failure(e))
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun deleteCustomer(customerId: String): Flow<Result<Unit>> = callbackFlow {
        try {
            firestore.collection(CUSTOMER_COLLECTION).document(customerId).delete()
                .addOnSuccessListener {
                    trySend(Result.success(Unit))
                }.addOnFailureListener { e ->
                trySend(Result.failure(e))
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun getCustomers(): Flow<Result<List<Customer>>> = callbackFlow {
        try {
            firestore.collection(CUSTOMER_COLLECTION).addSnapshotListener { value, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                } else {
                    val customers = value?.toObjects(Customer::class.java)
                    trySend(Result.success(customers ?: emptyList()))
                }
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }
}