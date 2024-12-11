package com.zaed.reservationmanager.ui.client.create

import com.zaed.reservationmanager.ui.client.ClientUIError

data class NewClientUiState(
    var clientName: String = "",
    var clientNameError: ClientUIError = ClientUIError.NONE,
    var nationality: String = "",
    var countryOfResidence: String = "",
    var countryOfResidenceError : ClientUIError = ClientUIError.NONE,
    var mobile: String = "",
    var mobileError: ClientUIError = ClientUIError.NONE,
    var email: String = "",
    var emailError: ClientUIError = ClientUIError.NONE,
    var errorMessage: ClientUIError = ClientUIError.NONE,
    var nationalities : List<String> = nationalitiesList,
    var countries : List<String> = countriesList,
    var loading: Boolean = false,
    var successStatus: Boolean = false
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