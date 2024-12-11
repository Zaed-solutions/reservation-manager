package com.zaed.reservationmanager.ui.reservation.create

import androidx.annotation.StringRes
import com.zaed.reservationmanager.R

enum class ReservationError(@StringRes val messageRes: Int) {
    NONE(0),
    CUSTOMER_PHONE_IS_REQUIRED(R.string.phone_is_required),
    TOURISM_COMPANY_IS_REQUIRED(R.string.tourism_company_is_required),
    TYPE_IS_REQUIRED (R.string.type_is_required),
    CAR_IS_REQUIRED(R.string.car_is_required),
    MOVEMENT_PRICE_IS_REQUIRED(R.string.movement_price_is_required),
    COLLECTION_PRICE_IS_REQUIRED(R.string.collection_price_is_required),

}