package com.zaed.reservationmanager.ui.company.details

import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyBalance
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.data.model.Ride

data class CompanyDetailsUiState(
    val company: Company = Company(),
    val balance: CompanyBalance = CompanyBalance(),
    val reservations: List<Reservation> = emptyList(),
    val rides: List<Ride> = emptyList(),
)
