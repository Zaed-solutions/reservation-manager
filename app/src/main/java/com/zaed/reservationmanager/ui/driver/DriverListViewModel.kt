package com.zaed.reservationmanager.ui.driver

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.model.EmployeeType
import com.zaed.reservationmanager.data.repository.EmployeeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    fun deleteEmployee(employeeId: String){
        viewModelScope.launch (Dispatchers.IO){
            employeeRepository.deleteEmployee(employeeId).collect{result->
                result.onSuccess {
                    Log.d("EmployeeListViewModel", "Employee deleted successfully")
                }.onFailure {
                    Log.e("EmployeeListViewModel", "Error deleting employee")
                    it.printStackTrace()
                }
            }
        }
    }
}
