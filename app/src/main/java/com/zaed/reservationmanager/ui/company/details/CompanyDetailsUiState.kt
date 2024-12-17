package com.zaed.reservationmanager.ui.company.details

import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyBalance
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.ReservationModel

data class CompanyDetailsUiState(
    val company: Company = Company(),
    val balance: CompanyBalance = CompanyBalance(),
    val reservations: List<ReservationModel> = emptyList(),
    val reservationTypes: List<String> = emptyList(),
    val cars: List<String> = emptyList(),
    val travelCompanies: List<Company> = emptyList(),
    val drivers: List<Employee> = emptyList(),
    val tourismCompanies: List<Company> = emptyList(),
    val employees: List<Employee> = emptyList(),
)
