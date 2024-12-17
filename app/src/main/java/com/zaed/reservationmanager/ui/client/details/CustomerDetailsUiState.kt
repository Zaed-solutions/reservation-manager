package com.zaed.reservationmanager.ui.client.details

import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.Ride

data class CustomerDetailsUiState (
    val customer: Customer = Customer(),
    val rides: List<Ride> = emptyList(),
    val reservations: List<Reservation> = emptyList()
)