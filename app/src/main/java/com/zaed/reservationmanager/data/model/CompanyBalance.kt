package com.zaed.reservationmanager.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CompanyBalance(
    val totalRidePrice: Double = 0.0,
    val totalPayment: Double = 0.0,
    val totalCollected: Double = 0.0,
)
