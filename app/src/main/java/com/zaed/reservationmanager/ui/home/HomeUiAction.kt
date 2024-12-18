package com.zaed.reservationmanager.ui.home

import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.ReservationModel
import com.zaed.reservationmanager.ui.home.component.TimeFilter

sealed interface HomeUiAction {
    data object ShowNavDrawer: HomeUiAction
    data object ExportReservationsAsCsv: HomeUiAction
    data object ExportCustomersAsCsv: HomeUiAction
    data object AddReservation: HomeUiAction
    data object AddCustomer: HomeUiAction
    data class OnDriverInfoSent(val reservationId: String) : HomeUiAction
    data class OnConfirmationSentToClient(val reservationId: String) : HomeUiAction
    data class OnInfoSentToTravelCompany(val reservationId: String) : HomeUiAction
    data class OnDeleteReservation(val reservationId: String) : HomeUiAction
    data class OnDeleteCustomer(val customerId: String) : HomeUiAction
    data class UpdateSearchQuery(val query: String) : HomeUiAction
    data class UpdateTimeFilter(val timeFilter: TimeFilter): HomeUiAction
    data class UpdateCountryFilter(val countryFilter: String): HomeUiAction
    data class OnViewCustomerDetails(val customerId: String): HomeUiAction
    data class OnEditCustomerClicked(val customer: Customer): HomeUiAction
    data class OnCompanyClicked(val companyId: String, val companyType: CompanyType): HomeUiAction
    data class OnCopyPhoneNumber(val phoneNumber: String): HomeUiAction
    data class OnMessagePhoneNumber(val phoneNumber: String): HomeUiAction
    data class SendDriverInfoToClient(val reservationId: String): HomeUiAction
    data class SendConfirmationToClient(val reservationId: String): HomeUiAction
    data class SendReservationInfoToTravelCompany(val reservationId: String): HomeUiAction
    data class FetchEmployees(val companyId: String): HomeUiAction
    data class FetchDrivers(val companyId: String): HomeUiAction
    data class UpdateReservation(val reservation: ReservationModel): HomeUiAction
}