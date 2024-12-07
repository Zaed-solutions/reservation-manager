package com.zaed.reservationmanager.data.source.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.zaed.reservationmanager.data.model.Company
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class CompanyRemoteDataSourceImpl(
    private val firestore: FirebaseFirestore
) : CompanyRemoteDataSource {
    companion object{
        private val TAG = "CompanyRemoteDataSource"
        private val COMPANY_COLLECTION = "companies"
    }
    override fun createCompany(company: Company): Flow<Result<Boolean>> = callbackFlow{
        try {
            firestore.collection(COMPANY_COLLECTION).whereEqualTo("name", company.name).get().addOnSuccessListener { data ->
                if(data.isEmpty){
                    val document = firestore.collection(COMPANY_COLLECTION).document()
                    document.set(company.copy(id = document.id)).addOnSuccessListener {
                        trySend(Result.success(true))
                    }.addOnFailureListener { e ->
                        trySend(Result.failure(e))
                    }
                } else {
                    trySend(Result.success(false))
                }
            }.addOnFailureListener { e ->
                trySend(Result.failure(e))
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose {  }
    }

    override fun updateCompany(company: Company): Flow<Result<Unit>> = callbackFlow {
        try{
            firestore.collection(COMPANY_COLLECTION).document(company.id).set(company).addOnSuccessListener {
                trySend(Result.success(Unit))
            }.addOnFailureListener { e ->
                trySend(Result.failure(e))
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose {  }
    }

    override fun deleteCompany(companyId: String): Flow<Result<Unit>> = callbackFlow {
        try{
            firestore.collection(COMPANY_COLLECTION).document(companyId).delete().addOnSuccessListener {
                trySend(Result.success(Unit))
            }.addOnFailureListener { e ->
                trySend(Result.failure(e))
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose {  }
    }

    override fun getCompanies(): Flow<Result<List<Company>>> = callbackFlow {
        try{
            firestore.collection(COMPANY_COLLECTION).addSnapshotListener { value, error ->
                if(error != null){
                    trySend(Result.failure(error))
                } else {
                    val companies = value?.toObjects(Company::class.java)
                    trySend(Result.success(companies ?: emptyList()))
                }
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose {  }
    }
}