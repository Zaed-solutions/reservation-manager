package com.zaed.reservationmanager.data.source.remote

import com.google.firebase.firestore.FirebaseFirestore
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
    override fun createReservation(reservation: Reservation): Flow<Result<String>> = callbackFlow {
        val reservationRef = firestore.collection(RESERVATION_COLLECTION).document()
        reservationRef.set(reservation.copy(id = reservationRef.id)).addOnSuccessListener {
            trySend(Result.success(reservationRef.id))
        }.addOnFailureListener {
            trySend(Result.failure(it))
        }
        awaitClose { }
    }

    override fun getReservationById(id: String): Flow<Result<Reservation>> {
        TODO("Not yet implemented")
    }

    override fun createRide(ride: Ride): Flow<Result<String>> = callbackFlow {
        val rideRef = firestore
            .collection(RESERVATION_COLLECTION)
            .document(ride.reservationId)
            .collection(RIDE_COLLECTION)
            .document()
        rideRef.set(ride.copy(id = rideRef.id)).addOnSuccessListener {
            trySend(Result.success(rideRef.id))
        }.addOnFailureListener {
            trySend(Result.failure(it))
        }
        awaitClose { }
    }



    override fun getRidesByReservationId(id: String): Flow<Result<List<Ride>>> = callbackFlow {
        firestore
            .collection(RESERVATION_COLLECTION)
            .document(id)
            .collection(RIDE_COLLECTION)
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
        awaitClose { }
    }
    override fun deleteReservation(id: String): Flow<Result<Boolean>>  = callbackFlow {
        firestore.collection(RESERVATION_COLLECTION).document(id).delete().addOnSuccessListener {
            trySend(Result.success(true))
        }.addOnFailureListener {
            trySend(Result.failure(it))
        }
        awaitClose { }
    }
}