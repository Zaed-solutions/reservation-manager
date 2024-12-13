package com.zaed.reservationmanager.ui.util

import androidx.datastore.preferences.core.stringSetPreferencesKey

object Constants {
    val RESERVATION_TYPES_KEY = stringSetPreferencesKey("reservation_types")
    val CAR_TYPES_KEY = stringSetPreferencesKey("car_types")
    val COUNTRIES_KEY = stringSetPreferencesKey("countries")

}