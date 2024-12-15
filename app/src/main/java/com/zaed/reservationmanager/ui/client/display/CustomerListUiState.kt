package com.zaed.reservationmanager.ui.client.display

import com.zaed.reservationmanager.data.model.Customer

data class CustomerListUiState(
    val customers: List<Customer> = emptyList(),
    val selectedCountry: String = "",
    val displayedCustomers: List<Customer> = emptyList(),
    val countries: List<String> = emptyList(),
    val errorMessage: String = "",
    val loading: Boolean = false,
)
