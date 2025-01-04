package com.zaed.reservationmanager.ui.reservation.create

import androidx.annotation.StringRes
import com.zaed.reservationmanager.R

enum class ReservationError(@StringRes val messageRes: Int) {
    NONE(0),
    CUSTOMER_PHONE_IS_REQUIRED(R.string.phone_is_required),
    EMAIL_IS_INVALID(R.string.email_is_invalid),
    TOURISM_COMPANY_IS_REQUIRED(R.string.tourism_company_is_required),
    TYPE_IS_REQUIRED(R.string.type_is_required),
    CAR_IS_REQUIRED(R.string.car_is_required),
    COLLECTING_PRICE_IS_REQUIRED(R.string.buying_price_is_required),
    COLLECTION_PRICE_IS_REQUIRED(R.string.collection_price_is_required),
    DATE_IS_REQUIRED(R.string.date_is_required),
    TIME_IS_REQUIRED(R.string.time_is_required),
    DRIVER_IS_REQUIRED(R.string.driver_is_required),
    START_LOCATION_IS_REQUIRED(R.string.start_location_is_required),
    END_LOCATION_IS_REQUIRED(R.string.end_location_is_required),
    CUSTOMER_PHONE_IS_INVALID(R.string.customer_phone_is_invalid),
    CUSTOMER_NAME_IS_REQUIRED(R.string.customer_name_is_required),
    EMPLOYEE_IS_REQUIRED(R.string.employee_is_required),
    CUSTOMER_COUNTRY_IS_REQUIRED(R.string.customer_country_is_required),
    TOURISM_EMPLOYEE_IS_REQUIRED(R.string.tourism_employee_is_required),
    RIDE_PRICE_IS_REQUIRED(R.string.selling_price_is_required),
    PHONE_NUMBER_1_IS_IN_USE(R.string.phone_number_1_is_already_in_use),
    PHONE_NUMBER_2_IS_IN_USE(R.string.phone_number_2_is_already_in_use),


}