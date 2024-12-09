package com.zaed.reservationmanager.ui.addemployee

import com.zaed.reservationmanager.data.model.Employee

data class AddEmployeeUiState(
    val employee: Employee = Employee(),
    val isNew: Boolean = true,
    val isFinished: Boolean = false,
    val companies: List<String> = emptyList(),
    val error: AddEmployeeUiError = AddEmployeeUiError.NONE
)
