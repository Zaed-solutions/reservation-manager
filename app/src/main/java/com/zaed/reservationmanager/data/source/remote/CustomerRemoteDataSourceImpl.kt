package com.zaed.reservationmanager.data.source.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.zaed.reservationmanager.data.model.Customer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class CustomerRemoteDataSourceImpl(
    private val firestore: FirebaseFirestore
) : CustomerRemoteDataSource{
    companion object{
        private val TAG = "CustomerRemoteDataSource"
        private val CUSTOMER_COLLECTION = "customers"
    }

    override fun createCustomer(customer: Customer): Flow<Result<Unit>> = callbackFlow{
        try {
            firestore.collection(CUSTOMER_COLLECTION).whereEqualTo("phoneNumber", customer.phoneNumber).get().addOnSuccessListener { data ->
                if(data.isEmpty){
                    val document = firestore.collection(CUSTOMER_COLLECTION).document()
                    document.set(customer.copy(id = document.id)).addOnSuccessListener {
                        trySend(Result.success(Unit))
                    }.addOnFailureListener { e ->
                        trySend(Result.failure(e))
                    }
                } else {
                    trySend(Result.failure(Exception("Customer with this phone number already exists")))
                }
            }.addOnFailureListener { e ->
                trySend(Result.failure(e))
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose {  }
    }

    override fun updateCustomer(customer: Customer): Flow<Result<Unit>> = callbackFlow {
        try{
            firestore.collection(CUSTOMER_COLLECTION).document(customer.id).set(customer).addOnSuccessListener {
                trySend(Result.success(Unit))
            }.addOnFailureListener { e ->
                trySend(Result.failure(e))
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose {  }
    }

    override fun deleteCustomer(customerId: String): Flow<Result<Unit>> = callbackFlow {
        try{
            firestore.collection(CUSTOMER_COLLECTION).document(customerId).delete().addOnSuccessListener {
                trySend(Result.success(Unit))
            }.addOnFailureListener { e ->
                trySend(Result.failure(e))
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose {  }
    }

    override fun getCustomers(): Flow<Result<List<Customer>>> = callbackFlow {
        try{
            firestore.collection(CUSTOMER_COLLECTION).addSnapshotListener { value, error ->
                if(error != null){
                    trySend(Result.failure(error))
                } else {
                    val customers = value?.toObjects(Customer::class.java)
                    trySend(Result.success(customers ?: emptyList()))
                }
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose {  }
    }
}