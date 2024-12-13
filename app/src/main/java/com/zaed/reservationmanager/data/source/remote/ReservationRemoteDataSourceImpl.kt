package com.zaed.reservationmanager.data.source.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.zaed.reservationmanager.data.model.CompanyBalance
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.Ride
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ReservationRemoteDataSourceImpl(
    private val firestore: FirebaseFirestore
) : ReservationRemoteDataSource {
    val RESERVATION_COLLECTION = "reservations"
    val RIDE_COLLECTION = "rides"
    override fun createReservation(reservation: Reservation): Flow<Result<Pair<String,Long>>> = callbackFlow {
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
                        doc.documents[0].get("reservationNumber") as Long ?: 0
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
                        trySend(Result.success(reservationRef.id to reservationNumber))
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

    override fun createRide(ride: Ride): Flow<Result<String>> = callbackFlow {
        try {
            val rideRef = firestore
                .collection(RIDE_COLLECTION)
                .document()
            rideRef.set(ride.copy(id = rideRef.id)).addOnSuccessListener {
                trySend(Result.success(rideRef.id))
            }.addOnFailureListener {
                trySend(Result.failure(it))
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun getReservations(): Flow<Result<List<Reservation>>> = callbackFlow {
        try {
            firestore.collection(RESERVATION_COLLECTION)
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

    override fun getRides(): Flow<Result<List<Ride>>> = callbackFlow {
        try {
            firestore.collection(RIDE_COLLECTION)
                .addSnapshotListener { tasks, error ->
                    if (error != null) {
                        trySend(Result.failure(error))
                    } else {
                        val rides = tasks?.toObjects(Ride::class.java)
                        trySend(Result.success(rides ?: emptyList()))
                    }
                }
        }catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { }
    }


    override fun getRidesByReservationId(id: String): Flow<Result<List<Ride>>> = callbackFlow {
        try {
            firestore
                .collection(RIDE_COLLECTION)
                .whereEqualTo("reservationId", id)
                .get().addOnSuccessListener { tasks ->
                    if (tasks.isEmpty) {
                        trySend(Result.success(emptyList()))
                    } else {
                        val rides = tasks.toObjects(Ride::class.java)
                        trySend(Result.success(rides))
                    }
                }.addOnFailureListener {
                    trySend(Result.failure(it))
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

    override fun deleteRide(id: String): Flow<Result<Boolean>> = callbackFlow {
        try {
            firestore.collection(RIDE_COLLECTION).document(id).delete().addOnSuccessListener {
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

    override fun updateRide(rideId: String, updates: Map<String, Any>): Flow<Result<Boolean>> = callbackFlow {
        try {
            firestore.collection(RIDE_COLLECTION).document(rideId).update(updates).addOnSuccessListener {
                trySend(Result.success(true))
            }.addOnFailureListener {
                trySend(Result.failure(it))
            }
        } catch (e: Exception){
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun getCompanyBalance(
        companyId: String,
        isTravel: Boolean
    ): Flow<Result<CompanyBalance>> = if(isTravel) getTravelCompanyBalance(companyId) else getTourismCompanyBalance(companyId)

    override fun getReservationsByCompanyId(companyId: String): Flow<Result<List<Reservation>>> = callbackFlow {
        try {
            firestore.collection(RESERVATION_COLLECTION).whereEqualTo("tourismCompanyId", companyId).get().addOnSuccessListener { data ->
                if (data.isEmpty) {
                    trySend(Result.success(emptyList()))
                } else {
                    val reservations = data.toObjects(Reservation::class.java)
                    trySend(Result.success(reservations))
                }
            }.addOnFailureListener {
                trySend(Result.failure(it))
            }
        } catch (e: Exception){
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    override fun getRidesByCompanyId(companyId: String): Flow<Result<List<Ride>>> = callbackFlow {
        try {
            firestore.collection(RIDE_COLLECTION).whereEqualTo("travelCompanyId", companyId).get().addOnSuccessListener { data ->
                if (data.isEmpty) {
                    trySend(Result.success(emptyList()))
                } else {
                    val rides = data.toObjects(Ride::class.java)
                    trySend(Result.success(rides))
                }
            }.addOnFailureListener {
                trySend(Result.failure(it))
            }
        } catch (e: Exception){
            trySend(Result.failure(e))
        }
        awaitClose { }
    }

    private fun getTravelCompanyBalance(companyId: String): Flow<Result<CompanyBalance>> = callbackFlow {
        try {
            firestore.collection(RIDE_COLLECTION).whereEqualTo("companyId", companyId).get().addOnSuccessListener { data ->
                if (data.isEmpty) {
                    trySend(Result.success(CompanyBalance()))
                } else {
                    val rides = data.toObjects(Ride::class.java)
                    var totalBuying: Double = 0.0
                    var totalSelling: Double = 0.0
                    var totalCollected: Double = 0.0
                    rides.forEach {
                        totalBuying += it.buyingPrice
                        totalSelling += it.sellingPrice
                        totalCollected += it.collectedPrice
                    }
                    trySend(Result.success(CompanyBalance(totalBuying = totalBuying, totalSelling = totalSelling, totalCollected = totalCollected)))
                }
            }.addOnFailureListener {
                trySend(Result.failure(it))
            }
        } catch (e: Exception){
            trySend(Result.failure(e))
        }
        awaitClose { }
    }
    private fun getTourismCompanyBalance(companyId: String): Flow<Result<CompanyBalance>> = callbackFlow {
        try {
            TODO("Not yet implemented")
        } catch (e: Exception){
            trySend(Result.failure(e))
        }
        awaitClose { }
    }
}