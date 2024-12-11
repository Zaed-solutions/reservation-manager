package com.zaed.reservationmanager.ui.reservation.create

import com.zaed.reservationmanager.data.model.Company
import com.zaed.reservationmanager.data.model.Employee

sealed class ReservationUiAction {
    data class UpdateCustomerNumber(val number: String) : ReservationUiAction()
    data class UpdateSelectedTourismCompany(val company: Company) : ReservationUiAction()
    data class UpdateSelectedTravelCompany(val company: Company) : ReservationUiAction()
    data class UpdateTourismEmployee(val employee: Employee) : ReservationUiAction()
    data object AddReservation : ReservationUiAction()
    data object Cancel : ReservationUiAction()
    data class UpdateReservationDate(val date: Long?) : ReservationUiAction()
    data class UpdateReservationTime(val time: Long?) : ReservationUiAction()
    data class UpdateDriver(val driver: Employee) : ReservationUiAction()
    data class UpdateReservationType(val type: String) : ReservationUiAction()
    data class UpdateReservationCar(val car: String) : ReservationUiAction()
    data class UpdateCustomerCountry(val country: String) : ReservationUiAction()
    data class UpdateStartLocation(val location: String) : ReservationUiAction()
    data class UpdateEndLocation(val location: String) : ReservationUiAction()
    data class UpdateCollectionPrice(val price: String) : ReservationUiAction()
    data class UpdateMovementPrice(val price: String) : ReservationUiAction()
    data class UpdateNote(val note: String) : ReservationUiAction()
    data class UpdateCustomerName(val name: String) : ReservationUiAction()
    data class UpdateTravelNumber(val number: String) : ReservationUiAction()
    data object SearchClient  : ReservationUiAction()
    data object AddMovement  : ReservationUiAction()

}