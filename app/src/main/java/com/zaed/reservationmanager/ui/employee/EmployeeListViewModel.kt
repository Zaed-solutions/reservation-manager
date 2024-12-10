package com.zaed.reservationmanager.ui.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.reservationmanager.data.model.EmployeeType
import com.zaed.reservationmanager.data.repository.EmployeeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EmployeeListViewModel (
    private val employeeRepository: EmployeeRepository,
): ViewModel() {
    val _state = MutableStateFlow(EmployeeListUiState())
    val state = _state.asStateFlow()

    init {
        getEmployees()
    }

    private fun getEmployees() {
        viewModelScope.launch {
            employeeRepository.getEmployees().collect{result->
                result.onSuccess {data->
                    _state.update {oldState->
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
}
