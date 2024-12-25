package com.zaed.reservationmanager.data.source.remote

import android.util.Log
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CompanyRemoteDataSourceImpl(
    private val firestore: FirebaseFirestore
) : CompanyRemoteDataSource {
    companion object {
        private val TAG = "CompanyRemoteDataSource"
        private val COMPANY_COLLECTION = "companies"
        private val EMPLOYEE_COLLECTION = "employees"
        private val RESERVATION_COLLECTION = "reservations"
    }

    override fun createCompany(company: Company): Flow<Result<Boolean>> = callbackFlow {
        try {
            firestore.collection(COMPANY_COLLECTION)
                .whereEqualTo("phoneNumber", company.phoneNumber)
                .get()
                .addOnSuccessListener { data ->
                    if (data.isEmpty) {
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
        awaitClose { }
    }

    override fun updateCompany(company: Company): Flow<Result<Boolean>> = callbackFlow {
        try {
            val reservations = firestore.collection(RESERVATION_COLLECTION)
                .where(
                    Filter.or(
                        Filter.equalTo("tourismCompanyId", company.id),
                        Filter.equalTo("travelCompanyId", company.id)
                    )
                ).get().await()
            val employees = firestore.collection(EMPLOYEE_COLLECTION)
                .whereEqualTo("companyId", company.id)
                .get().await()
            Log.d(TAG, "updateCustomer: reservations: ${reservations.size()}")
            firestore.collection(COMPANY_COLLECTION)
                .where(
                    Filter.and(
                        Filter.equalTo("phoneNumber", company.phoneNumber),
                        Filter.notEqualTo("id", company.id)
                    )
                )
                .get()
                .addOnSuccessListener { data ->
                    if (data.isEmpty) {
                        val batch = firestore.batch()
                        val companyRef =
                            firestore.collection(COMPANY_COLLECTION).document(company.id)
                        batch.set(companyRef, company)
                        val reservationUpdates = when (company.type) {
                            CompanyType.TOURISM -> mapOf(
                                "tourismCompany" to company.name,
                                "tourismCompanyPhone" to company.phoneNumber
                            )

                            CompanyType.TRAVEL -> mapOf(
                                "travelCompany" to company.name,
                                "travelCompanyPhone" to company.phoneNumber
                            )
                        }
                        reservations.forEach {
                            batch.update(it.reference, reservationUpdates)
                        }
                        employees.forEach{
                            batch.update(it.reference, mapOf("company" to company.name))
                        }

                        batch.commit().addOnSuccessListener {
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
        awaitClose {}
    }

    override fun deleteCompany(companyId: String): Flow<Result<Unit>> = callbackFlow {
        try {
            firestore.collection(COMPANY_COLLECTION).document(companyId).delete()
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

    override fun getCompanyById(companyId: String): Flow<Result<Company>> = callbackFlow {
        try {
            firestore.collection(COMPANY_COLLECTION).document(companyId).get()
                .addOnSuccessListener { data ->
                    val company = data.toObject(Company::class.java)
                    trySend(Result.success(company ?: Company()))
                }.addOnFailureListener { error ->
                    trySend(Result.failure(error))
                }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun getCompanies(): Flow<Result<List<Company>>> = callbackFlow {
        try {
            firestore.collection(COMPANY_COLLECTION).addSnapshotListener { value, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                } else {
                    val companies = value?.toObjects(Company::class.java)
                    trySend(Result.success(companies ?: emptyList()))
                }
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun getCompanies(isTravel: Boolean): Flow<Result<List<Company>>> = callbackFlow {
        try {
            firestore.collection(COMPANY_COLLECTION).whereEqualTo(
                "type",
                if (isTravel)
                    CompanyType.TRAVEL
                else
                    CompanyType.TOURISM
            ).get()
                .addOnSuccessListener { data ->
                    val companies = data?.toObjects(Company::class.java)
                    trySend(Result.success(companies ?: emptyList()))
                }.addOnFailureListener { error ->
                    trySend(Result.failure(error))
                }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun getCompaniesNames(isDriver: Boolean): Flow<Result<List<String>>> = callbackFlow {
        try {
            firestore.collection(COMPANY_COLLECTION).whereEqualTo(
                "type",
                if (isDriver)
                    CompanyType.TRAVEL
                else
                    CompanyType.TOURISM
            ).get()
                .addOnSuccessListener { data ->
                    val companies = data?.toObjects(Company::class.java)?.map { it.name }
                    trySend(Result.success(companies ?: emptyList()))
                }.addOnFailureListener { error ->
                    trySend(Result.failure(error))
                }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }
}