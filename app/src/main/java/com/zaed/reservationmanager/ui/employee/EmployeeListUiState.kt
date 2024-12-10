package com.zaed.reservationmanager.ui.employee

import com.zaed.reservationmanager.data.model.Employee

data class EmployeeListUiState(
    val employees: List<Employee> = emptyList(),
    val errorMessage: String = "",
    val loading: Boolean = false,
)
