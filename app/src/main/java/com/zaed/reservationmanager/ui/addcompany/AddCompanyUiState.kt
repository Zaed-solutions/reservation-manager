package com.zaed.reservationmanager.ui.addcompany

import com.zaed.reservationmanager.data.model.Company

data class AddCompanyUiState(
    val isNew: Boolean = true,
    val company: Company = Company(),
    val error: AddCompanyUiError = AddCompanyUiError.NONE,
    val isFinished: Boolean = false
)
