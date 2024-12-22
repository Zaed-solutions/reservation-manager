package com.zaed.reservationmanager.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Reservation(
    val id: String = "",
    val reservationNumber: Long = 0,
    val clientId: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val clientCountry: String = "",
    val date: Long = 0L,
    val time: Long = 0L,
    val type: String = "",
    val startLocation: String = "",
    val endLocation: String = "",
    val tourismCompanyId: String = "",
    val tourismCompany: String = "",
    val tourismCompanyPhone: String = "",
    val tourismEmployeeId: String = "",
    val tourismEmployee: String = "",
    val tourismEmployeePhone: String = "",
    val travelCompanyId: String = "",
    val travelCompany: String = "",
    val travelCompanyPhone: String = "",
    val driver: String = "",
    val driverPhoneNumber: String = "",
    val driverId: String = "",
    val car: String = "",
    val peopleCount: Int = 1,
    val carCount: Int = 1,
    val buyingPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val collectedAmount: Double = 0.0,
    val note: String = "",
    val sentConfirmToCustomer: Boolean = false,
    val sentDriverInfoToCustomer: Boolean = false,
    val sentToDriverCompany: Boolean = false,
    val archived: Boolean = false
)