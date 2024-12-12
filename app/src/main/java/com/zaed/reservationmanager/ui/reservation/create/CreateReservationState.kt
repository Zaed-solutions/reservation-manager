package com.zaed.reservationmanager.ui.reservation.create

import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.Ride
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.client.create.countriesList

data class CreateReservationState(
    val reservation: Reservation = Reservation(),
    val rides : List<Ride> = emptyList(),
    val date: Long = 0L,
    val time : Long = 0L,
    val customer: Customer = Customer(),
    val newRide: Ride = Ride(),
    val travelCompanies: List<Company> = emptyList(),//
    val tourismCompanies: List<Company> = emptyList(), //
    val transactionTypes: List<String> = reservationTypesList,
    val carTypes: List<String> = carTypesList,
    val drivers: List<Employee> = emptyList(),
    val countries: List<String> = countriesList,
    val employees: List<Employee> = emptyList(),
    val isFieldsEnabled: Boolean = false,
    val loading: Boolean = false,
    val successStatus: Boolean = false,
    val userMessage: String = "",
    val rideError: ReservationError = ReservationError.NONE,
    val reservationError: ReservationError = ReservationError.NONE
)
val reservationTypesList = listOf(
    "Reception",
    "Departure",
    "Mecca Attractions",
    "Medina Attractions",
    "Taif Tour",
    "Stationed"
)
val carTypesList = listOf(
    "Small",
    "Carnival",
    "Family",
    "H1",
    "Staria",
    "GMC",
    "Hiace",
    "Coaster",
    "Bus"
)