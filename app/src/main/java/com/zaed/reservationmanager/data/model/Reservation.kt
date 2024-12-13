package com.zaed.reservationmanager.data.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Reservation (
    val id: String = "",
    val reservationNumber: Long = 0,
    val flightNumber : String = "",
    val clientId: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val date: Long = Clock.System.now().epochSeconds,
    val clientCountry: String = "",
    val tourismCompanyId: String = "",
    val tourismCompany: String = "",
    val tourismCompanyPhone: String = "",
    val tourismEmployeeId: String = "",
    val tourismEmployee: String = "",
    val tourismEmployeePhone: String = "",
    val sentConfirmToCustomer: Boolean = false,
)

