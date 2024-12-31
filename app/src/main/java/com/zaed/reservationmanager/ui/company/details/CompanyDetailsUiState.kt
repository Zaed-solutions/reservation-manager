package com.zaed.reservationmanager.ui.company.details

import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyBalance
import com.zaed.reservationmanager.data.model.CompanyPayment
import com.zaed.reservationmanager.data.model.Employee
import com.zaed.reservationmanager.data.model.Reservation

data class CompanyDetailsUiState(
    val company: Company = Company(),
    val balance: CompanyBalance = CompanyBalance(),
    val payments: List<CompanyPayment> = emptyList(),
    val reservations: List<Reservation> = emptyList(),
    val reservationTypes: List<String> = emptyList(),
    val cars: List<String> = emptyList(),
    val travelCompanies: List<Company> = emptyList(),
    val drivers: List<Employee> = emptyList(),
    val tourismCompanies: List<Company> = emptyList(),
    val employees: List<Employee> = emptyList(),
)
