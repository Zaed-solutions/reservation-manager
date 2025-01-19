package com.zaed.reservationmanager.ui.company.display

import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyWithBalance

data class CompaniesUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val tourismCompanies: List<CompanyWithBalance> = emptyList(),
    val displayTourismCompanies: List<CompanyWithBalance> = emptyList(),
    val travelCompanies: List<CompanyWithBalance> = emptyList(),
    val displayTravelCompanies: List<CompanyWithBalance> = emptyList(),
)
