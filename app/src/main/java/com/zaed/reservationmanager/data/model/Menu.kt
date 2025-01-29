package com.zaed.reservationmanager.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Menu(
    val data : List<String> = emptyList(),
)
