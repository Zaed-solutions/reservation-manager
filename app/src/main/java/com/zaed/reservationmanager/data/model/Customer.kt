package com.zaed.reservationmanager.data.model

import java.util.Date

data class Customer(
    val id: String = "",
    val name: String = "",
    val nationality: String = "",
    val residenceCountry: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val createdAt: Date = Date(),
)
