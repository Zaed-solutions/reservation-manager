package com.zaed.reservationmanager.ui.reservation.display

sealed interface DisplayReservationUIAction{
    data class OnDeleteRide(val rideId:String): DisplayReservationUIAction
    data class OnDriverInfoSent(val rideId:String): DisplayReservationUIAction
    data class OnInfoSentToTravelCompany(val rideId:String): DisplayReservationUIAction
    data class OnDeleteReservation(val reservationId:String): DisplayReservationUIAction



}