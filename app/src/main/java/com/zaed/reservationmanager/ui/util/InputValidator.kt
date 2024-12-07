package com.zaed.reservationmanager.ui.util

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
}