package com.zaed.reservationmanager.ui.reservation.create

import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Movement
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.client.create.countriesList

data class CreateReservationState(
    val reservation: Reservation = Reservation(),
    val movements : List<Movement> = emptyList(),
    val newMovement: Movement = Movement(),
    val travelCompanies: List<String> = emptyList(),//
    val tourismCompanies: List<String> = emptyList(), //
    val transactionTypes: List<String> = reservationTypesList,
    val carTypes: List<String> = carTypesList,
    val drivers: List<String> = emptyList(),
    val countries: List<String> = countriesList,
    val employees: List<String> = emptyList(),
    val customer: Customer = Customer(),
    val selectedTravelCompany: String = "",
    val selectedTourismCompany: String = "",
    val isFieldsEnabled: Boolean = false,
    val loading: Boolean = false,
    val successStatus: Boolean = false,
    val userMessage: String = "",
    val errorMessage: ReservationError = ReservationError.NONE
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