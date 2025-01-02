package com.zaed.reservationmanager.data.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: String = "",
    val name: String = "",
    val nationality: String = "",
    val residenceCountry: String = "",
    val city :String ="",
    val phoneNumber1: String = "",
    val phoneNumber2: String = "",
    val email: String = "",
    val createdAtEpochSeconds: Long = Clock.System.now().epochSeconds,
)
