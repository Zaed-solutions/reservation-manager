package com.zaed.reservationmanager.ui.client.details

import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.ReservationModel

sealed interface CustomerDetailsUiAction {
    data class OnMessagePhone(val phoneNumber: String) : CustomerDetailsUiAction
    data class OnCopyPhone(val phoneNumber: String) : CustomerDetailsUiAction
    data class OnDeleteReservation(val reservationId: String) : CustomerDetailsUiAction
    data class OnFetchDrivers(val companyId: String) : CustomerDetailsUiAction
    data class OnFetchEmployees(val companyId: String) : CustomerDetailsUiAction
    data object OnBackPressed: CustomerDetailsUiAction
    data class OnCompanyClicked(val companyId: String, val companyType: CompanyType): CustomerDetailsUiAction
    data class OnAddReservation(val reservation: ReservationModel): CustomerDetailsUiAction
    data class OnUpdateReservation(val reservation: ReservationModel): CustomerDetailsUiAction
}