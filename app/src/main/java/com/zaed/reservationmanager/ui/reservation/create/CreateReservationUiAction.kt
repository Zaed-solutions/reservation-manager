package com.zaed.reservationmanager.ui.reservation.create

import com.zaed.reservationmanager.data.model.Reservation

sealed interface CreateReservationUiAction {
    data object OnBackPressed : CreateReservationUiAction
    data object SaveReservations : CreateReservationUiAction
    data class DeleteReservation(val reservationId: String) : CreateReservationUiAction
    data object SearchCustomer : CreateReservationUiAction
    data class AddReservation(val reservation: Reservation) : CreateReservationUiAction
    data class UpdateCustomerName(val name: String) : CreateReservationUiAction
    data class UpdateCustomerPhone(val phone: String) : CreateReservationUiAction
    data class UpdateCustomerPhone2(val phone: String) : CreateReservationUiAction
    data class UpdateCustomerEmail(val email: String) : CreateReservationUiAction
    data class UpdateCustomerCountry(val country: String) : CreateReservationUiAction
    data class UpdateCustomerNationality(val nationality: String) : CreateReservationUiAction
    data class FetchEmployees(val companyId: String) : CreateReservationUiAction
    data class FetchDrivers(val companyId: String) : CreateReservationUiAction
}