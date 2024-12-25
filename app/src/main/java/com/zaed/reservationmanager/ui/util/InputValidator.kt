package com.zaed.reservationmanager.ui.util

import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Message
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.messages.components.MessageError
import com.zaed.reservationmanager.ui.reservation.create.ReservationError
import kotlinx.datetime.Clock

object InputValidator {
    fun isEmailValid(email: String): Boolean {
        val emailRegex = Regex(
        """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$"""
        )
        return emailRegex.matches(email)
    }
    fun Customer.validate():Boolean {
        return if(name.isBlank()) {

            false
        } else if (email.isNotBlank() && !isEmailValid(email)) {
            false
        }else if (phoneNumber.isBlank() || !isPhoneNumberValid(phoneNumber)) {
            false
        }else if(residenceCountry.isBlank()) {
            false
        }else {
            true
        }
    }

    fun isPhoneNumberValid(phoneNumber: String): Boolean {
        val regex = Regex("^\\+\\d{8,}$")
        return regex.matches(phoneNumber)
    }

    fun isFaxNumberValid(faxNumber: String): Boolean {
        val regex = Regex("^(\\+?\\d+(\\s?|-?)\\d*(\\s?|-?)\\(?\\d{2,}\\)?(\\s?|-?)\\d{3,}\\s?\\d{3,})$")
        return regex.matches(faxNumber)
    }
    fun validateRide(reservation: Reservation): ReservationError? {
        return if (reservation.date == 0L) {
            ReservationError.DATE_IS_REQUIRED
        }  else if (reservation.type.isBlank()) {
            ReservationError.TYPE_IS_REQUIRED
        } else if (reservation.startLocation.isBlank()) {
            ReservationError.START_LOCATION_IS_REQUIRED
        } else if (reservation.endLocation.isBlank()) {
            ReservationError.END_LOCATION_IS_REQUIRED
        } else if (reservation.sellingPrice == 0) {
            ReservationError.SELLING_PRICE_IS_REQUIRED
        } else {
            null
        }
    }

    fun validateMessage(message: Message): MessageError? {
        return if(message.title.isBlank()){
            MessageError.TITLE_IS_REQUIRED
        } else if( message.message.isBlank()){
            MessageError.MESSAGE_IS_REQUIRED
        } else {
            null
        }
    }
}