package com.zaed.reservationmanager.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CompanyPayment(
    val id: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val createdAtEpochSeconds: Long = 0,
    val companyId: String = "",
    val companyName: String = "",
)
