package com.zaed.reservationmanager.ui.reservation.archive

import com.zaed.reservationmanager.data.model.Reservation

sealed interface ArchiveUiAction {
    data object ShowNavDrawer : ArchiveUiAction
    data class UnarchiveReservation(val reservationId: String) : ArchiveUiAction
    data class DeleteReservation(val reservation: Reservation) : ArchiveUiAction
    data class CopyPhoneNumber(val phoneNumber: String) : ArchiveUiAction
    data class MessagePhoneNumber(val phoneNumber: String) : ArchiveUiAction
    data class OnFetchEmployees(val companyId: String) : ArchiveUiAction
    data class OnFetchDrivers(val companyId: String) : ArchiveUiAction
    data class OnEditReservation(val reservation: Reservation, val onSuccess: () -> Unit) :
        ArchiveUiAction
    data class OnAddReservation(val reservation: Reservation, val onSuccess: () -> Unit) : ArchiveUiAction
    data class SendDriverInfoToClient(val reservationId: String): ArchiveUiAction
    data class SendConfirmationToClient(val reservationId: String) : ArchiveUiAction
    data class SendReservationInfoToTravelCompany(val reservationId: String) : ArchiveUiAction
    data class SendThanksMessageToCustomer(val reservationId: String) : ArchiveUiAction
    data class ThanksMessageSent(val reservationId: String) : ArchiveUiAction
    data class OnDriverInfoSent(val reservationId: String) : ArchiveUiAction
    data class OnConfirmationSentToClient(val reservationId: String) :ArchiveUiAction
    data class OnInfoSentToTravelCompany(val reservationId: String) :ArchiveUiAction
    data class OnViewCustomerDetails(val customerId: String) : ArchiveUiAction
}