package com.zaed.reservationmanager.ui.reservation.create

import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.Reservation

data class CreateReservationUiState(
    val reservations: List<Reservation> = emptyList(),
    val isNewCustomer: Boolean? = null,
    val customer: Customer = Customer(),
    val travelCompanies: List<Company> = emptyList(),//
    val tourismCompanies: List<Company> = emptyList(), //
    val reservationTypes: List<String> = emptyList(),
    val carTypes: List<String> = emptyList(),
    val drivers: List<Employee> = emptyList(),
    val countries: List<String> = emptyList(),
    val employees: List<Employee> = emptyList(),
    val isLoading: Boolean = false,
    val isFinished: Boolean = false,
    val reservationError: ReservationError = ReservationError.NONE
)
