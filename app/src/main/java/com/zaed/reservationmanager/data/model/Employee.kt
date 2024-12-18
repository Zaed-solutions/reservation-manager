package com.zaed.reservationmanager.data.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Employee(
    val id: String = "",
    val name: String = "",
    val company: String = "",
    val position: String = "",
    val phoneNumber1: String = "",
    val phoneNumber2: String = "",
    val nationality: String = "",
    val email: String = "",
    val createdAtEpochSeconds: Long = Clock.System.now().epochSeconds,
)

