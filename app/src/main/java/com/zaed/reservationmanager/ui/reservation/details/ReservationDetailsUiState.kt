package com.zaed.reservationmanager.ui.reservation.details

import com.zaed.reservationmanager.data.model.Reservation

data class ReservationDetailsUiState(
    val reservation: Reservation = Reservation(),
)
