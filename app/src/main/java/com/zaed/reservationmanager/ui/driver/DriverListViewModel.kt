package com.zaed.reservationmanager.ui.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.model.EmployeeType
import com.zaed.reservationmanager.data.repository.EmployeeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DriverListViewModel(
    private val employeeRepository: EmployeeRepository
): ViewModel()  {
    private val _state = MutableStateFlow(DriverListState())
    val state = _state.asStateFlow()

    init{
        fetchDrivers()
    }

    private fun fetchDrivers() {
        viewModelScope.launch {
            employeeRepository.getDrivers().collect{result->
                result.onSuccess { data->
                    _state.value = _state.value.copy(
                        drivers = data,
                        isLoading = false
                    )
                }.onFailure {
                    _state.value = _state.value.copy(
                        error = it.message,
                        isLoading = false
                    )

                }
            }
        }
    }

}
