package com.zaed.reservationmanager.data.model

data class Reservation (
    val id: String = "",
    val flightNumber : String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val date: Long = 0L,
    val clientCountry: String = "",
    val tourismCompany: String = "",
    val tourismCompanyPhone: String = "",
    val tourismEmployee: String = "",
    val tourismEmployeePhone: String = "",
    val sentConfirmToCustomer: Boolean = false,
)

