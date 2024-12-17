package com.zaed.reservationmanager.ui.client.details

import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.ReservationModel

data class CustomerDetailsUiState (
    val customer: Customer = Customer(),
    val reservations: List<ReservationModel> = emptyList(),
    val reservationTypes: List<String> = emptyList(),
    val cars: List<String> = emptyList(),
    val travelCompanies: List<Company> = emptyList(),
    val drivers: List<Employee> = emptyList(),
    val tourismCompanies: List<Company> = emptyList(),
    val employees: List<Employee> = emptyList(),

    )