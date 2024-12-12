package com.zaed.reservationmanager.data.repository

import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.Ride
import kotlinx.coroutines.flow.Flow

interface ReservationRepository {
    fun createReservation(reservation: Reservation): Flow<Result<String>>
    fun getReservationById(id: String): Flow<Result<Reservation>>
    fun createRide(ride: Ride): Flow<Result<String>>
    fun getRidesByReservationId(id: String): Flow<Result<List<Ride>>>
    fun deleteReservation(id: String): Flow<Result<Boolean>>
    fun deleteRide(id: String): Flow<Result<Boolean>>
    fun updateReservation(reservationId: String, updates: Map<String, Any>): Flow<Result<Boolean>>
    fun updateRide(rideId: String, updates: Map<String, Any>): Flow<Result<Boolean>>
}
