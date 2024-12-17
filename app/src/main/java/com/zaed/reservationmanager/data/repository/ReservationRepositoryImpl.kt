package com.zaed.reservationmanager.data.repository

import com.zaed.reservationmanager.data.model.CompanyBalance
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.ReservationModel
import com.zaed.reservationmanager.data.source.remote.ReservationRemoteDataSource
import kotlinx.coroutines.flow.Flow

class ReservationRepositoryImpl(
    private val remoteDataSource: ReservationRemoteDataSource
) : ReservationRepository {
    override fun createReservation(reservation: ReservationModel): Flow<Result<Pair<String,Long>>> {
        return remoteDataSource.createReservation(reservation)
    }

    override fun getReservationById(id: String): Flow<Result<ReservationModel>> {
        return remoteDataSource.getReservationById(id)
    }

    override fun getReservationByCustomerId(customerId: String): Flow<Result<List<ReservationModel>>> {
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

    override fun updateReservation(reservation: ReservationModel): Flow<Result<Boolean>> {
        return remoteDataSource.updateReservation(reservation)
    }


    override fun getReservations(): Flow<Result<List<ReservationModel>>> {
        return remoteDataSource.getReservations()
    }

    override suspend fun getCompanyBalance(
        companyId: String,
        companyType: CompanyType
    ): Result<CompanyBalance> {
        return remoteDataSource.getCompanyBalance(companyId)
    }

    override fun getReservationsByCompanyId(companyId: String): Flow<Result<List<ReservationModel>>> {
        return remoteDataSource.getReservationsByCompanyId(companyId)
    }
}