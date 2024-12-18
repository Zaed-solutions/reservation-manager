package com.zaed.reservationmanager.ui.company.add

import com.zaed.reservationmanager.data.model.Company

data class AddCompanyUiState(
    val isNew: Boolean = true,
    val company: Company = Company(),
    val countryList: List<String> = emptyList(),
    val error: AddCompanyUiError = AddCompanyUiError.NONE,
    val isFinished: Boolean = false
)
