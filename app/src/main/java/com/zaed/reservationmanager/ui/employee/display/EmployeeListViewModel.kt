package com.zaed.reservationmanager.ui.employee.display

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.repository.EmployeeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EmployeeListViewModel(
    private val employeeRepository: EmployeeRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(EmployeeListUiState())
    val state = _state.asStateFlow()

    init {
        getEmployees()
    }

    private fun getEmployees() {
        viewModelScope.launch {
            employeeRepository.getEmployees().collect { result ->
                result.onSuccess { data ->
                    _state.update { oldState ->
                        oldState.copy(
                            employees = data,
                            loading = false,
                            errorMessage = ""
                        )
                    }
                }.onFailure {
                    _state.update {
                        it.copy(
                            loading = false,
                            errorMessage = it.errorMessage
                        )
                    }
                }
            }
        }
    }

    fun deleteEmployee(employeeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            employeeRepository.deleteEmployee(employeeId).collect { result ->
                result.onSuccess {
                    Log.d("EmployeeListViewModel", "Employee deleted successfully")
                }.onFailure {
                    _state.update {
                        it.copy(
                            loading = false,
                            errorMessage = it.errorMessage
                        )
                    }
                    Log.e("EmployeeListViewModel", "Error deleting employee")
                    it.printStackTrace()
                }
            }
        }
    }
}
