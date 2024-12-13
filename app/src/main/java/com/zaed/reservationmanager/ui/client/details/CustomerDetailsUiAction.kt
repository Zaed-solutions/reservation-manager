package com.zaed.reservationmanager.ui.client.details

sealed interface CustomerDetailsUiAction {
    data class OnMessagePhone(val phoneNumber: String) : CustomerDetailsUiAction
    data class OnCopyPhone(val phoneNumber: String) : CustomerDetailsUiAction
    data class OnDeleteRide(val rideId: String) : CustomerDetailsUiAction
    data object OnBackPressed: CustomerDetailsUiAction
    data class OnCompanyClicked(val companyId: String): CustomerDetailsUiAction
    data class OnReservationDetailsClicked(val reservationId: String): CustomerDetailsUiAction
}