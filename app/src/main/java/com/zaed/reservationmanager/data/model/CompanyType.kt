package com.zaed.reservationmanager.data.model

import androidx.annotation.StringRes
import com.zaed.reservationmanager.R

enum class CompanyType(@StringRes val displayNameRes: Int) {
    TOURISM(R.string.tourism),
    TRAVEL(R.string.travel),
//    TRAVEL_AND_TOURISM(R.string.travel_and_tourism),
}