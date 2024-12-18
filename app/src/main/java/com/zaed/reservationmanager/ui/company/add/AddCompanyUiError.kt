package com.zaed.reservationmanager.ui.company.add

import androidx.annotation.StringRes
import com.zaed.reservationmanager.R

enum class AddCompanyUiError(@StringRes val messageRes: Int) {
    NONE(0),
    NAME_IS_REQUIRED(R.string.name_is_required),
    COUNTRY_IS_REQUIRED(R.string.country_is_required),
    NAME_IS_ALREADY_USED(R.string.a_company_with_name_already_exists),
    EMAIL_IS_INVALID(R.string.email_is_invalid),
    FAX_NUMBER_IS_INVALID(R.string.invalid_fax_number),
    PHONE_NUMBER_IS_INVALID(R.string.invalid_phone_number),
}