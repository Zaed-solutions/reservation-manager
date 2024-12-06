package com.zaed.reservationmanager.data.model

import java.util.Date

data class Company(
    val id: String = "",
    val name: String = "",
    val country: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val faxNumber: String = "",
    val createdAt: Date = Date(),
    val type: CompanyType = CompanyType.TOURISM,
)

enum class CompanyType {
    TOURISM,
    TRAVEL,
}
