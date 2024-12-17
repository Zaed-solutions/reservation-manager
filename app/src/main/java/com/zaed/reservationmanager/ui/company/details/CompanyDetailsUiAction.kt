package com.zaed.reservationmanager.ui.company.details

import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.ReservationModel

sealed interface CompanyDetailsUiAction {
    data object OnBackPressed: CompanyDetailsUiAction
    data class OnCopyPhoneNumber(val phoneNumber: String): CompanyDetailsUiAction
    data class OnMessagePhoneNumber(val phoneNumber: String): CompanyDetailsUiAction
    data class OnDeleteReservation(val reservationId: String): CompanyDetailsUiAction
    data class OnEditReservation(val reservation: ReservationModel): CompanyDetailsUiAction
    data class OnFetchEmployees(val companyId: String): CompanyDetailsUiAction
    data class OnFetchDrivers(val companyId: String): CompanyDetailsUiAction
    data object ExportReservationsAsCSV : CompanyDetailsUiAction
    data class OnCompanyClicked(val companyId: String, val type: CompanyType): CompanyDetailsUiAction
}