package com.zaed.reservationmanager.ui.client.details

import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Reservation

sealed interface CustomerDetailsUiAction {
    data class OnMessagePhone(val phoneNumber: String) : CustomerDetailsUiAction
    data class OnCopyPhone(val phoneNumber: String) : CustomerDetailsUiAction
    data class OnDeleteReservation(val reservationId: String) : CustomerDetailsUiAction
    data class OnFetchDrivers(val companyId: String) : CustomerDetailsUiAction
    data class OnFetchEmployees(val companyId: String) : CustomerDetailsUiAction
    data object OnBackPressed : CustomerDetailsUiAction
    data class OnCompanyClicked(val companyId: String, val companyType: CompanyType) :
        CustomerDetailsUiAction

    data class OnAddReservation(val reservation: Reservation) : CustomerDetailsUiAction
    data class OnUpdateReservation(val reservation: Reservation) : CustomerDetailsUiAction
    data class SendReservationConfirmation(val reservationId: String) : CustomerDetailsUiAction
    data class ReservationConfirmationSent(val reservationId: String) : CustomerDetailsUiAction
    data class SendReservationInfo(val reservationId: String) : CustomerDetailsUiAction
    data class ReservationInfoSent(val reservationId: String) : CustomerDetailsUiAction
    data class ArchiveReservation(val reservationId: String) : CustomerDetailsUiAction
    data class SendReservationInfoToTravelCompany(val reservationId: String) :
        CustomerDetailsUiAction

    data class ReservationInfoToTravelCompanySent(val reservationId: String) :
        CustomerDetailsUiAction
}