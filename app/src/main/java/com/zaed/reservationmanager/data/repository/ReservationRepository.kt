package com.zaed.reservationmanager.data.repository

import com.zaed.reservationmanager.data.model.CompanyBalance
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.Ride
import kotlinx.coroutines.flow.Flow

interface ReservationRepository {
    fun createReservation(reservation: Reservation): Flow<Result<Pair<String,Long>>>
    fun getReservationById(id: String): Flow<Result<Reservation>>
    fun createRide(ride: Ride): Flow<Result<String>>
    fun getRidesByReservationId(id: String): Flow<Result<List<Ride>>>
    fun deleteReservation(id: String): Flow<Result<Boolean>>
    fun getReservations(): Flow<Result<List<Reservation>>>
    fun getRides(): Flow<Result<List<Ride>>>
    fun deleteRide(id: String): Flow<Result<Boolean>>
    fun updateReservation(reservationId: String, updates: Map<String, Any>): Flow<Result<Boolean>>
    fun updateReservation(reservation: Reservation): Flow<Result<Boolean>>
    fun updateRide(rideId: String, updates: Map<String, Any>): Flow<Result<Boolean>>
    fun updateRide(ride: Ride): Flow<Result<Boolean>>
    fun getCompanyBalance(companyId: String, companyType: CompanyType): Flow<Result<CompanyBalance>>
    fun getReservationsByCompanyId(companyId: String): Flow<Result<List<Reservation>>>
    fun getRidesByCompanyId(companyId: String, companyType: CompanyType): Flow<Result<List<Ride>>>
    fun getRidesByCustomerId(customerId: String): Flow<Result<List<Ride>>>
}
