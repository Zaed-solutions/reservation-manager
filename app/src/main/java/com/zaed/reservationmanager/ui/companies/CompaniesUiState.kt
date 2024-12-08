package com.zaed.reservationmanager.ui.companies

import com.zaed.reservationmanager.data.model.Company

data class CompaniesUiState(
    val isLoading: Boolean = true,
    val tourismCompanies: List<Company> = emptyList(),
    val travelCompanies: List<Company> = emptyList(),
)
