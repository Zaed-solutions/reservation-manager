package com.zaed.reservationmanager.data.model

import java.util.Date

data class Employee(
    val id: String = "",
    val name: String = "",
    val company: String = "",
    val position: String = "",
    val phoneNumber1: String = "",
    val phoneNumber2: String = "",
    val nationality: String = "",
    val email: String = "",
    val createdAt: Date = Date(),
)
