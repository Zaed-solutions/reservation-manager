package com.zaed.reservationmanager.data.source.remote

import android.util.Log
import com.google.firebase.firestore.Filter
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
        private val RESERVATION_COLLECTION = "reservations"
    }

    override fun createCustomer(customer: Customer): Flow<Result<Pair<Boolean, String>>> = callbackFlow {
        Log.d(TAG, "createCustomer: $customer")
        try {
            firestore.collection(CUSTOMER_COLLECTION)
                .whereEqualTo("phoneNumber", customer.phoneNumber1).get()
                .addOnSuccessListener { data ->
                    if (data.isEmpty) {
                        val document = firestore.collection(CUSTOMER_COLLECTION).document()
                        document.set(customer.copy(id = document.id)).addOnSuccessListener {
                            trySend(Result.success(true to document.id))
                        }.addOnFailureListener { e ->
                            trySend(Result.failure(e))
                        }
                    } else {
                        trySend(Result.success(false to ""))
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
                Result.success(Customer())
            } else {
                val customer = task.toObjects(Customer::class.java).first()
                Result.success(customer)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCustomerById(id: String): Result<Customer> {
        return try {
            val task = firestore
                .collection(CUSTOMER_COLLECTION)
                .whereEqualTo("id", id)
                .get().await()
            if (task.isEmpty) {
                Result.failure(Exception("Customer not found"))
            } else {
                val customer = task.documents.first().toObject(Customer::class.java)
                    ?: throw Exception("Customer could not be parsed")
                Result.success(customer)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override fun updateCustomer(customer: Customer): Flow<Result<Boolean>> = callbackFlow {
        try {
            Log.d(TAG, "updateCustomer: called")
            val reservations = firestore.collection(RESERVATION_COLLECTION)
                .whereEqualTo("clientId", customer.id).get().await()
            Log.d(TAG, "updateCustomer: reservations: ${reservations.size()}")
            firestore.collection(CUSTOMER_COLLECTION)
                .where(
                    Filter.and(
                        Filter.equalTo("phoneNumber", customer.phoneNumber1),
                        Filter.notEqualTo("id", customer.id),
                    )
                ).get()
                .addOnSuccessListener { data ->
                    if(data.isEmpty) {
                        Log.d(TAG, "updateCustomer: data: $data")
                        val batch = firestore.batch()
                        val customerRef =
                            firestore.collection(CUSTOMER_COLLECTION).document(customer.id)
                        batch.set(customerRef, customer)
                        val updates = mapOf(
                            "clientName" to customer.name,
                            "clientPhone" to customer.phoneNumber1,
                            "clientCountry" to customer.residenceCountry
                        )
                        reservations.forEach {
                            batch.update(it.reference, updates)
                        }

                        batch.commit().addOnSuccessListener {
                            Log.d(TAG, "updateCustomer: batch success")
                            trySend(Result.success(true))
                        }.addOnFailureListener { e ->
                            Log.d(TAG, "updateCustomer: batch failure")
                            trySend(Result.failure(e))
                        }
                    } else {
                        Log.d(TAG, "updateCustomer: phone number already in use")
                        trySend(Result.success(false))
                    }
                }.addOnFailureListener { e ->
                    Log.d(TAG, "updateCustomer: error: $e")
                    trySend(Result.failure(e))
                }
        } catch (e: Exception) {
            Log.d(TAG, "updateCustomer: exception: $e")
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun deleteCustomer(customerId: String): Flow<Result<Boolean>> = callbackFlow {
        try {
            val reservations = firestore.collection(RESERVATION_COLLECTION).whereEqualTo("clientId", customerId).get().await()
            if(reservations.isEmpty){
                firestore.collection(CUSTOMER_COLLECTION).document(customerId).delete()
                    .addOnSuccessListener {
                        trySend(Result.success(true))
                    }.addOnFailureListener { e ->
                        trySend(Result.failure(e))
                    }
            } else {
                trySend(Result.success(false))
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

    override fun addCustomers(customers: List<Customer>): Flow<Result<Unit>> = callbackFlow {
        try {
            val batch = firestore.batch()
            customers.forEach { customer ->
                val customerRef =
                    firestore.collection(CUSTOMER_COLLECTION).document()
                batch.set(customerRef, customer.copy(id = customerRef.id))
            }
            batch.commit().addOnSuccessListener {
                trySend(Result.success(Unit))
            }.addOnFailureListener {
                trySend(Result.failure(it))
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun updateCustomers(customers: List<Customer>): Flow<Result<Unit>>  = callbackFlow {
        try {
            val batch = firestore.batch()
            customers.forEach { customer ->
                val map = mutableMapOf<String,Any>()
                if(customer.name.isNotBlank()) {
                    map["name"] = customer.name
                }
                if(customer.phoneNumber1.isNotBlank()) {
                    map["phoneNumber"] = customer.phoneNumber1
                }
                if(customer.residenceCountry.isNotBlank()) {
                    map["residenceCountry"] = customer.residenceCountry
                }
                if (customer.email.isNotBlank()) {
                    map["email"] = customer.email
                }
                if (customer.nationality.isNotBlank()) {
                    map["nationality"] = customer.nationality
                }
                val customerRef =
                    firestore.collection(CUSTOMER_COLLECTION).document(customer.id)
                batch.update(customerRef, map)
                val reservations = firestore.collection(RESERVATION_COLLECTION).whereEqualTo("clientId",  customer.id).get().await()
                val updates = mapOf(
                    "clientName" to customer.name,
                    "clientPhone" to customer.phoneNumber1,
                    "clientCountry" to customer.residenceCountry
                )
                reservations.forEach{
                    batch.update(it.reference, updates)
                }
            }
            batch.commit().addOnSuccessListener {
                trySend(Result.success(Unit))
            }.addOnFailureListener {
                trySend(Result.failure(it))
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }
}
