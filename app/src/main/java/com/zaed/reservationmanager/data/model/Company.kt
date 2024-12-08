package com.zaed.reservationmanager.data.model

import androidx.annotation.StringRes
import com.zaed.reservationmanager.R
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Company(
    val id: String = "",
    val name: String = "",
    val country: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val faxNumber: String = "",
    val createdAtEpochSeconds: Long = Clock.System.now().epochSeconds,
    val type: CompanyType = CompanyType.TOURISM,
)

enum class CompanyType(@StringRes val displayNameRes: Int) {
    TOURISM(R.string.tourism),
    TRAVEL(R.string.travel),
}
