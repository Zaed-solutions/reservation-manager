package com.zaed.reservationmanager.data.repository

import com.zaed.reservationmanager.data.model.CompanyBalance
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.Ride
import com.zaed.reservationmanager.data.source.remote.ReservationRemoteDataSource
import kotlinx.coroutines.flow.Flow

class ReservationRepositoryImpl(
    private val remoteDataSource: ReservationRemoteDataSource
) : ReservationRepository {
    override fun createReservation(reservation: Reservation): Flow<Result<Pair<String,Long>>> {
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

    override fun deleteReservation(id: String): Flow<Result<Boolean>> {
        return remoteDataSource.deleteReservation(id)
    }

    override fun deleteRide(id: String): Flow<Result<Boolean>> {
        return remoteDataSource.deleteRide(id)
    }

    override fun updateReservation(
        reservationId: String,
        updates: Map<String, Any>
    ): Flow<Result<Boolean>> {
        return remoteDataSource.updateReservation(reservationId, updates)
    }

    override fun updateRide(rideId: String, updates: Map<String, Any>): Flow<Result<Boolean>> {
        return remoteDataSource.updateRide(rideId, updates)
    }

    override fun getReservations(): Flow<Result<List<Reservation>>> {
        return remoteDataSource.getReservations()
    }

    override fun getRides(): Flow<Result<List<Ride>>> {
        return  remoteDataSource.getRides()
    }

    override fun getCompanyBalance(
        companyId: String,
        isTravel: Boolean
    ): Flow<Result<CompanyBalance>> {
        return remoteDataSource.getCompanyBalance(companyId, isTravel)
    }

    override fun getReservationsByCompanyId(companyId: String): Flow<Result<List<Reservation>>> {
        return remoteDataSource.getReservationsByCompanyId(companyId)
    }

    override fun getRidesByCompanyId(companyId: String): Flow<Result<List<Ride>>> {
        return remoteDataSource.getRidesByCompanyId(companyId)
    }
}