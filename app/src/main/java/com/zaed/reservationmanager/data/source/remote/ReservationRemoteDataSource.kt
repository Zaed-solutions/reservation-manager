package com.zaed.reservationmanager.data.source.remote

import com.zaed.reservationmanager.data.model.Reservation
import kotlinx.coroutines.flow.Flow

interface ReservationRemoteDataSource {
    fun createReservation(reservation: Reservation): Flow<Result<Pair<String, Long>>>
    fun createReservations(reservations: List<Reservation>): Flow<Result<Unit>>
    fun getReservationById(id: String): Flow<Result<Reservation>>
    fun getReservationsByCustomerId(customerId: String): Flow<Result<List<Reservation>>>
    fun deleteReservation(id: String): Flow<Result<Boolean>>
    fun getReservations(): Flow<Result<List<Reservation>>>
    fun updateReservation(reservationId: String, updates: Map<String, Any>): Flow<Result<Boolean>>
    fun updateReservation(reservation: Reservation): Flow<Result<Boolean>>
    fun getReservationsByCompanyId(companyId: String): Flow<Result<List<Reservation>>>
    fun getArchivedReservations(): Flow<Result<List<Reservation>>>
}
