package com.zaed.reservationmanager.data.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: String = "",
    val name: String = "",
    val nationality: String = "",
    val residenceCountry: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val createdAtEpochSeconds: Long = Clock.System.now().epochSeconds,
)
