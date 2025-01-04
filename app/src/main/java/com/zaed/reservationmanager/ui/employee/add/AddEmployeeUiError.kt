package com.zaed.reservationmanager.ui.employee.add

import androidx.annotation.StringRes
import com.zaed.reservationmanager.R

enum class AddEmployeeUiError(@StringRes val messageRes: Int) {
    NONE(0),
    NAME_IS_REQUIRED(R.string.name_is_required),
    PHONE_NUMBER_IS_REQUIRED(R.string.atleast_one_phone_number_is_required),
    NATIONALITY_IS_REQUIRED(R.string.nationality_is_required),
    NAME_OR_PHONE_ARE_ALREADY_USED(R.string.an_employee_with_name_already_exists),
    EMAIL_IS_INVALID(R.string.email_is_invalid),
    PHONE_NUMBER_1_IS_INVALID(R.string.invalid_phone_number),
    PHONE_NUMBER_2_IS_INVALID(R.string.invalid_phone_number),
    NAME_IS_ALREADY_USED(R.string.name_is_already_used),
    PHONE_NUMBER_1_IS_IN_USE(R.string.phone_number_1_is_already_in_use),
    PHONE_NUMBER_2_IS_IN_USE(R.string.phone_number_2_is_already_in_use),
}