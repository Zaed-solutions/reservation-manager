package com.zaed.reservationmanager.ui.client.create

import com.zaed.reservationmanager.data.model.Customer

data class NewClientUiState(
    val isNew : Boolean = true,
    val customer: Customer = Customer(),
    val error: ClientUIError = ClientUIError.NONE,
    val nationalities : List<String> = nationalitiesList,
    val countries : List<String> = countriesList,
    val loading: Boolean = false,
    val successStatus: Boolean = false
)

val countriesList = listOf(
    "Egypt",
    "Saudi Arabia",
    "Morocco",
    "Tunisia",
    "United Arab Emirates"
)
val nationalitiesList = listOf(
    "Egyptian",
    "Saudi",
    "Moroccan",
    "Tunisian",
    "Emirati"
)