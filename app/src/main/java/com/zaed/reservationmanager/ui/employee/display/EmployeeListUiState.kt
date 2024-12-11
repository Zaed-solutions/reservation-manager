package com.zaed.reservationmanager.ui.employee.display

import com.zaed.reservationmanager.data.model.Employee

data class EmployeeListUiState(
    val employees: List<Employee> = emptyList(),
    val errorMessage: String = "",
    val loading: Boolean = false,
)
