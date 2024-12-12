package com.zaed.reservationmanager.data.source.remote

import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.Ride
import kotlinx.coroutines.flow.Flow

interface ReservationRemoteDataSource {
    fun createReservation(reservation: Reservation): Flow<Result<String>>
    fun getReservationById(id: String): Flow<Result<Reservation>>
    fun createRide(ride: Ride): Flow<Result<String>>
    fun getRidesByReservationId(id: String):Flow<Result<List<Ride>>>
    fun deleteReservation(id: String): Flow<Result<Boolean>>
}
