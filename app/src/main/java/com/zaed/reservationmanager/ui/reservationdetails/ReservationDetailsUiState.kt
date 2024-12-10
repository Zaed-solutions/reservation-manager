package com.zaed.reservationmanager.ui.reservationdetails

import com.zaed.reservationmanager.data.model.Reservation

data class ReservationDetailsUiState(
    val reservation: Reservation = Reservation(),
)
