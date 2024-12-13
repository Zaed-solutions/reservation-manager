package com.zaed.reservationmanager.data.model

data class Ride(
    val id: String = "",
    val reservationId: String = "",
    val reservationNumber: Long = 0L,
    val date: Long = 0L,
    val type: String = "",
    val startLocation: String = "",
    val endLocation: String = "",
    val travelCompanyId: String = "",
    val travelCompany: String = "",
    val travelCompanyPhone: String = "",
    val driver: String = "",
    val driverPhoneNumber: String = "",
    val driverId: String = "",
    val car: String = "",
    val buyingPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val collectedPrice: Double = 0.0,
    val note: String = "",
    val sentDriverInfoToCustomer: Boolean = false,
    val sentToDriverCompany: Boolean = false,
    )