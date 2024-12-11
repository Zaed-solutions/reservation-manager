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

data class Movement(
    val id: String = "",
    val reservationId: String = "",
    val date: Long = 0L,
    val type: String = "",
    val car: String = "",
    val travelCompanyPhone: String = "",
    val driver: String = "",
    val travelCompany: String = "",
    val startLocation: String = "",
    val endLocation: String = "",
    val buyingPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val collectedPrice: Double = 0.0,
    val note: String = "",
    val sentDriverInfoToCustomer: Boolean = false,
    val sentToDriverCompany: Boolean = false,
    )
