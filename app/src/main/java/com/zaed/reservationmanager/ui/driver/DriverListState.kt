package com.zaed.reservationmanager.ui.driver

import com.zaed.reservationmanager.data.model.Employee

data class DriverListState (
    val drivers: List<Employee> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
