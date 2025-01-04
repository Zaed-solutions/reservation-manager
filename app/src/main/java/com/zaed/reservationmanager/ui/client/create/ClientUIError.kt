package com.zaed.reservationmanager.ui.client.create

import androidx.annotation.StringRes
import com.zaed.reservationmanager.R

enum class ClientUIError(@StringRes val messageRes: Int) {
    NONE(0),
    NAME_IS_REQUIRED(R.string.name_is_required),
    EMAIL_IS_REQUIRED(R.string.email_is_required),
    EMAIL_IS_INVALID(R.string.email_is_invalid),
    PHONE_NUMBER_IS_REQUIRED(R.string.mobile_is_required),
    PHONE_NUMBER_1_IS_INVALID(R.string.mobile_number_is_invalid),
    PHONE_NUMBER_2_IS_INVALID(R.string.mobile_number_is_invalid),
    PHONE_NUMBER_1_IS_IN_USE(R.string.phone_number_1_is_already_in_use),
    PHONE_NUMBER_2_IS_IN_USE(R.string.phone_number_2_is_already_in_use),
    PLEASE_FILL_IN_ALL_REQUIRED_FIELDS(R.string.please_fill_in_all_required_fields),
    CLIENT_WITH_THIS_PHONE_NUMBER_ALREADY_EXISTS(R.string.customer_with_this_phone_number_already_exists)
}