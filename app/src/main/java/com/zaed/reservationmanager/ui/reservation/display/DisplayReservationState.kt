package com.zaed.reservationmanager.ui.reservation.display

import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.Ride

data class DisplayReservationState(
    val rides: List<Ride> = emptyList(),
    val reservations: List<Reservation> = emptyList()
)
