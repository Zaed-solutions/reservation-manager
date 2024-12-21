package com.zaed.reservationmanager.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val title: String = "",
    val message: String = "",
    val id: String = ""
)
