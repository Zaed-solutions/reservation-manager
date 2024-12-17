package com.zaed.reservationmanager.ui.client.details

import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Reservation

sealed interface CustomerDetailsUiAction {
    data class OnMessagePhone(val phoneNumber: String) : CustomerDetailsUiAction
    data class OnCopyPhone(val phoneNumber: String) : CustomerDetailsUiAction
    data class OnDeleteRide(val rideId: String) : CustomerDetailsUiAction
    data class OnDeleteReservation(val reservationId: String) : CustomerDetailsUiAction
    data object OnBackPressed: CustomerDetailsUiAction
    data class OnCompanyClicked(val companyId: String, val companyType: CompanyType): CustomerDetailsUiAction
    data class OnReservationDetailsClicked(val reservationId: String): CustomerDetailsUiAction
    data class OnEditReservation(val reservation: Reservation): CustomerDetailsUiAction
    data object OnAddReservation: CustomerDetailsUiAction
    data class OnEmployeeClicked(val employeeId: String, val isDriver: Boolean): CustomerDetailsUiAction
}