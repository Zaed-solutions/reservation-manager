package com.zaed.reservationmanager.ui.dropdownmenu

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UpdateDropDownListsViewModel(
    private val menuDataStore: MenuDataStore
) : ViewModel() {
    fun initializeMenus() {
        viewModelScope.launch {
            menuDataStore.initializeMenus()
        }
    }

    fun getMenus(key: Preferences.Key<Set<String>>): Flow<Set<String>> {
        return menuDataStore.getMenus(key)
    }

    suspend fun saveMenus(key: Preferences.Key<Set<String>>, menus: Set<String>) {
        menuDataStore.saveMenus(key, menus)
    }

    fun deleteMenu(key: Preferences.Key<Set<String>>, item: String) {
        viewModelScope.launch {
            menuDataStore.getMenus(key).collect { currentMenus ->
                val updatedMenus = currentMenus.toMutableSet().apply {
                    remove(item)
                }
                menuDataStore.saveMenus(key, updatedMenus)
            }
        }

    }
}