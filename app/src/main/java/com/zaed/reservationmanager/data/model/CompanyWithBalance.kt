package com.zaed.reservationmanager.data.model

data class CompanyWithBalance (
    val company: Company = Company(),
    val balance: Int = 0
)