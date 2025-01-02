package com.zaed.reservationmanager.data.source.remote

import android.util.Log
import com.google.firebase.firestore.AggregateField
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyBalance
import com.zaed.reservationmanager.data.model.CompanyPayment
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
        private val COMPANY_PAYMENT_COLLECTION = "payments"
        private val EMPLOYEE_COLLECTION = "employees"
        private val RESERVATION_COLLECTION = "reservations"
    }

    override fun createCompany(company: Company): Flow<Result<Boolean>> = callbackFlow {
        try {
            firestore.collection(COMPANY_COLLECTION)
                .whereEqualTo("phoneNumber1", company.phoneNumber1)
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
                        Filter.equalTo("phoneNumber1", company.phoneNumber1),
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
                                "tourismCompanyPhone" to company.phoneNumber1
                            )

                            CompanyType.TRAVEL -> mapOf(
                                "travelCompany" to company.name,
                                "travelCompanyPhone" to company.phoneNumber1
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
            firestore.collection(COMPANY_COLLECTION).document(companyId).addSnapshotListener { data, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                } else {
                    val company = data?.toObject(Company::class.java) ?: Company()
                    trySend(Result.success(company))
                }
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun getCompanies(): Flow<Result<List<Company>>> = callbackFlow {
        try {
            firestore.collection(COMPANY_COLLECTION)
                .orderBy("name")
                .addSnapshotListener { value, error ->
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

    override suspend fun getCompanyBalance(
        companyId: String,
        companyType: CompanyType
    ): Result<CompanyBalance> {
        return try {
            val reservationQuery = firestore.collection(RESERVATION_COLLECTION).where(
                Filter.or(
                    Filter.equalTo("tourismCompanyId", companyId),
                    Filter.equalTo("travelCompanyId", companyId)
                )
            )
            val reservationSnapshot = reservationQuery.get().await()
            reservationSnapshot.documents.forEach { document ->
                Log.d("ReservationDocument", document.data.toString())
            }
            val paymentQuery = firestore.collection(COMPANY_PAYMENT_COLLECTION).whereEqualTo("companyId", companyId)
            val totalRidePriceResult = reservationQuery.aggregate(AggregateField.sum(if(companyType == CompanyType.TOURISM) "tourismRidePrice" else "travelRidePrice"))
                .get(AggregateSource.SERVER).await()
            val totalCollectedResult = reservationQuery.aggregate(AggregateField.sum(if(companyType == CompanyType.TOURISM)"tourismCollectedAmount" else "travelCollectedAmount"))
                .get(AggregateSource.SERVER).await()
            val totalPaymentResult =  paymentQuery.aggregate(AggregateField.sum("amount"))
                .get(AggregateSource.SERVER).await()

            val totalRidePrice = (totalRidePriceResult.get(AggregateField.sum(if (companyType == CompanyType.TOURISM) "tourismRidePrice" else "travelRidePrice")) as? Number)?.toDouble() ?: 0.0

            val totalCollected = (totalCollectedResult.get(AggregateField.sum(if (companyType == CompanyType.TOURISM) "tourismCollectedAmount" else "travelCollectedAmount")) as? Number)?.toDouble() ?: 0.0
            val totalPayment = (totalPaymentResult.get(AggregateField.sum("amount")) as? Number)?.toDouble() ?: 0.0
            Log.d(
                "CompanyBalance",
                "getCompanyBalance: $companyId $companyType totalRidePrice=$totalRidePrice, totalCollected=$totalCollected, totalPayment=$totalPayment"
            )
            Result.success(
                CompanyBalance(
                    totalRidePrice = totalRidePrice,
                    totalCollected = totalCollected,
                    totalPayment = totalPayment
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override fun getCompanyPayments(companyId: String): Flow<Result<List<CompanyPayment>>> = callbackFlow {
        try {
            firestore.collection(COMPANY_PAYMENT_COLLECTION).whereEqualTo("companyId", companyId)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        trySend(Result.failure(error))
                    } else {
                        val payments = value?.toObjects(CompanyPayment::class.java) ?: emptyList()
                        trySend(Result.success(payments))
                    }
                }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override suspend fun addPayment(payment: CompanyPayment): Result<Boolean> {
        return try {
            val document =firestore.collection(COMPANY_PAYMENT_COLLECTION).document()
            document.set(payment.copy(id = document.id)).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun editPayment(payment: CompanyPayment): Result<Boolean> {
        return try {
            firestore.collection(COMPANY_PAYMENT_COLLECTION).document(payment.id).set(payment).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePayment(paymentId: String): Result<Boolean> {
        return try {
            firestore.collection(COMPANY_PAYMENT_COLLECTION).document(paymentId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}