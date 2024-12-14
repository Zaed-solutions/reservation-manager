package com.zaed.reservationmanager.ui.reservation.details

import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.Ride
import com.zaed.reservationmanager.ui.reservation.create.ReservationError


data class ReservationDetailsUiState(
    val reservation: Reservation = Reservation(),
    val rides: List<Ride> = emptyList(),
    val types: List<String> = emptyList(),
    val cars: List<String> = emptyList(),
    val travelCompanies: List<Company> = emptyList(),
    val drivers: List<Employee> = emptyList(),
)
