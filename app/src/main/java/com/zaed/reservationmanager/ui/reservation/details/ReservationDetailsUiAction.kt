package com.zaed.reservationmanager.ui.reservation.details

import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Ride

sealed interface ReservationDetailsUiAction {
    data object OnBackPressed: ReservationDetailsUiAction
    data class OnClientClicked(val clientId: String): ReservationDetailsUiAction
    data class OnCopyPhoneNumber(val phoneNumber: String): ReservationDetailsUiAction
    data class OnMessagePhoneNumber(val phoneNumber: String): ReservationDetailsUiAction
    data class OnEmployeeClicked(val employeeId: String, val isDriver: Boolean = false): ReservationDetailsUiAction
    data class OnCompanyClicked(val companyId: String, val companyType: CompanyType): ReservationDetailsUiAction
    data object OnSendConfirmationMessage: ReservationDetailsUiAction
    data class OnDeleteRide(val rideId: String): ReservationDetailsUiAction
    data class OnAddRide(val ride: Ride): ReservationDetailsUiAction
    data class OnSendDriverInfoToCustomer(val rideId: String, val driverName: String, val driverPhoneNumber: String): ReservationDetailsUiAction
    data class OnSendInfoToTravelCompany(val ride: Ride): ReservationDetailsUiAction
    data class UpdateDrivers(val companyId: String): ReservationDetailsUiAction
    data object OnConfirmationMessageSent: ReservationDetailsUiAction
    data class OnDriverInfoSent(val rideId: String): ReservationDetailsUiAction
    data class OnInfoSentToTravelCompany(val rideId: String): ReservationDetailsUiAction
}