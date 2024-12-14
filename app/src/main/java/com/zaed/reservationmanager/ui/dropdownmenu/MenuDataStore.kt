package com.zaed.reservationmanager.ui.dropdownmenu

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface MenuDataStore {
    fun getMenus(key: Preferences.Key<Set<String>>): Flow<Set<String>>
    suspend fun saveMenus(key: Preferences.Key<Set<String>>, menus: Set<String>)
    suspend fun initializeMenus()

}