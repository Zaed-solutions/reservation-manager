package com.zaed.reservationmanager.data.source.remote

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyHistory
import com.zaed.reservationmanager.data.model.CompanyPayment
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.convertToCompanyHistoryList
import com.zaed.reservationmanager.ui.home.component.Report
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReservationRemoteDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val crashlytics: FirebaseCrashlytics
) : ReservationRemoteDataSource {
    private val TAG = "ReservationRemoteDataSource"
    private val RESERVATION_COLLECTION = "reservations"
    private val COMPANY_PAYMENT_COLLECTION = "payments"
    private val COMPANY_COLLECTION = "companies"
    override fun createReservation(reservation: Reservation): Flow<Result<Pair<String, Long>>> =
        callbackFlow {
            try {
                if(reservation.mainReservation){
                    createMainReservation(reservation)
                } else {
                    createSecondaryReservation(reservation)
                }
            } catch (e: Exception) {
                crashlytics.recordException(e)
                trySend(Result.failure(e))
            }
            awaitClose { }
        }

    private suspend fun ProducerScope<Result<Pair<String, Long>>>.createSecondaryReservation(reservation: Reservation) {
        val batch = firestore.batch()
        val reservationRef = firestore.collection(RESERVATION_COLLECTION).document()
        val mainReservationRef = firestore.collection(RESERVATION_COLLECTION).document(reservation.mainReservationId)
        var mainReservation = mainReservationRef.get().await().toObject(Reservation::class.java)!!
        mainReservation = mainReservation.copy(
            totalRidesPrice = (
                    if(mainReservation.totalRidesPrice == 0)
                        mainReservation.tourismRidePrice
                    else
                        mainReservation.totalRidesPrice
                    ) + reservation.tourismRidePrice,
            numberOfRides = mainReservation.numberOfRides + 1
        )
        val secondaryReservations = firestore.collection(RESERVATION_COLLECTION).whereEqualTo("mainReservationId", reservation.mainReservationId).get().await().toObjects(Reservation::class.java)
        secondaryReservations.forEach {
            batch.set(
                firestore
                    .collection(RESERVATION_COLLECTION)
                    .document(it.id),
                it.copy(
                    totalRidesPrice = mainReservation.totalRidesPrice,
                    numberOfRides = mainReservation.numberOfRides
                )
            )
        }
        batch.set(mainReservationRef, mainReservation)
        Log.d(TAG, "createSecondaryReservation: $mainReservation,,,,,,,,, $reservation")
        batch.set(reservationRef, reservation.copy(
            id = reservationRef.id,
            numberOfRides = mainReservation.numberOfRides,
            reservationNumber = mainReservation.reservationNumber,
            totalRidesPrice = mainReservation.totalRidesPrice
            )
        )
        batch.commit().addOnSuccessListener {
            trySend(Result.success(reservationRef.id to mainReservation.reservationNumber))
        }.addOnFailureListener {e ->
            crashlytics.recordException(e)
            trySend(Result.failure(e))
        }
    }

    private fun ProducerScope<Result<Pair<String, Long>>>.createMainReservation(
        reservation: Reservation
    ) {
        firestore.collection(RESERVATION_COLLECTION)
            .orderBy(
                "reservationNumber",
                Query.Direction.DESCENDING
            )
            .limit(1)
            .get()
            .addOnSuccessListener { doc ->
                val reservationNumber = if (!doc.isEmpty) {
                    doc.documents[0].get("reservationNumber") as Long
                } else {
                    0
                }
                val reservationRef = firestore.collection(RESERVATION_COLLECTION).document()
                reservationRef.set(
                    reservation.copy(
                        id = reservationRef.id,
                        reservationNumber = reservationNumber + 1,
                        totalRidesPrice = reservation.tourismRidePrice,
                    )
                ).addOnSuccessListener {
                    trySend(Result.success(reservationRef.id to (reservationNumber + 1)))
                }.addOnFailureListener { e ->
                    crashlytics.recordException(e)
                    trySend(Result.failure(e))
                }
            }
    }

    override fun createReservations(reservations: List<Reservation>): Flow<Result<Unit>> =
        callbackFlow {
            try {
                firestore.collection(RESERVATION_COLLECTION)
                    .orderBy(
                        "reservationNumber",
                        com.google.firebase.firestore.Query.Direction.DESCENDING
                    )
                    .limit(1)
                    .get()
                    .addOnSuccessListener { doc ->
                        var reservationNumber = if (!doc.isEmpty) {
                            doc.documents[0].get("reservationNumber") as Long
                        } else {
                            0
                        }
                        val batch = firestore.batch()
                        reservations.forEach { reservation ->
                            val reservationRef =
                                firestore.collection(RESERVATION_COLLECTION).document()
                            reservationNumber++
                            batch.set(
                                reservationRef,
                                reservation.copy(
                                    id = reservationRef.id,
                                    reservationNumber = reservationNumber
                                )
                            )
                        }
                        batch.commit().addOnSuccessListener {
                            trySend(Result.success(Unit))
                        }.addOnFailureListener {e ->
                            crashlytics.recordException(e)
                            trySend(Result.failure(e))
                        }
                    }
            } catch (e: Exception) {
                crashlytics.recordException(e)
                trySend(Result.failure(e))
            }
            awaitClose { }
        }

    override fun getReservationById(id: String): Flow<Result<Reservation>> = callbackFlow {
        try {
            firestore
                .collection(RESERVATION_COLLECTION)
                .document(id)
                .get().addOnSuccessListener {
                    val reservation = it.toObject(Reservation::class.java)
                    reservation?.let {
                        trySend(Result.success(reservation))
                    } ?: trySend(Result.failure(Exception("Reservation not found")))
                }.addOnFailureListener {e ->
                    crashlytics.recordException(e)
                    trySend(Result.failure(e))
                }
        } catch (e: Exception) {
            crashlytics.recordException(e)
            trySend(Result.failure(e))
        }
        awaitClose {}
    }

    override fun getReservationsByCustomerId(customerId: String): Flow<Result<List<Reservation>>> =
        callbackFlow {
            try {
                firestore.collection(RESERVATION_COLLECTION)
                    .whereEqualTo("clientId", customerId)
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            crashlytics.recordException(error)
                            trySend(Result.failure(error))
                        } else {
                            val reservations =
                                value?.toObjects(Reservation::class.java) ?: emptyList()
                            trySend(Result.success(reservations))
                        }
                    }
            } catch (e: Exception) {
                crashlytics.recordException(e)
                trySend(Result.failure(e))
            }
            awaitClose { }
        }


    override fun getReservations(): Flow<Result<List<Reservation>>> = callbackFlow {
        try {
            firestore.collection(RESERVATION_COLLECTION)
                .whereNotEqualTo("archived", true)
                .addSnapshotListener { task, error ->
                    if (error != null) {
                        crashlytics.recordException(error)
                        trySend(Result.failure(error))
                    } else {
                        val reservations = task?.toObjects(Reservation::class.java)
                        trySend(Result.success(reservations ?: emptyList()))
                    }
                }
        } catch (e: Exception) {
            crashlytics.recordException(e)
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun deleteReservation(reservation: Reservation): Flow<Result<Boolean>> = callbackFlow {
        try {
            if(reservation.mainReservation){
                deleteMainReservation(reservation)
            } else {
                deleteSecondaryReservation(reservation)
            }
            deleteMainReservation(reservation)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    private suspend fun ProducerScope<Result<Boolean>>.deleteSecondaryReservation(reservation: Reservation) {
        val batch = firestore.batch()
        val reservationRef = firestore.collection(RESERVATION_COLLECTION).document(reservation.id)
        batch.delete(reservationRef)
        var mainReservation = firestore.collection(RESERVATION_COLLECTION).document(reservation.mainReservationId).get().await().toObject(Reservation::class.java)
        mainReservation = mainReservation?.copy(
            numberOfRides = mainReservation.numberOfRides - 1,
            totalRidesPrice = mainReservation.totalRidesPrice - reservation.tourismRidePrice
        )
        batch.set(firestore.collection(RESERVATION_COLLECTION).document(reservation.mainReservationId), mainReservation!!)
        val secondaryReservations = firestore.collection(RESERVATION_COLLECTION).whereEqualTo("mainReservationId", reservation.mainReservationId).get().await().toObjects(Reservation::class.java)
        secondaryReservations.forEach {
            batch.set(
                firestore.collection(RESERVATION_COLLECTION).document(it.id),
                it.copy(
                    totalRidesPrice = mainReservation.totalRidesPrice,
                    numberOfRides = mainReservation.numberOfRides
                )
            )
        }
        batch.commit().addOnSuccessListener {
                trySend(Result.success(true))
            }.addOnFailureListener { e ->
                crashlytics.recordException(e)
                trySend(Result.failure(e))
            }
    }

    private suspend fun ProducerScope<Result<Boolean>>.deleteMainReservation(reservation: Reservation) {
        val batch = firestore.batch()
        val mainReservationRef = firestore.collection(RESERVATION_COLLECTION).document(reservation.id)
        batch.delete(mainReservationRef)
        val secondaryReservations = firestore.collection(RESERVATION_COLLECTION).whereEqualTo("mainReservationId", reservation.id).get().await().toObjects(Reservation::class.java)
        secondaryReservations.forEach { reservation ->
            batch.delete(firestore.collection(RESERVATION_COLLECTION).document(reservation.id))
        }
        batch.commit().addOnSuccessListener {
                trySend(Result.success(true))
            }.addOnFailureListener { e ->
                crashlytics.recordException(e)
                trySend(Result.failure(e))
            }
    }

    override fun updateReservation(
        reservationId: String,
        updates: Map<String, Any>
    ): Flow<Result<Boolean>> = callbackFlow {
        try {
            firestore.collection(RESERVATION_COLLECTION).document(reservationId).update(updates)
                .addOnSuccessListener {
                    trySend(Result.success(true))
                }.addOnFailureListener {e ->
                    crashlytics.recordException(e)
                    trySend(Result.failure(e))
                }
        } catch (e: Exception) {
            crashlytics.recordException(e)
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun updateReservation(
        reservation: Reservation
    ): Flow<Result<Boolean>> = callbackFlow {
        try {
            if(reservation.mainReservation){
                updateMainReservation(reservation)
            } else {
                updateSecondaryReservation(reservation)
            }
        } catch (e: Exception) {
            crashlytics.recordException(e)
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    private suspend fun ProducerScope<Result<Boolean>>.updateSecondaryReservation(
        reservation: Reservation
    ) {
        val batch = firestore.batch()
        val reservationRef = firestore.collection(RESERVATION_COLLECTION).document(reservation.id)
        val oldReservation = reservationRef.get().await().toObject(Reservation::class.java)
        if(reservation.tourismRidePrice != oldReservation?.tourismRidePrice){
            val mainReservationRef = firestore.collection(RESERVATION_COLLECTION).document(reservation.mainReservationId)
            var mainReservation = mainReservationRef.get().await().toObject(Reservation::class.java)
            mainReservation = mainReservation?.copy(
                totalRidesPrice = mainReservation.totalRidesPrice - oldReservation!!.tourismRidePrice + reservation.tourismRidePrice
            )
            batch.set(mainReservationRef, mainReservation!!)
            val secondaryReservations = firestore.collection(RESERVATION_COLLECTION).whereEqualTo("mainReservationId", reservation.mainReservationId).get().await().toObjects(Reservation::class.java)
            secondaryReservations.forEach {
                if(it.id != reservation.id){
                    batch.set(
                        firestore.collection(RESERVATION_COLLECTION).document(it.id),
                        it.copy(
                            totalRidesPrice = mainReservation.totalRidesPrice
                        )
                    )
                }
            }
            batch.set(reservationRef, reservation.copy(totalRidesPrice = mainReservation.totalRidesPrice))
        } else {
            batch.set(reservationRef, reservation)
        }
        batch.commit().addOnSuccessListener {
                trySend(Result.success(true))
            }.addOnFailureListener { e ->
                crashlytics.recordException(e)
                trySend(Result.failure(e))
            }
    }

    private suspend fun ProducerScope<Result<Boolean>>.updateMainReservation(
        reservation: Reservation
    ) {
        val batch = firestore.batch()
        val mainReservationRef = firestore.collection(RESERVATION_COLLECTION).document(reservation.id)
        val oldReservation = mainReservationRef.get().await().toObject(Reservation::class.java)
        if(reservation.tourismRidePrice != oldReservation?.tourismRidePrice || reservation.tourismCompanyId != oldReservation.tourismCompanyId){
            val secondaryReservations = firestore.collection(RESERVATION_COLLECTION).whereEqualTo("mainReservationId", reservation.id).get().await().toObjects(Reservation::class.java)
            val newReservation: Reservation = reservation.copy(
                totalRidesPrice = oldReservation!!.totalRidesPrice - oldReservation.tourismRidePrice + reservation.tourismRidePrice,
            )
            secondaryReservations.forEach {
                batch.set(
                    firestore.collection(RESERVATION_COLLECTION).document(it.id),
                    it.copy(
                        totalRidesPrice = newReservation.totalRidesPrice,
                        tourismCompany = reservation.tourismCompany,
                        tourismCompanyId = reservation.tourismCompanyId,
                        tourismCompanyPhone = reservation.tourismCompanyPhone,
                        tourismEmployee = reservation.tourismEmployee,
                        tourismEmployeeId = reservation.tourismEmployeeId,
                        tourismEmployeePhone = reservation.tourismEmployeePhone,
                    )
                )
            }
            batch.set(mainReservationRef, newReservation)
        } else {
            batch.set(mainReservationRef, reservation)
        }
        batch.commit().addOnSuccessListener {
                trySend(Result.success(true))
            }.addOnFailureListener { e ->
                crashlytics.recordException(e)
                trySend(Result.failure(e))
            }
    }

    override fun getReservationsByCompanyId(companyId: String): Flow<Result<List<Reservation>>> =
        callbackFlow {
            try {
                firestore.collection(RESERVATION_COLLECTION)
                    .where(
                        Filter.or(
                            Filter.equalTo("tourismCompanyId", companyId),
                            Filter.equalTo("travelCompanyId", companyId)
                        )
                    )
                    .addSnapshotListener { data, error ->
                        if (error != null) {
                            crashlytics.recordException(error)
                            trySend(Result.failure(error))
                        } else {
                            val reservations =
                                data?.toObjects(Reservation::class.java) ?: emptyList()
                            trySend(Result.success(reservations))

                        }
                    }
            } catch (e: Exception) {
                crashlytics.recordException(e)
                trySend(Result.failure(e))
            }
            awaitClose { }
        }

    override fun getArchivedReservations(): Flow<Result<List<Reservation>>> = callbackFlow {
        try {
            firestore.collection(RESERVATION_COLLECTION)
                .whereEqualTo("archived", true)
                .addSnapshotListener { task, error ->
                    if (error != null) {
                        crashlytics.recordException(error)
                        trySend(Result.failure(error))
                    } else {
                        val reservations = task?.toObjects(Reservation::class.java)
                        trySend(Result.success(reservations ?: emptyList()))
                    }
                }
        } catch (e: Exception) {
            crashlytics.recordException(e)
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun fetchReportReservations(report: Report): Flow<Result<List<Reservation>>> =
        callbackFlow {
            try {
                var query: Query = firestore.collection(RESERVATION_COLLECTION)
                if (report.company.id.isNotBlank()) {
                    if (report.company.type == CompanyType.TOURISM) {
                        query = query.whereEqualTo("tourismCompanyId", report.company.id)
                    } else {
                        query = query.whereEqualTo("travelCompanyId", report.company.id)
                    }
                }
                if (report.car.isNotBlank()) {
                    query = query.whereEqualTo("car", report.car)
                }
                if (report.fromEpochSeconds != 0L) {
                    query = query.whereGreaterThanOrEqualTo("date", report.fromEpochSeconds)
                }
                if (report.toEpochSeconds != 0L) {
                    query = query.whereLessThanOrEqualTo("date", report.toEpochSeconds)
                }
                query.addSnapshotListener { task, error ->
                    if (error != null) {
                        crashlytics.recordException(error)
                        trySend(Result.failure(error))
                    } else {
                        val reservations = task?.toObjects(Reservation::class.java)
                        trySend(Result.success(reservations ?: emptyList()))
                    }
                }
            } catch (e: Exception) {
                crashlytics.recordException(e)
                trySend(Result.failure(e))
            }
            awaitClose { }
        }

    override fun fetchCompanyOpenAccount(report: Report): Flow<Result<List<CompanyHistory>>> =
        callbackFlow {
            try {
                var query: Query = firestore.collection(RESERVATION_COLLECTION)

                if(report.companyType == CompanyType.TRAVEL){
                    query = query.whereNotEqualTo("travelCompanyId", "")
                }else{
                    query = query.whereNotEqualTo("tourismCompanyId", "")
                }
                if (report.fromEpochSeconds != 0L) {
                    query = query.whereGreaterThanOrEqualTo("date", report.fromEpochSeconds)
                }
                if (report.toEpochSeconds != 0L) {
                    query = query.whereLessThanOrEqualTo("date", report.toEpochSeconds)
                }
                val result1 = query.get().await()

                var query2:Query = firestore.collection(COMPANY_PAYMENT_COLLECTION)
                if (report.fromEpochSeconds != 0L) {
                    query2 = query2.whereGreaterThanOrEqualTo("createdAtEpochSeconds", report.fromEpochSeconds)
                }
                if (report.toEpochSeconds != 0L) {
                    query2 = query2.whereLessThanOrEqualTo("createdAtEpochSeconds", report.toEpochSeconds)
                }
                val result2 = query2.get().await()
                val query3 = firestore.collection(COMPANY_COLLECTION)
                    .whereEqualTo("type", report.companyType?.name?:"")
                    .get().await()
                val reservations = result1?.toObjects(Reservation::class.java)?.groupBy{
                    if(report.companyType == CompanyType.TRAVEL){
                        it.travelCompanyId
                    }else{
                        it.tourismCompanyId
                    }
                }?: emptyMap()
                val payments = result2?.toObjects(CompanyPayment::class.java)?.groupBy {
                    it.companyId
                }?: emptyMap()
                val companies = query3?.toObjects(Company::class.java)?: emptyList()

                val result = convertToCompanyHistoryList(reservations, payments, companies)
                    .filter {
                        it.reservations.isNotEmpty() || it.payments.isNotEmpty()
                    }
                Log.d("fetchCompanyOpenAccount", "fetchCompanyOpenAccountc: ${report.companyType}$companies")
                Log.d("fetchCompanyOpenAccount", "fetchCompanyOpenAccountr: $reservations")
                Log.d("fetchCompanyOpenAccount", "fetchCompanyOpenAccountp: $payments")
                trySend(Result.success(result))
            } catch (e: Exception) {
                crashlytics.recordException(e)
                trySend(Result.failure(e))
            }
            awaitClose { }
        }
}
