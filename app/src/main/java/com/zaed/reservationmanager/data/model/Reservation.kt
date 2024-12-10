package com.zaed.reservationmanager.data.model

data class Reservation(
    val id: String = "",
    val travelNumber : String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val clientCountry: String = "",
    val tourismCompany: String = "",
    val tourismEmployee: String = "",
    val sentConfirmToCustomer: Boolean = false,
)

data class Movement(
    val reservationId: String = "",
    val date: Long = 0L,
    val time: Long = 0L,
    val type: String = "",
    val car: String = "",
    val driverCompanyPhone: String = "",
    val driver: String = "",
    val travelCompany: String = "",
    val startLocation: String = "",
    val endLocation: String = "",
    val buyingPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val collectionPrice: Double = 0.0,
    val note: String = "",
    val sentDriverInfoToCustomer: Boolean = false,
    val sentToDriverCompany: Boolean = false,
    )
