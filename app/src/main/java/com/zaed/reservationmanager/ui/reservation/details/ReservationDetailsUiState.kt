package com.zaed.reservationmanager.ui.reservation.details

import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.Ride

data class ReservationDetailsUiState(
    val reservation: Reservation = Reservation(),
    val rides: List<Ride> = emptyList()
)
