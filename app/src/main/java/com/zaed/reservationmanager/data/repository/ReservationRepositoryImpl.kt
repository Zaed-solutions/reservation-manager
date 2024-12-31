package com.zaed.reservationmanager.data.repository

import com.zaed.reservationmanager.data.model.CompanyBalance
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.source.remote.ReservationRemoteDataSource
import kotlinx.coroutines.flow.Flow

class ReservationRepositoryImpl(
    private val remoteDataSource: ReservationRemoteDataSource
) : ReservationRepository {
    override fun createReservation(reservation: Reservation): Flow<Result<Pair<String, Long>>> {
        return remoteDataSource.createReservation(reservation)
    }

    override fun createReservations(reservations: List<Reservation>): Flow<Result<Unit>> {
        return remoteDataSource.createReservations(reservations)
    }

    override fun getReservationById(id: String): Flow<Result<Reservation>> {
        return remoteDataSource.getReservationById(id)
    }

    override fun getReservationByCustomerId(customerId: String): Flow<Result<List<Reservation>>> {
        return remoteDataSource.getReservationsByCustomerId(customerId)
    }

    override fun deleteReservation(id: String): Flow<Result<Boolean>> {
        return remoteDataSource.deleteReservation(id)
    }

    override fun updateReservation(
        reservationId: String,
        updates: Map<String, Any>
    ): Flow<Result<Boolean>> {
        return remoteDataSource.updateReservation(reservationId, updates)
    }

    override fun updateReservation(reservation: Reservation): Flow<Result<Boolean>> {
        return remoteDataSource.updateReservation(reservation)
    }


    override fun getReservations(): Flow<Result<List<Reservation>>> {
        return remoteDataSource.getReservations()
    }

    override fun getReservationsByCompanyId(companyId: String): Flow<Result<List<Reservation>>> {
        return remoteDataSource.getReservationsByCompanyId(companyId)
    }

    override fun getArchivedReservations(): Flow<Result<List<Reservation>>> {
        return remoteDataSource.getArchivedReservations()
    }
}