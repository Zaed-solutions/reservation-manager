package com.zaed.reservationmanager.data.model

data class Reservation (
    val id: String = "",
    val flightNumber : String = "",
    val clientId: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val date: Long = 0L,
    val clientCountry: String = "",
    val tourismCompanyId: String = "",
    val tourismCompany: String = "",
    val tourismCompanyPhone: String = "",
    val tourismEmployeeId: String = "",
    val tourismEmployee: String = "",
    val tourismEmployeePhone: String = "",
    val sentConfirmToCustomer: Boolean = false,
)

