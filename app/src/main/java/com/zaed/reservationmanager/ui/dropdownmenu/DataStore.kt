package com.zaed.reservationmanager.ui.dropdownmenu

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.zaed.reservationmanager.ui.util.Constants.CAR_TYPES_KEY
import com.zaed.reservationmanager.ui.util.Constants.COUNTRIES_KEY
import com.zaed.reservationmanager.ui.util.Constants.RESERVATION_TYPES_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("menu_prefs")

class MenuDataStoreImpl(val context: Context) : MenuDataStore {

    override fun getMenus(key: Preferences.Key<Set<String>>): Flow<Set<String>> {
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: emptySet()
        }
    }

    override suspend fun saveMenus(key: Preferences.Key<Set<String>>, menus: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[key] = menus
        }
    }

    override suspend fun initializeMenus() {
        context.dataStore.edit { preferences ->
            if (preferences[RESERVATION_TYPES_KEY] == null) {
                preferences[RESERVATION_TYPES_KEY] = reservationTypesList.toSet()
            }
            if (preferences[CAR_TYPES_KEY] == null) {
                preferences[CAR_TYPES_KEY] = carTypesList.toSet()
            }
            if (preferences[COUNTRIES_KEY] == null) {
                preferences[COUNTRIES_KEY] = countriesList.toSet()
            }
        }
    }
}

val reservationTypesList = listOf(
    "Reception",
    "Departure",
    "Mecca Attractions",
    "Medina Attractions",
    "Taif Tour",
    "Stationed"
)

val carTypesList = listOf(
    "Small",
    "Carnival",
    "Family",
    "H1",
    "Staria",
    "GMC",
    "Hiace",
    "Coaster",
    "Bus"
)

val countriesList = listOf(
    "USA",
    "UK",
    "Saudi Arabia",
    "India",
    "Egypt"
)