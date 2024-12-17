package com.zaed.reservationmanager.ui.util

import com.zaed.reservationmanager.data.model.Ride
import com.zaed.reservationmanager.ui.reservation.create.ReservationError
import kotlinx.coroutines.flow.update

object InputValidator {
    fun isEmailValid(email: String): Boolean {
        val emailRegex = Regex(
        """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$"""
        )
        return emailRegex.matches(email)
    }

    fun isPhoneNumberValid(phoneNumber: String): Boolean {
        val regex = Regex("^[+](?:[0-9\\-()/.]s?){6,15}[0-9]$")
        return regex.matches(phoneNumber)
    }

    fun isFaxNumberValid(faxNumber: String): Boolean {
        val regex = Regex("^(\\+?\\d+(\\s?|-?)\\d*(\\s?|-?)\\(?\\d{2,}\\)?(\\s?|-?)\\d{3,}\\s?\\d{3,})$")
        return regex.matches(faxNumber)
    }
    fun validateRide(ride: Ride): ReservationError? {
        if (ride.date == 0L) {
            return ReservationError.DATE_IS_REQUIRED
        }  else if (ride.type.isBlank()) {
            return ReservationError.TYPE_IS_REQUIRED
        } else if (ride.car.isBlank()) {
            return ReservationError.CAR_IS_REQUIRED
        } else if (ride.startLocation.isBlank()) {
            return ReservationError.START_LOCATION_IS_REQUIRED
        } else if (ride.endLocation.isBlank()) {
            return ReservationError.END_LOCATION_IS_REQUIRED
        } else if (ride.sellingPrice == 0.0) {
            return ReservationError.SELLING_PRICE_IS_REQUIRED
        } else {
            return null
        }
    }
}