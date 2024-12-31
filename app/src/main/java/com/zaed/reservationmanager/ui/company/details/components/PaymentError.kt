package com.zaed.reservationmanager.ui.company.details.components

import androidx.annotation.StringRes
import com.zaed.reservationmanager.R

enum class PaymentError(@StringRes val messageRes: Int) {
    NONE(0),
    AMOUNT_IS_REQUIRED(R.string.amount_is_required),
    DATE_IS_REQUIRED(R.string.date_is_required),
    DESCRIPTION_IS_REQUIRED(R.string.description_is_required),
}