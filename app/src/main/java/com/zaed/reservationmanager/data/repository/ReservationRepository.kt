package com.zaed.reservationmanager.data.repository

import com.zaed.reservationmanager.data.model.CompanyBalance
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.ReservationModel
import kotlinx.coroutines.flow.Flow

interface ReservationRepository {
    fun createReservation(reservation: ReservationModel): Flow<Result<Pair<String,Long>>>
    fun getReservationById(id: String): Flow<Result<ReservationModel>>
    fun getReservationByCustomerId(customerId: String): Flow<Result<List<ReservationModel>>>
    fun deleteReservation(id: String): Flow<Result<Boolean>>
    fun getReservations(): Flow<Result<List<ReservationModel>>>
    fun updateReservation(reservationId: String, updates: Map<String, Any>): Flow<Result<Boolean>>
    fun updateReservation(reservation: ReservationModel): Flow<Result<Boolean>>
    suspend fun getCompanyBalance(companyId: String, companyType: CompanyType): Result<CompanyBalance>
    fun getReservationsByCompanyId(companyId: String): Flow<Result<List<ReservationModel>>>
}
