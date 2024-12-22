package com.zaed.reservationmanager.ui.company.details

import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Reservation

sealed interface CompanyDetailsUiAction {
    data object OnBackPressed : CompanyDetailsUiAction
    data class OnCopyPhoneNumber(val phoneNumber: String) : CompanyDetailsUiAction
    data class OnMessagePhoneNumber(val phoneNumber: String) : CompanyDetailsUiAction
    data class OnDeleteReservation(val reservationId: String) : CompanyDetailsUiAction
    data class OnEditReservation(val reservation: Reservation, val onSuccess: () -> Unit) : CompanyDetailsUiAction
    data class OnFetchEmployees(val companyId: String) : CompanyDetailsUiAction
    data class OnFetchDrivers(val companyId: String) : CompanyDetailsUiAction
    data object ExportReservationsAsCSV : CompanyDetailsUiAction
    data class OnCompanyClicked(val companyId: String, val type: CompanyType) :
        CompanyDetailsUiAction

    data class SendReservationConfirmation(val reservationId: String) : CompanyDetailsUiAction
    data class FetchCustomerForUpdating(val customerId: String, val onSuccess: (Customer) -> Unit = {}) : CompanyDetailsUiAction
    data class ReservationConfirmationSent(val reservationId: String) : CompanyDetailsUiAction
    data class SendReservationInfo(val reservationId: String) : CompanyDetailsUiAction
    data class ReservationInfoSent(val reservationId: String) : CompanyDetailsUiAction
    data class SendReservationInfoToTravelCompany(val reservationId: String) :
        CompanyDetailsUiAction

    data class ReservationInfoToTravelCompanySent(val reservationId: String) :
        CompanyDetailsUiAction
    data class ArchiveReservation(val reservationId: String) :
        CompanyDetailsUiAction
}