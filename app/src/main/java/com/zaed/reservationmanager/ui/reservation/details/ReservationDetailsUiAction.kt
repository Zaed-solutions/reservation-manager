package com.zaed.reservationmanager.ui.reservation.details

sealed interface ReservationDetailsUiAction {
    data object OnBackPressed: ReservationDetailsUiAction
    data object OnClientClicked: ReservationDetailsUiAction
}