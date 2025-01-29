package com.zaed.reservationmanager.ui.dropdownmenu

import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.repository.Menus
import com.zaed.reservationmanager.data.repository.MenusDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UpdateDropDownListsViewModel(
    private val menusDataRepository: MenusDataRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UpdateDropDownListsUiState())
    val uiState = _uiState.asStateFlow()
    init {
        initializeMenus()
    }
    private fun initializeMenus() {
        viewModelScope.launch (Dispatchers.IO){
            Menus.entries.forEach { menu ->
                getMenu(menu)
            }
        }
    }

    private fun getMenu(menu:Menus) {
        viewModelScope.launch (Dispatchers.IO){
            menusDataRepository.getMenuByName(menu).collect{ result->
                result.onSuccess { data->
                    _uiState.update {
                        when(menu){
                            Menus.RESERVATION_TYPES -> it.copy(reservationTypes = data.data)
                            Menus.CAR_TYPES -> it.copy(carTypes = data.data)
                            Menus.COUNTRIES -> it.copy(countries = data.data)
                        }
                    }

                }.onFailure {error->
                    Log.d("error",error.message.toString())

                }
            }
        }
    }
     fun addItemToMenu(menu: Menus, item: String) {
        viewModelScope.launch (Dispatchers.IO){
            menusDataRepository.addItemToMenu(menu, item).onSuccess {
                Log.d("success",it.toString())
            }.onFailure {error->
                Log.d("error",error.message.toString())
            }
        }
    }
     fun deleteItemFromMenu(menu: Menus, item: String) {
        viewModelScope.launch (Dispatchers.IO){
            menusDataRepository.removeItemFromMenu(menu, item).onSuccess {
                Log.d("success",it.toString())
                }.onFailure {error->
                Log.d("error",error.message.toString())
            }
        }
    }
}

data class UpdateDropDownListsUiState (
    val reservationTypes: List<String> = emptyList(),
    val carTypes: List<String> = emptyList(),
    val countries: List<String> = emptyList()
)
