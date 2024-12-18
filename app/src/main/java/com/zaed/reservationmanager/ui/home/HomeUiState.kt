package com.zaed.reservationmanager.ui.home

import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.ReservationModel
import com.zaed.reservationmanager.ui.home.component.TimeFilter

data class HomeUiState(
    val isLoading: Boolean = false,
    val reservations: List<ReservationModel> = emptyList(),
    val displayedReservations: List<ReservationModel> = emptyList(),
    val searchQuery: String = "",
    val customers: List<Customer> = emptyList(),
    val selectedCountry: String = "",
    val displayedCustomers: List<Customer> = emptyList(),
    val countries: List<String> = emptyList(),
    val errorMessage: String = "",
    val loading: Boolean = false,
    val timeFilter: TimeFilter = TimeFilter.All,
    val reservationTypes: List<String> = emptyList(),
    val cars: List<String> = emptyList(),
    val travelCompanies: List<Company> = emptyList(),
    val drivers: List<Employee> = emptyList(),
    val tourismCompanies: List<Company> = emptyList(),
    val employees: List<Employee> = emptyList(),
)
