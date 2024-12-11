package com.zaed.reservationmanager.ui.company.display

import com.zaed.reservationmanager.data.model.Company

sealed interface CompaniesUiAction {
    data object OnShowNavDrawer : CompaniesUiAction
    data object OnAddCompanyClicked : CompaniesUiAction
    data class OnCompanyDetailsClicked(val companyId: String): CompaniesUiAction
    data class OnDeleteCompanyConfirmed(val companyId: String): CompaniesUiAction
    data class OnEditCompanyClicked(val company: Company): CompaniesUiAction
}