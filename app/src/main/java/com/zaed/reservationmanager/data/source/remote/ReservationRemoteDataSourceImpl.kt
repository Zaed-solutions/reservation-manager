package com.zaed.reservationmanager.data.source.remote

import android.util.Log
import com.google.firebase.firestore.AggregateField
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.zaed.reservationmanager.data.model.CompanyBalance
import com.zaed.reservationmanager.data.model.Reservation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReservationRemoteDataSourceImpl(
    private val firestore: FirebaseFirestore
) : ReservationRemoteDataSource {
    private val TAG = "ReservationRemoteDataSource"
    private val RESERVATION_COLLECTION = "reservations"
    override fun createReservation(reservation: Reservation): Flow<Result<Pair<String, Long>>> =
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
                        val reservationNumber = if (!doc.isEmpty) {
                            doc.documents[0].get("reservationNumber") as Long
                        } else {
                            0
                        }
                        val reservationRef = firestore.collection(RESERVATION_COLLECTION).document()
                        reservationRef.set(
                            reservation.copy(
                                id = reservationRef.id,
                                reservationNumber = reservationNumber + 1
                            )
                        ).addOnSuccessListener {
                            trySend(Result.success(reservationRef.id to (reservationNumber + 1)))
                        }.addOnFailureListener {
                            trySend(Result.failure(it))
                        }
                    }
            } catch (e: Exception) {
                trySend(Result.failure(e))
            }
            awaitClose { }
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
                        }.addOnFailureListener {
                            trySend(Result.failure(it))
                        }
                    }
            } catch (e: Exception) {
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
                }.addOnFailureListener {
                    trySend(Result.failure(it))
                }
        } catch (e: Exception) {
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
                            trySend(Result.failure(error))
                        } else {
                            val reservations =
                                value?.toObjects(Reservation::class.java) ?: emptyList()
                            trySend(Result.success(reservations))
                        }
                    }
            } catch (e: Exception) {
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
                        trySend(Result.failure(error))
                    } else {
                        val reservations = task?.toObjects(Reservation::class.java)
                        trySend(Result.success(reservations ?: emptyList()))
                    }
                }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun deleteReservation(id: String): Flow<Result<Boolean>> = callbackFlow {
        try {
            firestore.collection(RESERVATION_COLLECTION).document(id).delete()
                .addOnSuccessListener {
                    trySend(Result.success(true))
                }.addOnFailureListener {
                    trySend(Result.failure(it))
                }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun updateReservation(
        reservationId: String,
        updates: Map<String, Any>
    ): Flow<Result<Boolean>> = callbackFlow {
        try {
            firestore.collection(RESERVATION_COLLECTION).document(reservationId).update(updates)
                .addOnSuccessListener {
                    trySend(Result.success(true))
                }.addOnFailureListener {
                    trySend(Result.failure(it))
                }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun updateReservation(
        reservation: Reservation
    ): Flow<Result<Boolean>> = callbackFlow {
        try {
            firestore.collection(RESERVATION_COLLECTION).document(reservation.id).set(reservation)
                .addOnSuccessListener {
                    trySend(Result.success(true))
                }.addOnFailureListener {
                    trySend(Result.failure(it))
                }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
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
                            trySend(Result.failure(error))
                        } else {
                            val reservations =
                                data?.toObjects(Reservation::class.java) ?: emptyList()
                            trySend(Result.success(reservations))

                        }
                    }
            } catch (e: Exception) {
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
                        trySend(Result.failure(error))
                    } else {
                        val reservations = task?.toObjects(Reservation::class.java)
                        trySend(Result.success(reservations ?: emptyList()))
                    }
                }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override suspend fun getCompanyBalance(
        companyId: String
    ): Result<CompanyBalance> =
        try {
            val query = firestore.collection(RESERVATION_COLLECTION).where(
                Filter.or(
                    Filter.equalTo("tourismCompanyId", companyId),
                    Filter.equalTo("travelCompanyId", companyId)
                )
            )
            val totalBuyingResult = query.aggregate(AggregateField.sum("buyingPrice"))
                .get(AggregateSource.SERVER).await()
            val totalSellingResult = query.aggregate(AggregateField.sum("sellingPrice"))
                .get(AggregateSource.SERVER).await()
            val totalCollectedResult = query.aggregate(AggregateField.sum("collectedAmount"))
                .get(AggregateSource.SERVER).await()

            val totalBuying =
                (totalBuyingResult.get(AggregateField.sum("buyingPrice")) as? Double) ?: 0.0
            val totalSelling =
                (totalSellingResult.get(AggregateField.sum("sellingPrice")) as? Double) ?: 0.0
            val totalCollected =
                (totalCollectedResult.get(AggregateField.sum("collectedAmount")) as? Double)
                    ?: 0.0
            Log.d(
                "CompanyBalance",
                "getCompanyBalance: $companyId $totalBuying $totalSelling $totalCollected"
            )
            Result.success(
                CompanyBalance(
                    totalBuying = totalBuying,
                    totalSelling = totalSelling,
                    totalCollected = totalCollected
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
}
