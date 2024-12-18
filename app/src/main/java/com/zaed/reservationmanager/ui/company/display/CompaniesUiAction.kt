package com.zaed.reservationmanager.ui.company.display

import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.CompanyType

sealed interface CompaniesUiAction {
    data object OnShowNavDrawer : CompaniesUiAction
    data object OnAddCompanyClicked : CompaniesUiAction
    data class OnCompanyDetailsClicked(val companyId: String, val companyType: CompanyType) :
        CompaniesUiAction

    data class OnDeleteCompanyConfirmed(val companyId: String) : CompaniesUiAction
    data class OnEditCompanyClicked(val company: Company) : CompaniesUiAction
}