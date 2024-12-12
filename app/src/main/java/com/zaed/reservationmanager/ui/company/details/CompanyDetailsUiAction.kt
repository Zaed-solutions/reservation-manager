package com.zaed.reservationmanager.ui.company.details

sealed interface CompanyDetailsUiAction {
    data object OnBackPressed: CompanyDetailsUiAction
    data class OnCopyPhoneNumber(val phoneNumber: String): CompanyDetailsUiAction
    data class OnMessagePhoneNumber(val phoneNumber: String): CompanyDetailsUiAction
    data class OnDeleteRide(val rideId: String): CompanyDetailsUiAction
    data class OnDriverClicked(val driverId: String): CompanyDetailsUiAction
    data class OnCompanyClicked(val companyId: String): CompanyDetailsUiAction
}