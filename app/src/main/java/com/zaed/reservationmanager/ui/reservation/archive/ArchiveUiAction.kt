package com.zaed.reservationmanager.ui.reservation.archive

sealed interface ArchiveUiAction {
    data object ShowNavDrawer : ArchiveUiAction
    data class UnarchiveReservation(val reservationId: String): ArchiveUiAction
    data class DeleteReservation(val reservationId: String): ArchiveUiAction
    data class CopyPhoneNumber(val phoneNumber: String): ArchiveUiAction
    data class MessagePhoneNumber(val phoneNumber: String): ArchiveUiAction
}