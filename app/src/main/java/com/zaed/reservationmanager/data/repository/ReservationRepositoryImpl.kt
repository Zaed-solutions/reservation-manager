package com.zaed.reservationmanager.data.repository

import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.Ride
import com.zaed.reservationmanager.data.source.remote.ReservationRemoteDataSource
import kotlinx.coroutines.flow.Flow

class ReservationRepositoryImpl(
    private val remoteDataSource: ReservationRemoteDataSource
) : ReservationRepository {
    override fun createReservation(reservation: Reservation): Flow<Result<String>> {
        return remoteDataSource.createReservation(reservation)
    }

    override fun getReservationById(id: String): Flow<Result<Reservation>> {
        return remoteDataSource.getReservationById(id)
    }

    override fun createRide(ride: Ride): Flow<Result<String>> {
        return remoteDataSource.createRide(ride)
    }

    override fun getRidesByReservationId(id: String): Flow<Result<List<Ride>>> {
        return remoteDataSource.getRidesByReservationId(id)
    }
}