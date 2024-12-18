package com.zaed.reservationmanager.ui.client.create

import com.zaed.reservationmanager.data.model.Customer

data class NewClientUiState(
    val isNew: Boolean = true,
    val customer: Customer = Customer(),
    val error: ClientUIError = ClientUIError.NONE,
    val nationalities: List<String> = emptyList(),
    val countries: List<String> = emptyList(),
    val loading: Boolean = false,
    val successStatus: Boolean = false
)
