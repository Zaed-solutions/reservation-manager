package com.zaed.reservationmanager.ui.reservationdetails

sealed interface ReservationDetailsUiAction {
    data object OnBackPressed: ReservationDetailsUiAction
    data object OnClientClicked: ReservationDetailsUiAction
}